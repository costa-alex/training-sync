package io.github.costaalex.trainingsync.infrastructure

import io.github.costaalex.trainingsync.domain.Platform

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class CatchFeignException(
    val platform: Platform
)
