package io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.activity

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType
import io.github.costaalex.trainingsync.domain.activity.Activity
import io.github.costaalex.trainingsync.domain.activity.ActivityRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.TrainerRoadApiClientService
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.member.TRUsernameRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class TrainerRoadActivityRepository(
    private val trUsernameRepository: TRUsernameRepository,
    private val trainerRoadApiClientService: TrainerRoadApiClientService,
) : ActivityRepository {
    override fun platform() = Platform.TRAINER_ROAD

    override fun saveActivities(activities: List<Activity>) {
        TODO("Not yet implemented")
    }

    override fun getActivities(startDate: LocalDate, endDate: LocalDate, types: List<TrainingType>): List<Activity> {
        val memberId = trUsernameRepository.getMemberId()
        return trainerRoadApiClientService.getActivities(memberId, startDate, endDate)
    }
}
