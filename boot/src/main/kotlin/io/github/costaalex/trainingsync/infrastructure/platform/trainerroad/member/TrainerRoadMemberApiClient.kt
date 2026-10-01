package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.member

import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.TrainerRoadApiClientConfig
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping

@FeignClient(
    value = "TrainerRoadMemberApiClient",
    url = "\${app.trainer-road.api-url}",
    dismiss404 = true,
    primary = false,
    configuration = [TrainerRoadApiClientConfig::class]
)
interface TrainerRoadMemberApiClient {
    @GetMapping("/app/api/member-info")
    fun getMember(): TrainerRoadMemberDTO
}
