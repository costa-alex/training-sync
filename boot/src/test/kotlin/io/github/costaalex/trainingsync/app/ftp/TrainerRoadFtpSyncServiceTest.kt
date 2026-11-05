package io.github.costaalex.trainingsync.app.ftp

import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.IntervalsApiClient
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration.IntervalsConfiguration
import io.github.costaalex.trainingsync.infrastructure.platform.intervalsicu.configuration.IntervalsConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.TrainerRoadMemberDTO
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfiguration
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.configuration.TrainerRoadConfigurationRepository
import io.github.costaalex.trainingsync.infrastructure.platform.trainerroad.member.TrainerRoadMemberApiClient
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import tools.jackson.module.kotlin.jacksonObjectMapper
import tools.jackson.module.kotlin.readValue

class TrainerRoadFtpSyncServiceTest {

    private val trainerRoadMemberApiClient = mock<TrainerRoadMemberApiClient>()
    private val intervalsApiClient = mock<IntervalsApiClient>()
    private val intervalsConfigurationRepository = mock<IntervalsConfigurationRepository>()
    private val trainerRoadConfigurationRepository = mock<TrainerRoadConfigurationRepository>()
    private val service = TrainerRoadFtpSyncService(
        trainerRoadMemberApiClient,
        intervalsApiClient,
        intervalsConfigurationRepository,
        trainerRoadConfigurationRepository,
    )

    @Test
    fun `deserializes FTP from TrainerRoad member info`() {
        val objectMapper = jacksonObjectMapper()

        val member = objectMapper.readValue<TrainerRoadMemberDTO>(
            """{"memberId":1,"username":"athlete","ftp":250}"""
        )

        assertEquals(250, member.ftp)
    }

    @Test
    fun `updates only Ride and VirtualRide settings while preserving other fields`() {
        whenever(trainerRoadMemberApiClient.getMember())
            .thenReturn(TrainerRoadMemberDTO(1, "athlete", ftp = 250))
        whenever(trainerRoadConfigurationRepository.getConfiguration())
            .thenReturn(TrainerRoadConfiguration(null, false, true, true))
        whenever(intervalsConfigurationRepository.getConfiguration())
            .thenReturn(IntervalsConfiguration("key", "athlete", 0.1f, 0.2f, 0.3f))
        whenever(intervalsApiClient.getSportSettings("athlete")).thenReturn(
            listOf(
                mapOf(
                    "id" to 10,
                    "types" to listOf("Ride"),
                    "ftp" to 250,
                    "indoor_ftp" to 200,
                    "warmup_time" to 300,
                ),
                mapOf(
                    "id" to 11,
                    "types" to listOf("VirtualRide"),
                    "ftp" to 230,
                    "indoor_ftp" to 250,
                ),
                mapOf("id" to 12, "types" to listOf("Run"), "ftp" to 200, "indoor_ftp" to 200),
                mapOf("id" to 13, "types" to listOf("Ride", "Run"), "ftp" to 210, "indoor_ftp" to 210),
            )
        )

        assertTrue(service.syncFtpIfEnabled())

        verify(intervalsApiClient).updateSportSettings(
            athleteId = "athlete",
            id = "10",
            recalcHrZones = false,
            settings = mapOf(
                "id" to 10,
                "types" to listOf("Ride"),
                "ftp" to 250,
                "indoor_ftp" to 250,
                "warmup_time" to 300,
            ),
        )
        verify(intervalsApiClient).updateSportSettings(
            athleteId = "athlete",
            id = "11",
            recalcHrZones = false,
            settings = mapOf(
                "id" to 11,
                "types" to listOf("VirtualRide"),
                "ftp" to 250,
                "indoor_ftp" to 250,
            ),
        )
        verify(intervalsApiClient, times(2)).updateSportSettings(
            eq("athlete"),
            any(),
            eq(false),
            any(),
        )
        verify(intervalsApiClient, never()).updateSportSettings(
            eq("athlete"),
            eq("12"),
            eq(false),
            any(),
        )
        verify(intervalsApiClient, never()).updateSportSettings(
            eq("athlete"),
            eq("13"),
            eq(false),
            any(),
        )
    }

    @Test
    fun `skips settings already using TrainerRoad FTP`() {
        whenever(trainerRoadMemberApiClient.getMember())
            .thenReturn(TrainerRoadMemberDTO(1, "athlete", ftp = 250))
        whenever(trainerRoadConfigurationRepository.getConfiguration())
            .thenReturn(TrainerRoadConfiguration(null, false, true, true))
        whenever(intervalsConfigurationRepository.getConfiguration())
            .thenReturn(IntervalsConfiguration("key", "athlete", 0.1f, 0.2f, 0.3f))
        whenever(intervalsApiClient.getSportSettings("athlete")).thenReturn(
            listOf(
                mapOf(
                    "id" to 10,
                    "types" to listOf("Ride"),
                    "ftp" to 250,
                    "indoor_ftp" to 250,
                )
            )
        )

        assertFalse(service.syncFtpIfEnabled())

        verify(intervalsApiClient, never()).updateSportSettings(
            any(),
            any(),
            eq(false),
            any(),
        )
    }

    @Test
    fun `does not fetch or update FTP when the TrainerRoad setting is disabled`() {
        whenever(trainerRoadConfigurationRepository.getConfiguration())
            .thenReturn(TrainerRoadConfiguration(null, false, true))

        assertFalse(service.syncFtpIfEnabled())

        verify(trainerRoadMemberApiClient, never()).getMember()
        verify(intervalsApiClient, never()).getSportSettings(any())
    }
}
