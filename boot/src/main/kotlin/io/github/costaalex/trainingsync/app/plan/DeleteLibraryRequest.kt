package io.github.costaalex.trainingsync.app.plan

import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.domain.Platform

data class DeleteLibraryRequest(
    val externalData: ExternalData,
    val platform: Platform,
)
