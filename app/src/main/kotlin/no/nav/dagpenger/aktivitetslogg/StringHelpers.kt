package no.nav.dagpenger.aktivitetslogg

fun String.toStringOrNull(): String? = if (this == "null") null else this
