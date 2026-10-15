package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad

import io.github.costaalex.trainingsync.domain.workout.Workout
import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.domain.TrainingType
import io.github.costaalex.trainingsync.domain.workout.WorkoutDetails
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

class TrainerRoadApiClientServiceTest {
    private val trainerRoadApiClient = mock<TrainerRoadApiClient>()
    private val trainerRoadWorkoutCacheService = mock<TrainerRoadWorkoutCacheService>()
    private val service = TrainerRoadApiClientService(
        trainerRoadApiClient,
        mock<TrainerRoadConfigurationRepository>(),
        trainerRoadWorkoutCacheService,
    )

    @Test
    fun `should request an exclusive end date for a multi-day timeline`() {
        val startDate = LocalDate.of(2026, 9, 1)
        val endDate = LocalDate.of(2026, 9, 3)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-04"))
            .thenReturn(TrainerRoadTimelineDTO())

        service.getWorkoutsFromCalendar(startDate, endDate, 42)

        verify(trainerRoadApiClient).getTimeline(42, "2026-09-01", "2026-09-04")
    }

    @Test
    fun `should reject an inverted timeline range`() {
        assertThrows<IllegalArgumentException> {
            service.getWorkoutsFromCalendar(
                LocalDate.of(2026, 9, 3),
                LocalDate.of(2026, 9, 1),
                42,
            )
        }

        verify(trainerRoadApiClient, never()).getTimeline(any(), any(), any())
    }

    @Test
    fun `should load planned workouts through the cache service and skip missing workout IDs`() {
        val date = LocalDate.of(2026, 9, 1)
        val cachedWorkout = mock<Workout>()
        val datedWorkout = mock<Workout>()
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(
                        plannedActivity("missing", date, null),
                        plannedActivity("available", date, 7),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7")).thenReturn(cachedWorkout)
        whenever(cachedWorkout.withDate(date)).thenReturn(datedWorkout)

        val workouts = service.getWorkoutsFromCalendar(date, date, 42)

        assertEquals(listOf(datedWorkout), workouts)
        verify(trainerRoadWorkoutCacheService).getWorkout("7")
    }

    @Test
    fun `should skip activity when its export ID is missing`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(11, date.atStartOfDay()),
                    ),
                ),
            )
        whenever(trainerRoadApiClient.getActivities(42, "11"))
            .thenReturn(listOf(activityWithoutExportId(date.atStartOfDay())))

        val activities = service.getActivities(42, date, date)

        assertEquals(emptyList<Any>(), activities)
        verify(trainerRoadApiClient, never()).exportFit(any())
    }

    @Test
    fun `should associate a uniquely matching completed workout`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(plannedActivity("planned-1", date, 7)),
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(439841525, date.atTime(18, 0)),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7"))
            .thenReturn(workout("Cramer"))
        whenever(trainerRoadApiClient.getActivities(42, "439841525"))
            .thenReturn(listOf(activity("439841525", "TrainingPeaks Virtual - Workout: Cramer", date)))

        val workouts = service.getWorkoutsFromCalendar(date, date, 42)

        assertEquals("Cramer", workouts.single().details.name)
        assertEquals(date, workouts.single().date)
        verify(trainerRoadApiClient).associateActivity("planned-1", "439841525", false)
    }

    @Test
    fun `should use activityId when the activity record ID differs from the timeline ID`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(plannedActivity("planned-1", date, 7)),
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(439841525, date.atTime(18, 0)),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7"))
            .thenReturn(workout("Cramer"))
        whenever(trainerRoadApiClient.getActivities(42, "439841525"))
            .thenReturn(
                listOf(
                    activity(
                        id = "activity-record-id",
                        activityId = 439841525,
                        name = "TrainingPeaks Virtual - Workout: Cramer",
                        date = date,
                    ),
                ),
            )

        service.getWorkoutsFromCalendar(date, date, 42)

        verify(trainerRoadApiClient).associateActivity("planned-1", "439841525", false)
    }

    @Test
    fun `should skip association when multiple activities match the same planned workout`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(plannedActivity("planned-1", date, 7)),
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(11, date.atTime(18, 0)),
                        TrainerRoadTimelineDTO.ActivityStateDTO(12, date.atTime(19, 0)),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7"))
            .thenReturn(workout("Cramer"))
        whenever(trainerRoadApiClient.getActivities(42, "11,12"))
            .thenReturn(
                listOf(
                    activity("11", "TrainingPeaks Virtual - Workout: Cramer", date),
                    activity("12", "TrainingPeaks Virtual - Workout: Cramer", date),
                ),
            )

        service.getWorkoutsFromCalendar(date, date, 42)

        verify(trainerRoadApiClient, never()).associateActivity(any(), any(), any())
    }

    @Test
    fun `should not associate when planned and completed workout names differ`() {
        val date = LocalDate.of(2026, 9, 1)
        whenever(trainerRoadApiClient.getTimeline(42, "2026-09-01", "2026-09-02"))
            .thenReturn(
                TrainerRoadTimelineDTO(
                    plannedActivities = listOf(plannedActivity("planned-1", date, 7)),
                    activities = listOf(
                        TrainerRoadTimelineDTO.ActivityStateDTO(11, date.atTime(18, 0)),
                    ),
                ),
            )
        whenever(trainerRoadWorkoutCacheService.getWorkout("7"))
            .thenReturn(workout("Cramer"))
        whenever(trainerRoadApiClient.getActivities(42, "11"))
            .thenReturn(listOf(activity("11", "TrainingPeaks Virtual - Workout: Other", date)))

        service.getWorkoutsFromCalendar(date, date, 42)

        verify(trainerRoadApiClient, never()).associateActivity(any(), any(), any())
    }

    private fun plannedActivity(
        id: String,
        date: LocalDate,
        workoutId: Long?,
    ) = TrainerRoadTimelineDTO.PlannedActivityDTO(
        id,
        TrainerRoadTimelineDTO.DateDTO(date.year, date.monthValue, date.dayOfMonth),
        workoutId,
    )

    private fun activityWithoutExportId(date: LocalDateTime) = TrainerRoadActivityDTO(
        Id = "activity-without-export-id",
        date = date,
        completedRide = null,
        activity = null,
        activityId = null,
        name = "Activity",
        isOutside = false,
        activityType = null,
    )

    private fun workout(name: String) = Workout(
        WorkoutDetails(
            type = TrainingType.BIKE,
            subType = TrainingType.VIRTUAL_BIKE,
            name = name,
            description = null,
            duration = Duration.ofMinutes(60),
            load = null,
            externalData = ExternalData.empty(),
        ),
        date = null,
        structure = null,
    )

    private fun activity(
        id: String,
        name: String,
        date: LocalDate,
        activityId: Long? = null,
    ) = TrainerRoadActivityDTO(
        Id = id,
        date = date.atTime(18, 0),
        completedRide = TrainerRoadActivityDTO.CompletedRideDTO(
            Name = name,
            Date = date.atTime(18, 0),
            IsOutside = false,
            Tss = 50,
            EstimatedDuration = 3600,
            Duration = 3600,
            Distance = 25.0,
            WorkoutRecordId = 100,
        ),
        activity = null,
        activityId = activityId,
        name = name,
        isOutside = false,
        activityType = null,
    )
}