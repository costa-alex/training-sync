package io.github.costaalex.trainingsync.domain.config

import io.github.costaalex.trainingsync.domain.Platform

interface PlatformInfoRepository {
    fun platform(): Platform

    fun platformInfo(): PlatformInfo
}
