package io.github.costaalex.trainingsync.infrastructure.schedule

import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.sql.DriverManager

class ScheduleMigrationTest {
    @TempDir
    lateinit var temporaryDirectory: Path

    @Test
    fun `preserves schedules when upgrading through migration 0008`() {
        val databaseUrl = "jdbc:sqlite:${temporaryDirectory.resolve("upgrade.sqlite")}" 
        migrate(databaseUrl, "db/changelog/before-0008.yaml")

        val requestJson = """{"types":["BIKE"],"skipSynced":true}"""
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement(
                "INSERT INTO schedule_requests (id, request_json) VALUES (?, ?)"
            ).use { statement ->
                statement.setInt(1, 42)
                statement.setString(2, requestJson)
                statement.executeUpdate()
            }
        }

        migrate(databaseUrl, "db/changelog/db.changelog-master.yaml")

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.prepareStatement(
                "SELECT id, request_json FROM schedule_requests"
            ).use { statement ->
                statement.executeQuery().use { result ->
                    assertThat(result.next()).isTrue()
                    assertThat(result.getInt("id")).isEqualTo(42)
                    assertThat(result.getString("request_json")).isEqualTo(requestJson)
                    assertThat(result.next()).isFalse()
                }
            }
        }
    }

    @Test
    fun `sets null FTP sync history values to false`() {
        val databaseUrl = "jdbc:sqlite:${temporaryDirectory.resolve("ftp-history.sqlite")}"
        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.execute(
                    "CREATE TABLE sync_executions (id INTEGER PRIMARY KEY, ftp_synced BOOLEAN)"
                )
                statement.execute(
                    "INSERT INTO sync_executions (id, ftp_synced) VALUES (1, NULL), (2, 1)"
                )
            }
        }

        migrate(
            databaseUrl,
            "db/changelog/schemas/0018-fix-ftp-synced-null-values.yaml",
        )

        DriverManager.getConnection(databaseUrl).use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery(
                    "SELECT ftp_synced FROM sync_executions ORDER BY id"
                ).use { result ->
                    assertThat(result.next()).isTrue()
                    assertThat(result.getBoolean("ftp_synced")).isFalse()
                    assertThat(result.wasNull()).isFalse()
                    assertThat(result.next()).isTrue()
                    assertThat(result.getBoolean("ftp_synced")).isTrue()
                    assertThat(result.next()).isFalse()
                }
            }
        }
    }

    private fun migrate(databaseUrl: String, changeLog: String) {
        DriverManager.getConnection(databaseUrl).use { connection ->
            val database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(JdbcConnection(connection))
            Liquibase(
                changeLog,
                ClassLoaderResourceAccessor(),
                database,
            ).update()
        }
    }
}