package io.github.costaalex.trainingsync.app.activity

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType
import java.time.LocalDate

data class CopyActivitiesRequest(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val types: List<TrainingType>,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
)
