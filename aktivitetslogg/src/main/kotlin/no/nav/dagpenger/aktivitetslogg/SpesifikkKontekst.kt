package no.nav.dagpenger.aktivitetslogg

open class SpesifikkKontekst(val kontekstType: String, open val kontekstMap: Map<String, String> = mapOf()) {
    fun melding() = kontekstType + kontekstMap.entries.joinToString(separator = "") { " ${it.key}: ${it.value}" }

    fun sammeType(other: Aktivitetskontekst) = this.kontekstType == other.toSpesifikkKontekst().kontekstType

    fun toMap(): Map<String, Any> {
        return mapOf(
            "konteksttype" to kontekstType,
            "kontekstmap" to kontekstMap,
        )
    }

    override fun equals(other: Any?) =
        this === other || (other is SpesifikkKontekst && this.kontekstMap == other.kontekstMap && this.kontekstType == other.kontekstType)

    override fun hashCode() = 31 * kontekstType.hashCode() + kontekstMap.hashCode()
}
