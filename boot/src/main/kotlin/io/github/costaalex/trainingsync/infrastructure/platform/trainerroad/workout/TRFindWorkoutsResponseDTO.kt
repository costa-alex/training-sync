package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.workout

import com.fasterxml.jackson.annotation.JsonProperty

class TRFindWorkoutsResponseDTO(
    @JsonProperty("Workouts")
    val workouts: List<TrainerRoadWorkoutDetailsDTO>,
)
