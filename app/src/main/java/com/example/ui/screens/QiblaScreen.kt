package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.prayer.PrayerCalculator
import com.example.ui.theme.EmeraldDarkBackground
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.viewmodel.MainUiState
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(uiState: MainUiState) {
    val context = LocalContext.current
    val settings = uiState.settings

    val qiblaAngle = remember(settings.latitude, settings.longitude) {
        PrayerCalculator.calculateQiblaDirection(settings.latitude, settings.longitude)
    }

    var azimuth by remember { mutableStateOf(0f) }
    var manualSliderDegree by remember { mutableStateOf(0f) }
    var isSensorAvailable by remember { mutableStateOf(true) }

    // Android Hardware Sensor Listener
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientation = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    var azimuthDeg = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    if (azimuthDeg < 0) azimuthDeg += 360f
                    azimuth = azimuthDeg
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            isSensorAvailable = false
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val currentHeading = if (isSensorAvailable) azimuth else manualSliderDegree
    val angleDifference = abs((currentHeading - qiblaAngle + 540) % 360 - 180)
    val isFacingQibla = angleDifference < 10.0

    val animatedRotation by animateFloatAsState(
        targetValue = -currentHeading,
        animationSpec = spring(),
        label = "compassRotation"
    )

    val dialColor by animateColorAsState(
        targetValue = if (isFacingQibla) GoldAccent else EmeraldSecondary,
        label = "dialColor"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "اتجاه القبلة الشريفة",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${settings.cityName} (${qiblaAngle.roundToInt()}° من الشمال)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Qibla Compass Visual Dial
        Box(
            modifier = Modifier
                .size(280.dp)
                .testTag("qibla_compass_box"),
            contentAlignment = Alignment.Center
        ) {
            // Outer Ring
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(EmeraldSecondary.copy(alpha = 0.2f))
                    .border(3.dp, dialColor, CircleShape)
            )

            // Compass Dial Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(animatedRotation)
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2 - 16.dp.toPx()

                // Draw Degree Marks
                for (i in 0 until 360 step 15) {
                    val rad = Math.toRadians(i.toDouble())
                    val isMajor = i % 90 == 0
                    val markLength = if (isMajor) 14.dp.toPx() else 8.dp.toPx()
                    val start = Offset(
                        (center.x + (radius - markLength) * kotlin.math.sin(rad)).toFloat(),
                        (center.y - (radius - markLength) * kotlin.math.cos(rad)).toFloat()
                    )
                    val end = Offset(
                        (center.x + radius * kotlin.math.sin(rad)).toFloat(),
                        (center.y - radius * kotlin.math.cos(rad)).toFloat()
                    )
                    drawLine(
                        color = if (isMajor) GoldAccent else Color.Gray.copy(alpha = 0.5f),
                        start = start,
                        end = end,
                        strokeWidth = if (isMajor) 3.dp.toPx() else 1.5.dp.toPx()
                    )
                }

                // Draw Qibla Pointer Indicator Arrow
                val qiblaRad = Math.toRadians(qiblaAngle.toDouble())
                val arrowTip = Offset(
                    (center.x + (radius - 20.dp.toPx()) * kotlin.math.sin(qiblaRad)).toFloat(),
                    (center.y - (radius - 20.dp.toPx()) * kotlin.math.cos(qiblaRad)).toFloat()
                )

                drawCircle(
                    color = GoldAccent,
                    radius = 12.dp.toPx(),
                    center = arrowTip
                )
            }

            // Center Pointer Icon (Kaaba indicator)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Kaaba Direction",
                    tint = if (isFacingQibla) GoldAccent else MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(54.dp)
                        .rotate(qiblaAngle.toFloat() - currentHeading)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${qiblaAngle.roundToInt()}°",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Status Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFacingQibla) GoldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
            ),
            border = if (isFacingQibla) androidx.compose.foundation.BorderStroke(2.dp, GoldAccent) else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isFacingQibla) "أنت متجه نحو القبلة مباشرة! 🕋" else "درجة زاوية الكعبة المشرفة: ${qiblaAngle.roundToInt()}°",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isFacingQibla) GoldAccent else MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isFacingQibla)
                        "تم المحاذاة بنجاح مع الكعبة المشرفة بمكة المكرمة"
                    else
                        "قم بتدوير الهاتف حتى يتطابق المؤشر مع الكعبة المشرفة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Manual Dial Calibration Slider (If sensors absent or for manual adjustment)
        if (!isSensorAvailable) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "تدوير البوصلة يدوياً (${manualSliderDegree.roundToInt()}°)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = manualSliderDegree,
                    onValueChange = { manualSliderDegree = it },
                    valueRange = 0f..360f,
                    colors = SliderDefaults.colors(
                        thumbColor = GoldAccent,
                        activeTrackColor = GoldAccent
                    )
                )
            }
        }
    }
}
