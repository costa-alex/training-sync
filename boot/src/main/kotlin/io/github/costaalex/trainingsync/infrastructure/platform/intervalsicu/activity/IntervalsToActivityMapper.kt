package io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.activity

import io.github.costaalex.trainingsync.domain.activity.Activity
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.IntervalsActivityDTO

class IntervalsToActivityMapper(
    private val eventDTO: IntervalsActivityDTO
) {
    fun mapToActivity(): Activity {
        return Activity(
            eventDTO.start_date_local,
            eventDTO.mapType(),
            eventDTO.name,
            null
        )
    }
}
