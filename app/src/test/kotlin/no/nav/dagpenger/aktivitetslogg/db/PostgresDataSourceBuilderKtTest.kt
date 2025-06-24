package no.nav.dagpenger.aktivitetslogg.db

import io.kotest.matchers.shouldBe
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.junit.jupiter.api.Test

class PostgresDataSourceBuilderKtTest {
    @Test
    fun `converts property to env-var`() {
        val property = ConfigUtils.CLEAN_DISABLED

        property.toSnakeCase() shouldBe "FLYWAY_CLEAN_DISABLED"
    }

    @Test
    fun `does not convert uppercased env-var`() {
        val property = "DB_HOST"

        property.toSnakeCase() shouldBe "DB_HOST"
    }
}
