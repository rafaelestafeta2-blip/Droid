package com.example.data.api

import com.example.BuildConfig
import com.example.data.supabase.RlsDiagnosticEngine
import com.example.data.supabase.model.SupabaseRlsDiagnostic
import com.example.data.supabase.model.SupabaseToolAction
import com.example.model.AiFixSuggestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Serviço de integração com o Gemini com capacidades de Multi-Action:
 * Geração de código Kotlin e Tool Calling para ações em nuvem no Supabase.
 */
class GeminiAiService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  suspend fun analyzeAndFix(
    fileName: String,
    currentCode: String,
    errorLogs: String,
    modelName: String = "gemini-3.5-flash"
  ): AiFixSuggestion = withContext(Dispatchers.IO) {
    // Verificação de Auto-Diagnostic de RLS se o erro tiver padrão de permissão
    val rlsDiag = RlsDiagnosticEngine.diagnose(errorLogs)
    if (rlsDiag != null) {
      return@withContext createRlsDiagnosticSuggestion(rlsDiag, fileName, currentCode)
    }

    val prompt = GeminiPromptBuilder.buildCompilationFixPrompt(fileName, currentCode, errorLogs)
    callGeminiApi(prompt, fileName, currentCode, userQuery = "", modelName = modelName)
  }

  suspend fun generateFromCustomPrompt(
    fileName: String,
    currentCode: String,
    globalProjectIndex: String,
    userPrompt: String,
    hasSupabaseConfig: Boolean = false,
    modelName: String = "gemini-3.5-flash"
  ): AiFixSuggestion = withContext(Dispatchers.IO) {
    val prompt = GeminiPromptBuilder.buildCustomPrompt(
      userPrompt = userPrompt,
      globalProjectIndex = globalProjectIndex,
      fileName = fileName,
      currentCode = currentCode,
      hasSupabaseConfig = hasSupabaseConfig
    )
    callGeminiApi(prompt, fileName, currentCode, userQuery = userPrompt, modelName = modelName)
  }

  /**
   * Auto-Diagnóstico de RLS: Inspeciona a falha de permissão e cria sugestão com ação no Supabase.
   */
  fun diagnoseRls(errorMessage: String, fileName: String, currentCode: String): AiFixSuggestion? {
    val diagnostic = RlsDiagnosticEngine.diagnose(errorMessage) ?: return null
    return createRlsDiagnosticSuggestion(diagnostic, fileName, currentCode)
  }

  private fun createRlsDiagnosticSuggestion(
    diagnostic: SupabaseRlsDiagnostic,
    fileName: String,
    code: String
  ): AiFixSuggestion {
    return AiFixSuggestion(
      issueTitle = "Auto-Diagnóstico de RLS: ${diagnostic.tableName}",
      explanation = diagnostic.rootCause,
      fullFixedContent = code,
      changes = emptyList(),
      supabaseActions = listOf(
        SupabaseToolAction(
          type = "CREATE_RLS",
          sql = diagnostic.recommendedSqlFix,
          description = "Aplicar política permissiva de RLS para ${diagnostic.tableName}",
          tableName = diagnostic.tableName
        )
      ),
      supabaseDiagnostic = diagnostic
    )
  }

  private fun callGeminiApi(
    prompt: String,
    fileName: String,
    fallbackCode: String,
    userQuery: String,
    modelName: String
  ): AiFixSuggestion {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return GeminiResponseParser.fallbackSuggestion(fileName, fallbackCode, userQuery)
    }

    try {
      val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
      val body = JSONObject().apply {
        put("contents", JSONArray().apply {
          put(JSONObject().apply {
            put("parts", JSONArray().apply { put(JSONObject().apply { put("text", prompt) }) })
          })
        })
      }
      val request = Request.Builder()
        .url(url)
        .post(body.toString().toRequestBody("application/json".toMediaType()))
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        return GeminiResponseParser.fallbackSuggestion(fileName, fallbackCode, userQuery)
      }

      val text = response.body?.string().orEmpty()
      val json = JSONObject(text)
      val rawAiText = json.optJSONArray("candidates")?.optJSONObject(0)
        ?.optJSONObject("content")?.optJSONArray("parts")?.optJSONObject(0)?.optString("text").orEmpty()

      return GeminiResponseParser.parseResponseJson(rawAiText, fileName, fallbackCode, userQuery)
    } catch (_: Exception) {
      return GeminiResponseParser.fallbackSuggestion(fileName, fallbackCode, userQuery)
    }
  }
}
