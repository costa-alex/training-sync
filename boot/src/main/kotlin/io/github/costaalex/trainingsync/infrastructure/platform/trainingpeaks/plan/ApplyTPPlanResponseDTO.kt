package io.github.costaalex.trainingsync.infrastructure.platform.trainingpeaks.plan

import java.time.LocalDateTime

class ApplyTPPlanResponseDTO(
    val appliedPlanId: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
)
