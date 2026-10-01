package io.github.costaalex.trainingsync.app.plan

import io.github.costaalex.trainingsync.domain.Platform

data class CreateLibraryContainerRequest(
    val name: String,
    val platform: Platform,
)