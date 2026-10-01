package io.github.costaalex.trainingsync.domain.librarycontainer

import java.io.Serializable
import java.time.LocalDate
import io.github.costaalex.trainingsync.domain.ExternalData
import io.github.costaalex.trainingsync.infrastructure.utils.Date

data class LibraryContainer(
    val name: String,
    val startDate: LocalDate,
    val isPlan: Boolean,
    val workoutsAmount: Int,
    val externalData: ExternalData,
) : Serializable {
    companion object {
        fun planFromMonday(name: String, workoutsAmount: Int, externalData: ExternalData): LibraryContainer {
            return LibraryContainer(name, Date.thisMonday(), true, workoutsAmount, externalData)
        }
    }
}
