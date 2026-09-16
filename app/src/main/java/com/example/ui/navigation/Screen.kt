package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleAr: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector
) {
    object Home : Screen("home", "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home)
    object Qibla : Screen("qibla", "القبلة", Icons.Filled.Explore, Icons.Outlined.Explore)
    object PrayerTimes : Screen("prayer_times", "المواقيت", Icons.Filled.AccessTime, Icons.Outlined.AccessTime)
    object Athkar : Screen("athkar", "الأذكار", Icons.Filled.MenuBook, Icons.Outlined.MenuBook)
    object DndSettings : Screen("dnd_settings", "إعدادات الصامت", Icons.Filled.DoNotDisturbOn, Icons.Outlined.DoNotDisturbOn)
    object LocationSettings : Screen("location", "الموقع", Icons.Filled.LocationOn, Icons.Outlined.LocationOn)
    object History : Screen("history", "السجل", Icons.Filled.History, Icons.Outlined.History)
    object About : Screen("about", "عن التطبيق", Icons.Filled.Info, Icons.Outlined.Info)

    companion object {
        val bottomNavScreens = listOf(Home, Qibla, PrayerTimes, Athkar, DndSettings, LocationSettings, History, About)
    }
}
