package io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.config.AppConfiguration
import io.github.costaalex.trainingsync.infrastructure.PlatformException

data class IntervalsConfiguration(
    val apiKey: String,
    val athleteId: String,
    val powerRange: Float,
    val hrRange: Float,
    val paceRange: Float,
    val syncWeightToTrainerRoad: Boolean = false,
) {
    companion object {
        private val apiKeyConfigKey = "${Platform.INTERVALS.key}.api-key"
        private val athleteIdConfigKey = "${Platform.INTERVALS.key}.athlete-id"
        private val powerRangeConfigKey = "${Platform.INTERVALS.key}.power-range"
        private val hrRangeConfigKey = "${Platform.INTERVALS.key}.hr-range"
        private val paceRangeConfigKey = "${Platform.INTERVALS.key}.pace-range"
        private val syncWeightToTrainerRoadConfigKey = "${Platform.INTERVALS.key}.sync-weight-to-trainer-road"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(
        map[apiKeyConfigKey]!!,
        map[athleteIdConfigKey]!!,
        map[powerRangeConfigKey]!!.toFloat(),
        map[hrRangeConfigKey]!!.toFloat(),
        map[paceRangeConfigKey]!!.toFloat(),
        map[syncWeightToTrainerRoadConfigKey].toBoolean(),
    ) {
        val wrongValues = map.entries
            .filter { it.value.isNullOrBlank() }
        if (wrongValues.isNotEmpty()) {
            val entriesString = wrongValues.joinToString(separator = ", ") { it.toString() }
            throw PlatformException(Platform.INTERVALS, "Wrong values: $entriesString")
        }
    }
}
