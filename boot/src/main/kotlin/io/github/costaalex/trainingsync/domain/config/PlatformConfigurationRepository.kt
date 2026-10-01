package io.github.costaalex.trainingsync.domain.config

import io.github.costaalex.trainingsync.domain.Platform

interface PlatformConfigurationRepository {
    fun platform(): Platform

    fun updateConfig(request: UpdateConfigurationRequest)
}
