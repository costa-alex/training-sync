package io.github.costaalex.trainingsync.domain.workout

data class WorkoutCalendarResult(
    val workouts: List<Workout>,
    val associatedWorkouts: Int = 0,
)
