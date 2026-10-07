package com.example.ai

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

data class ChatMessage(
    val sender: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class MunicipalStats(
    val totalHouseholds: Int,
    val collectedToday: Int,
    val coveragePercent: Double,
    val segregationRate: Double,
    val wasteRecoveredTons: Double,
    val costRecoveryPercent: Double,
    val openComplaints: Int,
    val resolvedComplaints: Int
)

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val modelName = "gemini-3.5-flash"
    private val endpointUrl = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent"

    suspend fun chatAssistant(
        userMessage: String,
        language: String,
        history: List<ChatMessage>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackChatbot(userMessage, language)
        }

        try {
            val rootJson = JSONObject()
            val contentsArray = JSONArray()

            val systemObj = JSONObject().apply {
                val parts = JSONArray().apply {
                    put(JSONObject().put("text", """
                        You are the helpful CleanCity Municipal Waste Assistant for a clean city.
                        Respond to citizens in ${language} (support English, Telugu, Hindi, Kannada, Tamil).
                        Knowledge base:
                        - Monthly waste charge: Residential 90 rupees/month, Commercial shops 350 rupees/month, Restaurants 850 rupees/month.
                        - Pickup timings: Door-to-door is 7:00 AM to 9:30 AM daily.
                        - Two-bin rule: Green Bin (organic kitchen wet waste, fruit peels, vegetable scraps, leftover food), Blue Bin (dry recyclable paper, plastics, tins, cardboards). Sanitary and batteries go in a separate Red bag/hazardous bin.
                        - Uncollected waste reporting: Citizens can upload a photo of uncollected trash and capture their GPS location in the 'Report Waste' tab.
                        - Redressal SLA: Reported waste is inspected and cleared within a 24-hour window.
                        IMPORTANT: Do not use any emojis in your response. Keep answers concise, clear, and professional.
                    """.trimIndent()))
                }
                put("parts", parts)
            }
            rootJson.put("systemInstruction", systemObj)

            history.takeLast(4).forEach { msg ->
                val turn = JSONObject().apply {
                    val p = JSONArray().apply { put(JSONObject().put("text", msg.text)) }
                    put("role", if (msg.sender == "user") "user" else "model")
                    put("parts", p)
                }
                contentsArray.put(turn)
            }

            val currentTurn = JSONObject().apply {
                val p = JSONArray().apply { put(JSONObject().put("text", userMessage)) }
                put("role", "user")
                put("parts", p)
            }
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$endpointUrl?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val respJson = JSONObject(responseBody)
                val text = respJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext stripEmojis(text)
                }
            }
        } catch (_: Exception) {
            // Fallback
        }

        fallbackChatbot(userMessage, language)
    }

    suspend fun generateExecutiveSummary(stats: MunicipalStats): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext fallbackSummary(stats)
        }

        try {
            val prompt = """
                You are Chief Municipal Sanitation Advisor.
                Turn these raw city waste monitoring metrics into an executive briefing for the City Municipal Commissioner:
                - Total registered households: ${stats.totalHouseholds}
                - Households collected today: ${stats.collectedToday}
                - Route Coverage: ${String.format("%.1f", stats.coveragePercent)}%
                - Source Segregation Rate: ${String.format("%.1f", stats.segregationRate)}%
                - Daily Waste Recovered: ${stats.wasteRecoveredTons} Metric Tonnes
                - Cost Recovery: ${String.format("%.1f", stats.costRecoveryPercent)}%
                - Open Waste Reports: ${stats.openComplaints}
                - Resolved within 24h: ${stats.resolvedComplaints}

                Generate a 3-paragraph executive summary without emojis:
                1. Operational highlights and city sanitation progress
                2. Key concerns such as uncollected waste hotspots or pending reports
                3. Three actionable recommendations for field inspectors.
            """.trimIndent()

            val rootJson = JSONObject().apply {
                val cArray = JSONArray().apply {
                    val cObj = JSONObject().apply {
                        val pArray = JSONArray().apply { put(JSONObject().put("text", prompt)) }
                        put("parts", pArray)
                    }
                    put(cObj)
                }
                put("contents", cArray)
            }

            val requestBody = rootJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$endpointUrl?key=$apiKey")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val respJson = JSONObject(responseBody)
                val text = respJson.optJSONArray("candidates")
                    ?.optJSONObject(0)
                    ?.optJSONObject("content")
                    ?.optJSONArray("parts")
                    ?.optJSONObject(0)
                    ?.optString("text")
                if (!text.isNullOrBlank()) {
                    return@withContext stripEmojis(text)
                }
            }
        } catch (_: Exception) {
            // Fallback
        }

        fallbackSummary(stats)
    }

    private fun fallbackChatbot(message: String, language: String): String {
        val lower = message.lowercase()

        if (language.equals("Telugu", ignoreCase = true) || language.contains("తెలుగు")) {
            return when {
                lower.contains("fee") || lower.contains("charge") || lower.contains("bill") || lower.contains("cost") || lower.contains("pay") || lower.contains("ధర") || lower.contains("రుసుము") || lower.contains("బిల్లు") -> {
                    "క్లీన్‌సిటీ నెలవారీ మున్సిపల్ రుసుములు:\n- నివాస గృహాలు: నెలకు రూ. 90\n- వాణిజ్య దుకాణాలు: నెలకు రూ. 350\n- హోటళ్ళు / రెస్టారెంట్లు: నెలకు రూ. 850.\nమీరు 'Bills' విభాగంలో UPI ద్వారా చెల్లించి తక్షణమే డిజిటల్ రసీదు పొందవచ్చు."
                }
                lower.contains("time") || lower.contains("schedule") || lower.contains("when") || lower.contains("truck") || lower.contains("morning") || lower.contains("సమయం") || lower.contains("ఎప్పుడు") -> {
                    "డోర్-టు-డోర్ చెత్త సేకరణ ప్రతిరోజూ ఉదయం 7:00 నుండి 9:30 వరకు జరుగుతుంది. చెత్త సేకరించే సిబ్బంది మీ ఇంటికి సమీపంలో ఉన్నప్పుడు అలర్ట్ వస్తుంది."
                }
                lower.contains("rule") || lower.contains("segregat") || lower.contains("bin") || lower.contains("color") || lower.contains("డబ్బా") || lower.contains("చెత్త") -> {
                    "రెండు డబ్బాల విధానం (Two-Bin Standard):\n1. ఆకుపచ్చ డబ్బా (Green Bin): తడి చెత్త (కూరగాయలు, పండ్ల వ్యర్థాలు, ఆహార వ్యర్థాలు).\n2. నీలం డబ్బా (Blue Bin): పొడి చెత్త (కాగితం, ప్లాస్టిక్ పౌచ్‌లు, బాటిళ్ళు, డబ్బాలు).\n3. ఎరుపు బ్యాగ్ (Red Bag): ప్రమాదకర వ్యర్థాలు (డైపర్లు, గడువు ముగిసిన మందులు, బ్యాటరీలు)."
                }
                lower.contains("report") || lower.contains("trash") || lower.contains("uncollected") || lower.contains("dump") || lower.contains("photo") || lower.contains("ఫిర్యాదు") -> {
                    "రహదారిపై ఉన్న చెత్తను రిపోర్ట్ చేయడానికి దిగువన ఉన్న 'Report Waste' ట్యాబ్‌ను ఉపయోగించండి. ఫోటో తీసి, GPS లొకేషన్ ఎంచుకుని సమర్పించండి. 24 గంటల్లోగా పరిష్కరించబడుతుంది."
                }
                else -> {
                    "నమస్కారం! నేను క్లీన్‌సిటీ మున్సిపల్ అసిస్టెంట్‌ని. నేను మీకు ఈ వివరాలను అందించగలను:\n1. చెత్త సేకరణ సమయాలు\n2. ఆకుపచ్చ మరియు నీలం డబ్బాల నియమాలు\n3. చెత్త రిపోర్టింగ్ విధానం (ఫోటో & GPS)\n4. నెలవారీ యూజర్ ఫీజుల చెల్లింపు\nమీకు ఏ సమాచారం కావాలి?"
                }
            }
        }

        return when {
            lower.contains("fee") || lower.contains("charge") || lower.contains("bill") || lower.contains("cost") || lower.contains("pay") -> {
                "CleanCity Monthly User Charges:\n- Residential Households: 90 rupees/month\n- Commercial Shops: 350 rupees/month\n- Bulk Generators (Hotels/Restaurants): 850 rupees/month.\nYou can pay via UPI in the Bills tab and receive an instant digital receipt."
            }
            lower.contains("time") || lower.contains("schedule") || lower.contains("when") || lower.contains("truck") || lower.contains("morning") -> {
                "Door-to-door waste collection operates between 7:00 AM and 9:30 AM daily in Ward 42. You will receive an arrival alert when the collector is within 400 meters."
            }
            lower.contains("rule") || lower.contains("segregat") || lower.contains("bin") || lower.contains("color") -> {
                "Two-Bin One-Bag Standard:\n1. Green Bin: Organic wet waste (kitchen scraps, food leftovers, garden trimmings).\n2. Blue Bin: Clean dry recyclables (paper, plastic pouches, bottles, cartons).\n3. Red Bag: Domestic sanitary and hazardous waste (batteries, diapers, expired medicines)."
            }
            lower.contains("report") || lower.contains("trash") || lower.contains("uncollected") || lower.contains("dump") || lower.contains("photo") -> {
                "To report uncollected trash, tap the 'Report Waste' tab at the bottom. Take or upload a photo, tap 'Use Current Location' to pin your GPS coordinates, and submit. Our ward team has a 24-hour SLA to inspect and clear the site."
            }
            else -> {
                "Hello, I am CleanCity Assistant. I can assist you with:\n1. Door-to-door pickup schedules\n2. Segregation rules for Green, Blue, and Red bins\n3. Reporting uncollected trash with photo and GPS\n4. Monthly user fee payments\nWhat would you like to know?"
            }
        }
    }

    private fun fallbackSummary(stats: MunicipalStats): String {
        return """
            EXECUTIVE MUNICIPAL PERFORMANCE BRIEFING
            Ward Level Solid Waste Management Operations

            1. Operational Benchmarks and Coverage:
            The ward recorded ${String.format("%.1f", stats.coveragePercent)}% door-to-door coverage today, servicing ${stats.collectedToday} out of ${stats.totalHouseholds} registered properties. Segregation at source stands at ${String.format("%.1f", stats.segregationRate)}%, diverting ${stats.wasteRecoveredTons} MT of organic and recyclable waste away from dumping yards.

            2. Critical Alerts and Grievance Redressal:
            Cost recovery is tracking at ${String.format("%.1f", stats.costRecoveryPercent)}% of operating expenditure. Out of ${stats.openComplaints + stats.resolvedComplaints} public waste reports received, ${stats.resolvedComplaints} were cleared within the 24-hour SLA. ${stats.openComplaints} reports are currently in progress.

            3. Strategic Directives for Field Supervisors:
            - Prioritize uncollected waste spots logged via citizen photo reports.
            - Ensure commercial food zones maintain separate wet bins to avoid mixed waste penalties.
            - Conduct daily PPE checks for collection crews before morning route dispatch.
        """.trimIndent()
    }

    private fun stripEmojis(text: String): String {
        return text.replace(Regex("[\\p{So}\\p{Cn}]"), "").trim()
    }
}
