package com.mestizo.uute.domain.model

import java.util.Locale
import kotlin.math.PI
import kotlin.math.pow

data class InputData(
    val objectName: String = "",
    val objectAddress: String = "",
    val qHeatGcal: Double = 0.0,
    val qVentGcal: Double = 0.0,
    val qGvsGcal: Double = 0.0,
    val t1: Double = 95.0,
    val t2: Double = 70.0,
    val p1Bar: Double = 6.0,
    val p2Bar: Double = 4.0,
    val existingDn: Int = 50,
    val isAlmatyRegion: Boolean = false,
    val cableLengthMeters: Double = 5.0
)

data class SelectedEquipment(
    val modelName: String,
    val selectedDn: Int,
    val kvs: Double,
    val qMin: Double,
    val qNom: Double,
    val qMax: Double
)

data class CalculationResult(
    val qTotalGcal: Double,
    val massFlowG: Double,
    val densityT1: Double,
    val volumeFlowV: Double,
    val velocityMs: Double,
    val deltaPRasxBar: Double,
    val recommendedDn: Int,
    val status: RangeStatus,
    val alerts: List<String>,
    val protocolText: String
)

enum class RangeStatus { OPTIMAL, WARNING_UNDERSIZE, WARNING_OVERSIZE }

object CalculationEngine {

    fun calculateDensity(t: Double): Double {
        return 1000.0 - (0.018 * t) - (0.0038 * t.pow(2.0))
    }

    fun calculate(input: InputData, equipment: SelectedEquipment): CalculationResult {
        val qTotal = input.qHeatGcal + input.qVentGcal + input.qGvsGcal
        val deltaT = input.t1 - input.t2
        val massFlowG = if (deltaT > 0) (qTotal / deltaT) * 1000.0 else 0.0

        val density = calculateDensity(input.t1)
        val volumeFlowV = if (density > 0) (massFlowG * 1000.0) / density else 0.0

        val dnMeters = equipment.selectedDn / 1000.0
        val area = (PI * dnMeters.pow(2.0)) / 4.0
        val velocity = if (area > 0) volumeFlowV / (area * 3600.0) else 0.0

        val deltaPBar = if (equipment.kvs > 0) (volumeFlowV / equipment.kvs).pow(2.0) else 0.0

        val alerts = mutableListOf<String>()
        var status = RangeStatus.OPTIMAL

        if (volumeFlowV > equipment.qMax || velocity > 2.5 || deltaPBar > 0.1) {
            status = RangeStatus.WARNING_UNDERSIZE
            alerts.add(
                "Внимание: Диаметр DN занижен! Высокая скорость потока (v = ${String.format(Locale.US, "%.2f", velocity)} м/с) " +
                "и потери давления (ΔP = ${String.format(Locale.US, "%.3f", deltaPBar)} бар). Риск шума, кавитации и износа."
            )
        } else if (volumeFlowV < equipment.qMin || volumeFlowV < (0.3 * equipment.qNom)) {
            status = RangeStatus.WARNING_OVERSIZE
            alerts.add(
                "Внимание: Диаметр DN завышен! При минимальном водоразборе расход упадет ниже порога чувствительности Q_min."
            )
        }

        if ((equipment.selectedDn * 0.75) > (0.7 * input.existingDn)) {
            alerts.add("Внимание: Требуется установка расширителя трубопровода (футорки/катушки) в месте врезки термопреобразователя.")
        }
        if (input.cableLengthMeters > 10.0) {
            alerts.add("Рекомендуется перейти на 4-проводную схему или использовать датчики Pt500.")
        }
        if (equipment.modelName.contains("КМ-5", ignoreCase = true) && input.isAlmatyRegion) {
            alerts.add("Данная схема установки и настройки теплосчётчика КМ-5 (модификация КМ-5-5 в реверсном режиме) предназначена исключительно для объектов г. Алматы с учётом местной схемы открытого водоразбора.")
        }

        val protocol = StringBuilder().apply {
            appendLine("1. ПРИВЕДЕНИЕ НАГРУЗОК:")
            appendLine("   Q_сум = ${String.format(Locale.US, "%.3f", qTotal)} Гкал/ч")
            appendLine("2. РАСЧЕТ МАССОВОГО РАСХОДА G:")
            appendLine("   G = ($qTotal / (${input.t1} - ${input.t2})) * 1000 = ${String.format(Locale.US, "%.2f", massFlowG)} т/ч")
            appendLine("3. РАСЧЕТ ПЛОТНОСТИ ТЕПЛОНОСИТЕЛЯ ρ(T1):")
            appendLine("   ρ(${input.t1}°C) = 1000 - 0.018*${input.t1} - 0.0038*(${input.t1})² = ${String.format(Locale.US, "%.2f", density)} кг/м³")
            appendLine("4. ПЕРЕСЧЕТ В ОБЪЕМНЫЙ РАСХОД V:")
            appendLine("   V = (${String.format(Locale.US, "%.2f", massFlowG)} * 1000) / ${String.format(Locale.US, "%.2f", density)} = ${String.format(Locale.US, "%.2f", volumeFlowV)} м³/ч")
            appendLine("5. ГИДРАВЛИЧЕСКИЙ РАСЧЕТ ПОТЕРЬ ДАВЛЕНИЯ ΔP (DN${equipment.selectedDn}, Kvs = ${equipment.kvs}):")
            appendLine("   ΔP = ($volumeFlowV / ${equipment.kvs})² = ${String.format(Locale.US, "%.3f", deltaPBar)} бар")
        }.toString()

        return CalculationResult(
            qTotalGcal = qTotal,
            massFlowG = massFlowG,
            densityT1 = density,
            volumeFlowV = volumeFlowV,
            velocityMs = velocity,
            deltaPRasxBar = deltaPBar,
            recommendedDn = equipment.selectedDn,
            status = status,
            alerts = alerts,
            protocolText = protocol
        )
    }
}
