package com.brokencoders.narisuraksha.decoy

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen(
    viewModel: DecoyViewModel,
    onUnlockApp: () -> Unit,
    onSosTriggered: () -> Unit
) {
    val state by viewModel.calculatorState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.secretUnlockedEvent.collect {
            onUnlockApp()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.decoySosTriggeredEvent.collect {
            onSosTriggered()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1E1E1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Calculator Expression Display
            if (state.expression.isNotEmpty()) {
                Text(
                    text = state.expression,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFAAAAAA),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    textAlign = TextAlign.End
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Calculator Number Display
            Text(
                text = state.displayText,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = if (state.displayText.length > 8) 36.sp else 54.sp,
                    fontWeight = FontWeight.Light
                ),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                textAlign = TextAlign.End,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid
            val buttons = listOf(
                listOf(
                    CalcBtn("C", Color(0xFFA5A5A5), Color.Black) { viewModel.onClear() },
                    CalcBtn("⌫", Color(0xFFA5A5A5), Color.Black) { viewModel.onBackspace() },
                    CalcBtn("%", Color(0xFFA5A5A5), Color.Black) { viewModel.onOperation("%") },
                    CalcBtn("÷", Color(0xFFFF9F0A), Color.White) { viewModel.onOperation("÷") }
                ),
                listOf(
                    CalcBtn("7", Color(0xFF333333), Color.White) { viewModel.onDigit("7") },
                    CalcBtn("8", Color(0xFF333333), Color.White) { viewModel.onDigit("8") },
                    CalcBtn("9", Color(0xFF333333), Color.White) { viewModel.onDigit("9") },
                    CalcBtn("×", Color(0xFFFF9F0A), Color.White) { viewModel.onOperation("×") }
                ),
                listOf(
                    CalcBtn("4", Color(0xFF333333), Color.White) { viewModel.onDigit("4") },
                    CalcBtn("5", Color(0xFF333333), Color.White) { viewModel.onDigit("5") },
                    CalcBtn("6", Color(0xFF333333), Color.White) { viewModel.onDigit("6") },
                    CalcBtn("-", Color(0xFFFF9F0A), Color.White) { viewModel.onOperation("-") }
                ),
                listOf(
                    CalcBtn("1", Color(0xFF333333), Color.White) { viewModel.onDigit("1") },
                    CalcBtn("2", Color(0xFF333333), Color.White) { viewModel.onDigit("2") },
                    CalcBtn("3", Color(0xFF333333), Color.White) { viewModel.onDigit("3") },
                    CalcBtn("+", Color(0xFFFF9F0A), Color.White) { viewModel.onOperation("+") }
                ),
                listOf(
                    CalcBtn("0", Color(0xFF333333), Color.White, isDoubleWidth = true) { viewModel.onDigit("0") },
                    CalcBtn(".", Color(0xFF333333), Color.White) { viewModel.onDecimal() },
                    CalcBtn("=", Color(0xFFFF9F0A), Color.White, isEquals = true) {
                        viewModel.onEquals()
                    }
                )
            )

            buttons.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    row.forEach { btn ->
                        CalculatorButtonView(
                            btn = btn,
                            onLongClick = if (btn.isEquals) { { viewModel.onEqualsLongPress() } } else null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class CalcBtn(
    val label: String,
    val bg: Color,
    val textColor: Color,
    val isDoubleWidth: Boolean = false,
    val isEquals: Boolean = false,
    val onClick: () -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CalculatorButtonView(
    btn: CalcBtn,
    onLongClick: (() -> Unit)? = null
) {
    val size = 74.dp
    val width = if (btn.isDoubleWidth) 156.dp else size

    Box(
        modifier = Modifier
            .size(width = width, height = size)
            .clip(CircleShape)
            .background(btn.bg)
            .combinedClickable(
                onClick = btn.onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = btn.label,
            style = MaterialTheme.typography.titleLarge.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            ),
            color = btn.textColor
        )
    }
}
