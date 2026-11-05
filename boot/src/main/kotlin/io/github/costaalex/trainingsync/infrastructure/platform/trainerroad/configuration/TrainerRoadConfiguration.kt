package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.config.AppConfiguration

data class TrainerRoadConfiguration(
    val authCookie: String?,
    val removeHtmlTags: Boolean,
    val associateCompletedWorkouts: Boolean = true,
    val syncFtpToIntervals: Boolean = false,
) {
    companion object {
        private val authCookieKey = "${Platform.TRAINER_ROAD.key}.auth-cookie"
        private val removeHtmlTagsKey = "${Platform.TRAINER_ROAD.key}.remove-html-tags"
        private val associateCompletedWorkoutsKey =
            "${Platform.TRAINER_ROAD.key}.associate-completed-workouts"
        private val syncFtpToIntervalsKey =
            "${Platform.TRAINER_ROAD.key}.sync-ftp-to-intervals"
    }

    constructor(appConfiguration: AppConfiguration) : this(appConfiguration.configMap)

    constructor(map: Map<String, String?>) : this(
        map[authCookieKey],
        map[removeHtmlTagsKey].toBoolean(),
        map[associateCompletedWorkoutsKey]?.toBoolean() ?: true,
        map[syncFtpToIntervalsKey]?.toBoolean() ?: false,
    )

    fun canValidate(): Boolean {
        return authCookie != null
    }
}
