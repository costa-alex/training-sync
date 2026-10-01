package io.github.costaalex.trainingsync.app.workout

import java.time.LocalDate

data class WorkoutSyncFailure(
    val workoutName: String,
    val workoutDate: LocalDate?,
    val message: String
)