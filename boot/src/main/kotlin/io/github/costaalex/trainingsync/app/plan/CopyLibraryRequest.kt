package io.github.costaalex.trainingsync.app.plan

import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.librarycontainer.LibraryContainer
import io.github.costaalex.trainingsync.domain.workout.structure.StepModifier

data class CopyLibraryRequest(
    val libraryContainer: LibraryContainer,
    val newName: String,
    val stepModifier: StepModifier,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
