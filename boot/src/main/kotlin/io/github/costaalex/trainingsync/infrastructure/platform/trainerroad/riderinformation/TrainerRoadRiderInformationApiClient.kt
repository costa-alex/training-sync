package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.riderinformation

import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.TrainerRoadApiClientConfig
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
    value = "TrainerRoadRiderInformationApiClient",
    url = "\${app.trainer-road.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainerRoadApiClientConfig::class]
)
interface TrainerRoadRiderInformationApiClient {
    @GetMapping("/app/api/profile/rider-information")
    fun getRiderInformation(): Map<String, Any?>?

    @PutMapping("/app/api/profile/rider-information")
    fun updateRiderInformation(@RequestBody riderInformation: Map<String, Any?>)
}
