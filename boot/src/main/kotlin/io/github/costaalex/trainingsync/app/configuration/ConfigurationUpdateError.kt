package io.github.costaalex.trainingsync.app.configuration

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.infrastructure.PlatformErrorCode

data class ConfigurationUpdateError(
    val platform: Platform,
    val code: PlatformErrorCode,
    val message: String,
)