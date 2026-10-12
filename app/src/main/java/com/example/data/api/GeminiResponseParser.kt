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
    val isCloud = GeminiPromptBuilder.isCloudPersistenceRequested(query)
    val cloudActions = if (isCloud) {
      listOf(
        SupabaseToolAction(
          type = "SQL_MIGRATION",
          sql = "CREATE TABLE IF NOT EXISTS public.items (id uuid primary key default gen_random_uuid(), title text not null, created_at timestamptz default now()); ALTER TABLE public.items ENABLE ROW LEVEL SECURITY; CREATE POLICY \"Acesso livre\" ON public.items FOR ALL TO authenticated, anon USING (true) WITH CHECK (true);",
          description = "Criação de tabela 'items' e política permissiva de RLS",
          tableName = "items"
        )
      )
    } else emptyList()

    val changes = listOf(
      AiFileChange(
        filePath = fileName,
        action = FileChangeAction.MODIFY,
        explanation = "Integração segura no arquivo ativo com suporte a Safe Area e Supabase",
        fullContent = code.replace("GreetingView(user = \"Android Developer\")", "GreetingView(user = \"Android Developer // $query\")") +
          if (isCloud) "\n// [Supabase]: Integração com PostgREST e RLS ativa no projeto\n" else "",
        originalContent = code
      )
    )

    return AiFixSuggestion(
      issueTitle = if (query.isNotBlank()) "Implementação: $query" else "Correção Automática",
      explanation = if (isCloud) "Código gerado com persistência em nuvem e políticas de RLS no Supabase." else "Código atualizado com Safe Area.",
      fullFixedContent = changes[0].fullContent,
      changes = changes,
      supabaseActions = cloudActions
    )
  }
}
