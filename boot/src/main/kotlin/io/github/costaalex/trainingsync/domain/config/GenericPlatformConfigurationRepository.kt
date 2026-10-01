package io.github.costaalex.trainingsync.domain.config

import io.github.costaalex.trainingsync.domain.Platform
import org.springframework.stereotype.Repository

@Repository
class GenericPlatformConfigurationRepository(
    private val appConfigurationRepository: AppConfigurationRepository,

) : PlatformConfigurationRepository {
    override fun platform() = Platform.GENERIC

    override fun updateConfig(request: UpdateConfigurationRequest) {
        appConfigurationRepository.updateConfig(UpdateConfigurationRequest(request.getByPrefix(Platform.GENERIC.key)))
    }
}
