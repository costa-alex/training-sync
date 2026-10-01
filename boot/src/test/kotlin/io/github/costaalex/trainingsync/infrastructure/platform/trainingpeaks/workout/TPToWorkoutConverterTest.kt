package io.github.costaalex.trainingsync.infrastructure.platform.trainingpeaks.workout

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDateTime

class TPToWorkoutConverterTest {
    @Test
    fun `converts workout without structured steps without warning`() {
        val logger = LoggerFactory.getLogger(TPToWorkoutConverter::class.java) as Logger
        val appender = ListAppender<ILoggingEvent>().apply { start() }
        logger.addAppender(appender)

        try {
            val workout = TPToWorkoutConverter().toWorkout(
                TPWorkoutCalendarResponseDTO(
                    workoutDay = LocalDateTime.of(2026, 9, 7, 0, 0),
                    workoutId = "3940305210",
                    workoutTypeValueId = 2,
                    workoutSubTypeValueId = null,
                    title = "Lazy Mountain -1",
                    totalTimePlanned = 1.0,
                    tssPlanned = null,
                    description = null,
                    coachComments = null,
                    structure = null,
                )
            )

            assertThat(workout.details.name).isEqualTo("Lazy Mountain -1")
            assertThat(workout.details.duration).isEqualTo(Duration.ofMinutes(60))
            assertThat(workout.structure).isNull()
            assertThat(appender.list).noneMatch { it.level == Level.WARN }
        } finally {
            logger.detachAppender(appender)
        }
    }

    @Test
    fun `marks workout as completed when it has actual elapsed time`() {
        val workout = TPToWorkoutConverter().toWorkout(
            TPWorkoutCalendarResponseDTO(
                workoutDay = LocalDateTime.of(2026, 9, 7, 0, 0),
                workoutId = "3940305210",
                workoutTypeValueId = 2,
                workoutSubTypeValueId = null,
                title = "Lazy Mountain -1",
                totalTimePlanned = 60.0,
                tssPlanned = null,
                description = null,
                coachComments = null,
                structure = null,
                totalTime = 58.5,
                tssActual = null,
            )
        )

        assertThat(workout.details.completed).isTrue()
    }

    @Test
    fun `marks workout as completed when it has an actual TSS`() {
        val workout = TPToWorkoutConverter().toWorkout(
            TPWorkoutCalendarResponseDTO(
                workoutDay = LocalDateTime.of(2026, 9, 7, 0, 0),
                workoutId = "3940305210",
                workoutTypeValueId = 2,
                workoutSubTypeValueId = null,
                title = "Lazy Mountain -1",
                totalTimePlanned = 60.0,
                tssPlanned = 50,
                description = null,
                coachComments = null,
                structure = null,
                totalTime = null,
                tssActual = 48,
            )
        )

        assertThat(workout.details.completed).isTrue()
    }

    @Test
    fun `does not mark a planned workout as completed`() {
        val workout = TPToWorkoutConverter().toWorkout(
            TPWorkoutCalendarResponseDTO(
                workoutDay = LocalDateTime.of(2026, 9, 7, 0, 0),
                workoutId = "3940305210",
                workoutTypeValueId = 2,
                workoutSubTypeValueId = null,
                title = "Lazy Mountain -1",
                totalTimePlanned = 60.0,
                tssPlanned = 50,
                description = null,
                coachComments = null,
                structure = null,
                totalTime = null,
                tssActual = null,
            )
        )

        assertThat(workout.details.completed).isFalse()
    }
}