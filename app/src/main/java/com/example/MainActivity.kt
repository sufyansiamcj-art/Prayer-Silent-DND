package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.PrayerSilentTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PrayerSilentTheme {
                val uiState by viewModel.uiState.collectAsState()
                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = GoldAccent,
                            modifier = Modifier
                                .testTag("bottom_navigation_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            Screen.bottomNavScreens.forEach { screen ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        if (currentRoute != screen.route) {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) screen.iconFilled else screen.iconOutlined,
                                            contentDescription = screen.titleAr
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = screen.titleAr,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                        selectedTextColor = GoldAccent,
                                        indicatorColor = GoldAccent
                                    ),
                                    modifier = Modifier.testTag("nav_item_${screen.route}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            HomeScreen(
                                uiState = uiState,
                                onTogglePrayerDnd = { key, enabled -> viewModel.togglePrayerDnd(key, enabled) },
                                onTogglePlayAdhan = { enabled -> viewModel.toggleAdhanAudio(enabled) },
                                onTestDnd30s = { viewModel.testDnd30Seconds() },
                                onTestAdhanAlarm = { viewModel.testAdhanAlarm() },
                                onStopAdhan = { viewModel.stopAdhanAudio() },
                                onCancelActiveDnd = { viewModel.cancelActiveDnd() },
                                onNavigateToLocation = { navController.navigate(Screen.LocationSettings.route) },
                                onNavigateToSettings = { navController.navigate(Screen.DndSettings.route) },
                                onDetectDeviceLocation = { viewModel.detectDeviceLocation() },
                                onTestIqamaCountdown = { viewModel.testIqamaCountdown() },
                                onStopTestIqamaCountdown = { viewModel.stopTestIqamaCountdown() }
                            )
                        }

                        composable(Screen.Qibla.route) {
                            QiblaScreen(uiState = uiState)
                        }

                        composable(Screen.PrayerTimes.route) {
                            PrayerTimesScreen(
                                uiState = uiState,
                                onAdjustOffset = { key, delta -> viewModel.adjustPrayerOffset(key, delta) }
                            )
                        }

                        composable(Screen.Athkar.route) {
                            AthkarScreen()
                        }

                        composable(Screen.DndSettings.route) {
                            DndSettingsScreen(
                                uiState = uiState,
                                onUpdateDndMode = { mode -> viewModel.updateDndMode(mode) },
                                onUpdateDndOffset = { offset -> viewModel.updateDndOffset(offset) },
                                onUpdatePrayerDuration = { key, duration -> viewModel.updatePrayerDuration(key, duration) },
                                onSelectReciter = { reciter -> viewModel.setReciter(reciter) },
                                onTogglePlayAdhan = { enabled -> viewModel.toggleAdhanAudio(enabled) },
                                onTestAdhan = { viewModel.testAdhanAlarm() }
                            )
                        }

                        composable(Screen.LocationSettings.route) {
                            LocationSettingsScreen(
                                uiState = uiState,
                                onSelectCity = { city -> viewModel.selectCity(city) },
                                onSetCalcMethod = { index -> viewModel.setCalculationMethod(index) },
                                onSetJuristicMethod = { index -> viewModel.setJuristicMethod(index) },
                                onDetectDeviceLocation = { viewModel.detectDeviceLocation() }
                            )
                        }

                        composable(Screen.History.route) {
                            HistoryScreen(
                                uiState = uiState,
                                onClearHistory = { viewModel.clearHistory() }
                            )
                        }

                        composable(Screen.About.route) {
                            AboutScreen()
                        }
                    }
                }
            }
        }
    }
}
