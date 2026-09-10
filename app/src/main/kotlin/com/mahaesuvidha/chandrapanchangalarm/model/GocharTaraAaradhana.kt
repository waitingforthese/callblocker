package com.mahaesuvidha.chandrapanchangalarm.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.math.abs

/**
 * Computes Tara Bala for each current transit planet relative to the saved
 * birth Nakshatra. This feature intentionally monitors only Vipat, Pratyari
 * and Vadha (Naidhana) Tara, as requested by the user.
 */
object GocharTaraAaradhanaCalculator {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val names = listOf(
        "अश्विनी", "भरणी", "कृत्तिका", "रोहिणी", "मृगशीर्ष", "आर्द्रा", "पुनर्वसू", "पुष्य", "आश्लेषा",
        "मघा", "पूर्वाफाल्गुनी", "उत्तराफाल्गुनी", "हस्त", "चित्रा", "स्वाती", "विशाखा", "अनुराधा", "ज्येष्ठा",
        "मूळ", "पूर्वाषाढा", "उत्तराषाढा", "श्रवण", "धनिष्ठा", "शतभिषा", "पूर्वाभाद्रपदा", "उत्तराभाद्रपदा", "रेवती"
    )
    private val taraNames = listOf("जन्म", "संपत", "विपत", "क्षेम", "प्रत्यारी", "साधक", "वध", "मित्र", "अतिमित्र")

    data class CurrentInfo(
        val graha: Graha,
        val longitude: Double,
        val nakshatra: String,
        val tara: String,
        val target: Boolean
    )

    fun nakshatraIndex(longitude: Double): Int = ((normalize(longitude) / (360.0 / 27.0)).toInt()).coerceIn(0, 26)
    fun nakshatraName(longitude: Double): String = names[nakshatraIndex(longitude)]

    fun taraFor(birthNakshatra: String, transitNakshatraIndex: Int): String? {
        val birthIndex = names.indexOf(birthNakshatra)
        if (birthIndex < 0) return null
        val count = (transitNakshatraIndex - birthIndex + 27) % 27 + 1
        return taraNames[(count - 1) % 9]
    }

    fun current(profile: BirthProfile, millis: Long = System.currentTimeMillis()): List<CurrentInfo> {
        val positions = LiveTransitCalculator.positionsAt(millis)
        return Graha.entries.map { g ->
            val lon = positions[g] ?: 0.0
            val nak = nakshatraIndex(lon)
            val tara = taraFor(profile.birthNakshatra, nak) ?: "—"
            CurrentInfo(g, lon, names[nak], tara, tara == "विपत" || tara == "प्रत्यारी" || tara == "वध")
        }
    }

    fun targetNakshatras(birthNakshatra: String, tara: String): List<String> {
        val birth = names.indexOf(birthNakshatra)
        val taraIndex = taraNames.indexOf(tara)
        if (birth < 0 || taraIndex < 0) return emptyList()
        return (0..26).filter { ((it - birth + 27) % 27) % 9 == taraIndex }.map { names[it] }
    }

    fun selectedTaraIndices(birthNakshatra: String, selected: Set<String>): Set<Int> {
        val birth = names.indexOf(birthNakshatra)
        if (birth < 0) return emptySet()
        val wanted = selected.mapNotNull { taraNames.indexOf(it).takeIf { i -> i >= 0 } }.toSet()
        return (0..26).filter { idx -> ((idx - birth + 27) % 27) % 9 in wanted }.toSet()
    }

    /** Find the next instant at which a planet ENTERS any selected Tara Nakshatra. */
    fun nextMatchingEntryMillis(
        graha: Graha,
        birthNakshatra: String,
        selected: Set<String>,
        fromMillis: Long,
        endMillis: Long
    ): Long? {
        if (fromMillis >= endMillis) return null
        val targets = selectedTaraIndices(birthNakshatra, selected)
        if (targets.isEmpty()) return null

        var t = fromMillis + 60_000L
        var previous = nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, t))
        val step = when (graha) {
            Graha.CHANDRA -> 2L * 60L * 60L * 1000L
            Graha.SURYA, Graha.MANGAL, Graha.BUDH, Graha.SHUKRA -> 12L * 60L * 60L * 1000L
            else -> 24L * 60L * 60L * 1000L
        }
        while (t < endMillis) {
            val next = minOf(t + step, endMillis)
            val current = nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, next))
            if (current != previous) {
                // There can be a boundary between t and next. Binary-search the
                // first millisecond where the Nakshatra index becomes current.
                val boundary = findBoundary(graha, previous, next, t, next)
                if (current in targets && boundary <= endMillis) return boundary
                previous = current
            }
            t = next
        }
        return null
    }

    private fun findBoundary(graha: Graha, previousIndex: Int, currentIndex: Int, lowStart: Long, highStart: Long): Long {
        var low = lowStart
        var high = highStart
        while (high - low > 1000L) {
            val mid = low + (high - low) / 2L
            val idx = nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, mid))
            if (idx == previousIndex) low = mid else high = mid
        }
        return high
    }


    /** Returns the end instant of the current Nakshatra transit (next boundary). */
    fun nextNakshatraBoundaryMillis(graha: Graha, fromMillis: Long): Long? {
        var low = fromMillis
        val current = nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, low))
        var high = low + when (graha) {
            Graha.CHANDRA -> 12L * 60L * 60L * 1000L
            Graha.SURYA, Graha.MANGAL, Graha.BUDH, Graha.SHUKRA -> 4L * 24L * 60L * 60L * 1000L
            else -> 15L * 24L * 60L * 60L * 1000L
        }
        while (high < fromMillis + 40L * 24L * 60L * 60L * 1000L &&
            nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, high)) == current) {
            high += when (graha) {
                Graha.CHANDRA -> 12L * 60L * 60L * 1000L
                Graha.SURYA, Graha.MANGAL, Graha.BUDH, Graha.SHUKRA -> 4L * 24L * 60L * 60L * 1000L
                else -> 15L * 24L * 60L * 60L * 1000L
            }
        }
        if (nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, high)) == current) return null
        while (high - low > 1000L) {
            val mid = low + (high - low) / 2L
            if (nakshatraIndex(LiveTransitCalculator.longitudeAt(graha, mid)) == current) low = mid else high = mid
        }
        return high
    }

    /** Finds the next selected-Tara Nakshatra interval, including its exact end boundary. */
    fun nextMatchingIntervalMillis(
        graha: Graha, birthNakshatra: String, selected: Set<String>, fromMillis: Long,
        endMillis: Long = fromMillis + 370L * 24L * 60L * 60L * 1000L
    ): Pair<Long, Long>? {
        val start = nextMatchingEntryMillis(graha, birthNakshatra, selected, fromMillis, endMillis) ?: return null
        val end = nextNakshatraBoundaryMillis(graha, start + 1000L) ?: return null
        return start to end
    }

    fun dateAt(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
    fun startOfDate(date: LocalDate): Long = date.atStartOfDay(zone).toInstant().toEpochMilli()
    fun endOfDate(date: LocalDate): Long = date.atTime(LocalTime.MAX).atZone(zone).toInstant().toEpochMilli()
    fun parseDate(raw: String): LocalDate? = runCatching { LocalDate.parse(raw.trim(), dateFormatter) }.getOrNull()
    fun formatDate(date: LocalDate): String = date.format(dateFormatter)
    fun degreeText(longitude: Double): String = String.format(java.util.Locale.US, "%.2f°", longitude % 30.0)
    private fun normalize(v: Double): Double = ((v % 360.0) + 360.0) % 360.0
}
