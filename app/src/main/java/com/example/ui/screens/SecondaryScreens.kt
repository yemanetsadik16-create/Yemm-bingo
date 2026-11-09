package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BingoEngine
import com.example.ui.BingoUiState
import com.example.ui.BingoViewModel
import com.example.ui.Screen
import com.example.ui.theme.BingoBorderGold
import com.example.ui.theme.BingoBorderPurple
import com.example.ui.theme.BingoCardSurface
import com.example.ui.theme.BingoCardSurfaceVariant
import com.example.ui.theme.BingoGoldDark
import com.example.ui.theme.BingoGoldPrimary
import com.example.ui.theme.BingoNeonPurple
import com.example.ui.theme.BingoTextLight
import com.example.ui.theme.BingoTextMuted
import com.example.ui.theme.BingoVibrantGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ScheduleScreen(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "BINGO Game Schedule",
                color = BingoGoldPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(BingoEngine.SLOTS) { slot ->
            val isNext = slot.labelEn == uiState.nextSlotLabel
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNext) BingoCardSurfaceVariant else BingoCardSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isNext) 1.5.dp else 1.dp,
                    color = if (isNext) BingoGoldPrimary else BingoBorderPurple
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BINGO Game at ${slot.labelEn}",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${slot.labelAm} (${slot.hour24}:00)",
                            color = BingoTextMuted,
                            fontSize = 13.sp
                        )
                    }

                    if (isNext) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(BingoGoldPrimary)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NEXT",
                                color = Color(0xFF2A0F55),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HowToPlayScreen() {
    val steps = listOf(
        "1. Register and choose your preferred playing time slot (e.g. 8:00 O'clock Day), or tap 'Play For Free' to practice immediately.",
        "2. Pay 30 Birr per cartela via Telebirr to Yemane (0939383425). Each cartela covers 3 consecutive games played one after another in your time slot (10 Birr per game).",
        "3. Tap one or more card numbers (1 - 500). The cards you pick are registered to your session. Columns are B (1-15), I (16-30), N (31-45), G (46-60), and O (61-75). The center cell is FREE ★.",
        "4. Games start automatically when the countdown ends. All multiplayer participants receive the exact same called numbers simultaneously every 5 seconds with Ethiopian voice callouts.",
        "5. Tap called numbers to mark your cards, or toggle Auto-mark on. When your pattern fulfills the Target Goal, press BINGO! to win the jackpot!"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "How to Play YEME BINGO",
                color = BingoGoldPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        items(steps) { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = step,
                    color = BingoTextLight,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "About YEME BINGO",
                        color = BingoGoldPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "YEME BINGO is a premier multiplayer Ethiopian bingo platform featuring live scheduled rounds, authentic Amharic voice callouts, real-time Telebirr wallet deposits and withdrawals, and fair synchronized gameplay.",
                        color = BingoTextLight,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Official Payment Methods:\n" +
                                "• Telebirr: 0939383425 (Holder: Yemane)\n" +
                                "• CBE (Commercial Bank of Ethiopia): 0939383425 (Holder: Yemane)\n" +
                                "• CBE Birr (*847#): 0939383425 (Holder: Yemane)\n" +
                                "• E-Birr (*841#): 0939383425 (Holder: Yemane)\n\n" +
                                "Pricing Rule: 30 Birr = 1 cartela (valid for 3 consecutive rounds in a session).",
                        color = BingoGoldPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdminScreen(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    var adminPass by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) }
    var userSearchQuery by remember { mutableStateOf("") }

    if (!uiState.isAdminLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Admin Portal",
                        color = BingoGoldPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = adminPass,
                        onValueChange = { adminPass = it },
                        label = { Text("Enter Admin Password (admin)") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.adminLogin(adminPass) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BingoNeonPurple)
                    ) {
                        Text("Log In as Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val tabTitles = listOf("Payments", "Users", "Withdrawals", "Winners")
    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = BingoCardSurface,
            contentColor = BingoGoldPrimary,
            indicator = { tabPositions ->
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp)
                        .background(BingoGoldPrimary)
                )
            }
        ) {
            tabTitles.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == idx) BingoGoldPrimary else BingoTextMuted,
                            fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        when (selectedTab) {
            0 -> {
                // Payments
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    item {
                        Text(
                            text = "Pending Deposits (${uiState.deposits.count { it.status == "pending" }})",
                            color = BingoGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.deposits) { dep ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${dep.username} · ${dep.amount} Birr", color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = "Method: ${dep.paymentMethod.displayName}",
                                            color = BingoGoldPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = dep.status.uppercase(),
                                        color = if (dep.status == "approved") BingoVibrantGreen else if (dep.status == "pending") BingoGoldPrimary else Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                Text("Receipt / Ref: ${dep.receiptId}", color = BingoTextMuted, fontSize = 12.sp)

                                if (dep.status == "pending") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.approveDeposit(dep.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BingoVibrantGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Approve", color = Color.Black, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.declineDeposit(dep.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Decline", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // Users
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = userSearchQuery,
                            onValueChange = { userSearchQuery = it },
                            placeholder = { Text("Search username or phone...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    val filtered = uiState.allUsers.filter {
                        it.username.contains(userSearchQuery, ignoreCase = true) ||
                                it.phone.contains(userSearchQuery)
                    }

                    items(filtered) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${user.username} (${user.phone})", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Time: ${user.playingTime} | Wallet: ${user.wallet} Birr", color = BingoTextMuted, fontSize = 13.sp)

                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.adminAdjustUserWallet(user.username, 30) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BingoGoldPrimary)
                                    ) {
                                        Text("+30 Birr", color = Color.Black, fontSize = 11.sp)
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.adminAdjustUserWallet(user.username, -30) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("-30 Birr", color = Color.White, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            2 -> {
                // Withdrawals
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    items(uiState.withdrawals) { wd ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("${wd.username} · ${wd.amount} Birr via ${wd.paymentMethod.displayName}", color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Target Account: ${wd.targetAccount}", color = BingoGoldPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("Status: ${wd.status.uppercase()}", color = BingoTextMuted, fontSize = 12.sp)

                                if (wd.status == "pending") {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.approveWithdrawal(wd.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = BingoVibrantGreen),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Mark Paid", color = Color.Black, fontSize = 12.sp)
                                        }

                                        OutlinedButton(
                                            onClick = { viewModel.declineWithdrawal(wd.id) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Decline & Refund", color = Color.Red, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            3 -> {
                // Winners
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    item {
                        Text(
                            text = "Recent Winners (Last 24 Hours)",
                            color = BingoGoldPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(uiState.recentWinners) { win ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("🏆 ${win.username} (${win.phone})", color = BingoGoldPrimary, fontWeight = FontWeight.Bold)
                                Text("Reward: ${win.rewardBirr} Birr | Card #${win.cardNumber} (${win.callsCount} calls)", color = Color.White, fontSize = 13.sp)
                                Text("Time: ${win.gameTime} - Round ${win.roundIndex}", color = BingoTextMuted, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
