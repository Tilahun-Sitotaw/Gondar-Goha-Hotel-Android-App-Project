package com.gohahotel.connect.ui.payment

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gohahotel.connect.data.repository.PaymentCategory
import com.gohahotel.connect.data.repository.PaymentMethod
import com.gohahotel.connect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentSelectionSheet(
    selectedMethod: PaymentMethod,
    onMethodSelected: (PaymentMethod) -> Unit,
    onConfirm: () -> Unit,
    isProcessing: Boolean,
    amount: Double = 0.0,
    currency: String = "ETB"
) {
    // Group methods by category
    val grouped = PaymentMethod.entries.groupBy { it.category }
    val categoryOrder = listOf(
        PaymentCategory.ETHIOPIAN_MOBILE,
        PaymentCategory.ETHIOPIAN_BANK,
        PaymentCategory.INTERNATIONAL,
        PaymentCategory.CASH
    )

    // Track which categories are expanded
    val expanded = remember {
        mutableStateMapOf(
            PaymentCategory.ETHIOPIAN_MOBILE to true,
            PaymentCategory.ETHIOPIAN_BANK   to false,
            PaymentCategory.INTERNATIONAL    to false,
            PaymentCategory.CASH             to true
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        // ── Handle bar ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.White.copy(0.2f), CircleShape)
            )
        }

        // ── Header ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text("Select Payment Method",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = GoldPrimary)
            if (amount > 0) {
                Text("Total: $currency ${amount.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceDark.copy(0.6f))
            }
        }

        HorizontalDivider(color = Color.White.copy(0.06f))

        // ── Payment list ──────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
                .heightIn(max = 480.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categoryOrder.forEach { category ->
                val methods = grouped[category] ?: return@forEach

                // Category header — tappable to expand/collapse
                item(key = category.name) {
                    Surface(
                        onClick = { expanded[category] = !(expanded[category] ?: false) },
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor(category).copy(0.08f),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(categoryEmoji(category), fontSize = 16.sp)
                            Text(
                                category.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = categoryColor(category),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                if (expanded[category] == true) Icons.Default.ExpandLess
                                else Icons.Default.ExpandMore,
                                null,
                                tint = categoryColor(category),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Methods in this category
                if (expanded[category] == true) {
                    items(methods, key = { it.name }) { method ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit  = fadeOut() + shrinkVertically()
                        ) {
                            PaymentMethodRow(
                                method   = method,
                                selected = selectedMethod == method,
                                onClick  = { onMethodSelected(method) }
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(8.dp)) }
        }

        // ── Selected method summary ───────────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            color = GoldPrimary.copy(0.08f),
            border = BorderStroke(1.dp, GoldPrimary.copy(0.25f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(selectedMethod.emoji, fontSize = 22.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Selected: ${selectedMethod.displayName}",
                        color = GoldPrimary, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium)
                    if (selectedMethod.description.isNotBlank()) {
                        Text(selectedMethod.description,
                            color = OnSurfaceDark.copy(0.5f),
                            style = MaterialTheme.typography.labelSmall)
                    }
                }
                Icon(Icons.Default.CheckCircle, null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
            }
        }

        // ── Confirm button ────────────────────────────────────────────────────
        Button(
            onClick  = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
                .height(56.dp),
            shape  = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
            enabled = !isProcessing
        ) {
            if (isProcessing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF050D18), strokeWidth = 2.dp
                    )
                    Text("Processing payment...",
                        color = Color(0xFF050D18), fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(selectedMethod.emoji, fontSize = 18.sp)
                    Text(
                        if (amount > 0) "Pay $currency ${amount.toInt()} via ${selectedMethod.displayName}"
                        else "Confirm & Pay via ${selectedMethod.displayName}",
                        color = Color(0xFF050D18),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PaymentMethodRow(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit
) {
    val accent = categoryColor(method.category)
    Surface(
        onClick  = onClick,
        shape    = RoundedCornerShape(12.dp),
        color    = if (selected) accent.copy(0.12f) else Color(0xFF0E1B2A),
        border   = BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) accent else Color.White.copy(0.06f)
        ),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(accent.copy(0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(method.emoji, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(method.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) accent else OnSurfaceDark)
                if (method.description.isNotBlank()) {
                    Text(method.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceDark.copy(0.45f))
                }
            }

            if (selected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = accent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
private fun categoryColor(cat: PaymentCategory): Color = when (cat) {
    PaymentCategory.ETHIOPIAN_MOBILE -> Color(0xFF50C878)
    PaymentCategory.ETHIOPIAN_BANK   -> Color(0xFF4A90E2)
    PaymentCategory.INTERNATIONAL    -> Color(0xFFE29B4A)
    PaymentCategory.CASH             -> Color(0xFFD4A843)
}

private fun categoryEmoji(cat: PaymentCategory): String = when (cat) {
    PaymentCategory.ETHIOPIAN_MOBILE -> "📱"
    PaymentCategory.ETHIOPIAN_BANK   -> "🏦"
    PaymentCategory.INTERNATIONAL    -> "🌐"
    PaymentCategory.CASH             -> "💵"
}
