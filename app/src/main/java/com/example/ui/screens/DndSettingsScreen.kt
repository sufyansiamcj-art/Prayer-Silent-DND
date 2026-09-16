package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.service.AdhanAudioPlayer
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodel.MainUiState

@Composable
fun DndSettingsScreen(
    uiState: MainUiState,
    onUpdateDndMode: (String) -> Unit,
    onUpdateDndOffset: (Int) -> Unit,
    onUpdatePrayerDuration: (String, Int) -> Unit,
    onSelectReciter: (String) -> Unit,
    onTogglePlayAdhan: (Boolean) -> Unit = {},
    onTestAdhan: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = uiState.settings
    var isPlayingPreview by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isPlayingPreview) {
                AdhanAudioPlayer.stopPreview()
                isPlayingPreview = false
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: DND Silence Mode Type
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "نوع الوضع الصامت عند الصلاة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val modes = listOf(
                        Triple("DND", "عدم الإزعاج الكامل (DND)", "حجب الاتصالات والإشعارات والتنبيهات بالكامل"),
                        Triple("SILENT", "الوضع الصامت (Mute)", "كتم صوت الرنين والوسائط فقط"),
                        Triple("VIBRATE", "وضع الهزاز (Vibrate)", "تفعيل الاهتزاز بدلاً من الصوت")
                    )

                    modes.forEach { (modeKey, title, desc) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.dndMode == modeKey,
                                onClick = { onUpdateDndMode(modeKey) },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Start Offset Timing
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "توقيت بدء تفعيل وضع الصامت",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val offsets = listOf(
                        Pair(0, "فور رفع الأذان مباشرة (0 دقيقة)"),
                        Pair(2, "بعد الأذان بـ 2 دقيقة"),
                        Pair(5, "بعد الأذان بـ 5 دقائق (أثناء إقامة الصلاة)"),
                        Pair(10, "بعد الأذان بـ 10 دقائق"),
                        Pair(-1, "قبل الأذان بـ 1 دقيقة (تحسباً للوصول للمسجد)")
                    )

                    offsets.forEach { (offsetVal, label) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.dndTriggerOffsetMinutes == offsetVal,
                                onClick = { onUpdateDndOffset(offsetVal) },
                                colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Section 3: Duration per Prayer
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "مدة وضع الصامت لكل صلاة (بالدقائق):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    DurationStepperRow("صلاة الفجر", "FAJR", settings.fajrDurationMin) { onUpdatePrayerDuration("FAJR", it) }
                    DurationStepperRow("صلاة الظهر", "DHUHR", settings.dhuhrDurationMin) { onUpdatePrayerDuration("DHUHR", it) }
                    DurationStepperRow("صلاة العصر", "ASR", settings.asrDurationMin) { onUpdatePrayerDuration("ASR", it) }
                    DurationStepperRow("صلاة المغرب", "MAGHRIB", settings.maghribDurationMin) { onUpdatePrayerDuration("MAGHRIB", it) }
                    DurationStepperRow("صلاة العشاء", "ISHA", settings.ishaDurationMin) { onUpdatePrayerDuration("ISHA", it) }
                    DurationStepperRow("صلاة الجمعة (خاص بالجمعة)", "JUMUAH", settings.jumuahDurationMin) { onUpdatePrayerDuration("JUMUAH", it) }
                }
            }
        }

        // Section 4: Adhan Audio Toggle & Reciter Selector
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.playAdhanAudio) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    color = if (settings.playAdhanAudio) GoldAccent else MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_adhan_toggle")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = if (settings.playAdhanAudio) GoldAccent else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (settings.playAdhanAudio) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                contentDescription = null,
                                tint = if (settings.playAdhanAudio) EmeraldSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Text(
                                text = "تفعيل صوت الأذان التلقائي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (settings.playAdhanAudio)
                                    "مفعل: يؤذن تلقائياً بصوت الشيخ المختار عند دخول وقت الصلاة"
                                else
                                    "معطل: تحويل الهاتف للصامت فقط بدون رفع صوت الأذان",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = settings.playAdhanAudio,
                        onCheckedChange = onTogglePlayAdhan,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldSecondary,
                            checkedTrackColor = GoldAccent
                        ),
                        modifier = Modifier.testTag("switch_adhan_auto_play")
                    )
                }
            }
        }

        // Section 5: Reciter Selection & Preview
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "اختيار صوت المؤذن",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "استمع لمعاينة صوت كل قارئ قبل الاعتماد",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                if (isPlayingPreview) {
                                    AdhanAudioPlayer.stopPreview()
                                    isPlayingPreview = false
                                } else {
                                    isPlayingPreview = true
                                    AdhanAudioPlayer.playPreviewAdhanSound(settings.selectedReciter, context) {
                                        isPlayingPreview = false
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = GoldAccent)
                        ) {
                            Icon(
                                if (isPlayingPreview) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "معاينة الصوت",
                                tint = EmeraldSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AdhanAudioPlayer.RECITERS.forEach { reciter ->
                        val isSelected = settings.selectedReciter == reciter
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GoldAccent.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { onSelectReciter(reciter) },
                                    colors = RadioButtonDefaults.colors(selectedColor = GoldAccent)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = reciter,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (reciter.contains("صديق")) {
                                        Text(
                                            text = "صوت نقي مدمج يعمل بدون إنترنت",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GoldLight
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Test Real Adhan Alarm Button
                    OutlinedButton(
                        onClick = onTestAdhan,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_adhan_alarm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null)
                            Text("اختبار تفعيل الأذان ووضع الصامت الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DurationStepperRow(
    label: String,
    key: String,
    currentVal: Int,
    onValueChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { if (currentVal > 5) onValueChange(currentVal - 5) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }

            Text(
                text = "$currentVal د",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GoldAccent
            )

            IconButton(
                onClick = { if (currentVal < 120) onValueChange(currentVal + 5) },
                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    }
}
