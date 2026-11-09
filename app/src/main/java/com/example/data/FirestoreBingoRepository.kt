package com.example.data

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.example.model.DepositRequest
import com.example.model.UserProfile
import com.example.model.WinHistoryRecord
import com.example.model.WithdrawalRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
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
    val db: FirebaseFirestore?,
    val auth: FirebaseAuth?
) {
    companion object {
        private const val TAG = "FirestoreBingoRepo"

        fun create(context: Context): FirestoreBingoRepository {
            return try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context)
                }
                val databaseId = try {
                    context.getString(R.string.firestore_database_id)
                } catch (_: Exception) {
                    null
                }
                val firestore = if (!databaseId.isNullOrBlank()) {
                    FirebaseFirestore.getInstance(databaseId)
                } else {
                    FirebaseFirestore.getInstance()
                }
                val auth = Firebase.auth
                FirestoreBingoRepository(context.applicationContext, firestore, auth)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Firebase services. Running in safe local mode.", e)
                FirestoreBingoRepository(context.applicationContext, null, null)
            }
        }
    }

    val isAvailable: Boolean
        get() = db != null && auth != null

    val currentFirebaseUser: FirebaseUser?
        get() = auth?.currentUser

    /**
     * Interactive Google Sign-In via Credential Manager.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfile> {
        val currentAuth = auth
        if (currentAuth == null) {
            return Result.failure(IllegalStateException("Firebase Auth is not available on this device"))
        }

        return try {
            val webClientId = try {
                context.getString(R.string.default_web_client_id)
            } catch (_: Exception) {
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
                val authResult = currentAuth.signInWithCredential(
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
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sign out", e)
        }
    }

    /**
     * Load or initialize the UserProfile in Firestore.
     */
    suspend fun loadOrCreateUserProfile(
        userId: String,
        fallbackName: String,
        fallbackPhone: String
    ): UserProfile {
        val firestore = db
        if (firestore == null) {
            return UserProfile(
                username = fallbackName,
                phone = fallbackPhone,
                playingTime = "8:00 O’clock Day",
                wallet = 150
            )
        }

        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val username = userDoc.getString("username") ?: fallbackName
                val phone = userDoc.getString("phone") ?: fallbackPhone
                val playingTime = userDoc.getString("playingTime") ?: "8:00 O’clock Day"
                val wallet = (userDoc.getLong("wallet") ?: 150L).toInt()
                val seedsRaw = userDoc.get("ownedCardSeeds") as? Map<*, *>
                val seeds = seedsRaw?.mapNotNull { (k, v) ->
                    if (k is String && v is Number) k to v.toLong() else null
                }?.toMap() ?: emptyMap()

                UserProfile(
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
                newProfile
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading user profile", e)
            UserProfile(
                username = fallbackName,
                phone = fallbackPhone,
                playingTime = "8:00 O’clock Day",
                wallet = 150
            )
        }
    }

    /**
     * Upsert user profile to Firestore `users/{userId}`.
     */
    suspend fun saveUserProfile(userId: String, profile: UserProfile) {
        val firestore = db ?: return
        try {
            val data = hashMapOf<String, Any>(
                "userId" to userId,
                "username" to profile.username,
                "phone" to profile.phone,
                "playingTime" to profile.playingTime,
                "wallet" to profile.wallet,
                "ownedCardSeeds" to profile.ownedCardSeeds,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            val docRef = firestore.collection("users").document(userId)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                data["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(data).await()
            } else {
                docRef.set(data, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
        }
    }

    /**
     * Real-time listener for current user document.
     */
    fun observeUserProfile(userId: String, onUpdate: (UserProfile) -> Unit): ListenerRegistration {
        val firestore = db ?: return ListenerRegistration { }
        return firestore.collection("users").document(userId)
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
        val firestore = db ?: return
        val uid = auth?.currentUser?.uid ?: return
        try {
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
            firestore.collection("deposits").document(deposit.id).set(docData).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding deposit", e)
        }
    }

    /**
     * Save withdrawal to Firestore `withdrawals/{withdrawId}`.
     */
    suspend fun addWithdrawal(request: WithdrawalRequest) {
        val firestore = db ?: return
        val uid = auth?.currentUser?.uid ?: return
        try {
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
            firestore.collection("withdrawals").document(request.id).set(docData).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error adding withdrawal", e)
        }
    }

    /**
     * Save winning record to Firestore `winners/{winnerId}`.
     */
    suspend fun recordWin(win: WinHistoryRecord) {
        val firestore = db ?: return
        val uid = auth?.currentUser?.uid ?: "anonymous"
        try {
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
            firestore.collection("winners").document(win.id).set(docData).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error recording win", e)
        }
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
        val firestore = db ?: return
        if (auth?.currentUser == null) return
        try {
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

            val docRef = firestore.collection("game_rooms").document(roomId)
            val doc = docRef.get().await()
            if (!doc.exists()) {
                roomData["createdAt"] = FieldValue.serverTimestamp()
                docRef.set(roomData).await()
            } else {
                docRef.set(roomData, SetOptions.merge()).await()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing game room state", e)
        }
    }

    /**
     * Listen for multiplayer room changes.
     */
    fun observeGameRoom(
        roomId: String,
        onUpdate: (calledNumbers: List<Int>, status: String, winner: String?) -> Unit
    ): ListenerRegistration? {
        val firestore = db ?: return null
        if (auth?.currentUser == null) return null
        return try {
            firestore.collection("game_rooms").document(roomId)
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to observe game room", e)
            null
        }
    }
}
