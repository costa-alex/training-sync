package io.github.costaalex.trainingsync.infrastructure.platform.trainingpeaks.workout

import io.github.costaalex.trainingsync.domain.TrainingType
import io.github.costaalex.trainingsync.infrastructure.platform.trainingpeaks.workout.structure.TPWorkoutStructureDTO

abstract class TPBaseWorkoutResponseDTO(
    val id: String,
    val workoutTypeValueId: Int?,
    val workoutSubTypeValueId: Int?,
    val title: String?,
    val totalTimePlanned: Double?,
    val tssPlanned: Int?,
    val description: String?,
    val coachComments: String?,
    val structure: TPWorkoutStructureDTO?,
    val totalTime: Double? = null,
    val tssActual: Int? = null,
) {
    fun getWorkoutType(): TrainingType? = workoutTypeValueId?.let { TPTrainingTypeMapper.getByValue(it) }
    fun getWorkoutSubType(): TrainingType? = workoutSubTypeValueId?.let { TPTrainingTypeMapper.getSubtypeByValue(it) }

    fun isCompleted(): Boolean = (totalTime ?: 0.0) > 0.0 || (tssActual ?: 0) > 0
}
