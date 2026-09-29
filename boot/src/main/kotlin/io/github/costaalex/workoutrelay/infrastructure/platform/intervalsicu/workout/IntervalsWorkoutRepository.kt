package io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.workout

import java.time.LocalDate
import io.github.costaalex.workoutrelay.domain.ExternalData
import io.github.costaalex.workoutrelay.domain.Platform
import io.github.costaalex.workoutrelay.domain.librarycontainer.LibraryContainer
import io.github.costaalex.workoutrelay.domain.workout.Workout
import io.github.costaalex.workoutrelay.domain.workout.WorkoutDetails
import io.github.costaalex.workoutrelay.domain.workout.WorkoutRepository
import io.github.costaalex.workoutrelay.infrastructure.PlatformException
import io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.IntervalsApiClient
import io.github.costaalex.workoutrelay.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository

@Repository
class IntervalsWorkoutRepository(
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
) : WorkoutRepository {

    private val log = LoggerFactory.getLogger(this.javaClass)
    private val maxWorkoutsToSave = 10

    override fun platform() = Platform.INTERVALS

    override fun saveWorkoutsToCalendar(workouts: List<Workout>) {
        val converter = ToIntervalsWorkoutConverter()
        workouts.forEach {
            val request = converter.createEventRequestDTO(it)
            intervalsApiClient.createEvent(intervalsConfigurationRepository.getConfiguration().athleteId, request)
        }
    }

    override fun saveWorkoutsToLibrary(libraryContainer: LibraryContainer, workouts: List<Workout>) {
        val toIntervalsWorkoutConverter = ToIntervalsWorkoutConverter()
        for (fromIndex in workouts.indices step maxWorkoutsToSave) {
            val toIndex =
                if (fromIndex + maxWorkoutsToSave >= workouts.size) workouts.size else fromIndex + maxWorkoutsToSave

            val workoutsToSave = workouts.subList(fromIndex, toIndex)
            val requests =
                workoutsToSave.map { toIntervalsWorkoutConverter.createWorkoutRequestDTO(libraryContainer, it) }
            intervalsApiClient.createWorkouts(intervalsConfigurationRepository.getConfiguration().athleteId, requests)
        }
    }

    override fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate): List<Workout> {
        val configuration = intervalsConfigurationRepository.getConfiguration()
        val events = intervalsApiClient.getEvents(
            configuration.athleteId,
            startDate.toString(),
            endDate.toString(),
            configuration.powerRange,
            configuration.hrRange,
            configuration.paceRange,
        )
        return events
            .filter { it.isWorkout() }
            .mapNotNull { toWorkout(it) }
    }

    override fun getWorkoutFromLibrary(externalData: ExternalData): Workout {
        throw PlatformException(Platform.INTERVALS, "Intervals.icu workout library lookup is not supported")
    }

    override fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        throw PlatformException(Platform.INTERVALS, "Intervals.icu workout library search is not supported")
    }

    override fun getWorkoutsFromLibrary(libraryContainer: LibraryContainer): List<Workout> {
        throw PlatformException(Platform.INTERVALS, "Intervals.icu workout library listing is not supported")
    }

    override fun deleteWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate) {
        throw PlatformException(Platform.INTERVALS, "Intervals.icu calendar workout deletion is not supported")
    }

    private fun toWorkout(eventDTO: IntervalsEventDTO): Workout? {
        return try {
            FromIntervalsWorkoutConverter(eventDTO).toWorkout()
        } catch (e: PlatformException) {
            log.warn("Can't convert a workout ${eventDTO.name} on ${eventDTO.start_date_local}, skipping...", e)
            return null
        }
    }
}
