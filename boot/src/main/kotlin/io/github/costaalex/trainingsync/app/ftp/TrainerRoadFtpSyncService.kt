package io.github.costaalex.trainingsync.app.ftp

import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.IntervalsApiClient
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.member.TrainerRoadMemberApiClient
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.riderinformation.TrainerRoadRiderInformationApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrainerRoadFtpSyncService(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient,
    private val trainerRoadRiderInformationApiClient: TrainerRoadRiderInformationApiClient,
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)
    private val cyclingSportTypes = setOf(
        "Ride",
        "VirtualRide",
        "GravelRide",
        "MountainBikeRide",
        "TrackRide",
        "Cyclocross",
        "EBikeRide",
        "EMountainBikeRide",
        "Handcycle",
        "Velomobile",
    )

    fun syncFtpIfEnabled(): Boolean {
        return try {
            if (!trainerRoadConfigurationRepository.getConfiguration().syncFtpToIntervals) {
                log.debug("TrainerRoad FTP sync is disabled")
                return false
            }
            if (trainerRoadConfigurationRepository.platformInfo().infoMap["isValid"] != true) {
                log.info("Skipping FTP sync because TrainerRoad is not connected")
                return false
            }
            if (intervalsConfigurationRepository.platformInfo().infoMap["isValid"] != true) {
                log.info("Skipping FTP sync because Intervals.icu is not connected")
                return false
            }
            syncFtp()
        } catch (exception: Exception) {
            log.warn("Unable to sync FTP from TrainerRoad to Intervals.icu", exception)
            false
        }
    }

    private fun syncFtp(): Boolean {
        val ftp = getTrainerRoadFtp() ?: run {
            log.info("TrainerRoad rider information did not include FTP; skipping Intervals.icu FTP sync")
            return false
        }
        if (ftp <= 0) {
            log.warn("TrainerRoad returned an invalid FTP value; skipping Intervals.icu FTP sync")
            return false
        }
        log.info("TrainerRoad FTP: {}", ftp)

        val athleteId = intervalsConfigurationRepository.getConfiguration().athleteId
        val settings = intervalsApiClient.getSportSettings(athleteId)
        settings.forEach { setting ->
            log.info(
                "Intervals.icu sport setting types={} ftp={} indoor_ftp={}",
                (setting["types"] as? List<*>)?.joinToString() ?: "unavailable",
                setting["ftp"].toIntOrNull() ?: "unavailable",
                setting["indoor_ftp"].toIntOrNull() ?: "unavailable",
            )
        }
        val cyclingSettings = settings.mapNotNull { setting ->
            val types = setting["types"] as? List<*>
                ?: return@mapNotNull null
            val typeNames = types.filterIsInstance<String>()
            if (
                typeNames.size == types.size &&
                typeNames.isNotEmpty() &&
                typeNames.all { it in cyclingSportTypes } &&
                typeNames.any { it == "Ride" || it == "VirtualRide" }
            ) {
                setting to typeNames
            } else {
                null
            }
        }
        val updatedSettings = cyclingSettings.filter { (setting, _) ->
            (setting["ftp"] as? Number)?.toInt() != ftp ||
                (setting["indoor_ftp"] as? Number)?.toInt() != ftp
        }

        if (updatedSettings.isEmpty()) {
            if (cyclingSettings.isEmpty()) {
                log.info("Intervals.icu returned no Ride/VirtualRide sport settings")
            } else {
                log.info("No Intervals.icu sport settings require an FTP update")
            }
            return false
        }

        var updatedCount = 0
        updatedSettings.forEach { (_, types) ->
            val sportType = if ("Ride" in types) "Ride" else "VirtualRide"
            try {
                intervalsApiClient.updateSportSettings(
                    athleteId = athleteId,
                    sportType = sportType,
                    recalcHrZones = false,
                    settings = mapOf(
                        "types" to types,
                        "ftp" to ftp,
                        "indoor_ftp" to ftp,
                    ),
                )
                updatedCount++
            } catch (exception: Exception) {
                log.warn(
                    "Unable to update FTP for Intervals.icu sport type {}",
                    sportType,
                    exception,
                )
            }
        }

        if (updatedCount > 0) {
            log.info("Updated FTP for {} Intervals.icu sport settings", updatedCount)
        }
        return updatedCount > 0
    }

    private fun getTrainerRoadFtp(): Int? {
        val riderInformation = trainerRoadRiderInformationApiClient.getRiderInformation()
        val riderFtp = listOf("ftp", "Ftp", "FTP", "cyclingFtp")
            .firstNotNullOfOrNull { field ->
                riderInformation?.get(field)?.toIntOrNull()
            }

        return riderFtp ?: trainerRoadMemberApiClient.getMember().ftp
    }

    private fun Any?.toIntOrNull(): Int? {
        return when (this) {
            is Number -> toInt()
            is String -> toIntOrNull()
            else -> null
        }
    }
}
