package io.github.costaalex.trainingsync.infrastructure.configuration

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("app")
class DefaultConfiguration(
    val defaultConfig: Map<String, String>?,
)
