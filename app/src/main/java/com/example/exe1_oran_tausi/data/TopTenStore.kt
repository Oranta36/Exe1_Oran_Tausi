package com.example.exe1_oran_tausi.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ScoreEntry(
    val playerName: String,
    val km: Double,
    val coins: Int,
    val lat: Double = 0.0,
    val lon: Double = 0.0,
    val atMillis: Long = System.currentTimeMillis()
)

object TopTenStore {

    private const val PREFS_NAME = "top_ten_prefs"
    private const val KEY_ALL_SCORES = "scores_all"

    fun addRun(
        context: Context,
        playerName: String,
        km: Double,
        coins: Int,
        lat: Double = 0.0,
        lon: Double = 0.0
    ) {
        val name = playerName.trim()
        if (name.isBlank()) return

        val scores = loadTop10Global(context).toMutableList()

        scores.add(
            ScoreEntry(
                playerName = name,
                km = km,
                coins = coins,
                lat = lat,
                lon = lon
            )
        )

        val top10 = scores
            .sortedByDescending { it.km }
            .take(10)

        saveScores(context, top10)
    }

    fun loadTop10Global(context: Context): List<ScoreEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_ALL_SCORES, "[]") ?: "[]"

        val type = object : TypeToken<List<ScoreEntry>>() {}.type
        return Gson().fromJson<List<ScoreEntry>>(json, type) ?: emptyList()
    }

    private fun saveScores(context: Context, scores: List<ScoreEntry>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ALL_SCORES, Gson().toJson(scores))
            .apply()
    }
}
