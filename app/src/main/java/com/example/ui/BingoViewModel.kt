package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.BingoSoundManager
import com.example.data.BingoRepository
import com.example.game.BingoEngine
import com.example.game.BingoTimeManager
import com.example.model.AppLanguage
import com.example.model.BingoCard
import com.example.model.BingoGoal
import com.example.model.DepositRequest
import com.example.model.UserProfile
import com.example.model.WinHistoryRecord
import com.example.model.WithdrawalRequest
import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.FirestoreBingoRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
    data object Home : Screen()
    data object Register : Screen()
    data object Auth : Screen()
    data object LiveGame : Screen()
    data object Schedule : Screen()
    data object HowToPlay : Screen()
    data object About : Screen()
    data object Admin : Screen()
}

data class LiveGameState(
    val isGameActive: Boolean = false,
    val isPracticeMode: Boolean = true,
    val currentRoundNumber: Int = 1,
    val totalRounds: Int = 3,
    val goal: BingoGoal = BingoEngine.ALL_GOALS.first(),
    val prizePoolBirr: Int = 210,
    val calledNumbers: List<Int> = emptyList(),
    val lastCalledNumber: Int? = null,
    val currentReadyCount: String? = null, // "Ready", "3", "2", "1"
    val playerCards: List<BingoCard> = emptyList(),
    // Card index to set of marked (col, row)
    val cardMarks: Map<Int, Set<Pair<Int, Int>>> = emptyMap(),
    val winningCardIndex: Int? = null,
    val winningCells: List<Pair<Int, Int>>? = null,
    val winnerName: String? = null,
    val isGameOver: Boolean = false,
    val statusMessage: String? = null
)

data class BingoUiState(
    val currentScreen: Screen = Screen.Home,
    val language: AppLanguage = AppLanguage.EN,
    val currentUser: UserProfile? = null,
    val isAdminLoggedIn: Boolean = false,
    val soundEnabled: Boolean = true,
    val autoMarkEnabled: Boolean = false,
    val callerSpeed: Float = 1.15f,
    val callerPitch: Float = 0.98f,

    // Time & Countdown
    val countdownText: String = "00H:00M:00S",
    val nextSlotLabel: String = "8:00 O’clock Day",

    // Lobby & Cartela selection
    val selectedCardNumbers: Set<Int> = setOf(1),
    val previewCardNumber: Int = 1,
    val previewSeed: Long = 1001L,
    val previewCard: BingoCard = BingoEngine.generateCard(1, 1001L),
    val occupiedCards: Set<Int> = emptySet(),

    // Live Game
    val liveGame: LiveGameState = LiveGameState(),

    // Admin & Ledger
    val deposits: List<DepositRequest> = emptyList(),
    val withdrawals: List<WithdrawalRequest> = emptyList(),
    val allUsers: List<UserProfile> = emptyList(),
    val recentWinners: List<WinHistoryRecord> = emptyList(),

    // Feedback
    val toastMessage: String? = null,
    val isAuthLoading: Boolean = false,
    val isFirebaseConnected: Boolean = true
)

class BingoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = BingoRepository(application)
    val firestoreRepo = FirestoreBingoRepository.create(application)
    private var firestoreUserListener: ListenerRegistration? = null
    val soundManager = BingoSoundManager(application)

    private val _uiState = MutableStateFlow(BingoUiState())
    val uiState: StateFlow<BingoUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var liveGameJob: Job? = null

    init {
        val savedLang = when (repository.getLanguage()) {
            "am" -> AppLanguage.AM
            "ti" -> AppLanguage.TI
            else -> AppLanguage.EN
        }
        val currentUsername = repository.getCurrentUser()
        val userProfile = currentUsername?.let { repository.getUserProfile(it) }

        val savedSpeed = repository.getCallerSpeed()
        val savedPitch = repository.getCallerPitch()
        soundManager.setCallerSpeed(savedSpeed)
        soundManager.setCallerPitch(savedPitch)

        val firebaseUser = firestoreRepo.currentFirebaseUser
        if (firebaseUser != null) {
            attachFirestoreUserListener(firebaseUser.uid)
        }

        _uiState.update {
            it.copy(
                language = savedLang,
                currentUser = userProfile,
                soundEnabled = repository.isSoundEnabled(),
                autoMarkEnabled = repository.isAutoMarkEnabled(),
                callerSpeed = savedSpeed,
                callerPitch = savedPitch,
                deposits = repository.getDeposits(),
                withdrawals = repository.getWithdrawals(),
                allUsers = repository.getAllUsers().values.toList(),
                recentWinners = repository.getWinners()
            )
        }

        startCountdownTimer()
    }

    fun navigateTo(screen: Screen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setLanguage(lang: AppLanguage) {
        repository.setLanguage(lang.code)
        _uiState.update { it.copy(language = lang) }
    }

    fun toggleSound() {
        val newState = !_uiState.value.soundEnabled
        repository.setSoundEnabled(newState)
        _uiState.update { it.copy(soundEnabled = newState) }
        soundManager.vibrate(30L)
    }

    fun toggleAutoMark() {
        val newState = !_uiState.value.autoMarkEnabled
        repository.setAutoMarkEnabled(newState)
        _uiState.update { it.copy(autoMarkEnabled = newState) }
    }

    fun setCallerSpeed(speed: Float) {
        repository.setCallerSpeed(speed)
        soundManager.setCallerSpeed(speed)
        _uiState.update { it.copy(callerSpeed = speed) }
    }

    fun setCallerPitch(pitch: Float) {
        repository.setCallerPitch(pitch)
        soundManager.setCallerPitch(pitch)
        _uiState.update { it.copy(callerPitch = pitch) }
    }

    fun testCallerVoice() {
        soundManager.testCallerVoice()
        showToast("Playing sample caller voice...")
    }

    // --- Countdown Timer ---
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val userTime = _uiState.value.currentUser?.playingTime
                val session = BingoTimeManager.getNextSlot(userTime)
                val diff = session.startTimeMillis - BingoTimeManager.nowUtc3Millis()
                val text = BingoTimeManager.formatCountdown(diff.coerceAtLeast(0L))

                _uiState.update {
                    it.copy(
                        countdownText = text,
                        nextSlotLabel = session.slot.labelEn
                    )
                }
                delay(1000L)
            }
        }
    }

    // --- Authentication ---
    fun login(username: String, phoneOrPass: String): Boolean {
        val user = repository.getUserProfile(username)
        return if (user != null) {
            repository.setCurrentUser(username)
            _uiState.update { it.copy(currentUser = user, currentScreen = Screen.Auth) }
            showToast("Welcome back, $username!")
            true
        } else {
            showToast("User not found. Please register first.")
            false
        }
    }

    fun register(username: String, phone: String, playingTime: String): Boolean {
        if (username.length < 3) {
            showToast("Username must be at least 3 characters")
            return false
        }
        if (phone.length < 9) {
            showToast("Enter a valid phone number")
            return false
        }
        if (playingTime.isEmpty()) {
            showToast("Select your playing time")
            return false
        }

        val newUser = UserProfile(
            username = username,
            phone = phone,
            playingTime = playingTime,
            wallet = 150 // Free starting balance (5 cartelas) for immediate real fun
        )
        repository.saveUser(newUser)
        repository.setCurrentUser(username)
        _uiState.update {
            it.copy(
                currentUser = newUser,
                allUsers = repository.getAllUsers().values.toList(),
                currentScreen = Screen.Auth
            )
        }
        showToast("Registration successful! 150 Birr welcome wallet added.")
        return true
    }

    fun attachFirestoreUserListener(userId: String) {
        firestoreUserListener?.remove()
        firestoreUserListener = firestoreRepo.observeUserProfile(userId) { updatedProfile ->
            repository.saveUser(updatedProfile)
            _uiState.update { it.copy(currentUser = updatedProfile) }
        }
    }

    fun signInWithGoogle(activityContext: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthLoading = true) }
            val result = firestoreRepo.signInWithGoogle(activityContext)
            result.onSuccess { profile ->
                repository.setCurrentUser(profile.username)
                repository.saveUser(profile)
                val uid = firestoreRepo.currentFirebaseUser?.uid ?: ""
                if (uid.isNotEmpty()) {
                    attachFirestoreUserListener(uid)
                }
                _uiState.update {
                    it.copy(
                        currentUser = profile,
                        isAuthLoading = false,
                        currentScreen = Screen.Auth
                    )
                }
                showToast("Welcome ${profile.username}! Signed in with Google.")
            }.onFailure { error ->
                _uiState.update { it.copy(isAuthLoading = false) }
                if (error is GetCredentialCancellationException) {
                    showToast("Sign-in cancelled")
                } else {
                    showToast("Google sign-in error: ${error.localizedMessage ?: "Unknown error"}")
                }
            }
        }
    }

    fun logout() {
        val user = _uiState.value.currentUser?.username
        firestoreRepo.signOut()
        firestoreUserListener?.remove()
        firestoreUserListener = null
        repository.setCurrentUser(null)
        _uiState.update { it.copy(currentUser = null, currentScreen = Screen.Auth) }
        showToast(if (user != null) "Signed out from $user" else "Signed out successfully")
    }

    fun updatePlayingTime(newSlot: String) {
        val user = _uiState.value.currentUser ?: return
        val updated = user.copy(playingTime = newSlot)
        repository.saveUser(updated)
        val uid = firestoreRepo.currentFirebaseUser?.uid
        if (uid != null) {
            viewModelScope.launch {
                try {
                    firestoreRepo.saveUserProfile(uid, updated)
                } catch (_: Exception) {}
            }
        }
        _uiState.update { it.copy(currentUser = updated) }
        showToast("Playing time updated to $newSlot")
    }

    // --- Lobby & Cartela Selection ---
    fun toggleCardSelection(cardNumber: Int) {
        val currentSet = _uiState.value.selectedCardNumbers.toMutableSet()
        if (currentSet.contains(cardNumber)) {
            if (currentSet.size > 1) {
                currentSet.remove(cardNumber)
            }
        } else {
            if (currentSet.size < 10) {
                currentSet.add(cardNumber)
            } else {
                showToast("Maximum 10 cartelas per game")
                return
            }
        }
        val seed = _uiState.value.previewSeed
        _uiState.update {
            it.copy(
                selectedCardNumbers = currentSet,
                previewCardNumber = cardNumber,
                previewCard = BingoEngine.generateCard(cardNumber, seed)
            )
        }
    }

    fun selectPreviewCard(cardNumber: Int) {
        val seed = _uiState.value.previewSeed
        _uiState.update {
            it.copy(
                previewCardNumber = cardNumber,
                previewCard = BingoEngine.generateCard(cardNumber, seed)
            )
        }
    }

    fun refreshCardSeed() {
        val newSeed = (System.currentTimeMillis() % 100000L) + 1L
        _uiState.update {
            it.copy(
                previewSeed = newSeed,
                previewCard = BingoEngine.generateCard(it.previewCardNumber, newSeed)
            )
        }
        soundManager.vibrate(25L)
    }

    // --- Start / Join Live Game ---
    fun startOrJoinGame(isPractice: Boolean) {
        val cardsToPlay = _uiState.value.selectedCardNumbers.sorted().map { num ->
            BingoEngine.generateCard(num, _uiState.value.previewSeed)
        }

        val defaultMarks = mutableMapOf<Int, Set<Pair<Int, Int>>>()
        cardsToPlay.indices.forEach { index ->
            defaultMarks[index] = setOf(Pair(2, 2)) // Center FREE cell is marked
        }

        // Deduct cartela fee if real multiplayer mode
        if (!isPractice) {
            val user = _uiState.value.currentUser
            val cost = cardsToPlay.size * 30
            if (user != null) {
                if (user.wallet < cost) {
                    showToast("Insufficient balance: $cost Birr required. Please deposit.")
                    return
                }
                val updatedUser = user.copy(wallet = user.wallet - cost)
                repository.saveUser(updatedUser)
                _uiState.update { it.copy(currentUser = updatedUser) }
            }
        }

        val randomGoal = BingoEngine.ALL_GOALS.random()

        _uiState.update {
            it.copy(
                currentScreen = Screen.LiveGame,
                liveGame = LiveGameState(
                    isGameActive = true,
                    isPracticeMode = isPractice,
                    goal = randomGoal,
                    prizePoolBirr = if (isPractice) 100 else (cardsToPlay.size * 25 * 3),
                    playerCards = cardsToPlay,
                    cardMarks = defaultMarks,
                    calledNumbers = emptyList(),
                    isGameOver = false,
                    statusMessage = null
                )
            )
        }

        startLiveGameLoop()
    }

    private fun startLiveGameLoop() {
        liveGameJob?.cancel()
        liveGameJob = viewModelScope.launch {
            val seed = System.currentTimeMillis()
            val sequence = BingoEngine.generateCallSequence(seed)

            // 5 second "Ready" countdown in pure Amharic male voice
            for (step in listOf("ተዘጋጁ", "3", "2", "1")) {
                _uiState.update {
                    it.copy(
                        liveGame = it.liveGame.copy(currentReadyCount = step)
                    )
                }
                soundManager.speakReady(step)
                soundManager.vibrate(30L)
                delay(1000L)
            }

            _uiState.update {
                it.copy(liveGame = it.liveGame.copy(currentReadyCount = null))
            }

            // Call numbers one by one every 5 seconds
            for (num in sequence) {
                if (!isActive || _uiState.value.liveGame.isGameOver) break

                val newCalled = _uiState.value.liveGame.calledNumbers + num
                _uiState.update {
                    it.copy(
                        liveGame = it.liveGame.copy(
                            calledNumbers = newCalled,
                            lastCalledNumber = num
                        )
                    )
                }

                // Announce audio in pure Amharic male voice only
                soundManager.speakBallCall(num, _uiState.value.soundEnabled)

                // If auto-mark is enabled, mark corresponding cells on all user cards
                if (_uiState.value.autoMarkEnabled) {
                    autoMarkNumber(num)
                }


                delay(BingoTimeManager.INTERVAL_PER_BALL_MS)
            }
        }
    }

    private fun autoMarkNumber(number: Int) {
        val state = _uiState.value.liveGame
        val newMarks = state.cardMarks.toMutableMap()

        state.playerCards.forEachIndexed { cardIdx, card ->
            val set = newMarks[cardIdx]?.toMutableSet() ?: mutableSetOf(Pair(2, 2))
            for (c in 0..4) {
                for (r in 0..4) {
                    if (card.getNumberAt(c, r) == number) {
                        set.add(Pair(c, r))
                    }
                }
            }
            newMarks[cardIdx] = set
        }

        _uiState.update {
            it.copy(liveGame = it.liveGame.copy(cardMarks = newMarks))
        }
    }

    fun onCellClick(cardIndex: Int, col: Int, row: Int) {
        val game = _uiState.value.liveGame
        if (game.isGameOver) return

        val card = game.playerCards.getOrNull(cardIndex) ?: return
        val num = card.getNumberAt(col, row)

        // Center cell (0) is free
        if (col == 2 && row == 2) return

        if (!game.calledNumbers.contains(num)) {
            soundManager.vibrate(80L)
            showToast("$num hasn't been called yet!")
            return
        }

        val marks = game.cardMarks[cardIndex]?.toMutableSet() ?: mutableSetOf(Pair(2, 2))
        val pos = Pair(col, row)
        if (marks.contains(pos)) {
            marks.remove(pos)
        } else {
            marks.add(pos)
            soundManager.vibrate(30L)
        }

        val updatedMap = game.cardMarks.toMutableMap().apply { put(cardIndex, marks) }
        _uiState.update {
            it.copy(liveGame = it.liveGame.copy(cardMarks = updatedMap))
        }
    }

    fun claimBingo() {
        val game = _uiState.value.liveGame
        if (game.isGameOver) return

        var winningCardIdx: Int? = null
        var winningCells: List<Pair<Int, Int>>? = null

        game.playerCards.forEachIndexed { idx, _ ->
            val marks = game.cardMarks[idx] ?: emptySet()
            val winPattern = BingoEngine.checkWinningPattern(game.goal, marks)
            if (winPattern != null) {
                winningCardIdx = idx
                winningCells = winPattern
                return@forEachIndexed
            }
        }

        if (winningCardIdx != null && winningCells != null) {
            val user = _uiState.value.currentUser
            val winnerName = user?.username ?: "Guest"
            val prize = game.prizePoolBirr

            // Record win
            if (user != null) {
                val updatedWallet = user.wallet + prize
                val updatedUser = user.copy(wallet = updatedWallet)
                repository.saveUser(updatedUser)

                val winRecord = WinHistoryRecord(
                    id = UUID.randomUUID().toString(),
                    username = winnerName,
                    phone = user.phone,
                    gameTime = user.playingTime,
                    roundIndex = game.currentRoundNumber,
                    rewardBirr = prize,
                    cardNumber = game.playerCards[winningCardIdx!!].cardNumber,
                    callsCount = game.calledNumbers.size,
                    timestamp = System.currentTimeMillis()
                )
                repository.recordWin(winRecord)
                viewModelScope.launch {
                    try {
                        firestoreRepo.recordWin(winRecord)
                    } catch (e: Exception) {
                        android.util.Log.w("BingoViewModel", "Firestore recordWin error: ${e.message}")
                    }
                }

                _uiState.update {
                    it.copy(
                        currentUser = updatedUser,
                        recentWinners = repository.getWinners()
                    )
                }
            }

            soundManager.speakBingoWin(winnerName)

            _uiState.update {
                it.copy(
                    liveGame = it.liveGame.copy(
                        isGameOver = true,
                        winningCardIndex = winningCardIdx,
                        winningCells = winningCells,
                        winnerName = winnerName,
                        statusMessage = "BINGO! You won $prize Birr!"
                    )
                )
            }
        } else {
            soundManager.vibrate(100L)
            showToast("Not yet! Complete pattern: ${game.goal.nameEn} / ${game.goal.nameAm}")
        }
    }

    private fun triggerOpponentWin(opponentName: String, calls: Int) {
        soundManager.speakBingoWin(opponentName)
        _uiState.update {
            it.copy(
                liveGame = it.liveGame.copy(
                    isGameOver = true,
                    winnerName = opponentName,
                    statusMessage = "Game Over! $opponentName won after $calls calls."
                )
            )
        }
    }

    fun exitLiveGame() {
        liveGameJob?.cancel()
        _uiState.update {
            it.copy(
                currentScreen = Screen.Home,
                liveGame = LiveGameState()
            )
        }
    }

    // --- Payments & Wallet (Telebirr, CBE, CBE Birr, E-Birr) ---
    fun submitDeposit(amountBirr: Int, receiptLinkOrNum: String, paymentMethod: com.example.model.PaymentMethod = com.example.model.PaymentMethod.TELEBIRR) {
        val user = _uiState.value.currentUser ?: return
        if (amountBirr < 30) {
            showToast("Minimum deposit is 30 Birr (1 cartela)")
            return
        }
        val cleanReceipt = receiptLinkOrNum.trim().substringAfterLast("/")
        val request = DepositRequest(
            id = UUID.randomUUID().toString().take(8),
            username = user.username,
            amount = amountBirr,
            paymentMethod = paymentMethod,
            receiptId = cleanReceipt,
            fullReceipt = receiptLinkOrNum.trim(),
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )
        repository.addDeposit(request)
        viewModelScope.launch {
            try {
                firestoreRepo.addDeposit(request)
            } catch (e: Exception) {
                android.util.Log.w("BingoViewModel", "Firestore addDeposit error: ${e.message}")
            }
        }
        _uiState.update { it.copy(deposits = repository.getDeposits()) }
        showToast("Deposit submitted via ${paymentMethod.displayName}! Admin will verify your receipt.")
    }

    fun requestWithdrawal(amountBirr: Int, targetAccount: String, paymentMethod: com.example.model.PaymentMethod = com.example.model.PaymentMethod.TELEBIRR) {
        val user = _uiState.value.currentUser ?: return
        if (amountBirr < 30) {
            showToast("Minimum withdrawal is 30 Birr")
            return
        }
        if (targetAccount.length < 8) {
            showToast("Enter a valid ${paymentMethod.displayName} account / phone number")
            return
        }
        if (user.wallet < amountBirr) {
            showToast("Insufficient wallet balance")
            return
        }

        // Deduct immediately
        val updatedUser = user.copy(wallet = user.wallet - amountBirr)
        repository.saveUser(updatedUser)

        val request = WithdrawalRequest(
            id = UUID.randomUUID().toString().take(8),
            username = user.username,
            amount = amountBirr,
            paymentMethod = paymentMethod,
            targetAccount = targetAccount,
            timestamp = System.currentTimeMillis(),
            status = "pending"
        )
        repository.addWithdrawal(request)
        viewModelScope.launch {
            try {
                firestoreRepo.addWithdrawal(request)
            } catch (e: Exception) {
                android.util.Log.w("BingoViewModel", "Firestore addWithdrawal error: ${e.message}")
            }
        }

        _uiState.update {
            it.copy(
                currentUser = updatedUser,
                withdrawals = repository.getWithdrawals()
            )
        }
        showToast("Withdrawal requested for $amountBirr Birr to $targetAccount via ${paymentMethod.displayName}.")
    }

    // --- Admin Operations ---
    fun adminLogin(pass: String): Boolean {
        // Admin credentials
        if (pass == "admin" || pass == "admin123" || pass == "123456") {
            _uiState.update { it.copy(isAdminLoggedIn = true, currentScreen = Screen.Admin) }
            showToast("Admin access granted")
            return true
        }
        showToast("Incorrect admin password")
        return false
    }

    fun adminLogout() {
        _uiState.update { it.copy(isAdminLoggedIn = false, currentScreen = Screen.Home) }
    }

    fun approveDeposit(depositId: String, amount: Int? = null) {
        repository.updateDepositStatus(depositId, "approved", amount)
        _uiState.update {
            it.copy(
                deposits = repository.getDeposits(),
                allUsers = repository.getAllUsers().values.toList(),
                currentUser = it.currentUser?.let { u -> repository.getUserProfile(u.username) }
            )
        }
        showToast("Deposit approved & credited.")
    }

    fun declineDeposit(depositId: String) {
        repository.updateDepositStatus(depositId, "declined")
        _uiState.update { it.copy(deposits = repository.getDeposits()) }
        showToast("Deposit declined.")
    }

    fun approveWithdrawal(withdrawalId: String) {
        repository.updateWithdrawalStatus(withdrawalId, "approved")
        _uiState.update { it.copy(withdrawals = repository.getWithdrawals()) }
        showToast("Withdrawal marked as Paid.")
    }

    fun declineWithdrawal(withdrawalId: String) {
        repository.updateWithdrawalStatus(withdrawalId, "declined")
        _uiState.update {
            it.copy(
                withdrawals = repository.getWithdrawals(),
                allUsers = repository.getAllUsers().values.toList(),
                currentUser = it.currentUser?.let { u -> repository.getUserProfile(u.username) }
            )
        }
        showToast("Withdrawal declined & refunded to user.")
    }

    fun adminAdjustUserWallet(username: String, deltaBirr: Int) {
        val user = repository.getUserProfile(username) ?: return
        val updated = user.copy(wallet = (user.wallet + deltaBirr).coerceAtLeast(0))
        repository.saveUser(updated)
        _uiState.update {
            it.copy(
                allUsers = repository.getAllUsers().values.toList(),
                currentUser = if (it.currentUser?.username == username) updated else it.currentUser
            )
        }
        showToast("Updated $username's wallet.")
    }

    fun showToast(msg: String) {
        _uiState.update { it.copy(toastMessage = msg) }
    }

    fun dismissToast() {
        _uiState.update { it.copy(toastMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        liveGameJob?.cancel()
        soundManager.destroy()
    }
}
