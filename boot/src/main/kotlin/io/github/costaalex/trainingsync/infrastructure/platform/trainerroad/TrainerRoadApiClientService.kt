package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad

import feign.FeignException
import io.github.costaalex.trainingsync.domain.activity.Activity
import io.github.costaalex.trainingsync.domain.workout.WorkoutCalendarResult
import io.github.costaalex.trainingsync.domain.workout.Workout
import io.github.costaalex.trainingsync.domain.workout.WorkoutDetails
import io.github.costaalex.trainingsync.domain.workout.structure.SingleStep
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.activity.TrainerRoadActivityDTO
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.activity.TrainerRoadActivityMapper
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.workout.TrainerRoadWorkoutMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Locale

@Repository
class TrainerRoadApiClientService(
    private val trainerRoadApiClient: TrainerRoadApiClient,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
    private val trainerRoadWorkoutCacheService: TrainerRoadWorkoutCacheService,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun findWorkoutsFromLibraryByName(name: String): List<WorkoutDetails> {
        val removeHtmlTags = trainerRoadConfigurationRepository.getConfiguration().removeHtmlTags
        return trainerRoadApiClient.findWorkouts(TRFindWorkoutsRequestDTO(name, 0, 500)).workouts
            .map { TrainerRoadWorkoutMapper().toWorkoutDetails(it, removeHtmlTags) }
    }

    private fun getTimeline(
        memberId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): TrainerRoadTimelineDTO {
        require(!startDate.isAfter(endDate)) {
            "Start date cannot be after end date"
        }

        return trainerRoadApiClient.getTimeline(
            memberId,
            startDate.toString(),
            endDate.plusDays(1).toString(),
        )
    }

    fun getWorkoutsFromCalendar(startDate: LocalDate, endDate: LocalDate, memberId: Long): List<Workout> {
        return getWorkoutsFromCalendarWithAssociationResult(startDate, endDate, memberId).workouts
    }

    fun getWorkoutsFromCalendarWithAssociationResult(
        startDate: LocalDate,
        endDate: LocalDate,
        memberId: Long,
    ): WorkoutCalendarResult {
        val timeline = getTimeline(memberId, startDate, endDate)
        val plannedWorkouts = timeline
            .plannedActivities
            .filter { it.date.toLocalDate() in startDate..endDate }
            .mapNotNull { plannedActivity ->
                plannedActivity.workoutId
                    ?.let { getWorkout(it.toString()) }
                    ?.withDate(plannedActivity.date.toLocalDate())
                    ?.let { PlannedWorkout(plannedActivity.id, it) }
            }

        val associatedWorkouts = associateCompletedActivities(
            timeline,
            plannedWorkouts,
            memberId,
            startDate,
            endDate,
        )
        return WorkoutCalendarResult(
            workouts = plannedWorkouts.map { it.workout },
            associatedWorkouts = associatedWorkouts,
        )
    }

    fun getWorkout(trWorkoutId: String): Workout {
        return trainerRoadWorkoutCacheService.getWorkout(trWorkoutId)
            .also { workout ->
                log.debug(
                    "Mapped TrainerRoad workout {}, target preview: {}",
                    trWorkoutId,
                    targetPreview(workout),
                )
            }
    }

    fun getActivities(memberId: Long, startDate: LocalDate, endDate: LocalDate): List<Activity> {
        val timeline = getTimeline(memberId, startDate, endDate)
        val activities = getActivityDetails(timeline, memberId, startDate, endDate)
        val activityMapper = TrainerRoadActivityMapper()
        return activities.mapNotNull { mapToActivity(activityMapper, it) }
    }

    private fun getActivityDetails(
        timeline: TrainerRoadTimelineDTO,
        memberId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): List<TrainerRoadActivityDTO> {
        val activityIds = timeline.activities
            .filter {
                val startedDate = it.started?.toLocalDate()
                startedDate != null && startedDate in startDate..endDate
            }
            .map { it.id }

        if (activityIds.isEmpty()) {
            return emptyList()
        }

        return trainerRoadApiClient.getActivities(memberId, activityIds.joinToString(","))
            .filter { it.date.toLocalDate() in startDate..endDate }
    }

    private fun associateCompletedActivities(
        timeline: TrainerRoadTimelineDTO,
        plannedWorkouts: List<PlannedWorkout>,
        memberId: Long,
        startDate: LocalDate,
        endDate: LocalDate,
    ): Int {
        if (plannedWorkouts.isEmpty()) {
            log.debug(
                "Skipping TrainerRoad activity association because no planned workouts were found in the range",
            )
            return 0
        }

        val timelineActivityIds = timeline.activities
            .filter {
                val startedDate = it.started?.toLocalDate()
                startedDate != null && startedDate in startDate..endDate
            }
            .associateBy { it.id.toString() }
        if (timelineActivityIds.isEmpty()) {
            log.debug(
                "TrainerRoad association scan found no timeline activities in range. plannedWorkouts={}, " +
                    "timelineActivities={}",
                plannedWorkouts.size,
                timeline.activities.size,
            )
            return 0
        }

        val activities = try {
            getActivityDetails(timeline, memberId, startDate, endDate)
        } catch (exception: FeignException) {
            log.warn(
                "Unable to check TrainerRoad activities for workout associations (HTTP {})",
                exception.status(),
            )
            return 0
        }

        val activityKeys = mutableListOf<AssociationKey>()
        val plannedByKey = plannedWorkouts
            .mapNotNull { planned ->
                val name = normalizeWorkoutName(planned.workout.details.name) ?: return@mapNotNull null
                AssociationKey(planned.workout.date ?: return@mapNotNull null, name) to planned
            }
            .groupBy({ it.first }, { it.second })
        val activitiesByKey = activities
            .mapNotNull { activity ->
                val activityId = sequenceOf(
                    activity.Id,
                    activity.activityId?.toString(),
                    activity.activity?.id,
                ).filterNotNull()
                    .firstOrNull { it in timelineActivityIds }
                    ?: return@mapNotNull null
                val name = activity.completedRide?.Name ?: activity.name ?: return@mapNotNull null
                val workoutName = extractWorkoutName(name)?.let(::normalizeWorkoutName) ?: return@mapNotNull null
                val date = timelineActivityIds[activityId]?.started?.toLocalDate() ?: return@mapNotNull null
                val key = AssociationKey(date, workoutName)
                activityKeys += key
                key to activityId
            }
            .groupBy({ it.first }, { it.second })

        var associationCount = 0
        plannedByKey.forEach { (key, plannedMatches) ->
            val activityMatches = activitiesByKey[key].orEmpty()
            if (plannedMatches.size != 1 || activityMatches.size != 1) {
                if (plannedMatches.isNotEmpty() && activityMatches.isNotEmpty()) {
                    log.warn(
                        "Skipping ambiguous TrainerRoad workout association for {} on {} ({} planned, {} activities)",
                        key.workoutName,
                        key.date,
                        plannedMatches.size,
                        activityMatches.size,
                    )
                }
                return@forEach
            }

            val planned = plannedMatches.single()
            val activityId = activityMatches.single()
            try {
                trainerRoadApiClient.associateActivity(planned.plannedActivityId, activityId, false)
                associationCount++
                log.info(
                    "Associated TrainerRoad activity {} with planned activity {}",
                    activityId,
                    planned.plannedActivityId,
                )
            } catch (exception: FeignException) {
                log.warn(
                    "Failed to associate TrainerRoad activity {} with planned activity {} (HTTP {})",
                    activityId,
                    planned.plannedActivityId,
                    exception.status(),
                )
            }
        }

        log.debug(
            "TrainerRoad association scan completed. plannedWorkouts={}, timelineActivities={}, " +
                "activityDetails={}, namedActivityMatches={}, associations={}",
            plannedWorkouts.size,
            timelineActivityIds.size,
            activities.size,
            activityKeys.size,
            associationCount,
        )
        return associationCount
    }

    private fun extractWorkoutName(activityName: String): String? {
        val marker = "Workout:"
        val markerIndex = activityName.indexOf(marker, ignoreCase = true)
        if (markerIndex < 0) {
            return null
        }

        return activityName.substring(markerIndex + marker.length).trim().takeIf { it.isNotEmpty() }
    }

    private fun normalizeWorkoutName(workoutName: String): String? {
        return workoutName.trim()
            .takeIf { it.isNotEmpty() }
            ?.lowercase(Locale.ROOT)
    }

    private fun mapToActivity(activityMapper: TrainerRoadActivityMapper, it: TrainerRoadActivityDTO): Activity? {
        val activityId = it.completedRide?.WorkoutRecordId ?: it.activityId
        if (activityId == null) {
            log.warn("Skipping TrainerRoad activity {} because its export ID is missing", it.Id)
            return null
        }

        val resource = trainerRoadApiClient.exportFit(activityId.toString())
        return activityMapper.mapToActivity(it, resource)
    }

    private fun targetPreview(workout: Workout): String {
        return workout.structure?.steps
            ?.filterIsInstance<SingleStep>()
            ?.take(8)
            ?.joinToString { "${it.name}:${it.target.start}-${it.target.end}" }
            ?: "no structure"
    }

    private data class PlannedWorkout(
        val plannedActivityId: String,
        val workout: Workout,
    )

    private data class AssociationKey(
        val date: LocalDate,
        val workoutName: String,
    )
}