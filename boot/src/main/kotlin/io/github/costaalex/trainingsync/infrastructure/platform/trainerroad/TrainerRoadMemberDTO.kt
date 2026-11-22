package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

class TrainerRoadMemberDTO(
    @JsonAlias("memberId")
    val MemberId: Long,
    @JsonAlias("username")
    val Username: String?,
    @JsonProperty("ftp")
    @JsonAlias("FTP")
    val ftp: Int? = null,
)
