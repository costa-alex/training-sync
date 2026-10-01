package io.github.costaalex.trainingsync.infrastructure.schedule

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ScheduleRequestRepository :
    CrudRepository<ScheduleRequestEntity, Int>
