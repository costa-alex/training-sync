package io.github.costaalex.trainingsync.domain.workout.structure

class MultiStep(
    val name: String?,
    val repetitions: Int,
    val steps: List<SingleStep>
) : WorkoutStep {
    override fun isSingleStep() = false
}
