package app.teloleo.billing

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProEntitlementTest {
    @Test
    fun purchasedProductUnlocksAndPendingDoesNot() {
        val purchases = listOf(
            StorePurchase(setOf("teloleo_pro"), StorePurchaseState.PENDING, acknowledged = false, token = "p"),
            StorePurchase(setOf("other"), StorePurchaseState.PURCHASED, acknowledged = true, token = "o"),
        )
        assertFalse(ProEntitlement.isUnlocked(purchases))
        assertTrue(ProEntitlement.hasPending(purchases))
        assertEquals(
            listOf("owned"),
            ProEntitlement.tokensToAcknowledge(
                listOf(StorePurchase(setOf("teloleo_pro"), StorePurchaseState.PURCHASED, acknowledged = false, token = "owned")),
            ),
        )
    }

    @Test
    fun acknowledgedOrBlankTokensAreNotSentAgain() {
        val purchases = listOf(
            StorePurchase(setOf("teloleo_pro"), StorePurchaseState.PURCHASED, acknowledged = true, token = "done"),
            StorePurchase(setOf("teloleo_pro"), StorePurchaseState.PURCHASED, acknowledged = false, token = "  "),
        )
        assertTrue(ProEntitlement.isUnlocked(purchases))
        assertTrue(ProEntitlement.tokensToAcknowledge(purchases).isEmpty())
    }
}
