package io.github.costaalex.workoutrelay.rest.workout

import io.github.costaalex.workoutrelay.app.workout.CopyFromCalendarToCalendarRequest
import io.github.costaalex.workoutrelay.app.workout.CopyFromCalendarToLibraryRequest
import io.github.costaalex.workoutrelay.app.workout.CopyFromLibraryToLibraryRequest
import io.github.costaalex.workoutrelay.app.workout.CopyWorkoutsResponse
import io.github.costaalex.workoutrelay.app.workout.WorkoutService
import io.github.costaalex.workoutrelay.domain.Platform
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionService
import io.github.costaalex.workoutrelay.app.workout.execution.SyncExecutionTrigger

@Validated
@RestController
class WorkoutController(
    private val workoutService: WorkoutService,
    private val syncExecutionService: SyncExecutionService,
) {
    @PostMapping("/api/workout/copy-calendar-to-calendar")
    fun copyWorkoutsFromCalendarToCalendar(
        @Valid @RequestBody request: CopyFromCalendarToCalendarRequest
    ): CopyWorkoutsResponse {
        return syncExecutionService.execute(
            request = request,
            trigger = SyncExecutionTrigger.MANUAL
        )
    }

    @PostMapping("/api/workout/copy-calendar-to-library")
    fun copyWorkoutsFromCalendarToLibrary(@Valid @RequestBody request: CopyFromCalendarToLibraryRequest): CopyWorkoutsResponse {
        return workoutService.copyWorkoutsC2L(request)
    }

    @PostMapping("/api/workout/copy-library-to-library")
    fun copyWorkoutFromLibraryToLibrary(@Valid @RequestBody request: CopyFromLibraryToLibraryRequest): CopyWorkoutsResponse {
        return workoutService.copyWorkoutL2L(request)
    }

    @GetMapping("/api/workout/find")
    fun findWorkoutsByName(@RequestParam platform: Platform, @RequestParam name: String): List<WorkoutDetailsDTO> {
        return workoutService.findWorkoutsByName(platform, name)
            .map { workoutDetails ->
                WorkoutDetailsDTO(
                    workoutDetails.name,
                    workoutDetails.duration.toString().replace("PT", "").lowercase(),
                    workoutDetails.load,
                    workoutDetails.externalData
                )
            }
    }

    @DeleteMapping("/api/workout")
    fun deleteWorkoutsFromCalendar(@Valid @RequestBody request: DeleteWorkoutRequestDTO) {
        workoutService.deleteWorkoutsFromCalendar(request)
    }
}
