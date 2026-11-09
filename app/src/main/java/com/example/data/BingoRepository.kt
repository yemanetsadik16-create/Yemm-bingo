package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.DepositRequest
import com.example.model.UserProfile
import com.example.model.WinHistoryRecord
import com.example.model.WithdrawalRequest
import org.json.JSONArray
import org.json.JSONObject

/**
 * Robust local persistence with shared preferences & JSON serialization.
 * Stores user profiles, authentication state, cartela reservations,
 * wallet deposits/withdrawals, and 24h winner history.
 */
class BingoRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("yeme_bingo_store", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER = "current_user_name"
        private const val KEY_USERS_JSON = "users_map_json"
        private const val KEY_DEPOSITS_JSON = "deposits_list_json"
        private const val KEY_WITHDRAWALS_JSON = "withdrawals_list_json"
        private const val KEY_WINNERS_JSON = "winners_list_json"
        private const val KEY_LANGUAGE = "app_language"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_AUTO_MARK = "auto_mark_enabled"
        private const val KEY_CALLER_SPEED = "caller_voice_speed"
        private const val KEY_CALLER_PITCH = "caller_voice_pitch"
    }

    init {
        // Ensure all legacy demo accounts are purged so only accounts registered on the server exist
        purgeDemoAccounts()
    }

    private fun purgeDemoAccounts() {
        val users = getAllUsers().toMutableMap()
        var modified = false
        val demoNames = setOf("Abebe", "Sara", "Yemane")
        demoNames.forEach { demoName ->
            if (users.containsKey(demoName)) {
                val u = users[demoName]
                if (u?.phone == "0911223344" || u?.phone == "0922334455" || (u?.username == "Yemane" && u.wallet == 350)) {
                    users.remove(demoName)
                    modified = true
                }
            }
        }
        val current = getCurrentUser()
        if (current == "Abebe" || current == "Sara" || (current == "Yemane" && users["Yemane"] == null)) {
            setCurrentUser(null)
        }
        if (modified) {
            saveUsersMap(users)
        }
    }

    // --- Current Active User Session ---
    fun getCurrentUser(): String? {
        return prefs.getString(KEY_CURRENT_USER, null)
    }

    fun setCurrentUser(username: String?) {
        prefs.edit().putString(KEY_CURRENT_USER, username).apply()
    }

    // --- Users Collection ---
    fun getUserProfile(username: String): UserProfile? {
        return getAllUsers()[username]
    }

    fun saveUser(profile: UserProfile) {
        val all = getAllUsers().toMutableMap()
        all[profile.username] = profile
        saveUsersMap(all)
    }

    fun getAllUsers(): Map<String, UserProfile> {
        val raw = prefs.getString(KEY_USERS_JSON, null) ?: return emptyMap()
        val result = mutableMapOf<String, UserProfile>()
        try {
            val root = JSONObject(raw)
            val keys = root.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val userObj = root.getJSONObject(key)
                val username = userObj.getString("username")
                val phone = userObj.getString("phone")
                val time = userObj.getString("playingTime")
                val wallet = userObj.getInt("wallet")

                val seedsMap = mutableMapOf<String, Long>()
                if (userObj.has("seeds")) {
                    val sObj = userObj.getJSONObject("seeds")
                    val sKeys = sObj.keys()
                    while (sKeys.hasNext()) {
                        val sk = sKeys.next()
                        seedsMap[sk] = sObj.getLong(sk)
                    }
                }

                result[key] = UserProfile(
                    username = username,
                    phone = phone,
                    playingTime = time,
                    wallet = wallet,
                    ownedCardSeeds = seedsMap
                )
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun saveUsersMap(map: Map<String, UserProfile>) {
        val root = JSONObject()
        for ((k, v) in map) {
            val userObj = JSONObject()
            userObj.put("username", v.username)
            userObj.put("phone", v.phone)
            userObj.put("playingTime", v.playingTime)
            userObj.put("wallet", v.wallet)

            val sObj = JSONObject()
            v.ownedCardSeeds.forEach { (sk, sv) -> sObj.put(sk, sv) }
            userObj.put("seeds", sObj)

            root.put(k, userObj)
        }
        prefs.edit().putString(KEY_USERS_JSON, root.toString()).apply()
    }

    // --- Deposits ---
    fun getDeposits(): List<DepositRequest> {
        val raw = prefs.getString(KEY_DEPOSITS_JSON, null) ?: return emptyList()
        val list = mutableListOf<DepositRequest>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    DepositRequest(
                        id = o.getString("id"),
                        username = o.getString("username"),
                        amount = o.getInt("amount"),
                        paymentMethod = com.example.model.PaymentMethod.fromCode(o.optString("paymentMethod", "telebirr")),
                        receiptId = o.getString("receiptId"),
                        fullReceipt = o.getString("fullReceipt"),
                        timestamp = o.getLong("timestamp"),
                        status = o.getString("status")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun addDeposit(deposit: DepositRequest) {
        val list = getDeposits().toMutableList()
        list.add(0, deposit)
        saveDeposits(list)
    }

    fun updateDepositStatus(depositId: String, newStatus: String, adjustedAmount: Int? = null) {
        val list = getDeposits().toMutableList()
        val index = list.indexOfFirst { it.id == depositId }
        if (index != -1) {
            val old = list[index]
            val updated = old.copy(
                status = newStatus,
                amount = adjustedAmount ?: old.amount
            )
            list[index] = updated
            saveDeposits(list)

            if (newStatus == "approved") {
                val user = getUserProfile(updated.username)
                if (user != null) {
                    saveUser(user.copy(wallet = user.wallet + updated.amount))
                }
            }
        }
    }

    private fun saveDeposits(list: List<DepositRequest>) {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("username", it.username)
            o.put("amount", it.amount)
            o.put("paymentMethod", it.paymentMethod.code)
            o.put("receiptId", it.receiptId)
            o.put("fullReceipt", it.fullReceipt)
            o.put("timestamp", it.timestamp)
            o.put("status", it.status)
            arr.put(o)
        }
        prefs.edit().putString(KEY_DEPOSITS_JSON, arr.toString()).apply()
    }

    // --- Withdrawals ---
    fun getWithdrawals(): List<WithdrawalRequest> {
        val raw = prefs.getString(KEY_WITHDRAWALS_JSON, null) ?: return emptyList()
        val list = mutableListOf<WithdrawalRequest>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    WithdrawalRequest(
                        id = o.getString("id"),
                        username = o.getString("username"),
                        amount = o.getInt("amount"),
                        paymentMethod = com.example.model.PaymentMethod.fromCode(o.optString("paymentMethod", "telebirr")),
                        targetAccount = o.optString("targetAccount", o.optString("telebirrAccount", "")),
                        timestamp = o.getLong("timestamp"),
                        status = o.getString("status")
                    )
                )
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun addWithdrawal(request: WithdrawalRequest) {
        val list = getWithdrawals().toMutableList()
        list.add(0, request)
        saveWithdrawals(list)
    }

    fun updateWithdrawalStatus(withdrawalId: String, newStatus: String) {
        val list = getWithdrawals().toMutableList()
        val index = list.indexOfFirst { it.id == withdrawalId }
        if (index != -1) {
            val old = list[index]
            list[index] = old.copy(status = newStatus)
            saveWithdrawals(list)

            // If declined, refund wallet
            if (newStatus == "declined") {
                val user = getUserProfile(old.username)
                if (user != null) {
                    saveUser(user.copy(wallet = user.wallet + old.amount))
                }
            }
        }
    }

    private fun saveWithdrawals(list: List<WithdrawalRequest>) {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("username", it.username)
            o.put("amount", it.amount)
            o.put("paymentMethod", it.paymentMethod.code)
            o.put("targetAccount", it.targetAccount)
            o.put("timestamp", it.timestamp)
            o.put("status", it.status)
            arr.put(o)
        }
        prefs.edit().putString(KEY_WITHDRAWALS_JSON, arr.toString()).apply()
    }

    // --- Winners History (Purged after 24 hours) ---
    fun getWinners(): List<WinHistoryRecord> {
        val raw = prefs.getString(KEY_WINNERS_JSON, null) ?: return emptyList()
        val list = mutableListOf<WinHistoryRecord>()
        val oneDayAgo = System.currentTimeMillis() - 86400000L
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val ts = o.getLong("timestamp")
                if (ts >= oneDayAgo) {
                    list.add(
                        WinHistoryRecord(
                            id = o.getString("id"),
                            username = o.getString("username"),
                            phone = o.optString("phone", "-"),
                            gameTime = o.optString("gameTime", "-"),
                            roundIndex = o.optInt("roundIndex", 1),
                            rewardBirr = o.getInt("rewardBirr"),
                            cardNumber = o.getInt("cardNumber"),
                            callsCount = o.getInt("callsCount"),
                            timestamp = ts
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    fun recordWin(win: WinHistoryRecord) {
        val list = getWinners().toMutableList()
        list.add(0, win)
        saveWinners(list)
    }

    private fun saveWinners(list: List<WinHistoryRecord>) {
        val arr = JSONArray()
        list.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("username", it.username)
            o.put("phone", it.phone)
            o.put("gameTime", it.gameTime)
            o.put("roundIndex", it.roundIndex)
            o.put("rewardBirr", it.rewardBirr)
            o.put("cardNumber", it.cardNumber)
            o.put("callsCount", it.callsCount)
            o.put("timestamp", it.timestamp)
            arr.put(o)
        }
        prefs.edit().putString(KEY_WINNERS_JSON, arr.toString()).apply()
    }

    // --- Settings Preferences ---
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    fun setLanguage(lang: String) = prefs.edit().putString(KEY_LANGUAGE, lang).apply()

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()

    fun isAutoMarkEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_MARK, false)
    fun setAutoMarkEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_AUTO_MARK, enabled).apply()

    fun getCallerSpeed(): Float = prefs.getFloat(KEY_CALLER_SPEED, 1.15f)
    fun setCallerSpeed(speed: Float) = prefs.edit().putFloat(KEY_CALLER_SPEED, speed).apply()

    fun getCallerPitch(): Float = prefs.getFloat(KEY_CALLER_PITCH, 0.98f)
    fun setCallerPitch(pitch: Float) = prefs.edit().putFloat(KEY_CALLER_PITCH, pitch).apply()
}
