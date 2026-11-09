package com.example.game

import com.example.model.BingoCard
import com.example.model.BingoColumn
import com.example.model.BingoGoal
import com.example.model.GameSlot
import kotlin.math.abs

/**
 * Pure generator and logic engine strictly mirroring the original deterministic
 * pseudorandom generators and goal rules:
 * - Deterministic card generation for card #1..500
 * - Deterministic ball calling sequence (1..75)
 * - 17 target goal patterns
 * - Slot calculation in UTC+3 (Ethiopian time)
 */
object BingoEngine {

    val SLOTS = listOf(
        GameSlot(10, "4:00 O’clock Day", "4:00 ሰዓት ቀን", "4:00 ሰዓት መዓልቲ"),
        GameSlot(12, "6:00 O’clock Day", "6:00 ሰዓት ቀን", "6:00 ሰዓት መዓልቲ"),
        GameSlot(14, "8:00 O’clock Day", "8:00 ሰዓት ቀን", "8:00 ሰዓት መዓልቲ"),
        GameSlot(16, "10:00 O’clock Day", "10:00 ሰዓት ቀን", "10:00 ሰዓት መዓልቲ"),
        GameSlot(18, "12:00 O’clock Day", "12:00 ሰዓት ቀን", "12:00 ሰዓት መዓልቲ"),
        GameSlot(20, "2:00 O’clock Night", "2:00 ሰዓት ሌሊት", "2:00 ሰዓት ለይቲ")
    )

    /**
     * Mulberry32 PRNG mirroring JS function rng(s)
     */
    class Mulberry32(private var state: Int) {
        fun nextFloat(): Float {
            state = (state + 0x6D2B79F5.toInt())
            var t = ((state xor (state ushr 15)).toLong() * ((1 or state).toLong())).toInt()
            t = (t + ((t xor (t ushr 7)).toLong() * ((61 or t).toLong())).toInt()) xor t
            val u = (t xor (t ushr 14)).toLong() and 0xFFFFFFFFL
            return (u.toDouble() / 4294967296.0).toFloat()
        }

        fun nextInt(bound: Int): Int {
            return (nextFloat() * bound).toInt().coerceIn(0, bound - 1)
        }
    }

    private fun <T> shuffleWithPrng(list: MutableList<T>, rng: Mulberry32): List<T> {
        for (i in list.size - 1 downTo 1) {
            val j = rng.nextInt(i + 1)
            val tmp = list[i]
            list[i] = list[j]
            list[j] = tmp
        }
        return list
    }

    /**
     * Generates a 75-number callout sequence deterministically for a game seed
     */
    fun generateCallSequence(seed: Long): List<Int> {
        val rng = Mulberry32(seed.toInt())
        val numbers = (1..75).toMutableList()
        return shuffleWithPrng(numbers, rng)
    }

    /**
     * Generates a standard 5x5 card for cardNumber (1..500) and cardSeed
     * Columns B(1-15), I(16-30), N(31-45), G(46-60), O(61-75)
     * Cell at col 2, row 2 is 0 (FREE)
     */
    fun generateCard(cardNumber: Int, seed: Long): BingoCard {
        val formulaSeed = (seed * 1000L + cardNumber.toLong() * 7919L + 13L).toInt()
        val rng = Mulberry32(formulaSeed)

        val columns = mutableListOf<List<Int>>()
        for (col in 0..4) {
            val start = col * 15 + 1
            val end = col * 15 + 15
            val pool = (start..end).toMutableList()
            val shuffled = shuffleWithPrng(pool, rng)
            val colList = shuffled.take(5).toMutableList()
            if (col == 2) {
                colList[2] = 0 // Center Free
            }
            columns.add(colList)
        }

        return BingoCard(cardNumber = cardNumber, seed = seed, columns = columns)
    }

    /**
     * 17 Goals matching the original web app
     */
    val ALL_GOALS: List<BingoGoal> by lazy {
        val lines = mutableListOf<List<Pair<Int, Int>>>()
        // 5 rows & 5 columns
        for (i in 0..4) {
            val row = (0..4).map { c -> Pair(c, i) }
            val col = (0..4).map { r -> Pair(i, r) }
            lines.add(row)
            lines.add(col)
        }
        val diag1 = (0..4).map { i -> Pair(i, i) }
        val diag2 = (0..4).map { i -> Pair(i, 4 - i) }

        fun row(i: Int) = (0..4).map { c -> Pair(c, i) }
        fun col(i: Int) = (0..4).map { r -> Pair(i, r) }
        fun blk(w: Int, h: Int): List<List<Pair<Int, Int>>> {
            val res = mutableListOf<List<Pair<Int, Int>>>()
            for (x in 0..(5 - w)) {
                for (y in 0..(5 - h)) {
                    val q = mutableListOf<Pair<Int, Int>>()
                    for (a in 0 until w) {
                        for (c in 0 until h) {
                            q.add(Pair(x + a, y + c))
                        }
                    }
                    res.add(q)
                }
            }
            return res
        }
        fun cellsWhere(predicate: (c: Int, r: Int) -> Boolean): List<Pair<Int, Int>> {
            val res = mutableListOf<Pair<Int, Int>>()
            for (c in 0..4) {
                for (r in 0..4) {
                    if (predicate(c, r)) res.add(Pair(c, r))
                }
            }
            return res
        }

        val midCells = listOf(Pair(1, 1), Pair(2, 1), Pair(1, 2), Pair(2, 2))

        listOf(
            BingoGoal(
                id = "any_line",
                nameEn = "Any Line",
                nameAm = "ማንኛውም መስመር",
                nameTi = "ዝኾነ መስመር",
                patterns = lines + listOf(diag1, diag2)
            ),
            BingoGoal(
                id = "left_right_columns",
                nameEn = "Left & Right Columns",
                nameAm = "ግራ እና ቀኝ ዓምዶች",
                nameTi = "ጸጋምን የማንን ዓምዲታት",
                patterns = listOf(col(0) + col(4))
            ),
            BingoGoal(
                id = "top_bottom_rows",
                nameEn = "Top & Bottom Rows",
                nameAm = "ላይ እና ታች ረድፎች",
                nameTi = "ላዕለዋይን ታሕተዋይን መስመራት",
                patterns = listOf(row(0) + row(4))
            ),
            BingoGoal(
                id = "four_corners",
                nameEn = "Four Corners",
                nameAm = "አራቱ ማዕዘኖች",
                nameTi = "ኣርባዕተ ማዕዘናት",
                patterns = listOf(listOf(Pair(0, 0), Pair(4, 0), Pair(0, 4), Pair(4, 4)))
            ),
            BingoGoal(
                id = "both_diagonals",
                nameEn = "Both Diagonals",
                nameAm = "ሁለቱም ሰያፎች",
                nameTi = "ክልቲኡ ሰያፍ",
                patterns = listOf(diag1 + diag2)
            ),
            BingoGoal(
                id = "small_square",
                nameEn = "Small Square",
                nameAm = "ትንሽ ካሬ",
                nameTi = "ንእሽቶ ካሬ",
                patterns = blk(2, 2)
            ),
            BingoGoal(
                id = "large_square",
                nameEn = "Large Square",
                nameAm = "ትልቅ ካሬ",
                nameTi = "ዓቢ ካሬ",
                patterns = listOf(cellsWhere { c, r -> c == 0 || c == 4 || r == 0 || r == 4 })
            ),
            BingoGoal(
                id = "rectangle",
                nameEn = "Rectangle",
                nameAm = "አራት ማዕዘን",
                nameTi = "ኣርባዕተ ማዕዘን",
                patterns = blk(2, 3) + blk(3, 2)
            ),
            BingoGoal(
                id = "half_house",
                nameEn = "Half House",
                nameAm = "ግማሽ ቤት",
                nameTi = "ፍርቂ ገዛ",
                patterns = listOf(
                    cellsWhere { c, r -> c < 2 || (c == 2 && r <= 2) },
                    cellsWhere { c, r -> c > 2 || (c == 2 && r >= 2) },
                    cellsWhere { c, r -> r < 2 || (r == 2 && c <= 2) },
                    cellsWhere { c, r -> r > 2 || (r == 2 && c >= 2) }
                )
            ),
            BingoGoal(
                id = "full_house",
                nameEn = "Full House",
                nameAm = "ሙሉ ቤት",
                nameTi = "ምሉእ ገዛ",
                patterns = listOf(cellsWhere { _, _ -> true })
            ),
            BingoGoal(
                id = "two_lines_small_square",
                nameEn = "Two Lines + Small Square",
                nameAm = "ሁለት መስመር + ትንሽ ካሬ",
                nameTi = "ክልተ መስመር + ንእሽቶ ካሬ",
                patterns = listOf(
                    row(0) + row(4) + midCells,
                    col(0) + col(4) + midCells
                )
            ),
            BingoGoal(
                id = "plus_sign",
                nameEn = "Plus Sign",
                nameAm = "የመደመር ምልክት",
                nameTi = "ምልክት ምውሳኽ",
                patterns = listOf(cellsWhere { c, r -> c == 2 || r == 2 })
            ),
            BingoGoal(
                id = "letter_t",
                nameEn = "Letter T",
                nameAm = "ፊደል T",
                nameTi = "ፊደል T",
                patterns = listOf(cellsWhere { c, r -> r == 0 || c == 2 })
            ),
            BingoGoal(
                id = "letter_l",
                nameEn = "Letter L",
                nameAm = "ፊደል L",
                nameTi = "ፊደል L",
                patterns = listOf(cellsWhere { c, r -> c == 0 || r == 4 })
            ),
            BingoGoal(
                id = "diamond",
                nameEn = "Diamond",
                nameAm = "አልማዝ",
                nameTi = "ኣልማዝ",
                patterns = listOf(cellsWhere { c, r -> abs(c - 2) + abs(r - 2) == 2 })
            ),
            BingoGoal(
                id = "letter_h",
                nameEn = "Letter H",
                nameAm = "ፊደል H",
                nameTi = "ፊደል H",
                patterns = listOf(cellsWhere { c, r -> c == 0 || c == 4 || r == 2 })
            ),
            BingoGoal(
                id = "letter_z",
                nameEn = "Letter Z",
                nameAm = "ፊደል Z",
                nameTi = "ፊደል Z",
                patterns = listOf(cellsWhere { c, r -> r == 0 || r == 4 || (c + r == 4) })
            )
        )
    }

    /**
     * Checks if marked cells satisfy the goal.
     * Returns the winning pattern cells if true, or null if false.
     */
    fun checkWinningPattern(goal: BingoGoal, markedCells: Set<Pair<Int, Int>>): List<Pair<Int, Int>>? {
        for (pattern in goal.patterns) {
            val allMarked = pattern.all { cell -> markedCells.contains(cell) }
            if (allMarked) {
                return pattern
            }
        }
        return null
    }

    /**
     * Amharic phonetic representation for numbers and bingo calls
     * G59 -> "ጂ፣ ሃምሳ ዘጠኝ፣ አምስት ዘጠኝ"
     */
    private val AMO = listOf("", "አንድ", "ሁለት", "ሶስት", "አራት", "አምስት", "ስድስት", "ሰባት", "ስምንት", "ዘጠኝ")
    private val AMT = listOf("", "አስር", "ሃያ", "ሰላሳ", "አርባ", "ሃምሳ", "ስልሳ", "ሰባ")
    private val AMD = listOf("ዜሮ") + AMO.drop(1)
    private val AML = mapOf('B' to "ቢ", 'I' to "አይ", 'N' to "ኤን", 'G' to "ጂ", 'O' to "ኦ")

    fun amharicNumberWords(n: Int): String {
        if (n <= 0) return ""
        val tens = n / 10
        val ones = n % 10
        if (tens == 0) return AMO.getOrElse(ones) { "$n" }
        if (tens == 1) return if (ones != 0) "አስራ " + AMO[ones] else "አስር"
        val tensWord = AMT.getOrElse(tens) { "" }
        val onesWord = if (ones != 0) " " + AMO[ones] else ""
        return tensWord + onesWord
    }

    fun amharicCallout(n: Int): String {
        val col = BingoColumn.forNumber(n)
        val letterAm = AML[col.letter] ?: "${col.letter}"
        val numWords = amharicNumberWords(n)
        // Fast, punchy, and crystal-clear caller phrasing
        return "$letterAm፣ $numWords"
    }

    fun englishCallout(n: Int): String {
        val col = BingoColumn.forNumber(n)
        return "${col.letter} $n"
    }
}
