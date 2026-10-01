package io.github.costaalex.trainingsync.domain.workout.structure

import io.github.costaalex.trainingsync.utils.RampConverter

class SingleStep(
    val name: String?,
    val length: StepLength,
    val target: StepTarget,
    val cadence: StepTarget?,
    val ramp: Boolean,
    val intensity: StepIntensity? = null
) : WorkoutStep {

    override fun isSingleStep() = true

    fun convertRampToMultiStep(): MultiStep {
        if (!ramp) {
            throw IllegalStateException(
                "Step is not ramp step"
            )
        }

        return RampConverter(this)
            .toRampToMultiStep()
    }
}