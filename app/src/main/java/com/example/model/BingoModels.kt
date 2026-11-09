package com.example.model

/**
 * Representation of an individual B-I-N-G-O column:
 * B: 1-15, I: 16-30, N: 31-45, G: 46-60, O: 61-75
 */
enum class BingoColumn(val letter: Char, val rangeStart: Int, val rangeEnd: Int) {
    B('B', 1, 15),
    I('I', 16, 30),
    N('N', 31, 45),
    G('G', 46, 60),
    O('O', 61, 75);

    companion object {
        fun forNumber(number: Int): BingoColumn {
            return when (number) {
                in 1..15 -> B
                in 16..30 -> I
                in 31..45 -> N
                in 46..60 -> G
                in 61..75 -> O
                else -> B
            }
        }
    }
}

/**
 * Represents a 5x5 Bingo Card (columns 0..4, rows 0..4)
 * Center at (2, 2) is FREE (number = 0)
 */
data class BingoCard(
    val cardNumber: Int,
    val seed: Long,
    // 5 columns, each with 5 numbers
    val columns: List<List<Int>>
) {
    fun getNumberAt(col: Int, row: Int): Int {
        if (col in 0..4 && row in 0..4) {
            return columns[col][row]
        }
        return 0
    }
}

/**
 * Bingo Winning Goal patterns
 */
data class BingoGoal(
    val id: String,
    val nameEn: String,
    val nameAm: String,
    val nameTi: String,
    // list of required cell sets. Completing ANY set satisfies the goal.
    // Each set is a list of Pair(col, row)
    val patterns: List<List<Pair<Int, Int>>>
)

/**
 * Scheduled slot in Ethiopian time UTC+3
 */
data class GameSlot(
    val hour24: Int, // 10, 12, 14, 16, 18, 20
    val labelEn: String,
    val labelAm: String,
    val labelTi: String
)

data class TransactionRecord(
    val label: String,
    val amount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
    val username: String,
    val phone: String,
    val playingTime: String,
    val wallet: Int = 120, // default starting promo wallet for rich gameplay
    val transactions: List<TransactionRecord> = emptyList(),
    // Key: slot and card id, e.g. "s14c7", value: seed
    val ownedCardSeeds: Map<String, Long> = emptyMap()
)

/**
 * The 4 supported Ethiopian payment methods
 */
enum class PaymentMethod(
    val code: String,
    val displayName: String,
    val accountHolder: String = "Yemane",
    val accountNumber: String = "0939383425",
    val description: String,
    val hintPlaceholder: String
) {
    TELEBIRR(
        code = "telebirr",
        displayName = "Telebirr",
        accountHolder = "Yemane",
        accountNumber = "0939383425",
        description = "Pay with Telebirr app or *127# to Yemane (0939383425)",
        hintPlaceholder = "Telebirr receipt link or transaction number"
    ),
    CBE(
        code = "cbe",
        displayName = "CBE (Commercial Bank of Ethiopia)",
        accountHolder = "Yemane",
        accountNumber = "0939383425",
        description = "Transfer via CBE Mobile Banking to Yemane (0939383425)",
        hintPlaceholder = "CBE transaction reference or receipt ID"
    ),
    CBE_BIRR(
        code = "cbe_birr",
        displayName = "CBE Birr",
        accountHolder = "Yemane",
        accountNumber = "0939383425",
        description = "Pay via CBE Birr (*847# or CBE Birr App) to Yemane (0939383425)",
        hintPlaceholder = "CBE Birr transaction ID or SMS confirmation"
    ),
    EBIRR(
        code = "ebirr",
        displayName = "E-Birr",
        accountHolder = "Yemane",
        accountNumber = "0939383425",
        description = "Send via E-Birr (*841# or E-Birr App) to Yemane (0939383425)",
        hintPlaceholder = "E-Birr receipt reference or confirmation code"
    );

    companion object {
        fun fromCode(code: String?): PaymentMethod {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: TELEBIRR
        }
    }
}

data class DepositRequest(
    val id: String,
    val username: String,
    val amount: Int,
    val paymentMethod: PaymentMethod = PaymentMethod.TELEBIRR,
    val receiptId: String,
    val fullReceipt: String,
    val timestamp: Long,
    val status: String // "pending", "approved", "declined"
)

data class WithdrawalRequest(
    val id: String,
    val username: String,
    val amount: Int,
    val paymentMethod: PaymentMethod = PaymentMethod.TELEBIRR,
    val targetAccount: String,
    val timestamp: Long,
    val status: String // "pending", "approved", "declined"
)

data class WinHistoryRecord(
    val id: String,
    val username: String,
    val phone: String,
    val gameTime: String,
    val roundIndex: Int,
    val rewardBirr: Int,
    val cardNumber: Int,
    val callsCount: Int,
    val timestamp: Long
)

enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    AM("am", "አማርኛ"),
    TI("ti", "ትግርኛ")
}
