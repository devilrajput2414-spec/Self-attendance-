package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PayrollRecordEntity
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Rose600
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.util.PayrollEngine

@Composable
fun PayslipDialog(
    payroll: PayrollRecordEntity,
    onDownloadClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDownloadClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("download_payslip_pdf_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Download PDF")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_payslip_dialog_btn")
            ) {
                Text("Close")
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Official Form IV B Title Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(1.5.dp, Slate800, shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "Golf Ceramics Pvt. Ltd.",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = Slate900
                        )
                        Text(
                            text = "PAY-SLIP FORM IV B [ SEE RULE 26(2) ]",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp
                            ),
                            color = Slate800
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Payslip for the month of: ${payroll.month}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Header Employee Particulars & Attendance Grid (Form IV B)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            PayslipFieldItem("Name", payroll.employeeName)
                            PayslipFieldItem("Emp. Id", if (payroll.customEmployeeId.isNotBlank()) payroll.customEmployeeId else "${100000 + payroll.employeeId}")
                            PayslipFieldItem("Designation", payroll.designation)
                            PayslipFieldItem("Department", payroll.department)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            PayslipFieldItem("Month Days", "${payroll.monthDays}")
                            PayslipFieldItem("Working Days", "${payroll.totalWorkingDays}")
                            PayslipFieldItem("Off Day", "${payroll.offDays}")
                            PayslipFieldItem("C.L. / P.L. / S.L.", "${payroll.casualLeave} / ${payroll.paidLeave} / ${payroll.sickLeave}")
                            PayslipFieldItem("L.W.P.", "${payroll.leaveWithoutPay}")
                            PayslipFieldItem("PF-UAN-No.", payroll.pfUanNo)
                        }
                    }
                }

                // Table Breakdown: Earnings vs Deductions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.LightGray, shape = RoundedCornerShape(8.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Table Header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 6.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = "EARNINGS",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Emerald600
                            )
                            Text(
                                text = "DEDUCTIONS",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                color = Rose600,
                                textAlign = TextAlign.End
                            )
                        }

                        HorizontalDivider()

                        // Detailed Earnings and Deductions List
                        Column(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableRowPair("Basic Salary", PayrollEngine.formatCurrency(payroll.basicSalary), "Mess", PayrollEngine.formatCurrency(payroll.messDeduction))
                            TableRowPair("HRA", PayrollEngine.formatCurrency(payroll.hraAmount), "Advance", PayrollEngine.formatCurrency(payroll.advanceRecovery))
                            TableRowPair("Sp. Allow.", PayrollEngine.formatCurrency(payroll.specialAllowance), "LWF", PayrollEngine.formatCurrency(payroll.lwfDeduction))
                            TableRowPair("Conveyance", PayrollEngine.formatCurrency(payroll.conveyanceAllowance), "Prof Tax", PayrollEngine.formatCurrency(payroll.ptDeduction))
                            TableRowPair("CL/PL/SL", "₹0", "E.S.I.", PayrollEngine.formatCurrency(payroll.esiDeduction))
                            TableRowPair("P.F. (Employer)", PayrollEngine.formatCurrency(payroll.pfDeduction), "P.F. (Emp.)", PayrollEngine.formatCurrency(payroll.pfDeduction))
                            TableRowPair("Bonus", "₹0", "Co Loan", PayrollEngine.formatCurrency(payroll.coLoanDeduction))
                            TableRowPair("Leave Encash", "₹0", "T.D.S. (I.T.)", PayrollEngine.formatCurrency(payroll.tdsDeduction))
                            if (payroll.perExpDeduction > 0) {
                                TableRowPair("Other", "₹0", "Per. Exp", PayrollEngine.formatCurrency(payroll.perExpDeduction))
                            }
                        }

                        HorizontalDivider()

                        // Totals Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Gross Pay:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Text(PayrollEngine.formatCurrency(payroll.grossEarnings), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Emerald600)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Ded.:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                                Text("- " + PayrollEngine.formatCurrency(payroll.totalDeductions), style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = Rose600)
                            }
                        }
                    }
                }

                // Net Pay Highlight Card (Form IV B Net Pay Format)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Emerald100,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Emerald600),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Pay :",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Emerald600
                                )
                            )
                            Text(
                                text = PayrollEngine.formatCurrency(payroll.netPayable),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Emerald600
                                )
                            )
                        }

                        Text(
                            text = PayrollEngine.formatAmountToWords(payroll.netPayable),
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = Slate800
                        )
                    }
                }

                // Official Signatures & "PAID" Stamp Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Prepared By: System Admin", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Payment Date: ${payroll.generatedDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text("Issue Date: ${payroll.generatedDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }

                    // Authentic PAID Stamp
                    Box(
                        modifier = Modifier
                            .border(2.dp, Rose600, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PAID",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            ),
                            color = Rose600
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun PayslipFieldItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Slate900
        )
    }
}

@Composable
private fun TableRowPair(
    earnLabel: String,
    earnVal: String,
    dedLabel: String,
    dedVal: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(earnLabel, style = MaterialTheme.typography.bodySmall)
            Text(earnVal, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dedLabel, style = MaterialTheme.typography.bodySmall)
            Text(dedVal, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
