package io.github.costaalex.trainingsync.app.workout

import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.domain.Platform
import io.github.costaalex.trainingsync.domain.librarycontainer.LibraryContainer

data class CopyFromLibraryToLibraryRequest(
    val workoutExternalData: ExternalData,
    val targetLibraryContainer: LibraryContainer,
    val sourcePlatform: Platform,
    val targetPlatform: Platform,
)
