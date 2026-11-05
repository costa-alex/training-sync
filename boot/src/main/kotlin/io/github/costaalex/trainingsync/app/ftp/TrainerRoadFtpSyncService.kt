package io.github.costaalex.trainingsync.app.ftp

import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.IntervalsApiClient
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.member.TrainerRoadMemberApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TrainerRoadFtpSyncService(
    private val trainerRoadMemberApiClient: TrainerRoadMemberApiClient,
    private val intervalsApiClient: IntervalsApiClient,
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
) {
    private val log = LoggerFactory.getLogger(this.javaClass)

    fun syncFtpIfEnabled(): Boolean {
        return try {
            if (!trainerRoadConfigurationRepository.getConfiguration().syncFtpToIntervals) {
                return false
            }
            syncFtp()
        } catch (exception: Exception) {
            log.warn("Unable to sync FTP from TrainerRoad to Intervals.icu", exception)
            false
        }
    }

    private fun syncFtp(): Boolean {
        val ftp = trainerRoadMemberApiClient.getMember().ftp ?: run {
            log.info("TrainerRoad member info did not include FTP; skipping Intervals.icu FTP sync")
            return false
        }
        if (ftp <= 0) {
            log.warn("TrainerRoad returned an invalid FTP value; skipping Intervals.icu FTP sync")
            return false
        }

        val athleteId = intervalsConfigurationRepository.getConfiguration().athleteId
        val settings = intervalsApiClient.getSportSettings(athleteId)
        val updatedSettings = settings.filter { setting ->
            val types = setting["types"] as? List<*>
            types?.isNotEmpty() == true &&
                types.all { it == "Ride" || it == "VirtualRide" } &&
                types.any { it == "Ride" || it == "VirtualRide" }
        }.filter { setting ->
            (setting["ftp"] as? Number)?.toInt() != ftp ||
                (setting["indoor_ftp"] as? Number)?.toInt() != ftp
        }

        var updated = false
        updatedSettings.forEach { setting ->
            val id = setting["id"]?.toString()
            if (id == null) {
                log.warn("Intervals.icu sport settings are missing an ID; skipping FTP update")
                return@forEach
            }

            try {
                intervalsApiClient.updateSportSettings(
                    athleteId = athleteId,
                    id = id,
                    recalcHrZones = false,
                    settings = setting + mapOf(
                        "ftp" to ftp,
                        "indoor_ftp" to ftp,
                    ),
                )
                updated = true
            } catch (exception: Exception) {
                log.warn(
                    "Unable to update FTP for Intervals.icu sport setting {}",
                    id,
                    exception,
                )
            }
        }

        if (updated) {
            log.info("Updated FTP for Intervals.icu sport settings")
        }
        return updated
    }
}
