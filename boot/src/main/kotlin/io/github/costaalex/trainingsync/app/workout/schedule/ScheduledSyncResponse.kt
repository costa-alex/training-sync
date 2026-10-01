package io.github.costaalex.trainingsync.app.workout.schedule

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType

data class ScheduledSyncResponse(
    val id: Int,
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
    val startOffsetDays: Int,
    val endOffsetDays: Int
)
