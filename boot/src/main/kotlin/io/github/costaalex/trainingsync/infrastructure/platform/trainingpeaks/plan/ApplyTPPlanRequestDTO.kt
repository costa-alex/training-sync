package io.github.costaalex.trainingsync.infrastructure.platform.trainingpeaks.plan

class ApplyTPPlanRequestDTO(
    val athleteId: String,
    val planId: String,
    val targetDate: String,
    val startType: String,
)
