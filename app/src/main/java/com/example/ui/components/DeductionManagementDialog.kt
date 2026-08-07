package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.DeductionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeductionManagementDialog(
    employeeName: String,
    onSaveDeduction: (type: DeductionType, amount: Double, description: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember { mutableStateOf(DeductionType.ADVANCE_SALARY) }
    var amountText by remember { mutableStateOf("1500") }
    var descText by remember { mutableStateOf("Emergency Advance Request") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text("Add Ledger Entry", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Recording Deduction / Advance for: $employeeName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Deduction Type Choices
                Text(
                    text = "Deduction Type",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                val deductionOptions = listOf(
                    DeductionType.ADVANCE_SALARY to "Advance",
                    DeductionType.MESS_CHARGES to "Mess",
                    DeductionType.LWF to "LWF",
                    DeductionType.PROF_TAX to "Prof Tax",
                    DeductionType.ESI to "E.S.I.",
                    DeductionType.CO_LOAN to "Co Loan",
                    DeductionType.TDS to "T.D.S. (I.T.)",
                    DeductionType.PER_EXP to "Per. Exp",
                    DeductionType.OTHER to "Other"
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    deductionOptions.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { (type, label) ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = {
                                        selectedType = type
                                        descText = "$label Deduction"
                                    },
                                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f).testTag("type_chip_${type.name.lowercase()}")
                                )
                            }
                            // Fill remaining space if less than 3
                            repeat(3 - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth().testTag("deduction_amount_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                OutlinedTextField(
                    value = descText,
                    onValueChange = { descText = it },
                    label = { Text("Notes / Description") },
                    modifier = Modifier.fillMaxWidth().testTag("deduction_desc_field"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onSaveDeduction(selectedType, amount, descText)
                    }
                },
                modifier = Modifier.testTag("save_deduction_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Record Entry", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
