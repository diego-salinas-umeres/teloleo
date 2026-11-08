package app.teloleo.billing

import app.teloleo.scope.ProductScope

enum class StorePurchaseState {
    PURCHASED,
    PENDING,
    OTHER,
}

data class StorePurchase(
    val productIds: Set<String>,
    val state: StorePurchaseState,
    val acknowledged: Boolean,
    val token: String,
)

/**
 * El Pro es un producto no consumible. No se consume: si se consumiera,
 * Play dejaría de devolverlo en la consulta y el desbloqueo se perdería.
 */
object ProEntitlement {
    fun isUnlocked(
        purchases: List<StorePurchase>,
        productId: String = ProductScope.PRO_PRODUCT_ID,
    ): Boolean = purchases.any { purchase ->
        productId in purchase.productIds && purchase.state == StorePurchaseState.PURCHASED
    }

    fun hasPending(
        purchases: List<StorePurchase>,
        productId: String = ProductScope.PRO_PRODUCT_ID,
    ): Boolean = purchases.any { purchase ->
        productId in purchase.productIds && purchase.state == StorePurchaseState.PENDING
    }

    fun tokensToAcknowledge(
        purchases: List<StorePurchase>,
        productId: String = ProductScope.PRO_PRODUCT_ID,
    ): List<String> = purchases.mapNotNull { purchase ->
        val token = purchase.token.trim()
        if (productId in purchase.productIds &&
            purchase.state == StorePurchaseState.PURCHASED &&
            !purchase.acknowledged &&
            token.isNotEmpty()
        ) {
            token
        } else {
            null
        }
    }
}
