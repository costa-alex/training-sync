package io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.workout

import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.domain.TrainingType
import io.github.costaalex.trainingsync.domain.librarycontainer.LibraryContainer
import io.github.costaalex.trainingsync.domain.workout.Workout
import io.github.costaalex.trainingsync.domain.workout.WorkoutDetails
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ToIntervalsWorkoutConverterTest {
    private val converter = ToIntervalsWorkoutConverter()

    @Test
    fun `maps an indoor virtual bike workout to VirtualRide`() {
        val workout = workout(type = TrainingType.BIKE, subType = TrainingType.VIRTUAL_BIKE)

        val request = converter.createEventRequestDTO(workout)

        assertThat(request.type).isEqualTo("VirtualRide")
    }

    @Test
    fun `maps an outdoor bike workout to Ride`() {
        val workout = workout(type = TrainingType.BIKE, subType = TrainingType.BIKE)

        val request = converter.createEventRequestDTO(workout)

        assertThat(request.type).isEqualTo("Ride")
    }

    @Test
    fun `maps a mountain bike workout to MountainBikeRide`() {
        val workout = workout(type = TrainingType.MTB, subType = TrainingType.MTB)

        val request = converter.createEventRequestDTO(workout)

        assertThat(request.type).isEqualTo("MountainBikeRide")
    }

    @Test
    fun `maps a run workout to Run regardless of subtype`() {
        val workout = workout(type = TrainingType.RUN, subType = TrainingType.RUN)

        val request = converter.createEventRequestDTO(workout)

        assertThat(request.type).isEqualTo("Run")
    }

    @Test
    fun `maps an indoor virtual bike workout to VirtualRide when saving to a library`() {
        val workout = workout(type = TrainingType.BIKE, subType = TrainingType.VIRTUAL_BIKE)
        val libraryContainer = LibraryContainer(
            name = "My Library",
            startDate = LocalDate.of(2026, 9, 10),
            isPlan = false,
            workoutsAmount = 0,
            externalData = ExternalData.empty().withIntervals("42"),
        )

        val request = converter.createWorkoutRequestDTO(libraryContainer, workout)

        assertThat(request.type).isEqualTo("VirtualRide")
    }

    private fun workout(
        type: TrainingType,
        subType: TrainingType,
    ) = Workout(
        details = WorkoutDetails(
            type = type,
            subType = subType,
            name = "Workout",
            description = null,
            duration = null,
            load = null,
            externalData = ExternalData.empty(),
        ),
        date = LocalDate.of(2026, 9, 10),
        structure = null,
    )
}
