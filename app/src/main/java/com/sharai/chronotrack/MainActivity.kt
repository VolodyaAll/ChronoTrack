package com.sharai.chronotrack

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sharai.chronotrack.data.model.Activity
import com.sharai.chronotrack.data.preferences.AppPreferences
import com.sharai.chronotrack.service.TimeTrackingService
import com.sharai.chronotrack.ui.navigation.Screen
import com.sharai.chronotrack.ui.screens.ActivitiesScreen
import com.sharai.chronotrack.ui.screens.CommentsScreen
import com.sharai.chronotrack.ui.screens.EditActivityScreen
import com.sharai.chronotrack.ui.screens.SettingsScreen
import com.sharai.chronotrack.ui.screens.StatisticsScreen
import com.sharai.chronotrack.ui.screens.TimeEntryDetailsScreen
import com.sharai.chronotrack.ui.theme.ChronoTrackTheme
import com.sharai.chronotrack.viewmodel.ActivityViewModel
import com.sharai.chronotrack.viewmodel.CommentViewModel
import com.sharai.chronotrack.viewmodel.SettingsViewModel
import com.sharai.chronotrack.viewmodel.StatisticsViewModel
import com.sharai.chronotrack.viewmodel.TimeEntryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val activityViewModel: ActivityViewModel by viewModels {
        ActivityViewModel.Factory(
            (application as ChronoTrackApp).activityRepository,
            (application as ChronoTrackApp).timeEntryRepository
        )
    }

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        StatisticsViewModel.Factory(
            (application as ChronoTrackApp).timeEntryRepository,
            (application as ChronoTrackApp).activityRepository
        )
    }

    private val commentViewModel: CommentViewModel by viewModels {
        CommentViewModel.Factory(
            (application as ChronoTrackApp).commentRepository
        )
    }

    private val timeEntryViewModel: TimeEntryViewModel by viewModels {
        TimeEntryViewModel.Factory(
            (application as ChronoTrackApp).timeEntryRepository
        )
    }

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(
            (application as ChronoTrackApp).appPreferences
        )
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startTimeTrackingService()
        }
    }

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences(AppPreferences.PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(AppPreferences.LANGUAGE_PREF_KEY, AppPreferences.SYSTEM_LANGUAGE)
            ?: AppPreferences.SYSTEM_LANGUAGE

        if (languageCode == AppPreferences.SYSTEM_LANGUAGE) {
            super.attachBaseContext(newBase)
            return
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        checkNotificationPermissionAndStartService()

        setContent {
            ChronoTrackTheme {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.Activities,
                    Screen.Statistics,
                    Screen.Comments,
                    Screen.Settings
                )

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route

                        val showBottomBar = !currentRoute.isNullOrEmpty() &&
                            !currentRoute.startsWith("edit") &&
                            !currentRoute.startsWith("time_entry")

                        if (showBottomBar) {
                            NavigationBar {
                                items.forEach { screen ->
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                when (screen) {
                                                    Screen.Activities -> Icons.Default.Timer
                                                    Screen.Statistics -> Icons.Default.DateRange
                                                    Screen.Settings -> Icons.Default.Settings
                                                    Screen.Comments -> Icons.Default.Comment
                                                    else -> Icons.Default.Timer
                                                },
                                                contentDescription = stringResource(screen.titleResId)
                                            )
                                        },
                                        label = { Text(stringResource(screen.titleResId)) },
                                        selected = currentRoute == screen.route,
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Activities.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Activities.route) {
                            ActivitiesScreen(
                                viewModel = activityViewModel,
                                onEditActivity = { activityId ->
                                    navController.navigate(Screen.EditActivity.createRoute(activityId))
                                },
                                onTimeEntryClick = { timeEntryId ->
                                    navController.navigate(Screen.TimeEntryDetails.createRoute(timeEntryId))
                                },
                                onCommentsClick = {
                                    navController.navigate(Screen.Comments.route)
                                }
                            )
                        }
                        composable(Screen.Statistics.route) {
                            StatisticsScreen(
                                viewModel = statisticsViewModel,
                                onTimeEntryClick = { timeEntryId ->
                                    navController.navigate(Screen.TimeEntryDetails.createRoute(timeEntryId))
                                }
                            )
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen()
                        }
                        composable(
                            route = "edit/{activityId}",
                            arguments = listOf(navArgument("activityId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val activityId = backStackEntry.arguments?.getLong("activityId") ?: -1L
                            val selectedActivity by activityViewModel.selectedActivity.collectAsState()
                            val activeActivities by activityViewModel.allActiveActivities.collectAsState(initial = emptyList())
                            val archivedActivities by activityViewModel.archivedActivities.collectAsState(initial = emptyList())
                            val scope = rememberCoroutineScope()

                            LaunchedEffect(activityId) {
                                if (activityId != -1L) {
                                    activityViewModel.loadActivity(activityId)
                                } else {
                                    activityViewModel.clearSelectedActivity()
                                }
                            }

                            EditActivityScreen(
                                activity = selectedActivity,
                                activeActivities = activeActivities,
                                archivedActivities = archivedActivities,
                                onSave = { name, color, icon ->
                                    scope.launch {
                                        if (activityId == -1L) {
                                            activityViewModel.insertActivity(
                                                Activity(name = name, color = color, icon = icon)
                                            )
                                        } else {
                                            selectedActivity?.let { activity ->
                                                activityViewModel.updateActivity(
                                                    activity.copy(name = name, color = color, icon = icon)
                                                )
                                            }
                                        }
                                        navController.popBackStack()
                                    }
                                },
                                onDelete = if (activityId != -1L) {
                                    {
                                        scope.launch {
                                            selectedActivity?.let { activityViewModel.archiveActivity(it.id) }
                                            navController.popBackStack()
                                        }
                                    }
                                } else null,
                                onPermanentDelete = if (activityId != -1L) {
                                    { id ->
                                        scope.launch {
                                            activityViewModel.deleteActivityWithTimeEntries(id)
                                            navController.popBackStack()
                                        }
                                    }
                                } else null,
                                onCancel = { navController.popBackStack() },
                                onRestoreActivity = { archivedActivityId ->
                                    scope.launch {
                                        activityViewModel.restoreActivity(archivedActivityId)
                                        navController.popBackStack()
                                    }
                                }
                            )
                        }
                        composable(
                            route = "time_entry/{timeEntryId}",
                            arguments = listOf(navArgument("timeEntryId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val timeEntryId = backStackEntry.arguments?.getLong("timeEntryId") ?: -1L

                            LaunchedEffect(timeEntryId) {
                                if (timeEntryId != -1L) {
                                    activityViewModel.loadTimeEntry(timeEntryId)
                                }
                            }

                            val timeEntry by activityViewModel.selectedTimeEntry.collectAsState()
                            val activity by activityViewModel.activityForSelectedTimeEntry.collectAsState()

                            if (timeEntry != null && activity != null) {
                                TimeEntryDetailsScreen(
                                    timeEntry = timeEntry!!,
                                    activity = activity!!,
                                    commentViewModel = commentViewModel,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }

                        composable(Screen.Comments.route) {
                            CommentsScreen(
                                onBack = { navController.popBackStack() },
                                onTimeEntryClick = { timeEntryId ->
                                    navController.navigate(Screen.TimeEntryDetails.createRoute(timeEntryId))
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startTimeTrackingService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            startTimeTrackingService()
        }
    }

    private fun startTimeTrackingService() {
        val serviceIntent = Intent(this, TimeTrackingService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
