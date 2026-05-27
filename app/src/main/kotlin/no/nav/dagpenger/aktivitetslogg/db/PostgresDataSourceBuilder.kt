package no.nav.dagpenger.aktivitetslogg.db

import ch.qos.logback.core.util.OptionHelper.getEnv
import ch.qos.logback.core.util.OptionHelper.getSystemProperty
import com.zaxxer.hikari.HikariDataSource
import io.github.oshai.kotlinlogging.KotlinLogging
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.CleanResult
import org.flywaydb.core.internal.configuration.ConfigUtils
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

// Understands how to create a data source from environment variables
internal object PostgresDataSourceBuilder {
    const val DB_URL_KEY = "DB_JDBC_URL"
    const val DB_USERNAME_KEY = "DB_USERNAME"
    const val DB_PASSWORD_KEY = "DB_PASSWORD"

    private fun getOrThrow(key: String): String =
        getEnv(key.toSnakeCase().also { logger.info { "Henter $it fra environment variables" } }) ?: getSystemProperty(key)

    val dataSource by lazy {
        HikariDataSource().apply {
            jdbcUrl = getOrThrow(DB_URL_KEY) // .ensurePrefix("jdbc:postgresql://").stripCredentials()
            username = getOrThrow(DB_USERNAME_KEY)
            password = getOrThrow(DB_PASSWORD_KEY)

            // Default 30 sekund
            connectionTimeout = 10.seconds.inWholeMilliseconds
            // Default 10 minutter
            idleTimeout = 10.minutes.inWholeMilliseconds
            // Default 2 minutter
            keepaliveTime = 2.minutes.inWholeMilliseconds
            // Default 30 minutter
            maxLifetime = 30.minutes.inWholeMilliseconds
            leakDetectionThreshold = 10.seconds.inWholeMilliseconds
            metricRegistry =
                PrometheusMeterRegistry(
                    PrometheusConfig.DEFAULT,
                    PrometheusRegistry.defaultRegistry,
                    Clock.SYSTEM,
                )
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

private fun String.stripCredentials() = this.replace(Regex("://.*:.*@"), "://")

private fun String.ensurePrefix(prefix: String) =
    if (this.startsWith(prefix)) {
        this
    } else {
        prefix + this.substringAfter("//")
    }
