package io.github.costaalex.trainingsync

import io.github.costaalex.trainingsync.infrastructure.configuration.DefaultConfiguration
import io.github.costaalex.trainingsync.infrastructure.configuration.SchedulerProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling
import io.github.costaalex.trainingsync.infrastructure.configuration.SyncHistoryProperties

@SpringBootApplication
@EnableFeignClients
@EnableCaching
@EnableScheduling
@EnableConfigurationProperties(
    DefaultConfiguration::class,
    SchedulerProperties::class,
    SyncHistoryProperties::class
)
class TrainingSyncApplication

fun main(args: Array<String>) {
    runApplication<TrainingSyncApplication>(*args)
}
