package com.example.data.api

import com.example.data.supabase.model.SupabaseToolAction
import com.example.model.AiFileChange
import com.example.model.AiFixSuggestion
import com.example.model.FileChangeAction
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utilitário de parsing de respostas da API Gemini com suporte a Multi-Action
 * (Arquivos Kotlin e Migrações/Políticas do Supabase).
 */
object GeminiResponseParser {

  fun parseResponseJson(
    rawText: String,
    activeFile: String,
    fallbackCode: String,
    query: String
  ): AiFixSuggestion {
    val clean = rawText
      .substringAfter("```json")
      .substringAfter("```")
      .substringBeforeLast("```")
      .trim()

    return try {
      val json = JSONObject(clean)
      val changes = parseFileChanges(json.optJSONArray("changes"), activeFile, fallbackCode)
      val supabaseActions = parseSupabaseActions(json.optJSONArray("supabaseActions"))

      val activeChange = changes.firstOrNull { it.filePath.endsWith(activeFile) } ?: changes.firstOrNull()

      AiFixSuggestion(
        issueTitle = json.optString("issueTitle", if (query.isNotBlank()) "Implementação: $query" else "Alteração de Código"),
        explanation = json.optString("explanation", "Arquivos Kotlin e ações de banco de dados gerados com sucesso."),
        fullFixedContent = activeChange?.fullContent ?: fallbackCode,
        changes = changes,
        supabaseActions = supabaseActions
      )
    } catch (_: Exception) {
      fallbackSuggestion(activeFile, fallbackCode, query)
    }
  }

  private fun parseFileChanges(
    array: JSONArray?,
    activeFile: String,
    fallbackCode: String
  ): List<AiFileChange> {
    if (array == null) return emptyList()
    val list = mutableListOf<AiFileChange>()
    for (i in 0 until array.length()) {
      val item = array.getJSONObject(i)
      val path = item.optString("filePath", activeFile)
      val action = if (item.optString("action", "MODIFY").uppercase() == "CREATE") {
        FileChangeAction.CREATE
      } else {
        FileChangeAction.MODIFY
      }
      list.add(
        AiFileChange(
          filePath = path,
          action = action,
          explanation = item.optString("explanation", ""),
          fullContent = item.optString("fullContent", fallbackCode),
          originalContent = if (action == FileChangeAction.MODIFY) fallbackCode else ""
        )
      )
    }
    return list
  }

  private fun parseSupabaseActions(array: JSONArray?): List<SupabaseToolAction> {
    if (array == null) return emptyList()
    val list = mutableListOf<SupabaseToolAction>()
    for (i in 0 until array.length()) {
      val item = array.getJSONObject(i)
      list.add(
        SupabaseToolAction(
          type = item.optString("type", "SQL_MIGRATION"),
          sql = item.optString("sql", ""),
          description = item.optString("description", "Ação de banco de dados no Supabase"),
          tableName = item.optString("tableName", null)
        )
      )
    }
    return list
  }

  fun fallbackSuggestion(fileName: String, code: String, query: String): AiFixSuggestion {
    return AiSmartGenerator.generateSuggestionForPrompt(fileName, code, query)
  }
}
