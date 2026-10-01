package io.github.costaalex.trainingsync.domain.workout.structure

import java.io.Serializable

interface WorkoutStep : Serializable {
    fun isSingleStep(): Boolean
}
