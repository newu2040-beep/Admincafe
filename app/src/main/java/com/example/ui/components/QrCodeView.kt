package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CafeGold
import java.security.MessageDigest

/**
 * Deterministic vector QR code renderer with finder patterns and table branding.
 */
@Composable
fun QrCodeView(
    data: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    label: String = ""
) {
    val matrix = remember(data) { generateQrMatrix(data, 25) }

    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(2.dp, CafeGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val moduleSize = this.size.width / 25f
                val darkColor = Color(0xFF13100E)

                for (r in 0 until 25) {
                    for (c in 0 until 25) {
                        if (matrix[r][c]) {
                            // Check if this module belongs to finder corners for slight corner rounding
                            val isFinderOuter = (r in 0..6 && c in 0..6) ||
                                    (r in 0..6 && c in 18..24) ||
                                    (r in 18..24 && c in 0..6)

                            val cornerRadius = if (isFinderOuter) CornerRadius(moduleSize * 0.4f, moduleSize * 0.4f) else CornerRadius.Zero

                            drawRoundRect(
                                color = darkColor,
                                topLeft = Offset(c * moduleSize, r * moduleSize),
                                size = Size(moduleSize, moduleSize),
                                cornerRadius = cornerRadius
                            )
                        }
                    }
                }
            }

            // Central cafe logo badge
            Box(
                modifier = Modifier
                    .size(size * 0.22f)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.5.dp, CafeGold, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "AC",
                    color = CafeGold,
                    fontSize = (size.value * 0.08f).sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        if (label.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                color = Color(0xFF13100E),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun generateQrMatrix(input: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) { false } }

    // Finder pattern helper
    fun drawFinder(top: Int, left: Int) {
        for (r in 0..6) {
            for (c in 0..6) {
                val isBorder = r == 0 || r == 6 || c == 0 || c == 6
                val isCenter = r in 2..4 && c in 2..4
                matrix[top + r][left + c] = isBorder || isCenter
            }
        }
    }

    // Three standard finders
    drawFinder(0, 0)
    drawFinder(0, size - 7)
    drawFinder(size - 7, 0)

    // Timing lines
    for (i in 7 until size - 7) {
        matrix[6][i] = (i % 2 == 0)
        matrix[i][6] = (i % 2 == 0)
    }

    // Deterministic pseudo data fill based on hash
    val md = MessageDigest.getInstance("SHA-256")
    val hash = md.digest(input.toByteArray())
    var hashIdx = 0

    for (r in 0 until size) {
        for (c in 0 until size) {
            // Skip finder zones
            val inFinder1 = r in 0..7 && c in 0..7
            val inFinder2 = r in 0..7 && c in (size - 8) until size
            val inFinder3 = r in (size - 8) until size && c in 0..7
            val inCenter = r in 10..14 && c in 10..14 // clear for center badge
            val isTiming = (r == 6) || (c == 6)

            if (!inFinder1 && !inFinder2 && !inFinder3 && !inCenter && !isTiming) {
                val byteVal = hash[hashIdx % hash.size].toInt()
                val bitVal = (byteVal ushr ((r * size + c) % 8)) and 1
                matrix[r][c] = (bitVal == 1)
                hashIdx++
            }
        }
    }

    return matrix
}
