package io.github.costaalex.trainingsync.app.plan

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.librarycontainer.LibraryContainer
import io.github.costaalex.trainingsync.domain.librarycontainer.LibraryContainerRepository
import io.github.costaalex.trainingsync.domain.workout.Workout
import io.github.costaalex.trainingsync.domain.workout.WorkoutRepository
import io.github.costaalex.trainingsync.domain.workout.structure.StepModifier
import org.springframework.stereotype.Service

@Service
class LibraryService(
    workoutRepositories: List<WorkoutRepository>,
    planRepositories: List<LibraryContainerRepository>,
) {
    private val workoutRepositoryMap = workoutRepositories.associateBy { it.platform() }
    private val planRepositoryMap = planRepositories.associateBy { it.platform() }

    fun findByPlatform(platform: Platform): List<LibraryContainer> {
        val repository = getPlanRepository(platform)
        return repository.getLibraryContainers()
    }

    fun copyLibrary(request: CopyLibraryRequest): CopyPlanResponse {
        require(request.newName.isNotBlank()) {
            "Library name cannot be blank"
        }

        val targetPlanRepository = getPlanRepository(request.targetPlatform)
        val sourceWorkoutRepository = getWorkoutRepository(request.sourcePlatform)
        val targetWorkoutRepository = getWorkoutRepository(request.targetPlatform)

        val workouts = sourceWorkoutRepository.getWorkoutsFromLibrary(request.libraryContainer)
            .map { it.addWorkoutStepModifier(request.stepModifier) }

        require(workouts.isNotEmpty()) {
            "Source library has no workouts to copy"
        }

        val newPlan = targetPlanRepository.createLibraryContainer(
            request.newName,
            request.libraryContainer.isPlan,
            workouts.first().date
        )
        targetWorkoutRepository.saveWorkoutsToLibrary(newPlan, workouts)
        return CopyPlanResponse(newPlan.name, workouts.size, newPlan.externalData)
    }

    fun deleteLibrary(request: DeleteLibraryRequest) {
        val planRepository = getPlanRepository(request.platform)
        planRepository.deleteLibraryContainer(request.externalData)
    }

    fun create(request: CreateLibraryContainerRequest): LibraryContainer {
        val planRepository = getPlanRepository(request.platform)
        return planRepository.createLibraryContainer(request.name, false, null)
    }

    private fun getWorkoutRepository(
        platform: Platform
    ): WorkoutRepository =
        checkNotNull(workoutRepositoryMap[platform]) {
            "No WorkoutRepository registered for platform $platform"
        }

    private fun getPlanRepository(
        platform: Platform
    ): LibraryContainerRepository =
        checkNotNull(planRepositoryMap[platform]) {
            "No LibraryContainerRepository registered for platform $platform"
        }

    private fun Workout.addWorkoutStepModifier(stepModifier: StepModifier): Workout =
        Workout(details, date, structure?.addModifier(stepModifier))
}

