package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldDarkBackground
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight

data class ThikrItem(
    val id: Int,
    val text: String,
    val totalCount: Int,
    val reward: String
)

@Composable
fun AthkarScreen() {
    var selectedTab by remember { mutableStateOf(0) } // 0: Post-Prayer, 1: Morning/Evening, 2: Tasbeeh

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = GoldAccent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("أذكار الصلاة", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("الصباح والمساء", fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("السبحة الإلكترونية", fontWeight = FontWeight.Bold) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> AthkarListTab(postPrayerAthkar)
                1 -> AthkarListTab(morningEveningAthkar)
                2 -> DigitalTasbeehTab()
            }
        }
    }
}

@Composable
fun AthkarListTab(athkarList: List<ThikrItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(athkarList) { thikr ->
            ThikrCard(thikr = thikr)
        }
    }
}

@Composable
fun ThikrCard(thikr: ThikrItem) {
    var countLeft by remember { mutableStateOf(thikr.totalCount) }
    val isCompleted = countLeft == 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) EmeraldSecondary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (countLeft > 0) countLeft--
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = thikr.text,
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            if (thikr.reward.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = thikr.reward,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { if (countLeft > 0) countLeft-- },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCompleted) Color.Gray else GoldAccent,
                        contentColor = EmeraldDarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isCompleted) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تم التكرار", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text("التكرار المتبقي: $countLeft من ${thikr.totalCount}", fontWeight = FontWeight.Bold)
                    }
                }

                TextButton(onClick = { countLeft = thikr.totalCount }) {
                    Text("إعادة", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun DigitalTasbeehTab() {
    val context = LocalContext.current
    var count by remember { mutableStateOf(0) }
    var targetGoal by remember { mutableStateOf(33) }
    var selectedZikr by remember { mutableStateOf("سُبْحَانَ اللَّهِ") }

    val zikrOptions = listOf(
        "سُبْحَانَ اللَّهِ",
        "الْحَمْدُ لِلَّهِ",
        "اللَّهُ أَكْبَرُ",
        "لا إِلَهَ إِلا اللَّهُ",
        "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ"
    )

    fun vibratePhone() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(40)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tasbeeh_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Zikr selector
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = selectedZikr,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                zikrOptions.take(3).forEach { zikr ->
                    FilterChip(
                        selected = selectedZikr == zikr,
                        onClick = {
                            selectedZikr = zikr
                            count = 0
                        },
                        label = { Text(zikr, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Giant Touch Ring
        Box(
            modifier = Modifier
                .size(220.dp)
                .clip(CircleShape)
                .background(EmeraldSecondary.copy(alpha = 0.3f))
                .border(4.dp, GoldAccent, CircleShape)
                .clickable {
                    count++
                    vibratePhone()
                }
                .testTag("tasbeeh_touch_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 54.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccent
                )
                Text(
                    text = "من $targetGoal",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "اضغط هنا للتسبيح",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldLight
                )
            }
        }

        // Target Selector & Reset
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetGoal == 33,
                        onClick = { targetGoal = 33 },
                        label = { Text("33") }
                    )
                    FilterChip(
                        selected = targetGoal == 100,
                        onClick = { targetGoal = 100 },
                        label = { Text("100") }
                    )
                    FilterChip(
                        selected = targetGoal == 1000,
                        onClick = { targetGoal = 1000 },
                        label = { Text("1000") }
                    )
                }

                IconButton(
                    onClick = { count = 0 },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "تصفير العداد", tint = GoldAccent)
                }
            }
        }
    }
}

val postPrayerAthkar = listOf(
    ThikrItem(1, "أَسْتَغْفِرُ اللَّهَ (ثَلاثاً)، اللَّهُمَّ أَنْتَ السَّلامُ وَمِنْكَ السَّلامُ، تَبَارَكْتَ يَا ذَا الجَلالِ وَالإِكْرَامِ.", 1, ""),
    ThikrItem(2, "سُبْحَانَ اللَّهِ", 33, "التسبيح بعد كل صلاة مكتوبة"),
    ThikrItem(3, "الْحَمْدُ لِلَّهِ", 33, "التحميد بعد الصلاة"),
    ThikrItem(4, "اللَّهُ أَكْبَرُ", 33, "التكبير بعد الصلاة"),
    ThikrItem(5, "لا إِلهَ إِلاَّ اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ، لَهُ المُلْكُ وَلَهُ الحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ.", 1, "تمام المائة لغفران الذنوب ولو كانت مثل زبد البحر")
)

val morningEveningAthkar = listOf(
    ThikrItem(1, "اللَّهُ لا إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لا تَأْخُذُهُ سِنَةٌ وَلا نَوْمٌ... (آية الكرسي)", 1, "حفظ من الجن حتى يمسي/يصبح"),
    ThikrItem(2, "قُلْ هُوَ اللَّهُ أَحَدٌ... وقُلْ أَعُوذُ بِرَبِّ الْفَلَقِ... وقُلْ أَعُوذُ بِرَبِّ النَّاسِ", 3, "تكفيه من كل شيء"),
    ThikrItem(3, "أَصْبَحْنَا وَأَصْبَحَ المُلْكُ لِلَّهِ، وَالحَمْدُ لِلَّهِ، لا إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لا شَرِيكَ لَهُ...", 1, "")
)
