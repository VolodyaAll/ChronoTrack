package com.sharai.chronotrack.data.model

import androidx.room.Embedded

data class CommentWithActivityAndTimeEntry(
    @Embedded val comment: Comment,
    @Embedded(prefix = "te_") val timeEntry: TimeEntry,
    @Embedded(prefix = "a_") val activity: Activity
)
