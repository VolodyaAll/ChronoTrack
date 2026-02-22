package com.sharai.chronotrack.ui.navigation

import com.sharai.chronotrack.R

sealed class Screen(val route: String, val titleResId: Int) {
    object Activities : Screen("activities", R.string.activities)
    object Statistics : Screen("statistics", R.string.statistics)
    object Settings : Screen("settings", R.string.settings)
    object EditActivity : Screen("edit/{activityId}", R.string.edit_activity) {
        fun createRoute(activityId: Long) = "edit/$activityId"
    }
    object TimeEntryDetails : Screen("time_entry/{timeEntryId}", R.string.activity_details) {
        fun createRoute(timeEntryId: Long) = "time_entry/$timeEntryId"
    }
    object Comments : Screen("comments", R.string.comments)
}