package io.github.costaalex.workoutrelay.infrastructure

import io.github.costaalex.workoutrelay.domain.Platform

class Signature {
    companion object {
        private const val REPOSITORY_URL = "https://github.com/costa-alex/workout-relay"

        fun description(platform: Platform): String =
            "Synced to ${platform.title} via Workout Relay ($REPOSITORY_URL)"
    }
}
