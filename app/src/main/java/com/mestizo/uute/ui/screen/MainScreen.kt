package com.mestizo.uute.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mestizo.uute.domain.model.CalculationEngine
import com.mestizo.uute.domain.model.CalculationResult
import com.mestizo.uute.domain.model.InputData
import com.mestizo.uute.domain.model.RangeStatus
import com.mestizo.uute.domain.model.SelectedEquipment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var objectName by remember { mutableStateOf("") }
    var qHeat by remember { mutableStateOf("0.450") }
    var qVent by remember { mutableStateOf("0.120") }
    var qGvs by remember { mutableStateOf("0.230") }
    var t1 by remember { mutableStateOf("95.0") }
    var t2 by remember { mutableStateOf("70.0") }
    var selectedDn by remember { mutableStateOf("50") }
    var result by remember { mutableStateOf<CalculationResult?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Подбор оборудования УУТЭ v2.0") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("1. Исходные данные", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = objectName,
                onValueChange = { objectName = it },
                label = { Text("Наименование объекта") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = qHeat,
                    onValueChange = { qHeat = it },
                    label = { Text("Q_от (Гкал/ч)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = qVent,
                    onValueChange = { qVent = it },
                    label = { Text("Q_вент (Гкал/ч)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = qGvs,
                    onValueChange = { qGvs = it },
                    label = { Text("Q_гвс (Гкал/ч)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = selectedDn,
                    onValueChange = { selectedDn = it },
                    label = { Text("DN (мм)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = t1,
                    onValueChange = { t1 = it },
                    label = { Text("T1 (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = t2,
                    onValueChange = { t2 = it },
                    label = { Text("T2 (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val input = InputData(
                        objectName = objectName,
                        qHeatGcal = qHeat.toDoubleOrNull() ?: 0.0,
                        qVentGcal = qVent.toDoubleOrNull() ?: 0.0,
                        qGvsGcal = qGvs.toDoubleOrNull() ?: 0.0,
                        t1 = t1.toDoubleOrNull() ?: 95.0,
                        t2 = t2.toDoubleOrNull() ?: 70.0
                    )
                    val eq = SelectedEquipment(
                        modelName = "Питерфлоу РС",
                        selectedDn = selectedDn.toIntOrNull() ?: 50,
                        kvs = 160.0,
                        qMin = 0.2,
                        qNom = 15.0,
                        qMax = 30.0
                    )
                    result = CalculationEngine.calculate(input, eq)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Рассчитать гидравлику")
            }

            result?.let { res ->
                Spacer(modifier = Modifier.height(20.dp))
                Text("2. Результаты подбора", style = MaterialTheme.typography.titleMedium)
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Нагрузка Q_сум: ${String.format("%.3f", res.qTotalGcal)} Гкал/ч")
                        Text("Объемный расход V: ${String.format("%.2f", res.volumeFlowV)} м³/ч")
                        Text("Скорость v: ${String.format("%.2f", res.velocityMs)} м/с")
                        Text("Потери ΔP: ${String.format("%.3f", res.deltaPRasxBar)} бар")
                    }
                }

                res.alerts.forEach { alert ->
                    val bgColor = when (res.status) {
                        RangeStatus.WARNING_UNDERSIZE -> Color(0xFFC62828)
                        RangeStatus.WARNING_OVERSIZE -> Color(0xFFF57C00)
                        else -> Color(0xFF0D47A1)
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(bgColor)
                            .padding(12.dp)
                    ) {
                        Text(text = alert, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("3. Протокол расчета", style = MaterialTheme.typography.titleMedium)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.LightGray.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = res.protocolText,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}
