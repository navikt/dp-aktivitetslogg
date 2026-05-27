package no.nav.dagpenger.aktivitetslogg.helpers.db

import no.nav.dagpenger.aktivitetslogg.db.PostgresDataSourceBuilder
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.testcontainers.postgresql.PostgreSQLContainer
import javax.sql.DataSource

internal object Postgres {
    private val instance by lazy {
        PostgreSQLContainer("postgres:18.0").apply {
            start()
        }
    }

    fun withMigratedDb(block: () -> Unit) {
        withCleanDb {
            PostgresDataSourceBuilder.runMigration()
            block()
        }
    }

    fun withMigratedDb(): DataSource {
        setup()
        PostgresDataSourceBuilder.clean()
        PostgresDataSourceBuilder.runMigration()
        return PostgresDataSourceBuilder.dataSource
    }

    private fun setup() {
        System.setProperty(ConfigUtils.CLEAN_DISABLED, "false")
        System.setProperty(PostgresDataSourceBuilder.DB_JDBC_URL_KEY, instance.jdbcUrl)
        System.setProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY, instance.password)
        System.setProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY, instance.username)
    }

    private fun tearDown() {
        System.clearProperty(PostgresDataSourceBuilder.DB_JDBC_URL_KEY)
        System.clearProperty(PostgresDataSourceBuilder.DB_USERNAME_KEY)
        System.clearProperty(PostgresDataSourceBuilder.DB_PASSWORD_KEY)
        System.clearProperty(ConfigUtils.CLEAN_DISABLED)
    }

    private fun withCleanDb(block: () -> Unit) {
        setup()
        PostgresDataSourceBuilder
            .clean()
            .run {
                block()
            }.also {
                tearDown()
            }
    }
}
