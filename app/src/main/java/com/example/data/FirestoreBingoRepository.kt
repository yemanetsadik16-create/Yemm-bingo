package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.R
import com.example.model.DepositRequest
import com.example.model.PaymentMethod
import com.example.model.UserProfile
import com.example.model.WinHistoryRecord
import com.example.model.WithdrawalRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreBingoRepository(
    private val context: Context,
    val db: FirebaseFirestore,
    val auth: FirebaseAuth = Firebase.auth
) {
    companion object {
        private const val TAG = "FirestoreBingoRepo"

        fun create(context: Context): FirestoreBingoRepository {
            val databaseId = context.getString(R.string.firestore_database_id)
            val firestore = FirebaseFirestore.getInstance(databaseId)
            return FirestoreBingoRepository(context.applicationContext, firestore)
        }
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth.currentUser

    /**
     * Interactive Google Sign-In via Credential Manager.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> {
        return try {
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (e: Exception) {
                "108825468223-69fijp6luf8djgkl2nntgcser1vsfff6.apps.googleusercontent.com"
            }
            val credentialManager = CredentialManager.create(activityContext)

            val signInOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(
                context = activityContext,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authResult = auth.signInWithCredential(
                    GoogleAuthProvider.getCredential(idToken, null)
                ).await()

                val user = authResult.user ?: throw IllegalStateException("FirebaseUser is null")
                val displayName = user.displayName?.ifBlank { null }
                    ?: user.email?.substringBefore("@")
                    ?: "Player_${user.uid.take(5)}"
                val phone = user.phoneNumber ?: "0939383425"

                // Fetch existing or create user document in Firestore
                val profile = loadOrCreateUserProfile(user.uid, displayName, phone)
                Result.success(profile)
            } else {
                Result.failure(IllegalStateException("Unexpected credential type: ${credential.type}"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-in was cancelled by user", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed Google Sign-In", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    /**
     * Load or initialize the UserProfile in Firestore.
     */
    suspend fun loadOrCreateUserProfile(
        userId: String,
        fallbackName: String,
        fallbackPhone: String
    ): UserProfile {
        val userDoc = db.collection("users").document(userId).get().await()
        if (userDoc.exists()) {
            val username = userDoc.getString("username") ?: fallbackName
            val phone = userDoc.getString("phone") ?: fallbackPhone
            val playingTime = userDoc.getString("playingTime") ?: "8:00 O’clock Day"
            val wallet = (userDoc.getLong("wallet") ?: 150L).toInt()
            val seedsRaw = userDoc.get("ownedCardSeeds") as? Map<*, *>
            val seeds = seedsRaw?.mapNotNull { (k, v) ->
                if (k is String && v is Number) k to v.toLong() else null
            }?.toMap() ?: emptyMap()

            return UserProfile(
                username = username,
                phone = phone,
                playingTime = playingTime,
                wallet = wallet,
                ownedCardSeeds = seeds
            )
        } else {
            val newProfile = UserProfile(
                username = fallbackName,
                phone = fallbackPhone,
                playingTime = "8:00 O’clock Day",
                wallet = 150
            )
            saveUserProfile(userId, newProfile)
            return newProfile
        }
    }

    /**
     * Upsert user profile to Firestore `users/{userId}`.
     */
    suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        val data = hashMapOf<String, Any>(
            "userId" to userId,
            "username" to profile.username,
            "phone" to profile.phone,
            "playingTime" to profile.playingTime,
            "wallet" to profile.wallet,
            "ownedCardSeeds" to profile.ownedCardSeeds,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val docRef = db.collection("users").document(userId)
        val doc = docRef.get().await()
        if (!doc.exists()) {
            data["createdAt"] = FieldValue.serverTimestamp()
            docRef.set(data).await()
        } else {
            docRef.set(data, SetOptions.merge()).await()
        }
    }

    /**
     * Real-time listener for current user document.
     */
    fun observeUserProfile(userId: String, onUpdate: (UserProfile) -> Unit): ListenerRegistration {
        return db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing user profile: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val username = snapshot.getString("username") ?: "Player"
                    val phone = snapshot.getString("phone") ?: ""
                    val playingTime = snapshot.getString("playingTime") ?: "8:00 O’clock Day"
                    val wallet = (snapshot.getLong("wallet") ?: 0L).toInt()
                    val seedsRaw = snapshot.get("ownedCardSeeds") as? Map<*, *>
                    val seeds = seedsRaw?.mapNotNull { (k, v) ->
                        if (k is String && v is Number) k to v.toLong() else null
                    }?.toMap() ?: emptyMap()

                    onUpdate(
                        UserProfile(
                            username = username,
                            phone = phone,
                            playingTime = playingTime,
                            wallet = wallet,
                            ownedCardSeeds = seeds
                        )
                    )
                }
            }
    }

    /**
     * Save deposit to Firestore `deposits/{depositId}`.
     */
    suspend fun addDeposit(deposit: DepositRequest) {
        val uid = auth.currentUser?.uid ?: return
        val docData = hashMapOf(
            "depositId" to deposit.id,
            "userId" to uid,
            "username" to deposit.username,
            "amount" to deposit.amount,
            "paymentMethod" to deposit.paymentMethod.code,
            "receiptId" to deposit.receiptId,
            "fullReceipt" to deposit.fullReceipt,
            "status" to deposit.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("deposits").document(deposit.id).set(docData).await()
    }

    /**
     * Save withdrawal to Firestore `withdrawals/{withdrawId}`.
     */
    suspend fun addWithdrawal(request: WithdrawalRequest) {
        val uid = auth.currentUser?.uid ?: return
        val docData = hashMapOf(
            "withdrawId" to request.id,
            "userId" to uid,
            "username" to request.username,
            "amount" to request.amount,
            "paymentMethod" to request.paymentMethod.code,
            "targetAccount" to request.targetAccount,
            "status" to request.status,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("withdrawals").document(request.id).set(docData).await()
    }

    /**
     * Save winning record to Firestore `winners/{winnerId}`.
     */
    suspend fun recordWin(win: WinHistoryRecord) {
        val uid = auth.currentUser?.uid ?: "anonymous"
        val docData = hashMapOf(
            "winnerId" to win.id,
            "userId" to uid,
            "username" to win.username,
            "phone" to win.phone,
            "gameTime" to win.gameTime,
            "roundIndex" to win.roundIndex,
            "rewardBirr" to win.rewardBirr,
            "cardNumber" to win.cardNumber,
            "callsCount" to win.callsCount,
            "createdAt" to FieldValue.serverTimestamp()
        )
        db.collection("winners").document(win.id).set(docData).await()
    }

    /**
     * Multiplayer live room sync.
     */
    suspend fun syncGameRoomState(
        roomId: String,
        slot: String,
        round: Int,
        status: String,
        calledNumbers: List<Int>,
        winnerName: String?,
        winnerCard: Int?,
        prizePool: Int
    ) {
        if (auth.currentUser == null) return
        val roomData = hashMapOf<String, Any>(
            "roomId" to roomId,
            "slot" to slot,
            "round" to round,
            "status" to status,
            "calledNumbers" to calledNumbers,
            "prizePool" to prizePool,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        winnerName?.let { roomData["winnerName"] = it }
        winnerCard?.let { roomData["winnerCardNumber"] = it }

        val docRef = db.collection("game_rooms").document(roomId)
        val doc = docRef.get().await()
        if (!doc.exists()) {
            roomData["createdAt"] = FieldValue.serverTimestamp()
            docRef.set(roomData).await()
        } else {
            docRef.set(roomData, SetOptions.merge()).await()
        }
    }

    /**
     * Listen for multiplayer room changes.
     */
    fun observeGameRoom(
        roomId: String,
        onUpdate: (calledNumbers: List<Int>, status: String, winner: String?) -> Unit
    ): ListenerRegistration? {
        if (auth.currentUser == null) return null
        return db.collection("game_rooms").document(roomId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error observing game room: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val rawList = snapshot.get("calledNumbers") as? List<*>
                    val called = rawList?.filterIsInstance<Number>()?.map { it.toInt() } ?: emptyList()
                    val status = snapshot.getString("status") ?: "waiting"
                    val winner = snapshot.getString("winnerName")
                    onUpdate(called, status, winner)
                }
            }
    }
}
