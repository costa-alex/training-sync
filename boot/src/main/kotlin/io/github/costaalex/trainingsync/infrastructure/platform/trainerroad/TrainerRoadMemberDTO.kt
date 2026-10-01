package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad

import com.fasterxml.jackson.annotation.JsonAlias

class TrainerRoadMemberDTO(
    @JsonAlias("memberId")
    val MemberId: Long,
    @JsonAlias("username")
    val Username: String?,
)
