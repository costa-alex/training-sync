package io.github.costaalex.trainingsync.rest.configuration

import io.github.costaalex.trainingsync.domain.TrainingType

class TrainingTypeDTO(
    val title: String,
    val value: String
) {
    constructor(trainingType: TrainingType) : this(trainingType.title, trainingType.name)
}
