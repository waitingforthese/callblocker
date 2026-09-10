package com.mahaesuvidha.chandrapanchangalarm.settings

import android.content.Context
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfileStore
import com.mahaesuvidha.chandrapanchangalarm.model.Graha

/** Per-profile settings for transit-planet Vipat / Pratyari / Vadha Tara Aaradhana. */
class GocharTaraAaradhanaPrefs(private val context: Context) {
    private val prefs get() = context.getSharedPreferences("life_alarm_gochar_tara_aaradhana", Context.MODE_PRIVATE)
    private fun key(graha: Graha, suffix: String): String {
        val p = BirthProfileStore.load(context.applicationContext)
        val id = p?.let { "${it.name}|${it.birthDate}|${it.birthTime}|${it.birthPlace}" } ?: "default"
        return "${id.hashCode()}_${graha.name}_$suffix"
    }

    fun isEnabled(graha: Graha): Boolean = prefs.getBoolean(key(graha, "enabled"), false)
    fun setEnabled(graha: Graha, value: Boolean) = prefs.edit().putBoolean(key(graha, "enabled"), value).apply()

    fun vipat(graha: Graha): Boolean = prefs.getBoolean(key(graha, "vipat"), true)
    fun setVipat(graha: Graha, value: Boolean) = prefs.edit().putBoolean(key(graha, "vipat"), value).apply()

    fun pratyari(graha: Graha): Boolean = prefs.getBoolean(key(graha, "pratyari"), true)
    fun setPratyari(graha: Graha, value: Boolean) = prefs.edit().putBoolean(key(graha, "pratyari"), value).apply()

    fun vadha(graha: Graha): Boolean = prefs.getBoolean(key(graha, "vadha"), true)
    fun setVadha(graha: Graha, value: Boolean) = prefs.edit().putBoolean(key(graha, "vadha"), value).apply()

    fun startDate(graha: Graha): String = prefs.getString(key(graha, "start_date"), "") ?: ""
    fun setStartDate(graha: Graha, value: String) = prefs.edit().putString(key(graha, "start_date"), value).apply()

    fun endDate(graha: Graha): String = prefs.getString(key(graha, "end_date"), "") ?: ""
    fun setEndDate(graha: Graha, value: String) = prefs.edit().putString(key(graha, "end_date"), value).apply()

    fun selectedTaraNames(graha: Graha): List<String> = buildList {
        if (vipat(graha)) add("विपत")
        if (pratyari(graha)) add("प्रत्यारी")
        if (vadha(graha)) add("वध")
    }
}
