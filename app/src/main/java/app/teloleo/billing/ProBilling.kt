package app.teloleo.billing

import android.app.Activity
import android.os.Handler
import android.os.Looper
import app.teloleo.scope.ProductScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

enum class StoreNotice {
    None,
    StoreUnavailable,
    ProductMissing,
    PurchaseFailed,
}

data class BillingOffer(
    val formattedPrice: String? = null,
    val productAvailable: Boolean = false,
    val purchasePending: Boolean = false,
    val notice: StoreNotice = StoreNotice.None,
)

/**
 * Compra no consumible en el teléfono. Play Billing habla con la app de Play:
 * TeLoLeo no abre red ni confirma el pago en un servidor. No se llama a consume.
 */
class ProBilling(
    activity: Activity,
    private val onEntitlement: (Boolean) -> Unit,
    private val onOffer: (BillingOffer) -> Unit,
) : PurchasesUpdatedListener {
    private val main = Handler(Looper.getMainLooper())
    private var host: Activity? = activity
    private var offer = BillingOffer()
    private var connecting = false

    private val client: BillingClient = BillingClient.newBuilder(activity.applicationContext)
        .setListener(this)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build(),
        )
        .build()

    fun start() {
        if (client.isReady) {
            refresh()
            loadPrice()
            return
        }
        if (connecting) return
        connecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                connecting = false
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refresh()
                    loadPrice()
                } else {
                    publish(offer.copy(productAvailable = false, notice = StoreNotice.StoreUnavailable))
                }
            }

            override fun onBillingServiceDisconnected() {
                connecting = false
            }
        })
    }

    fun refresh() {
        if (!client.isReady) {
            start()
            return
        }
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        client.queryPurchasesAsync(params) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                apply(purchases)
            }
        }
    }

    fun purchase() {
        val activity = host ?: return
        if (activity.isFinishing || activity.isDestroyed) return
        if (!client.isReady) {
            publish(offer.copy(productAvailable = false, notice = StoreNotice.StoreUnavailable))
            start()
            return
        }
        client.queryProductDetailsAsync(productQuery()) { result, details ->
            main.post { launchPurchase(activity, result, details.orEmpty()) }
        }
    }

    fun destroy() {
        host = null
        connecting = false
        client.endConnection()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> apply(purchases.orEmpty())
            BillingClient.BillingResponseCode.USER_CANCELED -> publish(offer.copy(notice = StoreNotice.None))
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
            -> publish(offer.copy(notice = StoreNotice.StoreUnavailable))
            else -> publish(offer.copy(notice = StoreNotice.PurchaseFailed))
        }
    }

    private fun launchPurchase(
        activity: Activity,
        result: BillingResult,
        details: List<ProductDetails>,
    ) {
        if (activity.isFinishing || activity.isDestroyed) return
        val product = details.firstOrNull { it.productId == ProductScope.PRO_PRODUCT_ID }
        val price = product?.oneTimePurchaseOfferDetails?.formattedPrice
        if (result.responseCode != BillingClient.BillingResponseCode.OK || product == null || price.isNullOrBlank()) {
            publish(offer.copy(productAvailable = false, notice = StoreNotice.ProductMissing))
            return
        }
        publish(
            offer.copy(
                formattedPrice = price,
                productAvailable = true,
                notice = StoreNotice.None,
            ),
        )
        val flow = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .build(),
                ),
            )
            .build()
        val launch = client.launchBillingFlow(activity, flow)
        if (launch.responseCode != BillingClient.BillingResponseCode.OK &&
            launch.responseCode != BillingClient.BillingResponseCode.USER_CANCELED
        ) {
            publish(offer.copy(notice = StoreNotice.PurchaseFailed))
        }
    }

    private fun loadPrice() {
        if (!client.isReady) return
        client.queryProductDetailsAsync(productQuery()) { result, details ->
            val product = details.orEmpty().firstOrNull { it.productId == ProductScope.PRO_PRODUCT_ID }
            val price = product?.oneTimePurchaseOfferDetails?.formattedPrice
            if (result.responseCode != BillingClient.BillingResponseCode.OK || price.isNullOrBlank()) {
                publish(offer.copy(productAvailable = false, notice = StoreNotice.ProductMissing))
                return@queryProductDetailsAsync
            }
            publish(
                offer.copy(
                    formattedPrice = price,
                    productAvailable = true,
                    notice = StoreNotice.None,
                ),
            )
        }
    }

    private fun apply(purchases: List<Purchase>) {
        val stored = purchases.map { it.toStorePurchase() }
        val unlocked = ProEntitlement.isUnlocked(stored)
        val pending = ProEntitlement.hasPending(stored)
        main.post { onEntitlement(unlocked) }
        publish(
            offer.copy(
                purchasePending = pending && !unlocked,
                notice = if (pending && !unlocked) offer.notice else StoreNotice.None,
            ),
        )
        if (!client.isReady) return
        ProEntitlement.tokensToAcknowledge(stored).forEach { token ->
            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(token).build()
            client.acknowledgePurchase(params) { }
        }
    }

    private fun publish(next: BillingOffer) {
        offer = next
        val snapshot = next
        main.post { onOffer(snapshot) }
    }

    private fun productQuery(): QueryProductDetailsParams {
        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(ProductScope.PRO_PRODUCT_ID)
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        return QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()
    }

    private fun Purchase.toStorePurchase(): StorePurchase {
        val state = when (purchaseState) {
            Purchase.PurchaseState.PURCHASED -> StorePurchaseState.PURCHASED
            Purchase.PurchaseState.PENDING -> StorePurchaseState.PENDING
            else -> StorePurchaseState.OTHER
        }
        return StorePurchase(
            productIds = products.toSet(),
            state = state,
            acknowledged = isAcknowledged,
            token = purchaseToken,
        )
    }
}
