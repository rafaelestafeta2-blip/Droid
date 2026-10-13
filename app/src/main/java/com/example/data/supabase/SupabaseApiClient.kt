package com.example.data.supabase

import com.example.data.supabase.model.SupabaseConfig
import com.example.data.supabase.model.SupabaseMigrationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP para a Supabase Management API e Project REST API.
 */
class SupabaseApiClient(
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(20, TimeUnit.SECONDS)
    .readTimeout(20, TimeUnit.SECONDS)
    .build()
) {
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  /**
   * Limpa o Project Reference para garantir que URLs completas como
   * "https://abcdef.supabase.co" sejam convertidas apenas no ID "abcdef".
   */
  fun sanitizeProjectRef(raw: String): String {
    var cleaned = raw.trim()
    cleaned = cleaned.removePrefix("https://").removePrefix("http://")
    if (cleaned.contains(".")) {
      cleaned = cleaned.substringBefore(".")
    }
    return cleaned.trimEnd('/')
  }

  /**
   * Executa uma query SQL arbitrária via Management API.
   * Endpoint: POST https://api.supabase.com/v1/projects/{ref}/database/query
   */
  suspend fun executeSqlQuery(
    config: SupabaseConfig,
    sql: String
  ): SupabaseMigrationResult = withContext(Dispatchers.IO) {
    val cleanRef = sanitizeProjectRef(config.projectRef)
    val token = config.accessToken.trim()

    if (cleanRef.isBlank() || token.isBlank()) {
      return@withContext SupabaseMigrationResult(
        success = false,
        message = "Configuração do Supabase incompleta (Token ou Project Ref ausente)"
      )
    }

    val url = "$BASE_URL/projects/$cleanRef/database/query"
    val payload = JSONObject().apply {
      put("query", sql)
    }

    val request = Request.Builder()
      .url(url)
      .addHeader("Authorization", "Bearer $token")
      .addHeader("Content-Type", "application/json")
      .post(payload.toString().toRequestBody(jsonMediaType))
      .build()

    try {
      val response = okHttpClient.newCall(request).execute()
      val responseBody = response.body?.string().orEmpty()

      if (response.isSuccessful) {
        SupabaseMigrationResult(
          success = true,
          message = "Query executada com sucesso no Supabase.",
          affectedRows = parseAffectedRows(responseBody)
        )
      } else {
        val errorMessage = parseErrorMessage(responseBody, response.code)
        SupabaseMigrationResult(
          success = false,
          message = "Erro ao executar query no Supabase (HTTP ${response.code})",
          errorDetails = errorMessage
        )
      }
    } catch (e: IOException) {
      SupabaseMigrationResult(
        success = false,
        message = "Falha de rede ao conectar à API do Supabase: ${e.message}",
        errorDetails = e.localizedMessage
      )
    } catch (e: Exception) {
      SupabaseMigrationResult(
        success = false,
        message = "Erro inesperado: ${e.message}",
        errorDetails = e.localizedMessage
      )
    }
  }

  /**
   * Testa a conectividade com o projeto Supabase.
   * Suporta tanto Personal Access Tokens (Management API) quanto chaves do projeto (REST/Auth API).
   */
  suspend fun testConnection(config: SupabaseConfig): Boolean = withContext(Dispatchers.IO) {
    val cleanRef = sanitizeProjectRef(config.projectRef)
    val token = config.accessToken.trim()
    if (cleanRef.isBlank() || token.isBlank()) return@withContext false

    // 1. Tentar Management API se o token for Personal Access Token (sbp_...)
    if (token.startsWith("sbp_")) {
      val mgmtResult = executeSqlQuery(config.copy(projectRef = cleanRef, accessToken = token), "SELECT 1 as ping;")
      if (mgmtResult.success) return@withContext true
    }

    // 2. Tentar Auth Health Endpoint do projeto (funciona com sb_secret, service_role, anon)
    try {
      val healthUrl = "https://$cleanRef.supabase.co/auth/v1/health"
      val request = Request.Builder()
        .url(healthUrl)
        .addHeader("apikey", token)
        .addHeader("Authorization", "Bearer $token")
        .get()
        .build()
      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful || response.code == 200) {
        return@withContext true
      }
    } catch (_: Exception) {}

    // 3. Tentar REST Root Endpoint
    try {
      val restUrl = "https://$cleanRef.supabase.co/rest/v1/"
      val request = Request.Builder()
        .url(restUrl)
        .addHeader("apikey", token)
        .addHeader("Authorization", "Bearer $token")
        .get()
        .build()
      val response = okHttpClient.newCall(request).execute()
      if (response.isSuccessful || response.code in 200..299) {
        return@withContext true
      }
    } catch (_: Exception) {}

    // 4. Fallback final via Management API query
    val fallback = executeSqlQuery(config.copy(projectRef = cleanRef, accessToken = token), "SELECT 1 as ping;")
    fallback.success
  }

  private fun parseAffectedRows(responseBody: String): Int {
    return try {
      if (responseBody.trim().startsWith("[")) {
        JSONArray(responseBody).length()
      } else if (responseBody.trim().startsWith("{")) {
        val obj = JSONObject(responseBody)
        obj.optInt("count", 1)
      } else {
        1
      }
    } catch (_: Exception) {
      1
    }
  }

  private fun parseErrorMessage(body: String, statusCode: Int): String {
    return try {
      val json = JSONObject(body)
      json.optString("error", json.optString("message", "HTTP $statusCode: $body"))
    } catch (_: Exception) {
      if (body.isNotBlank()) body else "Código HTTP: $statusCode"
    }
  }

  companion object {
    private const val BASE_URL = "https://api.supabase.com/v1"
  }
}
