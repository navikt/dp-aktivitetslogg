package no.nav.dagpenger.aktivitetslogg.db

import ch.qos.logback.core.util.OptionHelper.getEnv
import ch.qos.logback.core.util.OptionHelper.getSystemProperty
import com.zaxxer.hikari.HikariDataSource
import mu.KotlinLogging
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.internal.configuration.ConfigUtils
import java.util.Locale

private val logger = KotlinLogging.logger {}

// Understands how to create a data source from environment variables
internal object PostgresDataSourceBuilder {
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"
    const val DB_DATABASE_KEY = "DB_DATABASE"
    const val DB_HOST_KEY = "DB_HOST"
    const val DB_PORT_KEY = "DB_PORT"

    private fun getOrThrow(key: String): String =
        getEnv(
            key
                .toSnakeCase()
                .also { logger.info { "Henter $it fra environment variables" } },
        ) ?: getSystemProperty(key)

    val dataSource by lazy {
        HikariDataSource().apply {
            dataSourceClassName = "org.postgresql.ds.PGSimpleDataSource"
            addDataSourceProperty("serverName", getOrThrow(DB_HOST_KEY))
            addDataSourceProperty("portNumber", getOrThrow(DB_PORT_KEY))
            addDataSourceProperty("databaseName", getOrThrow(DB_DATABASE_KEY))
            addDataSourceProperty("user", getOrThrow(DB_USERNAME_KEY))
            addDataSourceProperty("password", getOrThrow(DB_PASSWORD_KEY))
            maximumPoolSize = 10
            minimumIdle = 1
            idleTimeout = 10001
            connectionTimeout = 1000
            initializationFailTimeout = 5000
            maxLifetime = 30001
        }
    }

    private val flyWayBuilder: FluentConfiguration = Flyway.configure().connectRetries(10)

    fun clean(): CleanResult =
        flyWayBuilder
            .cleanDisabled(getOrThrow(ConfigUtils.CLEAN_DISABLED).toBooleanStrict())
            .dataSource(dataSource)
            .load()
            .clean()

    internal fun runMigration(initSql: String? = null): Int =
        flyWayBuilder
            .dataSource(dataSource)
            .initSql(initSql)
            .load()
            .migrate()
            .migrations
            .size
}

fun String.toSnakeCase() = this.replace(Regex("([a-z])([A-Z])|\\."), "$1_$2").uppercase(Locale.getDefault())
