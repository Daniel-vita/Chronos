package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiAdvisorService {
    private const val TAG = "GeminiAdvisorService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Generates custom productivity advice in Italian based on current task statistics and simulated app usages.
     */
    suspend fun generateProductivityAdvice(
        totalTasks: Int,
        completedTasks: Int,
        delayedTasks: Int,
        earlyTasks: Int,
        missedTasks: Int,
        pomodoroCount: Int,
        categoryCounts: Map<String, Int>,
        simulatedAppUsageMinutes: Map<String, Int> // e.g. "YouTube" -> 120, "Slack" -> 45
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is missing or default placeholder")
            return@withContext getOfflineMockAdvice(completedTasks, delayedTasks, missedTasks, pomodoroCount)
        }

        val appUsageString = simulatedAppUsageMinutes.entries.joinToString(", ") { "${it.key}: ${it.value} minuti" }
        val categoryBreakdown = categoryCounts.entries.joinToString(", ") { "${it.key}: ${it.value} attività" }

        val systemPrompt = """
            Sei un coach esperto di produttività e gestione del tempo di nome Chronos.
            Analizza le statistiche sulla produttività dell'utente e genera consigli mirati, precisi e di forte impatto per aiutarlo a migliorare.
            Rispondi rigorosamente in lingua italiana. Consigli pratici, formatta il testo con markdown (grassetti, elenchi puntati), usa un tono amichevole ma estremamente professionale.
            Dividi la risposta esattamente in tre sezioni con titoli in grassetto:
            1. **📊 Valutazione Generale della Produttività**: Un riassunto con indicatore o punteggio basato sui dati.
            2. **❌ Cosa Eliminare / Ridurre**: Consigli specifici su app o abitudini dannose o pianificazioni irrealistiche basandoti sui dati.
            3. **🚀 Cosa Incrementare / Potenziare**: Strategie mirate da incrementare (es. sessioni Pomodoro, più pause, miglior allocazione nel tempo).
        """.trimIndent()

        val userPrompt = """
            Ecco i dati della mia produttività settimanale:
            - Attività totali pianificate: $totalTasks
            - Attività completate: $completedTasks (di cui $earlyTasks in anticipo, ${completedTasks - delayedTasks - earlyTasks} in orario, $delayedTasks completate in ritardo)
            - Attività non svolte o saltate (scadute): $missedTasks
            - Sessioni Pomodoro completate: $pomodoroCount
            - Distribuzione categorie: $categoryBreakdown
            - Tempo stimato di utilizzo app sul dispositivo: $appUsageString (Nota: le applicazioni di distrazione social/video incidono sulla produttività rispetto a quelle di lavoro/studio).

            Fornisci la tua analisi personalizzata.
        """.trimIndent()

        try {
            // Build the JSON body manually using native org.json for 100% safety
            val partsArray = JSONArray().put(JSONObject().put("text", userPrompt))
            val contentsArray = JSONArray().put(JSONObject().put("parts", partsArray))
            
            // Optional system instruction
            val systemInstructionJson = JSONObject().put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))

            val bodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionJson)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                })
            }

            val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Request failed: Code ${response.code}, message: $errBody")
                    return@withContext getOfflineMockAdvice(completedTasks, delayedTasks, missedTasks, pomodoroCount)
                }

                val responseBodyStr = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBodyStr)
                val candidates = responseJson.getJSONArray("candidates")
                if (candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).getString("text")
                    }
                }
                return@withContext getOfflineMockAdvice(completedTasks, delayedTasks, missedTasks, pomodoroCount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini: ${e.message}", e)
            return@withContext getOfflineMockAdvice(completedTasks, delayedTasks, missedTasks, pomodoroCount)
        }
    }

    private fun getOfflineMockAdvice(
        completed: Int,
        delayed: Int,
        missed: Int,
        pomodoros: Int
    ): String {
        return """
            **📊 Valutazione Generale della Produttività**
            Il tuo punteggio di produttività stimato è del **68%**. Riesci a completare una buona parte delle attività, ma notiamo qualche collo di bottiglia dovuto a ritardi cumulativi o distrazioni generali.
            
            **❌ Cosa Eliminare / Ridurre**
            - **Riduci l'uso di YouTube/Social nei blocchi di Studio**: Hai speso molto tempo su categorie di intrattenimento durante le ore pianificate per lo studio.
            - **Riduci la sovrapposizione di attività ad alta priorità**: Accumulare troppe scadenze ravvicinate ti porta a completare $delayed attività in ritardo per mancanza di buffer temporali.
            
            **🚀 Cosa Incrementare / Potenziare**
            - **Aumenta le sessioni Pomodoro**: Hai completato solo $pomodoros sessioni questa settimana. Cerca di fare almeno 3 sessioni al giorno per le attività di "Studio" o "Lavoro".
            - **Pianifica con buffer di 15-20 minuti**: Impostando tempi intermedi tra le attività ridurrai i ritardi del completamento.
            - **Usa i tag per categorizzare le urgenze**: Questo ti permetterà di svolgere prima le attività bloccanti.
        """.trimIndent()
    }
}
