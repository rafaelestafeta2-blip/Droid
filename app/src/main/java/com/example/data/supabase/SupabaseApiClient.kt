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
 * Cliente HTTP para a Supabase Management API (https://api.supabase.com/v1).
 * Responsabilidade única: Execução de chamadas REST e consultas SQL remotas.
 */
class SupabaseApiClient(
  private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(25, TimeUnit.SECONDS)
    .readTimeout(25, TimeUnit.SECONDS)
    .build()
) {
  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  /**
   * Executa uma query SQL arbitrária (DDL, DML, Migração) via Management API.
   * Endpoint: POST https://api.supabase.com/v1/projects/{ref}/database/query
   */
  suspend fun executeSqlQuery(
    config: SupabaseConfig,
    sql: String
  ): SupabaseMigrationResult = withContext(Dispatchers.IO) {
    if (!config.isValid) {
      return@withContext SupabaseMigrationResult(
        success = false,
        message = "Configuração do Supabase incompleta (Token ou Ref ausente)"
      )
    }

    val url = "$BASE_URL/projects/${config.projectRef}/database/query"
    val payload = JSONObject().apply {
      put("query", sql)
    }

    val request = Request.Builder()
      .url(url)
      .addHeader("Authorization", "Bearer ${config.accessToken}")
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
   * Testa a conectividade com o projeto Supabase executando um 'SELECT 1;'.
   */
  suspend fun testConnection(config: SupabaseConfig): Boolean = withContext(Dispatchers.IO) {
    val result = executeSqlQuery(config, "SELECT 1 as ping;")
    result.success
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
