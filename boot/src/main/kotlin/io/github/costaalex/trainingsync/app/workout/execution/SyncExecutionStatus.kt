package io.github.costaalex.trainingsync.app.workout.execution

enum class SyncExecutionStatus {
    RUNNING,
    SUCCESS,
    NO_CHANGES,
    PARTIAL_SUCCESS,
    FAILED,
    INTERRUPTED
}