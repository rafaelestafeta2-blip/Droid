package com.example.data.supabase

import com.example.data.supabase.model.SupabaseRlsDiagnostic
import java.util.regex.Pattern

/**
 * Motor de diagnóstico automático de erros de Row Level Security (RLS) e permissões do Supabase.
 * Analisa logs e mensagens de erro do PostgreSQL/PostgREST e gera recomendações com SQL executável.
 */
object RlsDiagnosticEngine {

  private val TABLE_RLS_REGEX = Pattern.compile(
    """(?:violates row-level security policy for table\s+["']?([a-zA-Z0-9_]+)["']?|permission denied for (?:table|relation)\s+["']?([a-zA-Z0-9_]+)["']?)""",
    Pattern.CASE_INSENSITIVE
  )

  /**
   * Avalia uma linha de log ou exceção para determinar se é uma falha de RLS.
   */
  fun diagnose(errorMessage: String): SupabaseRlsDiagnostic? {
    if (!isRlsOrPermissionError(errorMessage)) return null

    val matcher = TABLE_RLS_REGEX.matcher(errorMessage)
    val tableName = if (matcher.find()) {
      matcher.group(1) ?: matcher.group(2) ?: "tabela_afetada"
    } else {
      extractTableFallback(errorMessage)
    }

    val operation = detectOperation(errorMessage)
    val rootCause = "A tabela '$tableName' possui Row Level Security (RLS) ativo no Supabase, " +
      "mas não existe política permitindo operações de $operation para os papéis 'authenticated' ou 'anon'."

    val recommendedSql = """
      -- Ajuste de RLS gerado pelo DroidStudio
      ALTER TABLE public.$tableName ENABLE ROW LEVEL SECURITY;
      
      DROP POLICY IF EXISTS "droidstudio_auto_policy_$tableName" ON public.$tableName;
      
      CREATE POLICY "droidstudio_auto_policy_$tableName" ON public.$tableName
        FOR ALL
        TO authenticated, anon
        USING (true)
        WITH CHECK (true);
    """.trimIndent()

    return SupabaseRlsDiagnostic(
      errorCode = if (errorMessage.contains("42501")) "42501" else "RLS_VIOLATION",
      tableName = tableName,
      attemptedOperation = operation,
      rootCause = rootCause,
      recommendedSqlFix = recommendedSql,
      isAutoFixable = true
    )
  }

  fun isRlsOrPermissionError(msg: String): Boolean {
    val lower = msg.lowercase()
    return lower.contains("row-level security") ||
      lower.contains("permission denied for table") ||
      lower.contains("permission denied for relation") ||
      lower.contains("code: 42501") ||
      lower.contains("pgrst301") ||
      lower.contains("violates row-level security policy")
  }

  private fun detectOperation(msg: String): String {
    val upper = msg.uppercase()
    return when {
      upper.contains("INSERT") -> "INSERT"
      upper.contains("SELECT") -> "SELECT"
      upper.contains("UPDATE") -> "UPDATE"
      upper.contains("DELETE") -> "DELETE"
      else -> "ACESSO GERAL (ALL)"
    }
  }

  private fun extractTableFallback(msg: String): String {
    val words = msg.split(" ", "\"", "'")
    val tableIdx = words.indexOfFirst { it.equals("table", ignoreCase = true) }
    if (tableIdx != -1 && tableIdx + 1 < words.size) {
      return words[tableIdx + 1].trim('"', '\'', '.', ',')
    }
    return "tabela"
  }
}
