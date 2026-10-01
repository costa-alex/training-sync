package io.github.costaalex.trainingsync.domain.config

class AppConfiguration(
    val configMap: Map<String, String>,
) {
    fun find(key: String): String? = configMap[key]
}
