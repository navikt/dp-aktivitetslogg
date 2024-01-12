# dp-aktivitetslogg

## Beskrivelse



### aktivitetslogg bibliotek

Har kode for å logge aktiviteter til en aktivitetslogg. En aktivitetslogg er en liste av aktiviteter. En aktivitet er en melding med en alvorlighetsgrad.

Feks:

```kotlin
val aktivitetslogg = Aktivitetslogg()
aktivitetslogg.info("hei")
```
Legger til en aktivitet med melding "hei" og alvorlighetsgrad "INFO".

Aktivitetsloggen kan brukes til å logge aktiviteter i en applikasjon.  

Det finnes en rekke "alvorlighetsgrader" disse er i stigende rekkefølge: `info`, `behov`, `varsel`, `funksjonellFeil`, `logiskFeil` og er håndtert av [`IAktivitetslogg`](aktivitetslogg/src/main/kotlin/no/nav/dagpenger/aktivitetslogg/IAktivitetslogg.kt) interfacet. 

Aktivitetsloggen har et konsept om aktivietskontekst. En aktivitetskontekst er nøkkeldata relatert til hvor aktiviteten logges fra. Feks kan en aktivitetskontekst være en meldingsid, en requestid, en brukerid, en transaksjonsid etc. 

Objekter kan implementere `Aktivitetskontekst` for å gi en aktivitetskontekst.

Feks:

```kotlin
class MinKlasse(val meldingsid: String) : Aktivitetskontekst {
    override fun tilAktivitetskontekst() = mapOf("meldingsid" to meldingsid) // gir en aktivitetskontekst med nøkkel "meldingsid" og verdi meldingsid
}
val aktivitetslogg = Aktivitetslogg()
aktivitetslogg.kontekst(MinKlasse("123"))

aktivitetslogg.info("hei") // har med alle aktivitetskontekster som er lagt til aktivitetsloggen
```

Aktivitetsloggen kan publiseres på Kafka; 

```kotlin
internal class AktivitetsloggMediator(private val rapidsConnection: RapidsConnection) {
    fun håndter(hendelse: PersonHendelse) {
        rapidsConnection.publish(
            JsonMessage.newMessage(
                "aktivitetslogg",
                mapOf(
                    "hendelse" to
                        mapOf(
                            "type" to hendelse.toSpesifikkKontekst().kontekstType,
                            "meldingsreferanseId" to hendelse.meldingsreferanseId(),
                        ),
                    "ident" to hendelse.ident(),
                    "aktiviteter" to AktivitetsloggJsonBuilder(hendelse).asList(),
                ),
            ).toJson(),
        )
    }
```

### aktivitetslogg api

Lytter til alle aktivitetslogger som publiseres på kafka og lagrer disse i en database samt tilbyr et api for å hente ut aktivitetslogger.

## Komme i gang

`./gradlew build`


## Kontakt oss

i #po-arbeid-dev på Slack