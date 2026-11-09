package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BingoEngine
import com.example.model.AppLanguage
import com.example.ui.BingoUiState
import com.example.ui.BingoViewModel
import com.example.ui.Screen
import com.example.ui.components.BingoCardView
import com.example.ui.components.CalledNumbersBoard
import com.example.ui.components.EthiopianBanner
import com.example.ui.theme.BingoBorderGold
import com.example.ui.theme.BingoBorderPurple
import com.example.ui.theme.BingoCardSurface
import com.example.ui.theme.BingoCardSurfaceVariant
import com.example.ui.theme.BingoDarkBg
import com.example.ui.theme.BingoGoldDark
import com.example.ui.theme.BingoGoldPrimary
import com.example.ui.theme.BingoNeonPurple
import com.example.ui.theme.BingoTextLight
import com.example.ui.theme.BingoTextMuted
import com.example.ui.theme.BingoVibrantGreen

@Composable
fun HomeScreen(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            EthiopianBanner(
                text = "ቢንጎ ጨዋታ የ ${uiState.nextSlotLabel.split(" ").firstOrNull() ?: "8:00"} ሰአት ከካውንትዳውን በኋላ ይጀምራል ።"
            )
        }

        item {
            com.example.ui.components.CountdownDisplay(
                countdownText = uiState.countdownText,
                slotLabel = uiState.nextSlotLabel
            )
        }

        // Welcome / Login Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("login_card"),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.currentUser != null) {
                        Text(
                            text = "Welcome, ${uiState.currentUser.username}!",
                            color = BingoGoldPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Playing Time: ${uiState.currentUser.playingTime}",
                            color = BingoTextMuted,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Wallet: ${uiState.currentUser.wallet} Birr (${uiState.currentUser.wallet / 30} Cartelas)",
                            color = BingoVibrantGreen,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.startOrJoinGame(isPractice = false) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("btn_play_now"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BingoNeonPurple
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("▶ Start Real Game (${uiState.selectedCardNumbers.size} Cartelas)", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Auth) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_go_account"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BingoGoldPrimary)
                        ) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = BingoGoldPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("👤 My Account & Sign Out", color = BingoGoldPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = "⇥ Login",
                            color = BingoGoldPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_username"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BingoGoldPrimary,
                                unfocusedBorderColor = BingoBorderPurple,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_password"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BingoGoldPrimary,
                                unfocusedBorderColor = BingoBorderPurple,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = { viewModel.login(username, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_login_submit"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BingoNeonPurple
                            )
                        ) {
                            Text("⇥ Login", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Register) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_go_register"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BingoGoldPrimary)
                        ) {
                            Text("✚ New Player? Register Here", color = BingoGoldPrimary, fontSize = 15.sp)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { viewModel.navigateTo(Screen.Auth) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_go_auth_page"),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("🔑 Full Sign In / Sign Out Screen", color = Color.White, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { viewModel.startOrJoinGame(isPractice = true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_play_free"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BingoGoldPrimary
                )
            ) {
                Text("▶ Play For Free (Practice Mode)", color = Color(0xFF2A0F55), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: BingoViewModel
) {
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedSlot by remember { mutableStateOf(BingoEngine.SLOTS[2].labelEn) } // default 8:00
    var pass by remember { mutableStateOf("") }
    var confirmPass by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .testTag("register_card"),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp)
                ) {
                    Text(
                        text = "User Registration",
                        color = BingoGoldPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_username"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BingoGoldPrimary,
                            unfocusedBorderColor = BingoBorderPurple,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone No (Telebirr)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_phone"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BingoGoldPrimary,
                            unfocusedBorderColor = BingoBorderPurple,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Playing Time Slot (Ethiopian UTC+3):",
                        color = BingoTextMuted,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    BingoEngine.SLOTS.forEach { slot ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedSlot == slot.labelEn) BingoCardSurfaceVariant else Color.Transparent)
                                .clickable { selectedSlot = slot.labelEn }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "BINGO Game at ${slot.labelEn}",
                                color = if (selectedSlot == slot.labelEn) BingoGoldPrimary else BingoTextLight,
                                fontSize = 14.sp,
                                fontWeight = if (selectedSlot == slot.labelEn) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = pass,
                        onValueChange = { pass = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_pass"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BingoGoldPrimary,
                            unfocusedBorderColor = BingoBorderPurple,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("Confirm Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("reg_confirm_pass"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BingoGoldPrimary,
                            unfocusedBorderColor = BingoBorderPurple,
                            focusedTextColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pass != confirmPass) {
                                viewModel.showToast("Passwords do not match")
                            } else {
                                viewModel.register(username, phone, selectedSlot)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_register_submit"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BingoNeonPurple)
                    ) {
                        Text("✚ Complete Registration", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Already have an account? Login",
                        color = BingoGoldPrimary,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .clickable { viewModel.navigateTo(Screen.Home) }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuthScreen(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    var depositAmount by remember { mutableStateOf("60") }
    var depositReceipt by remember { mutableStateOf("") }
    var showDepositDialog by remember { mutableStateOf(false) }

    var withdrawAmount by remember { mutableStateOf("60") }
    var withdrawPhone by remember { mutableStateOf(uiState.currentUser?.phone ?: "") }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    var showSignOutDialog by remember { mutableStateOf(false) }
    var showTimeSlotDialog by remember { mutableStateOf(false) }

    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    var isRegisterMode by remember { mutableStateOf(false) }
    var regUsername by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regSlot by remember { mutableStateOf(BingoEngine.SLOTS[2].labelEn) } // default 8:00
    var regPass by remember { mutableStateOf("") }
    var regConfirmPass by remember { mutableStateOf("") }
    var filterCardInput by remember { mutableStateOf("") }

    // Sign Out Confirmation Dialog
    if (showSignOutDialog && uiState.currentUser != null) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = {
                Text(
                    text = "Sign Out Confirmation",
                    color = BingoGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of ${uiState.currentUser.username}? Your wallet balance (${uiState.currentUser.wallet} Birr) and cartelas are saved securely.",
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Yes, Sign Out", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showSignOutDialog = false },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = BingoCardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Time Slot Change Dialog
    if (showTimeSlotDialog && uiState.currentUser != null) {
        AlertDialog(
            onDismissRequest = { showTimeSlotDialog = false },
            title = {
                Text(
                    text = "Change Playing Time Slot",
                    color = BingoGoldPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select your preferred Ethiopian scheduled game time (UTC+3):",
                        color = BingoTextMuted,
                        fontSize = 13.sp
                    )
                    BingoEngine.SLOTS.forEach { slot ->
                        val isCurrent = uiState.currentUser.playingTime == slot.labelEn
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) BingoGoldPrimary else BingoCardSurfaceVariant)
                                .clickable {
                                    viewModel.updatePlayingTime(slot.labelEn)
                                    showTimeSlotDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "BINGO at ${slot.labelEn}",
                                color = if (isCurrent) Color(0xFF2A0F55) else Color.White,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                            if (isCurrent) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2A0F55))
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(
                    onClick = { showTimeSlotDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Close", color = Color.White)
                }
            },
            containerColor = BingoCardSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            com.example.ui.components.CountdownDisplay(
                countdownText = uiState.countdownText,
                slotLabel = uiState.currentUser?.playingTime ?: uiState.nextSlotLabel
            )
        }

        // --- SIGNED OUT STATE ---
        if (uiState.currentUser == null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp))
                        .testTag("auth_screen_card"),
                    colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Brand Icon & Title
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        listOf(BingoGoldPrimary, BingoGoldDark)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isRegisterMode) Icons.Default.Person else Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF2A0F55),
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isRegisterMode) "Player Registration" else "Player Sign In",
                            color = BingoGoldPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isRegisterMode) "Create an account to join Ethiopian Bingo multiplayer" else "Sign in to access cartelas, wallet balance & payouts",
                            color = BingoTextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        val context = androidx.compose.ui.platform.LocalContext.current

                        // Interactive Google Sign-In with Credential Manager
                        Button(
                            onClick = { viewModel.signInWithGoogle(context) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_google_signin_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                            enabled = !uiState.isAuthLoading
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4285F4)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("G", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                                Text(
                                    text = if (uiState.isAuthLoading) "Connecting to Google..." else "Sign in with Google",
                                    color = Color(0xFF1F1F1F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BingoBorderPurple.copy(alpha = 0.5f))
                            Text(
                                text = "  OR USERNAME  ",
                                color = BingoTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = BingoBorderPurple.copy(alpha = 0.5f))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Tab Switcher
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BingoCardSurfaceVariant)
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (!isRegisterMode) BingoGoldPrimary else Color.Transparent)
                                    .clickable { isRegisterMode = false }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔑 Sign In",
                                    color = if (!isRegisterMode) Color(0xFF2A0F55) else BingoTextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isRegisterMode) BingoGoldPrimary else Color.Transparent)
                                    .clickable { isRegisterMode = true }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "✚ Register",
                                    color = if (isRegisterMode) Color(0xFF2A0F55) else BingoTextLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        if (!isRegisterMode) {
                            // SIGN IN FORM
                            OutlinedTextField(
                                value = loginUsername,
                                onValueChange = { loginUsername = it },
                                label = { Text("Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BingoGoldPrimary)
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signin_username"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BingoGoldPrimary,
                                    unfocusedBorderColor = BingoBorderPurple,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = BingoGoldPrimary)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                                            tint = BingoTextMuted
                                        )
                                    }
                                },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_signin_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BingoGoldPrimary,
                                    unfocusedBorderColor = BingoBorderPurple,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Remember this account", color = BingoTextLight, fontSize = 13.sp)
                                Switch(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = BingoGoldPrimary,
                                        checkedTrackColor = BingoNeonPurple
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (loginUsername.isBlank()) {
                                        viewModel.showToast("Please enter your username")
                                    } else {
                                        viewModel.login(loginUsername.trim(), loginPassword)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("auth_signin_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BingoNeonPurple)
                            ) {
                                Text("⇥ Sign In", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            }


                        } else {
                            // QUICK REGISTER FORM
                            OutlinedTextField(
                                value = regUsername,
                                onValueChange = { regUsername = it },
                                label = { Text("Choose Username") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = BingoGoldPrimary)
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_reg_username"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPhone,
                                onValueChange = { regPhone = it },
                                label = { Text("Phone Number (Telebirr/CBE)") },
                                placeholder = { Text("09XXXXXXXX") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_reg_phone"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Playing Time Slot (Ethiopian UTC+3):",
                                color = BingoTextMuted,
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Start)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            BingoEngine.SLOTS.forEach { slot ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (regSlot == slot.labelEn) BingoCardSurfaceVariant else Color.Transparent)
                                        .clickable { regSlot = slot.labelEn }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "BINGO Game at ${slot.labelEn}",
                                        color = if (regSlot == slot.labelEn) BingoGoldPrimary else BingoTextLight,
                                        fontSize = 13.sp,
                                        fontWeight = if (regSlot == slot.labelEn) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regPass,
                                onValueChange = { regPass = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_reg_pass"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = regConfirmPass,
                                onValueChange = { regConfirmPass = it },
                                label = { Text("Confirm Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_reg_confirm_pass"),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (regPass != regConfirmPass) {
                                        viewModel.showToast("Passwords do not match")
                                    } else {
                                        viewModel.register(regUsername, regPhone, regSlot)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("auth_register_button"),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BingoNeonPurple)
                            ) {
                                Text("✚ Complete Registration (150 Birr Bonus)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // --- SIGNED IN STATE ---
            val user = uiState.currentUser

            // 1. User Profile & Active Session Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(16.dp, RoundedCornerShape(22.dp))
                        .testTag("account_profile_card"),
                    colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, BingoBorderGold),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar circle with Initial
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            listOf(BingoGoldPrimary, BingoGoldDark)
                                        )
                                    )
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.username.take(1).uppercase(),
                                    color = Color(0xFF2A0F55),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.username,
                                        color = Color.White,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(BingoVibrantGreen.copy(alpha = 0.2f))
                                            .border(1.dp, BingoVibrantGreen, RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "ONLINE",
                                            color = BingoVibrantGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Phone: ${user.phone}",
                                    color = BingoTextLight,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "Linked: Telebirr · CBE · CBE Birr · E-Birr",
                                    color = BingoTextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "☁ Cloud Firestore: Connected (Real-Time Sync)",
                                    color = BingoGoldPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BingoBorderGold.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Playing Time Slot
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "Scheduled Playing Time:", color = BingoTextMuted, fontSize = 12.sp)
                                Text(
                                    text = user.playingTime,
                                    color = BingoGoldPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = { showTimeSlotDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold)
                            ) {
                                Text("Change Slot", color = BingoGoldPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // 2. Wallet & Financials Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .shadow(12.dp, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Account Wallet Balance", color = BingoTextMuted, fontSize = 13.sp)
                        Text(
                            text = "${user.wallet} Birr",
                            color = BingoGoldPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "${user.wallet / 30} Cartelas Available (30 Birr = 1 cartela / 3 games)",
                            color = BingoTextLight,
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showDepositDialog = !showDepositDialog },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary)
                            ) {
                                Text("Deposit", color = Color(0xFF2A0F55), fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showWithdrawDialog = !showWithdrawDialog },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                            ) {
                                Text("Withdraw", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Deposit Form (All 4 Ethiopian Methods)
            if (showDepositDialog) {
                item {
                    var selectedDepositMethod by remember { mutableStateOf(com.example.model.PaymentMethod.TELEBIRR) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Deposit Money (30 Birr = 1 Cartela)",
                                color = BingoGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(text = "Select Payment Method:", color = BingoTextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                com.example.model.PaymentMethod.entries.forEach { method ->
                                    val isSelected = selectedDepositMethod == method
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BingoGoldPrimary else BingoCardSurface)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) Color.White else BingoBorderPurple,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedDepositMethod = method }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = method.displayName.split(" ").first(),
                                            color = if (isSelected) Color(0xFF2A0F55) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BingoDarkBg),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Send via ${selectedDepositMethod.displayName}:",
                                        color = BingoGoldPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Account Holder: ${selectedDepositMethod.accountHolder}\nAccount Number: ${selectedDepositMethod.accountNumber}",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = selectedDepositMethod.description,
                                        color = BingoTextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = depositAmount,
                                onValueChange = { depositAmount = it },
                                label = { Text("Amount Paid (Birr, min 30)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = depositReceipt,
                                onValueChange = { depositReceipt = it },
                                label = { Text("Receipt ID, SMS Reference or Link") },
                                placeholder = { Text(selectedDepositMethod.hintPlaceholder) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val amt = depositAmount.toIntOrNull() ?: 30
                                    viewModel.submitDeposit(amt, depositReceipt, selectedDepositMethod)
                                    showDepositDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BingoNeonPurple)
                            ) {
                                Text("Submit ${selectedDepositMethod.displayName} Payment", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Withdrawal Form (All 4 Ethiopian Methods)
            if (showWithdrawDialog) {
                item {
                    var selectedWithdrawMethod by remember { mutableStateOf(com.example.model.PaymentMethod.TELEBIRR) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Withdraw Funds",
                                color = BingoGoldPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(text = "Receive via:", color = BingoTextMuted, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                com.example.model.PaymentMethod.entries.forEach { method ->
                                    val isSelected = selectedWithdrawMethod == method
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BingoGoldPrimary else BingoCardSurface)
                                            .border(
                                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                                color = if (isSelected) Color.White else BingoBorderPurple,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable { selectedWithdrawMethod = method }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = method.displayName.split(" ").first(),
                                            color = if (isSelected) Color(0xFF2A0F55) else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = withdrawAmount,
                                onValueChange = { withdrawAmount = it },
                                label = { Text("Amount (Birr) min 30") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = withdrawPhone,
                                onValueChange = { withdrawPhone = it },
                                label = { Text("${selectedWithdrawMethod.displayName} Account / Phone") },
                                placeholder = { Text("Your ${selectedWithdrawMethod.displayName} number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    val amt = withdrawAmount.toIntOrNull() ?: 30
                                    viewModel.requestWithdrawal(amt, withdrawPhone, selectedWithdrawMethod)
                                    showWithdrawDialog = false
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BingoVibrantGreen)
                            ) {
                                Text("Request Payout via ${selectedWithdrawMethod.displayName}", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 3. Cartela Selection & Management
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Select Cartela (1 - 500)",
                            color = BingoGoldPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.selectedCardNumbers.size} Selected (${uiState.selectedCardNumbers.size * 30} Birr)",
                            color = BingoTextMuted,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = { viewModel.refreshCardSeed() },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BingoCardSurfaceVariant)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Shuffle", tint = BingoGoldPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shuffle", color = BingoGoldPrimary, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = filterCardInput,
                    onValueChange = {
                        filterCardInput = it
                        val num = it.toIntOrNull()
                        if (num != null && num in 1..500) {
                            viewModel.selectPreviewCard(num)
                        }
                    },
                    label = { Text("Jump to Card # (1 - 500)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BingoGoldPrimary,
                        unfocusedBorderColor = BingoBorderPurple,
                        focusedTextColor = Color.White
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Cartela Badges
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (cardNum in 1..40) {
                            val isSelected = uiState.selectedCardNumbers.contains(cardNum)
                            val isOccupied = uiState.occupiedCards.contains(cardNum)

                            val badgeBg = when {
                                isSelected -> BingoGoldPrimary
                                isOccupied -> Color(0xFF6B1D28)
                                else -> BingoCardSurfaceVariant
                            }

                            val badgeTxt = when {
                                isSelected -> Color(0xFF2A0F55)
                                isOccupied -> Color(0xFFFF9AA8)
                                else -> BingoTextLight
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(badgeBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) Color.White else BingoBorderPurple,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        if (!isOccupied) {
                                            viewModel.toggleCardSelection(cardNum)
                                        } else {
                                            viewModel.showToast("Card #$cardNum is occupied at your playing time!")
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$cardNum",
                                    color = badgeTxt,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Preview of selected card
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Preview · Cartela #${uiState.previewCardNumber}",
                    color = BingoGoldPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                BingoCardView(
                    card = uiState.previewCard,
                    markedCells = setOf(Pair(2, 2)),
                    isInteractive = false
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = { viewModel.startOrJoinGame(isPractice = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_join_game"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary)
                ) {
                    Text(
                        text = "▶ Start Real Game (${uiState.selectedCardNumbers.size} Cartelas)",
                        color = Color(0xFF2A0F55),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // 4. CALLER VOICE SETTINGS
            item {
                CallerVoiceSettingsCard(uiState = uiState, viewModel = viewModel)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // 5. DEDICATED FULLY FUNCTIONAL SIGN OUT SECTION
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .border(1.2.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF220D1A)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color(0xFFEF4444))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account Session & Sign Out",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "You are logged in as ${user.username}. Signing out ends your active session on this device. All your balances (${user.wallet} Birr) and cartelas remain safe.",
                            color = BingoTextLight,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showSignOutDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_sign_out"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("⇤ Sign Out (${user.username})", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.logout()
                                isRegisterMode = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_switch_account"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = BingoGoldPrimary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("⇄ Switch to Another Account", color = BingoGoldPrimary, fontSize = 14.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun CallerVoiceSettingsCard(
    uiState: BingoUiState,
    viewModel: BingoViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.4f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎙️ Male Caller Voice (የአንባቢ ድምፅ)",
                        color = BingoGoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Authentic Ethiopian Amharic Male Caller",
                        color = BingoTextMuted,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.testCallerVoice() },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("▶ Test Voice", color = Color(0xFF2A0F55), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Speaking Speed (የአነባበብ ፍጥነት):", color = BingoTextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    1.15f to "Fast & Crisp\n(1.15x)",
                    1.28f to "Ultra Fast\n(1.28x)",
                    1.00f to "Standard\n(1.00x)"
                ).forEach { (spd, label) ->
                    val isSel = kotlin.math.abs(uiState.callerSpeed - spd) < 0.03f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) BingoGoldPrimary else BingoCardSurface)
                            .border(
                                width = if (isSel) 1.5.dp else 0.5.dp,
                                color = if (isSel) Color.White else BingoBorderPurple,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setCallerSpeed(spd) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color(0xFF2A0F55) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Voice Pitch (የድምፅ ቃና):", color = BingoTextLight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0.98f to "Crisp Male\n(ጥርት ያለ)",
                    0.90f to "Deep Male\n(ጥልቅ)",
                    1.05f to "Energetic\n(ቀልጣፋ)"
                ).forEach { (ptc, label) ->
                    val isSel = kotlin.math.abs(uiState.callerPitch - ptc) < 0.03f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) BingoGoldPrimary else BingoCardSurface)
                            .border(
                                width = if (isSel) 1.5.dp else 0.5.dp,
                                color = if (isSel) Color.White else BingoBorderPurple,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.setCallerPitch(ptc) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color(0xFF2A0F55) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LiveGameScreen(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    val game = uiState.liveGame

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Target Goal Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .shadow(8.dp, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Target Goal:",
                            color = BingoTextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${game.goal.nameEn} (${game.goal.nameAm})",
                            color = BingoGoldPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Reward Pool:",
                            color = BingoTextMuted,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "${game.prizePoolBirr} Birr",
                            color = BingoVibrantGreen,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Countdown "Ready" overlay if counting down
        if (game.currentReadyCount != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = BingoNeonPurple),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = game.currentReadyCount,
                        color = Color.White,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    )
                }
            }
        }

        // Called Numbers Master Board
        item {
            CalledNumbersBoard(
                calledNumbers = game.calledNumbers,
                lastCalled = game.lastCalledNumber
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Player's Bingo Cards (Supports multiple cards)
        items(game.playerCards.indices.toList()) { cardIdx ->
            val card = game.playerCards[cardIdx]
            val marked = game.cardMarks[cardIdx] ?: emptySet()
            val isWinningCard = (game.winningCardIndex == cardIdx)

            BingoCardView(
                card = card,
                markedCells = marked,
                winningCells = if (isWinningCard) game.winningCells else null,
                onCellClick = { col, row ->
                    viewModel.onCellClick(cardIdx, col, row)
                },
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        // Action Bingo! Button
        item {
            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { viewModel.claimBingo() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .shadow(12.dp, RoundedCornerShape(32.dp))
                    .testTag("btn_claim_bingo"),
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary)
            ) {
                Text(
                    text = "BINGO!",
                    color = Color(0xFF2A0F55),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Game controls (Auto-mark, sound, exit)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = uiState.autoMarkEnabled,
                        onCheckedChange = { viewModel.toggleAutoMark() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BingoGoldPrimary,
                            checkedTrackColor = BingoNeonPurple
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Auto-mark", color = BingoTextLight, fontSize = 14.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.toggleSound() },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                ) {
                    Text(
                        text = if (uiState.soundEnabled) "🔊 Sound ON" else "🔇 Mute",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.exitLiveGame() },
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderPurple)
                ) {
                    Text("‹ Back", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            CallerVoiceSettingsCard(uiState = uiState, viewModel = viewModel)

            // Game Status Overlay (Winner)
            if (game.isGameOver) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(2.dp, BingoGoldPrimary),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (game.winnerName == (uiState.currentUser?.username ?: "Guest")) "🏆 YOU WON!" else "GAME OVER",
                            color = BingoGoldPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = game.statusMessage ?: "Game finished.",
                            color = Color.White,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { viewModel.exitLiveGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary)
                        ) {
                            Text("Back to Home", color = Color(0xFF2A0F55), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
