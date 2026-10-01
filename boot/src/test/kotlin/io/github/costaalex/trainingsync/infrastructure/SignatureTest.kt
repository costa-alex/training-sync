package io.github.costaalex.trainingsync.infrastructure

import io.github.costaalex.trainingsync.domain.Platform
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SignatureTest {
    @Test
    fun `mentions TrainingPeaks when imported to TrainingPeaks`() {
        val description = Signature.description(Platform.TRAINING_PEAKS)

        assertThat(description).isEqualTo(
            "Synced to TrainingPeaks via TrainingSync (https://github.com/costa-alex/training-sync)"
        )
    }

    @Test
    fun `mentions Intervals icu when imported to Intervals icu`() {
        val description = Signature.description(Platform.INTERVALS)

        assertThat(description).isEqualTo(
            "Synced to Intervals.icu via TrainingSync (https://github.com/costa-alex/training-sync)"
        )
    }
}
