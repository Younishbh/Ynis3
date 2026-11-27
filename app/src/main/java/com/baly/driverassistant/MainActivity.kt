package com.baly.driverassistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        BalyAssistantScreen()
                    }
                }
            }
        }
    }
}

@Composable
private fun BalyAssistantScreen() {
    var fare by rememberSaveable { mutableStateOf("5000") }
    var distance by rememberSaveable { mutableStateOf("5") }
    var minutes by rememberSaveable { mutableStateOf("20") }
    var consumption by rememberSaveable { mutableStateOf("3") }
    var fuelPrice by rememberSaveable { mutableStateOf("1000") }
    var operatingCost by rememberSaveable { mutableStateOf("100") }
    var minimumProfit by rememberSaveable { mutableStateOf("1500") }
    var minimumPerKm by rememberSaveable { mutableStateOf("300") }
    var minimumPerHour by rememberSaveable { mutableStateOf("3000") }
    var maximumDistance by rememberSaveable { mutableStateOf("15") }

    val fareValue = fare.number()
    val distanceValue = distance.number()
    val durationHours = minutes.number() / 60.0
    val fuelCost = distanceValue * consumption.number() / 100.0 * fuelPrice.number()
    val netProfit = fareValue - fuelCost - distanceValue * operatingCost.number()
    val profitPerKm = if (distanceValue > 0) netProfit / distanceValue else 0.0
    val profitPerHour = if (durationHours > 0) netProfit / durationHours else 0.0
    val recommendation = when {
        distanceValue > maximumDistance.number() || netProfit < minimumProfit.number() ||
            profitPerKm < minimumPerKm.number() || profitPerHour < minimumPerHour.number() -> "رفض الطلب"
        netProfit >= minimumProfit.number() * 1.5 &&
            profitPerKm >= minimumPerKm.number() * 1.5 &&
            profitPerHour >= minimumPerHour.number() * 1.5 -> "قبول الطلب"
        else -> "طلب متوسط — القرار للسائق"
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("مساعد سائق بلي", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("أدخل تفاصيل الطلب لتقدير تكلفته وربحه. التطبيق لا يقبل أو يرفض الطلب نيابةً عنك.", style = MaterialTheme.typography.bodyMedium)

        SectionCard("تفاصيل الطلب") {
            NumberField("أجرة التوصيل", fare, { fare = it }, "دينار")
            NumberField("المسافة الإجمالية (تشمل الوصول للاستلام)", distance, { distance = it }, "كم")
            NumberField("المدة المتوقعة", minutes, { minutes = it }, "دقيقة")
        }
        SectionCard("إعدادات الدراجة والتكاليف") {
            NumberField("استهلاك الوقود لكل 100 كم", consumption, { consumption = it }, "لتر")
            NumberField("سعر لتر البنزين", fuelPrice, { fuelPrice = it }, "دينار")
            NumberField("مصاريف التشغيل لكل كم", operatingCost, { operatingCost = it }, "دينار")
        }
        SectionCard("حدود التوصية") {
            NumberField("الحد الأدنى لصافي الربح", minimumProfit, { minimumProfit = it }, "دينار")
            NumberField("الحد الأدنى للربح لكل كم", minimumPerKm, { minimumPerKm = it }, "دينار")
            NumberField("الحد الأدنى للربح لكل ساعة", minimumPerHour, { minimumPerHour = it }, "دينار")
            NumberField("الحد الأقصى للمسافة", maximumDistance, { maximumDistance = it }, "كم")
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("نتيجة التحليل", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                ResultRow("أجرة الطلب", fareValue.money())
                ResultRow("تكلفة الوقود", fuelCost.money())
                ResultRow("صافي الربح التقديري", netProfit.money())
                ResultRow("الربح لكل كيلومتر", "${profitPerKm.money()} / كم")
                ResultRow("الربح لكل ساعة", "${profitPerHour.money()} / ساعة")
                Spacer(Modifier.height(4.dp))
                Text("التوصية: $recommendation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        Text("الحساب تقديري ويعتمد على دقة البيانات المدخلة. لا توجد قراءة شاشة أو نافذة عائمة في هذه النسخة الأولية.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            content()
        }
    }
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit, suffix: String) {
    OutlinedTextField(
        value = value,
        onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' || it == ',' }.replace(',', '.')) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        suffix = { Text(suffix) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
private fun ResultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(label)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun String.number(): Double = replace(',', '.').toDoubleOrNull()?.coerceAtLeast(0.0) ?: 0.0
private fun Double.money(): String = "${String.format(Locale.US, "%.0f", this)} د.ع"
