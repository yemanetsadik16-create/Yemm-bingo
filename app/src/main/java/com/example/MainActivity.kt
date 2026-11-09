package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppLanguage
import com.example.ui.BingoUiState
import com.example.ui.BingoViewModel
import com.example.ui.Screen
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LiveGameScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.BingoBorderGold
import com.example.ui.theme.BingoCardSurface
import com.example.ui.theme.BingoCardSurfaceVariant
import com.example.ui.theme.BingoDarkBg
import com.example.ui.theme.BingoGoldDark
import com.example.ui.theme.BingoGoldPrimary
import com.example.ui.theme.BingoNeonPurple
import com.example.ui.theme.BingoTextLight
import com.example.ui.theme.BingoTextMuted
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: BingoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsState()
                BingoApp(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BingoApp(
    uiState: BingoUiState,
    viewModel: BingoViewModel
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    // Handle Android system back button
    BackHandler(enabled = uiState.currentScreen != Screen.Home) {
        when (uiState.currentScreen) {
            Screen.LiveGame -> viewModel.exitLiveGame()
            else -> viewModel.navigateTo(Screen.Home)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = BingoCardSurface,
                drawerContentColor = BingoTextLight,
                modifier = Modifier
                    .width(300.dp)
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Brand Logo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BingoDarkBg)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "YEME BINGO",
                            color = BingoGoldPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (uiState.currentUser != null) "◉ ${uiState.currentUser.username}" else "Ethiopia & Global Multiplayer",
                            color = BingoTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                HorizontalDivider(color = BingoBorderGold.copy(alpha = 0.3f))

                // Language Selector Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        AppLanguage.EN to "English",
                        AppLanguage.AM to "አማርኛ",
                        AppLanguage.TI to "ትግርኛ"
                    ).forEach { (lang, label) ->
                        val isSel = uiState.language == lang
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) BingoGoldPrimary else BingoCardSurfaceVariant)
                                .clickable { viewModel.setLanguage(lang) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) Color(0xFF2A0F55) else BingoTextLight,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                HorizontalDivider(color = BingoBorderGold.copy(alpha = 0.2f))

                val authItemTitle = if (uiState.currentUser != null) "👤 Account & Sign Out" else "🔑 Sign In / Sign Out"
                val navItems = listOf(
                    "⌂ Home" to Screen.Home,
                    authItemTitle to Screen.Auth,
                    "☰ Schedule" to Screen.Schedule,
                    "ⓘ How to Play" to Screen.HowToPlay,
                    "● About Us" to Screen.About,
                    "⚙ Admin Portal" to Screen.Admin
                )

                navItems.forEach { (title, screen) ->
                    NavigationDrawerItem(
                        label = { Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp) },
                        selected = uiState.currentScreen == screen,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.navigateTo(screen)
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = BingoCardSurfaceVariant,
                            selectedTextColor = BingoGoldPrimary,
                            unselectedTextColor = BingoTextLight
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (uiState.currentUser != null) {
                    NavigationDrawerItem(
                        label = { Text("⇤ Sign Out (${uiState.currentUser.username})", color = Color.Red) },
                        selected = false,
                        onClick = {
                            scope.launch { drawerState.close() }
                            viewModel.logout()
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBarView(
                    language = uiState.language,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLanguageClick = {
                        val next = when (uiState.language) {
                            AppLanguage.EN -> AppLanguage.AM
                            AppLanguage.AM -> AppLanguage.TI
                            AppLanguage.TI -> AppLanguage.EN
                        }
                        viewModel.setLanguage(next)
                    },
                    onHomeClick = { viewModel.navigateTo(Screen.Home) }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = BingoDarkBg,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (uiState.currentScreen) {
                    Screen.Home -> HomeScreen(uiState = uiState, viewModel = viewModel)
                    Screen.Register -> RegisterScreen(viewModel = viewModel)
                    Screen.Auth -> AuthScreen(uiState = uiState, viewModel = viewModel)
                    Screen.LiveGame -> LiveGameScreen(uiState = uiState, viewModel = viewModel)
                    Screen.Schedule -> ScheduleScreen(uiState = uiState, viewModel = viewModel)
                    Screen.HowToPlay -> HowToPlayScreen()
                    Screen.About -> AboutScreen()
                    Screen.Admin -> AdminScreen(uiState = uiState, viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun TopAppBarView(
    language: AppLanguage,
    onMenuClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onHomeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF2A0F55),
                        Color(0xFF6D28D9),
                        Color(0xFF2A0F55)
                    )
                )
            )
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.clickable { onHomeClick() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "▦ YEME BINGO",
                color = BingoGoldPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, BingoBorderGold.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    .clickable { onLanguageClick() }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.EN -> "EN"
                        AppLanguage.AM -> "አማ"
                        AppLanguage.TI -> "ትግ"
                    },
                    color = BingoGoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.testTag("menu_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = BingoGoldPrimary
                )
            }
        }
    }
}
