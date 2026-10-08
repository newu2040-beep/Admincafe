package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OrderStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (status) {
        "DRAFT", "NEW" -> Triple(StatusNewColor.copy(alpha = 0.15f), StatusNewColor, StatusNewColor)
        "ACCEPTED", "PREPARING" -> Triple(StatusPreparingColor.copy(alpha = 0.15f), StatusPreparingColor, StatusPreparingColor)
        "READY" -> Triple(StatusReadyColor.copy(alpha = 0.15f), StatusReadyColor, StatusReadyColor)
        "SERVED" -> Triple(StatusServedColor.copy(alpha = 0.15f), StatusServedColor, StatusServedColor)
        "COMPLETED" -> Triple(Color(0xFF10B981).copy(alpha = 0.12f), Color(0xFF10B981), Color(0xFF10B981))
        "CANCELLED" -> Triple(StatusCancelledColor.copy(alpha = 0.15f), StatusCancelledColor, StatusCancelledColor)
        else -> Triple(Color.Gray.copy(alpha = 0.15f), Color.Gray, Color.Gray)
    }

    Row(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(dotColor, CircleShape)
        )
        Text(
            text = status,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TableStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (status) {
        "AVAILABLE" -> Pair(StatusAvailableColor.copy(alpha = 0.15f), StatusAvailableColor)
        "OCCUPIED" -> Pair(StatusOccupiedColor.copy(alpha = 0.15f), StatusOccupiedColor)
        "RESERVED" -> Pair(StatusReservedColor.copy(alpha = 0.15f), StatusReservedColor)
        "CLEANING" -> Pair(Color(0xFF06B6D4).copy(alpha = 0.15f), Color(0xFF06B6D4))
        "DISABLED" -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
        else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Text(
        text = status,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
            .background(bgColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun WarningConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = CafeGold
            )
        },
        title = { Text(text = title, fontWeight = FontWeight.Bold) },
        text = { Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(confirmText, color = MaterialTheme.colorScheme.onError)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    )
}
