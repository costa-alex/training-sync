package io.github.costaalex.trainingsync.app.workout.execution

import io.github.costaalex.trainingsync.app.ftp.TrainerRoadFtpSyncService
import io.github.costaalex.trainingsync.app.weight.WeightSyncService
import io.github.costaalex.trainingsync.app.workout.CopyFromCalendarToCalendarRequest
import io.github.costaalex.trainingsync.app.workout.CopyWorkoutsResponse
import io.github.costaalex.trainingsync.app.workout.WorkoutService
import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.TrainingType
import io.github.costaalex.trainingsync.infrastructure.configuration.SyncHistoryProperties
import io.github.costaalex.trainingsync.infrastructure.sync.SyncExecutionEntity
import io.github.costaalex.trainingsync.infrastructure.sync.SyncExecutionRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class SyncExecutionServiceFtpHistoryTest {

    @Test
    fun `records FTP updates in sync execution history`() {
        val workoutService = mock<WorkoutService>()
        val syncExecutionRepository = mock<SyncExecutionRepository>()
        val maintenanceService = mock<SyncExecutionMaintenanceService>()
        val weightSyncService = mock<WeightSyncService>()
        val trainerRoadFtpSyncService = mock<TrainerRoadFtpSyncService>()
        val date = LocalDate.of(2026, 11, 5)
        val request = CopyFromCalendarToCalendarRequest(
            startDate = date,
            endDate = date,
            types = listOf(TrainingType.BIKE),
            skipSynced = true,
            sourcePlatform = Platform.TRAINER_ROAD,
            targetPlatform = Platform.INTERVALS,
        )
        whenever(workoutService.copyWorkoutsC2C(request)).thenReturn(
            CopyWorkoutsResponse(
                copied = 0,
                filteredOut = 0,
                skippedByType = 0,
                skippedAlreadySynced = 0,
                startDate = date,
                endDate = date,
                externalData = ExternalData.empty(),
            )
        )
        whenever(trainerRoadFtpSyncService.syncFtpIfEnabled()).thenReturn(true)
        whenever(syncExecutionRepository.save(any<SyncExecutionEntity>())).thenAnswer { invocation ->
            invocation.getArgument<SyncExecutionEntity>(0).also { execution ->
                execution.id = 1
            }
        }

        val service = SyncExecutionService(
            workoutService = workoutService,
            syncExecutionRepository = syncExecutionRepository,
            maintenanceService = maintenanceService,
            syncHistoryProperties = SyncHistoryProperties(),
            weightSyncService = weightSyncService,
            trainerRoadFtpSyncService = trainerRoadFtpSyncService,
        )

        service.execute(request, SyncExecutionTrigger.MANUAL)

        val savedExecutions = argumentCaptor<SyncExecutionEntity>()
        verify(syncExecutionRepository, times(2)).save(savedExecutions.capture())
        val completedExecution = savedExecutions.allValues.last()
        assertTrue(completedExecution.ftpSynced)
        assertTrue(SyncExecutionResponse.fromEntity(completedExecution).ftpSynced)
    }
}
