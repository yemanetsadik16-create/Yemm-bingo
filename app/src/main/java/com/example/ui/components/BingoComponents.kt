package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BingoCard
import com.example.model.BingoGoal
import com.example.ui.theme.BingoBorderGold
import com.example.ui.theme.BingoCardSurface
import com.example.ui.theme.BingoCardSurfaceVariant
import com.example.ui.theme.BingoDarkBg
import com.example.ui.theme.BingoGoldDark
import com.example.ui.theme.BingoGoldPrimary
import com.example.ui.theme.BingoTextLight
import com.example.ui.theme.BingoTextMuted
import com.example.ui.theme.BingoVibrantGreen
import com.example.ui.theme.ColumnBColor
import com.example.ui.theme.ColumnGColor
import com.example.ui.theme.ColumnIColor
import com.example.ui.theme.ColumnNColor
import com.example.ui.theme.ColumnOColor

@Composable
fun EthiopianBanner(text: String, onDismiss: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ethiopian_banner"),
        colors = CardDefaults.cardColors(containerColor = BingoCardSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Info",
                tint = BingoGoldPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = BingoTextLight,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CountdownDisplay(
    countdownText: String,
    slotLabel: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = countdownText,
            color = BingoGoldPrimary,
            fontSize = 42.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("countdown_display")
        )
        Text(
            text = "Next: BINGO Game at $slotLabel",
            color = BingoTextMuted,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BingoCardView(
    card: BingoCard,
    markedCells: Set<Pair<Int, Int>>,
    winningCells: List<Pair<Int, Int>>? = null,
    onCellClick: ((col: Int, row: Int) -> Unit)? = null,
    isInteractive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val headers = listOf("B", "I", "N", "G", "O")
    val headerColors = listOf(
        ColumnBColor,
        ColumnIColor,
        ColumnNColor,
        ColumnGColor,
        ColumnOColor
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp))
            .testTag("bingo_card_${card.cardNumber}"),
        colors = CardDefaults.cardColors(containerColor = BingoCardSurface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, BingoBorderGold.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Card Title Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cartela #${card.cardNumber}",
                    color = BingoGoldPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Center is FREE ★",
                    color = BingoTextMuted,
                    fontSize = 12.sp
                )
            }

            // B-I-N-G-O Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                headers.forEachIndexed { i, letter ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .background(
                                color = BingoCardSurfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter,
                            color = headerColors[i],
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 5 Rows of Numbers
            for (row in 0..4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (col in 0..4) {
                        val num = card.getNumberAt(col, row)
                        val isCenter = (col == 2 && row == 2)
                        val pos = Pair(col, row)
                        val isMarked = markedCells.contains(pos) || isCenter
                        val isWinCell = winningCells?.contains(pos) == true

                        val cellBg = when {
                            isWinCell -> Brush.linearGradient(listOf(BingoGoldPrimary, BingoGoldDark))
                            isMarked -> Brush.linearGradient(listOf(BingoVibrantGreen, Color(0xFF1B8246)))
                            else -> Brush.linearGradient(listOf(BingoCardSurfaceVariant, BingoCardSurfaceVariant))
                        }

                        val textColor = when {
                            isWinCell -> Color(0xFF2A0F55)
                            isMarked -> Color.White
                            else -> BingoTextLight
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(cellBg)
                                .border(
                                    width = if (isWinCell || isMarked) 1.5.dp else 1.dp,
                                    color = if (isWinCell) Color.White else if (isMarked) BingoBorderGold else BingoCardSurfaceVariant,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(enabled = isInteractive && !isCenter) {
                                    onCellClick?.invoke(col, row)
                                }
                                .testTag("cell_${col}_${row}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCenter) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Free space",
                                    tint = if (isWinCell) Color(0xFF2A0F55) else BingoGoldPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "$num",
                                    color = textColor,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalledNumbersBoard(
    calledNumbers: List<Int>,
    lastCalled: Int?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .testTag("called_numbers_board"),
        colors = CardDefaults.cardColors(containerColor = BingoDarkBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, BingoBorderGold.copy(alpha = 0.3f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calls: ${calledNumbers.size} / 75",
                    color = BingoGoldPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (lastCalled != null) {
                    Text(
                        text = "Last: ${com.example.game.BingoEngine.englishCallout(lastCalled)}",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 10 columns grid for numbers 1..75
            val rowsCount = 8
            for (r in 0 until rowsCount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.5.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (c in 0 until 10) {
                        val num = r * 10 + c + 1
                        if (num <= 75) {
                            val isCalled = calledNumbers.contains(num)
                            val isLast = num == lastCalled

                            val bg = when {
                                isLast -> BingoGoldPrimary
                                isCalled -> Color(0xFF6D28D9)
                                else -> BingoCardSurface
                            }
                            val txtColor = when {
                                isLast -> Color(0xFF2A0F55)
                                isCalled -> Color.White
                                else -> BingoTextMuted.copy(alpha = 0.6f)
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(bg)
                                    .border(
                                        width = if (isLast) 1.5.dp else 0.5.dp,
                                        color = if (isLast) Color.White else Color.Transparent,
                                        shape = RoundedCornerShape(4.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$num",
                                    color = txtColor,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCalled) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
