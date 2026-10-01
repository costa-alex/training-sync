package io.github.costaalex.trainingsync.app.workout.schedule

class ScheduleAlreadyRunningException(
    val scheduleId: Int
) : IllegalStateException(
    "Scheduled sync $scheduleId is already running"
)