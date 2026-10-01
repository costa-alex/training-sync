package io.github.costaalex.trainingsync.app.weight

import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.IntervalsApiClient
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.riderinformation.TrainerRoadRiderInformationApiClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate
import kotlin.math.abs

@Service
class WeightSyncService(
    private val intervalsConfigurationRepository: IntervalsConfigurationRepository,
    private val trainerRoadConfigurationRepository: TrainerRoadConfigurationRepository,
    private val intervalsApiClient: IntervalsApiClient,
    private val trainerRoadRiderInformationApiClient: TrainerRoadRiderInformationApiClient,
) {
    companion object {
        private const val WEIGHT_KG_FIELD = "weightKg"
        private const val WEIGHT_TOLERANCE_KG = 0.01
    }

    private val log = LoggerFactory.getLogger(this.javaClass)

    fun syncWeightIfEnabled() {
        try {
            syncWeight()
        } catch (exception: Exception) {
            log.warn(
                "Unable to sync weight from Intervals.icu to TrainerRoad",
                exception
            )
        }
    }

    private fun syncWeight() {
        val intervalsConfig =
            try {
                intervalsConfigurationRepository.getConfiguration()
            } catch (exception: Exception) {
                return
            }

        if (!intervalsConfig.syncWeightToTrainerRoad) {
            return
        }

        if (!trainerRoadConfigurationRepository.getConfiguration().canValidate()) {
            return
        }

        val wellness = intervalsApiClient.getWellness(
            intervalsConfig.athleteId,
            LocalDate.now().toString()
        )

        val intervalsWeight = wellness?.weight ?: return

        val riderInformation =
            trainerRoadRiderInformationApiClient.getRiderInformation()
                ?: return

        val currentWeight =
            (riderInformation[WEIGHT_KG_FIELD] as? Number)?.toDouble()

        if (
            currentWeight != null &&
            abs(currentWeight - intervalsWeight) < WEIGHT_TOLERANCE_KG
        ) {
            return
        }

        trainerRoadRiderInformationApiClient.updateRiderInformation(
            riderInformation + (WEIGHT_KG_FIELD to intervalsWeight)
        )

        log.info("Updated TrainerRoad weight from Intervals.icu wellness data")
    }
}
