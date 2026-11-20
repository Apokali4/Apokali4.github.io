package ru.apokali4.aptechnayasmena

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GameShiftPanel(onReward: (Int) -> Unit) {
    val progress = remember { ShiftProgress() }
    var stage by remember { mutableStateOf(progress.stage) }
    var selected by remember { mutableIntStateOf(-1) }
    var resultText by remember { mutableStateOf("") }
    var resultWasCorrect by remember { mutableStateOf(false) }
    val task = ShiftFlow.steps.getOrNull(progress.completedCount)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("СЮЖЕТНАЯ СМЕНА · " + progress.completedCount.coerceAtMost(3) + "/3", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            if (stage == ShiftStage.COMPLETE || task == null) {
                Text("Смена закрыта!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Ты принял поставку, разместил товар и обслужил покупателя. Результат сохранён в опыте персонажа.")
                Text("Получено за смену: 70 XP", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            } else {
                Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(task.scene, style = MaterialTheme.typography.bodyLarge)
                task.options.forEachIndexed { index, option ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .clickable { selected = index }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        RadioButton(selected = selected == index, onClick = { selected = index })
                        Text(option, modifier = Modifier.weight(1f).padding(top = 12.dp, end = 8.dp))
                    }
                }
                Button(
                    onClick = {
                        val result = progress.choose(selected)
                        resultWasCorrect = result.correct
                        resultText = result.explanation
                        if (result.correct) onReward(result.rewardXp)
                        stage = result.nextStage
                        selected = -1
                    },
                    enabled = selected >= 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Выполнить действие") }
                if (resultText.isNotBlank()) {
                    Text(resultText, color = if (resultWasCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
