package io.github.costaalex.trainingsync.rest.workout

import io.github.costaalex.trainingsync.domain.ExternalData

class WorkoutDetailsDTO(
    val name: String,
    val duration: String?,
    val load: Int?,
    val externalData: ExternalData,
)
