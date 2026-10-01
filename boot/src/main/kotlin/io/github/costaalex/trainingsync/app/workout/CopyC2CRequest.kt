package io.github.costaalex.trainingsync.app.workout

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType
import java.time.LocalDate

data class CopyFromCalendarToCalendarRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val replaceChangedWorkouts: Boolean = false
)