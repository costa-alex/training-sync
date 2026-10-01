package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad

import feign.RequestInterceptor
import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.infrastructure.PlatformException
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.cookie.TRCookieRepository
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders

class TrainerRoadApiClientConfig(
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository

) {
    @Bean
    fun requestInterceptor(): RequestInterceptor {
        return RequestInterceptor { template ->
            val cookie = trainerRoadConfigurationRepository.getConfiguration().authCookie
                ?: throw PlatformException(Platform.TRAINER_ROAD, "Access to the platform is not configured")
            template.header(HttpHeaders.COOKIE, cookie)
        }
    }
}
