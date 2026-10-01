package io.github.costaalex.trainingsync.rest.workout

import java.time.LocalDate
import io.github.costaalex.trainingsync.domain.Platform

class DeleteWorkoutRequestDTO(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val platform: Platform,
)
