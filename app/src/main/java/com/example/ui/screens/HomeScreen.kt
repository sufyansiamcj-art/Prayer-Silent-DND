package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.prayer.PrayerTimeItem
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainUiState

@Composable
fun HomeScreen(
    uiState: MainUiState,
    onTogglePrayerDnd: (String, Boolean) -> Unit,
    onTogglePlayAdhan: (Boolean) -> Unit,
    onTestDnd30s: () -> Unit,
    onTestAdhanAlarm: () -> Unit,
    onStopAdhan: () -> Unit,
    onCancelActiveDnd: () -> Unit,
    onNavigateToLocation: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDetectDeviceLocation: () -> Unit = {},
    onTestIqamaCountdown: () -> Unit = {},
    onStopTestIqamaCountdown: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Active DND Notification Banner (If currently running)
        if (uiState.isDndActive) {
            item {
                ActiveDndBanner(
                    prayerName = uiState.activeDndPrayerName,
                    remainingSeconds = uiState.activeDndRemainingSeconds,
                    isAdhanPlaying = uiState.isAdhanPlaying,
                    onStopAdhan = onStopAdhan,
                    onCancel = onCancelActiveDnd
                )
            }
        }

        // Live Iqamah Countdown Banner (When prayer time enters or test mode is active)
        if (uiState.isPrayerTimeEntered && uiState.currentEnteredPrayer != null) {
            item {
                IqamahLiveBanner(
                    prayerName = uiState.currentEnteredPrayer.nameAr,
                    formattedRemainingTime = uiState.formattedTimeToIqama,
                    progress = uiState.iqamaProgress,
                    isTest = uiState.isTestIqamaActive,
                    onStopTest = onStopTestIqamaCountdown
                )
            }
        }

        // Hero Card: Mosque Image & Countdown
        item {
            HeroPrayerCard(
                uiState = uiState,
                onLocationClick = onNavigateToLocation
            )
        }

        // Quick Adhan Audio Switch & Reciter Control Card
        item {
            AdhanQuickControlCard(
                playAdhanAudio = uiState.settings.playAdhanAudio,
                selectedReciter = uiState.settings.selectedReciter,
                isAdhanPlaying = uiState.isAdhanPlaying,
                onTogglePlayAdhan = onTogglePlayAdhan,
                onTestAdhanAlarm = onTestAdhanAlarm,
                onStopAdhan = onStopAdhan,
                onNavigateToSettings = onNavigateToSettings
            )
        }

        // 30-second Test DND and Iqamah Test Controls
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = GoldAccent.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GoldAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "أدوات الاختبار السريع وتجربة التنبيه",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يمكنك اختبار وضع الصامت لمدة 30 ثانية أو تجربة شاشة عداد الإقامة التنازلي للتأكد من المظهر والعمل",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onTestDnd30s,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldAccent,
                                contentColor = EmeraldDarkBackground
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("test_dnd_button")
                        ) {
                            Icon(Icons.Default.VolumeOff, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("صامت 30ث", fontWeight = FontWeight.Bold)
                        }

                        if (uiState.isTestIqamaActive) {
                            Button(
                                onClick = onStopTestIqamaCountdown,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_iqama_button")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إيقاف الإقامة", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            FilledTonalButton(
                                onClick = onTestIqamaCountdown,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_iqama_button")
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تجربة الإقامة", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Section Title: Prayer Times & DND Toggles
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مواقيت الصلاة ووضع الصامت",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                TextButton(onClick = onNavigateToSettings) {
                    Text("إعدادات المدة", color = GoldAccent, fontWeight = FontWeight.Bold)
                }
            }
        }

        // List of Prayers
        uiState.schedule?.allPrayers?.let { prayers ->
            items(prayers) { prayer ->
                PrayerItemRow(
                    prayer = prayer,
                    isNext = prayer.key == uiState.nextPrayer?.key,
                    isAdhanAudioEnabled = uiState.settings.playAdhanAudio,
                    onToggleDnd = { enabled -> onTogglePrayerDnd(prayer.key, enabled) }
                )
            }
        }
    }
}

@Composable
fun HeroPrayerCard(
    uiState: MainUiState,
    onLocationClick: () -> Unit
) {
    val schedule = uiState.schedule
    val nextPrayer = uiState.nextPrayer
    val settings = uiState.settings

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .testTag("hero_prayer_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Hero Background Image
            Image(
                painter = painterResource(id = R.drawable.img_mosque_hero_1786566774821),
                contentDescription = "Mosque Hero",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Dark Emerald Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                EmeraldDarkBackground.copy(alpha = 0.92f)
                            )
                        )
                    )
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Location & Dates
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.15f))
                            .clickable { onLocationClick() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${settings.cityName}، ${settings.countryName}",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = schedule?.hijriDateString ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = GoldLight,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Middle: Next Prayer Title & Countdown OR Iqamah Countdown
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isPrayerTimeEntered && uiState.currentEnteredPrayer != null) {
                        Text(
                            text = "🕌 حان الآن وقت صلاة ${uiState.currentEnteredPrayer.nameAr}",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldAccent,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = uiState.formattedTimeToIqama,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 42.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = GoldLight
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "الوقت المتبقي لإقامة الصلاة",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.iqamaProgress },
                            color = GoldAccent,
                            trackColor = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier
                                .width(180.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    } else {
                        Text(
                            text = if (nextPrayer != null) "الصلاة القادمة: ${nextPrayer.nameAr}" else "صلاتي صامتة",
                            style = MaterialTheme.typography.titleMedium,
                            color = GoldLight,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.timeToNextPrayerString,
                            style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "الموعد: ${nextPrayer?.formattedTime ?: "--:--"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Footer: DND Mode status indicator
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSecondary.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = GoldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        val modeText = when (settings.dndMode) {
                            "DND" -> "وضع عدم الإزعاج الكامل (DND)"
                            "SILENT" -> "الوضع الصامت (Mute)"
                            else -> "وضع الهزاز (Vibrate)"
                        }
                        Text(
                            text = modeText,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    val offsetText = when (settings.dndTriggerOffsetMinutes) {
                        0 -> "مع الأذان"
                        -1 -> "قبل الأذان بـ 1 د"
                        else -> "بعد الأذان بـ ${settings.dndTriggerOffsetMinutes} د"
                    }
                    Text(
                        text = offsetText,
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AdhanQuickControlCard(
    playAdhanAudio: Boolean,
    selectedReciter: String,
    isAdhanPlaying: Boolean,
    onTogglePlayAdhan: (Boolean) -> Unit,
    onTestAdhanAlarm: () -> Unit,
    onStopAdhan: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (playAdhanAudio) GoldAccent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (playAdhanAudio) GoldAccent else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_adhan_quick_control")
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (playAdhanAudio) GoldAccent else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (playAdhanAudio) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (playAdhanAudio) EmeraldSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "صوت الأذان التلقائي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Surface(
                                color = if (playAdhanAudio) EmeraldSecondary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (playAdhanAudio) "مفعّل" else "معطّل",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (playAdhanAudio) GoldLight else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Text(
                            text = if (playAdhanAudio) "يؤذن تلقائياً بصوت نقي عند دخول كل صلاة" else "كتم الهاتف فقط دون تشغيل صوت الأذان",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = playAdhanAudio,
                    onCheckedChange = onTogglePlayAdhan,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = EmeraldSecondary,
                        checkedTrackColor = GoldAccent
                    ),
                    modifier = Modifier.testTag("switch_quick_adhan")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reciter info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = selectedReciter,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                TextButton(
                    onClick = onNavigateToSettings,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "تغيير القارئ",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Live Adhan Test or Stop Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (isAdhanPlaying) {
                    Button(
                        onClick = onStopAdhan,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("stop_adhan_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("إيقاف صوت الأذان الآن", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = onTestAdhanAlarm,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldAccent),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_adhan_alarm_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("اختبار صوت الأذان", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveDndBanner(
    prayerName: String,
    remainingSeconds: Long,
    isAdhanPlaying: Boolean,
    onStopAdhan: () -> Unit,
    onCancel: () -> Unit
) {
    val mins = remainingSeconds / 60
    val secs = remainingSeconds % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isAdhanPlaying) EmeraldSecondary else MaterialTheme.colorScheme.errorContainer
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_dnd_banner")
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isAdhanPlaying) GoldAccent else MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdhanPlaying) Icons.Default.Campaign else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = if (isAdhanPlaying) EmeraldDarkBackground else Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isAdhanPlaying) "يرفع الأذان الآن ($prayerName)" else "وضع الصامت مفعل الآن ($prayerName)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAdhanPlaying) Color.White else MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            text = if (isAdhanPlaying) "صوت الأذان يعمل • متبقي $formattedTime لوضع الصامت" else "متبقي $formattedTime لإعادة الهاتف طبيعيًا",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isAdhanPlaying) GoldLight else MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                IconButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isAdhanPlaying) Color.Black.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "إلغاء الصامت",
                        tint = Color.White
                    )
                }
            }

            if (isAdhanPlaying) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onStopAdhan,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = EmeraldDarkBackground),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("إيقاف صوت الأذان (مع بقاء وضع الصامت)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerItemRow(
    prayer: PrayerTimeItem,
    isNext: Boolean,
    isAdhanAudioEnabled: Boolean,
    onToggleDnd: (Boolean) -> Unit
) {
    val isSunrise = prayer.key == "SUNRISE"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNext) EmeraldSecondary.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isNext) androidx.compose.foundation.BorderStroke(1.5.dp, GoldAccent) else null,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_row_${prayer.key}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (prayer.key) {
                        "FAJR" -> Icons.Default.NightsStay
                        "SUNRISE" -> Icons.Default.WbSunny
                        "DHUHR" -> Icons.Default.WbSunny
                        "ASR" -> Icons.Default.WbTwilight
                        "MAGHRIB" -> Icons.Default.NightsStay
                        else -> Icons.Default.Bedtime
                    },
                    contentDescription = null,
                    tint = if (isNext) GoldAccent else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = prayer.nameAr,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isNext) {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "القادمة",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldDarkBackground,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (!isSunrise) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "مدة الصامت: ${prayer.durationMinutes} دقيقة",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (prayer.isDndEnabled) {
                                Text(
                                    text = if (isAdhanAudioEnabled) "• أذان + صامت" else "• صامت فقط",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAdhanAudioEnabled) GoldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "وقت الإشراق",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = prayer.formattedTime,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!isSunrise) {
                    Switch(
                        checked = prayer.isDndEnabled,
                        onCheckedChange = onToggleDnd,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = EmeraldDarkBackground,
                            checkedTrackColor = GoldAccent
                        ),
                        modifier = Modifier.testTag("switch_dnd_${prayer.key}")
                    )
                }
            }
        }
    }
}

@Composable
fun IqamahLiveBanner(
    prayerName: String,
    formattedRemainingTime: String,
    progress: Float,
    isTest: Boolean,
    onStopTest: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iqama_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = EmeraldDarkBackground),
        border = androidx.compose.foundation.BorderStroke(2.dp, GoldAccent.copy(alpha = pulseAlpha)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("iqamah_countdown_banner")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GoldAccent.copy(alpha = 0.2f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = GoldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "حان الآن وقت صلاة $prayerName",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "الوقت المتبقي لإقامة الصلاة",
                            style = MaterialTheme.typography.bodySmall,
                            color = GoldLight
                        )
                    }
                }

                if (isTest) {
                    OutlinedButton(
                        onClick = onStopTest,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("stop_test_iqama_button")
                    ) {
                        Text("إنهاء التجربة", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Massive countdown display
            Text(
                text = formattedRemainingTime,
                style = MaterialTheme.typography.displaySmall.copy(fontSize = 46.sp, fontWeight = FontWeight.Black),
                color = GoldAccent,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                color = GoldAccent,
                trackColor = Color.White.copy(alpha = 0.2f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الأذان",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = "يتم التحويل للصامت لحين انقضاء الصلاة",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
                Text(
                    text = "الإقامة",
                    style = MaterialTheme.typography.labelSmall,
                    color = GoldAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
