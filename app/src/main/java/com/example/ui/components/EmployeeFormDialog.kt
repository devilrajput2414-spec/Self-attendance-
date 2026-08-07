package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.UserEntity
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeFormDialog(
    existingEmployee: UserEntity? = null,
    onSaveEmployee: (name: String, email: String, designation: String, dept: String, salary: Double, pf: Double, pt: Double, customEmployeeId: String, pfUanNo: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(existingEmployee?.name ?: "") }
    var email by remember { mutableStateOf(existingEmployee?.email ?: "") }
    var customEmployeeId by remember { mutableStateOf(existingEmployee?.customEmployeeId ?: "") }
    var designation by remember { mutableStateOf(existingEmployee?.designation ?: "Staff / Worker") }
    var dept by remember { mutableStateOf(existingEmployee?.department ?: "General") }
    var pfUanNo by remember { mutableStateOf(existingEmployee?.pfUanNo ?: "102293272082") }
    var salaryText by remember { mutableStateOf(existingEmployee?.basicSalary?.toString() ?: "30000") }
    var pfText by remember { mutableStateOf(existingEmployee?.pfPercentage?.toString() ?: "12.0") }
    var ptText by remember { mutableStateOf(existingEmployee?.professionalTax?.toString() ?: "200") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Blue600
                )
                Text(
                    if (existingEmployee != null) "Edit Worker Profile" else "Add Worker Profile",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Enter your worker details, employee ID, designation, department, and PF UAN below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Worker / Employee Full Name *") },
                    placeholder = { Text("e.g. Ramesh Kumar") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_name_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customEmployeeId,
                    onValueChange = { customEmployeeId = it },
                    label = { Text("Employee ID / Code") },
                    placeholder = { Text("e.g. EMP-101") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_custom_id_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = designation,
                    onValueChange = { designation = it },
                    label = { Text("Designation / Role") },
                    placeholder = { Text("e.g. Operator, Technician, Supervisor") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_designation_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dept,
                    onValueChange = { dept = it },
                    label = { Text("Department") },
                    placeholder = { Text("e.g. Production, Site A, Maintenance") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_dept_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = pfUanNo,
                    onValueChange = { pfUanNo = it },
                    label = { Text("PF UAN Number") },
                    placeholder = { Text("e.g. 102293272082") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_pf_uan_field"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address (Optional)") },
                    placeholder = { Text("Auto-generated if left empty") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_email_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                OutlinedTextField(
                    value = salaryText,
                    onValueChange = { salaryText = it },
                    label = { Text("Monthly Basic Salary (₹)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_employee_salary_field"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = pfText,
                        onValueChange = { pfText = it },
                        label = { Text("PF %") },
                        modifier = Modifier.weight(1f).testTag("add_employee_pf_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = ptText,
                        onValueChange = { ptText = it },
                        label = { Text("PT Fixed (₹)") },
                        modifier = Modifier.weight(1f).testTag("add_employee_pt_field"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val salary = salaryText.toDoubleOrNull() ?: 30000.0
                    val pf = pfText.toDoubleOrNull() ?: 12.0
                    val pt = ptText.toDoubleOrNull() ?: 200.0
                    if (name.isNotBlank()) {
                        onSaveEmployee(name, email, designation, dept, salary, pf, pt, customEmployeeId, pfUanNo)
                    }
                },
                modifier = Modifier.testTag("save_employee_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
            ) {
                Text(if (existingEmployee != null) "Save Changes" else "Create Profile", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
