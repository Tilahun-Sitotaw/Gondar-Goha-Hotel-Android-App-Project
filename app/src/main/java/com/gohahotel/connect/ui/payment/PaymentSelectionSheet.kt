package com.gohahotel.connect.ui.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gohahotel.connect.data.repository.PaymentMethod
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionSheet(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    isProcessing: Boolean
) {
    val methods = listOf(
        Triple(PaymentMethod.TELE_BIRR, "TeleBirr", Icons.Default.Smartphone),
        Triple(PaymentMethod.CBE_BIRR, "CBE Birr", Icons.Default.Payments),
        Triple(PaymentMethod.CREDIT_CARD, "Credit / Debit Card", Icons.Default.CreditCard),
        Triple(PaymentMethod.CASH_AT_HOTEL, "Pay at Hotel", Icons.Default.Payments)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Payment Method",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = GoldPrimary
        )
        
        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(methods) { (method, name, icon) ->
                PaymentMethodItem(
                    name = name,
                    icon = icon,
                    selected = selectedMethod == method,
                    onClick = { onMethodSelected(method) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = SurfaceDark, modifier = Modifier.size(24.dp))
            } else {
                Text("Confirm & Pay", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PaymentMethodItem(
    name: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (selected) GoldPrimary.copy(alpha = 0.1f) else CardDark,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) GoldPrimary else MaterialTheme.colorScheme.outline.copy(0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(GoldPrimary.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
            }
            
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )

            if (selected) {
                Icon(Icons.Default.CheckCircle, null, tint = GoldPrimary)
            }
        }
    }
}
