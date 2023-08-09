package no.nav.dagpenger.aktivitetslogg.db

import io.kotest.matchers.shouldBe
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.junit.jupiter.api.Test

class PostgresDataSourceBuilderKtTest {
    @Test
    fun `converts property to env-var`() {
        val cleanDisabled = ConfigUtils.CLEAN_DISABLED

        cleanDisabled.toSnakeCase() shouldBe "FLYWAY_CLEAN_DISABLED"
    }
}
