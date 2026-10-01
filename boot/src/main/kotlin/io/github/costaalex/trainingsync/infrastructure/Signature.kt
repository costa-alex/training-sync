package io.github.costaalex.trainingsync.infrastructure

import io.github.costaalex.trainingsync.domain.Platform

class Signature {
    companion object {
        private const val REPOSITORY_URL = "https://github.com/costa-alex/training-sync"

        fun description(platform: Platform): String =
            "Synced to ${platform.title} via TrainingSync ($REPOSITORY_URL)"
    }
}
