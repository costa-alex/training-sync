package io.github.costaalex.trainingsync.app.plan

import io.github.costaalex.trainingsync.domain.ExternalData

data class CopyPlanResponse(
    val planName: String,
    val workouts: Int,
    val externalData: ExternalData,
)
