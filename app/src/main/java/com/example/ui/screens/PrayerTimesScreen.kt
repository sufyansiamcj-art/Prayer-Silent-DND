package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.prayer.PrayerCalculator
import com.example.data.prayer.PrayerTimeItem
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodel.MainUiState

@Composable
fun PrayerTimesScreen(
    uiState: MainUiState,
    onAdjustOffset: (String, Int) -> Unit
) {
    val schedule = uiState.schedule
    val settings = uiState.settings

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldSecondary.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dates_banner_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = GoldAccent)
                            Text(
                                text = "مواقيت الصلاة اليوم",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "طريقة الحساب: ${PrayerCalculator.CALCULATION_METHODS.getOrElse(settings.calcMethodIndex) { "أم القرى" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = schedule?.hijriDateString ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldAccent
                        )
                        Text(
                            text = schedule?.gregorianDateString ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "التعديل اليدوي للمواقيت (بالدقائق):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        schedule?.allPrayers?.let { prayers ->
            items(prayers) { prayer ->
                PrayerAdjustRow(
                    prayer = prayer,
                    offsetMin = getManualOffset(settings, prayer.key),
                    onAdjust = { delta -> onAdjustOffset(prayer.key, delta) }
                )
            }
        }
    }
}

private fun getManualOffset(settings: com.example.data.local.PrayerSettings, key: String): Int {
    return when (key) {
        "FAJR" -> settings.fajrManualOffset
        "SUNRISE" -> settings.sunriseManualOffset
        "DHUHR" -> settings.dhuhrManualOffset
        "ASR" -> settings.asrManualOffset
        "MAGHRIB" -> settings.maghribManualOffset
        "ISHA" -> settings.ishaManualOffset
        else -> 0
    }
}

@Composable
fun PrayerAdjustRow(
    prayer: PrayerTimeItem,
    offsetMin: Int,
    onAdjust: (Int) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = prayer.nameAr,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = prayer.formattedTime,
                    style = MaterialTheme.typography.bodyLarge,
                    color = GoldAccent,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onAdjust(-1) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "إنقاص دقيقة")
                }

                Text(
                    text = if (offsetMin >= 0) "+$offsetMin د" else "$offsetMin د",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { onAdjust(+1) },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "زيادة دقيقة")
                }
            }
        }
    }
}
