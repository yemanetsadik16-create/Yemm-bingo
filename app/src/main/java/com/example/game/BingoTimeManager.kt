package com.example.game

import com.example.model.GameSlot
import java.util.Calendar
import java.util.TimeZone

/**
 * Handles live timing for Ethiopian Time (UTC+3 / East Africa Time).
 * Calculates remaining countdown to the next scheduled slot, active game sessions,
 * 3-round intervals (75 balls * 5s + break), and deterministic slot IDs.
 */
object BingoTimeManager {

    private val TIME_ZONE_UTC3: TimeZone = TimeZone.getTimeZone("GMT+3")
    const val INTERVAL_PER_BALL_MS = 5000L
    const val BREAK_BETWEEN_ROUNDS_MS = 20000L
    const val TOTAL_ROUNDS = 3
    const val BALLS_PER_GAME = 75
    const val LEAD_TIME_MS = 5000L // 5 seconds "Ready" count before ball 1

    data class ScheduledSession(
        val slot: GameSlot,
        val startTimeMillis: Long,
        val sessionId: String,
        val rounds: List<RoundSchedule>
    )

    data class RoundSchedule(
        val roundNumber: Int,
        val roundGid: String,
        val startTimeMillis: Long,
        val endTimeMillis: Long
    )

    fun nowUtc3Millis(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Determines the next game slot and full schedule
     */
    fun getNextSlot(targetSlotLabel: String? = null): ScheduledSession {
        val now = nowUtc3Millis()
        val calendar = Calendar.getInstance(TIME_ZONE_UTC3).apply {
            timeInMillis = now
        }

        // Search slots today, tomorrow, day after
        val candidateSlots = mutableListOf<ScheduledSession>()

        for (dayOffset in 0..2) {
            val checkCal = Calendar.getInstance(TIME_ZONE_UTC3).apply {
                timeInMillis = now
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }

            for (slot in BingoEngine.SLOTS) {
                if (targetSlotLabel != null && slot.labelEn != targetSlotLabel) {
                    continue
                }

                val slotCal = Calendar.getInstance(TIME_ZONE_UTC3).apply {
                    set(Calendar.YEAR, checkCal.get(Calendar.YEAR))
                    set(Calendar.MONTH, checkCal.get(Calendar.MONTH))
                    set(Calendar.DAY_OF_MONTH, checkCal.get(Calendar.DAY_OF_MONTH))
                    set(Calendar.HOUR_OF_DAY, slot.hour24)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val slotStart = slotCal.timeInMillis
                val rounds = mutableListOf<RoundSchedule>()
                var roundStart = slotStart
                val sessionId = "g" + slotCal.get(Calendar.YEAR) +
                        String.format("%02d", slotCal.get(Calendar.MONTH) + 1) +
                        String.format("%02d", slotCal.get(Calendar.DAY_OF_MONTH)) +
                        String.format("%02d", slot.hour24)

                for (r in 1..TOTAL_ROUNDS) {
                    val roundEnd = roundStart + LEAD_TIME_MS + (BALLS_PER_GAME * INTERVAL_PER_BALL_MS)
                    rounds.add(
                        RoundSchedule(
                            roundNumber = r,
                            roundGid = "${sessionId}r$r",
                            startTimeMillis = roundStart,
                            endTimeMillis = roundEnd
                        )
                    )
                    roundStart = roundEnd + BREAK_BETWEEN_ROUNDS_MS
                }

                candidateSlots.add(
                    ScheduledSession(
                        slot = slot,
                        startTimeMillis = slotStart,
                        sessionId = sessionId,
                        rounds = rounds
                    )
                )
            }
        }

        // Return current active session or first upcoming session
        val activeOrUpcoming = candidateSlots.firstOrNull { session ->
            val finalRoundEnd = session.rounds.last().endTimeMillis
            now < finalRoundEnd
        } ?: candidateSlots.first()

        return activeOrUpcoming
    }

    /**
     * Format countdown string (e.g. "01H:23M:45S")
     */
    fun formatCountdown(remainingMillis: Long): String {
        val totalSecs = (remainingMillis / 1000).coerceAtLeast(0)
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return "${hours}H:${mins}M:${secs}S"
    }
}
