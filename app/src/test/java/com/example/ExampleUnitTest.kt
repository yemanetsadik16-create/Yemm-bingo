package com.example

import com.example.game.BingoEngine
import com.example.model.BingoColumn
import com.example.model.PaymentMethod
import com.example.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Bingo game engine, Ethiopian payment configurations,
 * and UserProfile authentication models.
 */
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testBingoCardGeneration() {
        val card = BingoEngine.generateCard(1, 1001L)
        assertEquals(1, card.cardNumber)
        assertEquals(5, card.columns.size)

        // Verify column B numbers in 1..15
        card.columns[0].forEachIndexed { row, num ->
            assertTrue("Col 0 row $row: $num should be in 1..15", num in 1..15)
        }

        // Verify center FREE cell (column 2, row 2) is 0
        assertEquals(0, card.getNumberAt(2, 2))
    }

    @Test
    fun testBingoColumnRanges() {
        assertEquals(BingoColumn.B, BingoColumn.forNumber(7))
        assertEquals(BingoColumn.I, BingoColumn.forNumber(22))
        assertEquals(BingoColumn.N, BingoColumn.forNumber(35))
        assertEquals(BingoColumn.G, BingoColumn.forNumber(55))
        assertEquals(BingoColumn.O, BingoColumn.forNumber(72))
    }

    @Test
    fun testAllFourEthiopianPaymentMethods() {
        val methods = PaymentMethod.entries
        assertEquals(4, methods.size)

        val codes = methods.map { it.code }
        assertTrue(codes.contains("telebirr"))
        assertTrue(codes.contains("cbe"))
        assertTrue(codes.contains("cbe_birr"))
        assertTrue(codes.contains("ebirr"))

        // Verify all 4 methods share the user-specified account holder & account number
        methods.forEach { method ->
            assertEquals("Yemane", method.accountHolder)
            assertEquals("0939383425", method.accountNumber)
        }
    }

    @Test
    fun testUserProfileModel() {
        val user = UserProfile(
            username = "Yemane",
            phone = "0939383425",
            playingTime = "8:00 O’clock Day",
            wallet = 350
        )
        assertEquals("Yemane", user.username)
        assertEquals("0939383425", user.phone)
        assertEquals(350, user.wallet)
        assertEquals(11, user.wallet / 30) // 11 cartelas
    }

    @Test
    fun testAmharicCalloutPhrasing() {
        val call15 = BingoEngine.amharicCallout(15)
        assertTrue(call15.isNotBlank())
        assertTrue(call15.contains("ቢ") || call15.contains("15") || call15.contains("አስራ አምስት"))

        val call59 = BingoEngine.amharicCallout(59)
        assertTrue(call59.isNotBlank())
        assertTrue(call59.contains("ጂ") || call59.contains("59") || call59.contains("ሃምሳ ዘጠኝ"))
    }

    @Test
    fun testFirestoreDepositAndWithdrawalModels() {
        val deposit = com.example.model.DepositRequest(
            id = "dep_test",
            username = "Yemane",
            amount = 120,
            paymentMethod = PaymentMethod.TELEBIRR,
            receiptId = "TB998877",
            fullReceipt = "https://telebirr/r/TB998877",
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )
        assertEquals("dep_test", deposit.id)
        assertEquals(120, deposit.amount)
        assertEquals("telebirr", deposit.paymentMethod.code)

        val withdrawal = com.example.model.WithdrawalRequest(
            id = "wth_test",
            username = "Yemane",
            amount = 60,
            paymentMethod = PaymentMethod.CBE,
            targetAccount = "0939383425",
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )
        assertEquals("wth_test", withdrawal.id)
        assertEquals(60, withdrawal.amount)
        assertEquals("0939383425", withdrawal.targetAccount)
    }
}
