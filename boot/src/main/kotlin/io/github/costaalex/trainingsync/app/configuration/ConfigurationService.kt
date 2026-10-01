package io.github.costaalex.trainingsync.app.configuration

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.config.AppConfiguration
import io.github.costaalex.trainingsync.domain.config.AppConfigurationRepository
import io.github.costaalex.trainingsync.domain.config.DebugModeService
import io.github.costaalex.trainingsync.domain.config.PlatformConfigurationRepository
import io.github.costaalex.trainingsync.domain.config.PlatformInfo
import io.github.costaalex.trainingsync.domain.config.PlatformInfoRepository
import io.github.costaalex.trainingsync.domain.config.UpdateConfigurationRequest
import io.github.costaalex.trainingsync.infrastructure.PlatformException
import io.github.costaalex.trainingsync.infrastructure.PlatformErrorCode
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.cache.CacheManager

@Service
class ConfigurationService(
    private val platformConfigurationRepositories: List<PlatformConfigurationRepository>,
    platformInfoRepositories: List<PlatformInfoRepository>,
    private val appConfigurationRepository: AppConfigurationRepository,
    private val debugModeService: DebugModeService,
    private val cacheManager: CacheManager,
) {
    companion object {
        private const val MASKED_VALUE = "********"
        private const val PLATFORM_INFO_CACHE_NAME = "platformInfoCache"

        private val SENSITIVE_CONFIGURATION_KEYS =
            setOf(
                "intervals.api-key",
                "training-peaks.auth-cookie",
                "trainer-road.auth-cookie",
            )
    }
    
    private val platformInfoRepositoryMap = platformInfoRepositories.associateBy { it.platform() }
    private val log = LoggerFactory.getLogger(this.javaClass)
    
    fun getConfiguration(key: String): String? = appConfigurationRepository.getConfiguration(key)

    fun getConfigurations(): AppConfiguration = appConfigurationRepository.getConfigurations()
    
    fun getConfigurationsForDisplay(): AppConfiguration {
        val configuration =
            appConfigurationRepository.getConfigurations()

        val maskedConfig =
            configuration.configMap.mapValues {
                (key, value) ->

                if (
                    key in SENSITIVE_CONFIGURATION_KEYS &&
                    value.isNotBlank()
                ) {
                    MASKED_VALUE
                } else {
                    value
                }
            }

        return AppConfiguration(maskedConfig)
    }

    fun updateConfiguration(
        request: UpdateConfigurationRequest,
    ): List<ConfigurationUpdateError> {
        val sanitizedRequest =
            removeMaskedSensitiveValues(request)

        val errors =
            platformConfigurationRepositories.mapNotNull {
                updateConfiguration(
                    sanitizedRequest,
                    it,
                )
            }

        handleDebugModeIfNecessary(
            sanitizedRequest
        )

        return errors
    }
    
    private fun removeMaskedSensitiveValues(
        request: UpdateConfigurationRequest,
    ): UpdateConfigurationRequest {
        val sanitizedConfig =
            request.configMap.filterNot {
                (key, value) ->

                key in SENSITIVE_CONFIGURATION_KEYS &&
                    value == MASKED_VALUE
            }

        return UpdateConfigurationRequest(
            sanitizedConfig
        )
    }

   fun platformInfo(): Map<Platform, PlatformInfo> =
    platformInfoRepositoryMap.entries.associate { entry ->
        val platform = entry.key
        val repository = entry.value

        val info = try {
            repository.platformInfo()
        } catch (exception: Exception) {
            log.warn("Unable to validate connection to {}", platform.title, exception)

            PlatformInfo(mapOf("isValid" to false))
        }

        platform to info
    }

    fun platformInfo(
        platform: Platform,
    ): PlatformInfo {
        val repository = getPlatformInfoRepository(platform)
        return try {
            repository.platformInfo()
        } catch (exception: Exception) {
            log.warn("Unable to validate connection to {}", platform.title, exception)
            PlatformInfo(mapOf("isValid" to false))
        }
    }
 
    fun refreshPlatformInfo(): Map<Platform, PlatformInfo> {
        val cache = cacheManager.getCache(PLATFORM_INFO_CACHE_NAME)
        if (cache == null) {
            log.warn("Cache '{}' not found; platform info may be stale", PLATFORM_INFO_CACHE_NAME)
        } else {
            cache.clear()
        }
        return platformInfo()
    }

    private fun getPlatformInfoRepository(
        platform: Platform,
    ): PlatformInfoRepository =
        checkNotNull(
            platformInfoRepositoryMap[platform]
        ) {
            "No PlatformInfoRepository registered for platform $platform"
        }

    private fun handleDebugModeIfNecessary(request: UpdateConfigurationRequest) {
        debugModeService.handleDebugMode(request.configMap)
    }

    private fun updateConfiguration(
        request: UpdateConfigurationRequest,
        repository: PlatformConfigurationRepository,
    ): ConfigurationUpdateError? {
        return try {
            repository.updateConfig(request)
            null
        } catch (exception: PlatformException) {
            ConfigurationUpdateError(
                platform = exception.platform,
                code = exception.code,
                message = exception.message ?: "Unable to update configuration",
            )
        } catch (exception: Exception) {
            val platform = repository.platform()

            log.error(
                "Unexpected error while updating configuration for {}",
                platform.title,
                exception,
            )

            ConfigurationUpdateError(
                platform = platform,
                code = PlatformErrorCode.REQUEST_FAILED,
                message = exception.message ?: "Unexpected configuration error",
            )
        }
    }
}
