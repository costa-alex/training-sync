package io.github.costaalex.trainingsync.app.workout.schedule

import io.github.costaalex.trainingsync.app.workout.CopyFromCalendarToCalendarRequest
import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType
import java.time.LocalDate

data class C2CTodayScheduledRequest(
    val types: List<TrainingType>,
    val skipSynced: Boolean,
    val sourcePlatform: Platform,
    val targetPlatform: Platform
) : Schedulable {
    fun forToday() = CopyFromCalendarToCalendarRequest(
        startDate = LocalDate.now(),
        endDate = LocalDate.now(),
        types = types,
        skipSynced = skipSynced,
        sourcePlatform = sourcePlatform,
        targetPlatform = targetPlatform,
        replaceChangedWorkouts =
            sourcePlatform == Platform.TRAINER_ROAD &&
                targetPlatform == Platform.TRAINING_PEAKS
    )
}