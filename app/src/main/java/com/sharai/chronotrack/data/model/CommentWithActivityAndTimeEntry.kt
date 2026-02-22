package com.sharai.chronotrack.data.model

/**
 * Класс, объединяющий комментарий с соответствующей активностью и записью времени
 */
data class CommentWithActivityAndTimeEntry(
    val comment: Comment,
    val timeEntry: TimeEntry,
    val activity: Activity
) 