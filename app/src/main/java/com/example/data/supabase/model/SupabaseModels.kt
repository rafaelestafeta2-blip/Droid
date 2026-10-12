package com.example.data.supabase.model

/**
 * Configuração de acesso à API de Gerenciamento do Supabase.
 */
data class SupabaseConfig(
  val accessToken: String = "",
  val projectRef: String = "",
  val isConnected: Boolean = false
) {
  val isValid: Boolean get() = accessToken.isNotBlank() && projectRef.isNotBlank()
}

/**
 * Metadados de uma tabela no banco de dados Supabase.
 */
data class SupabaseTable(
  val name: String,
  val schema: String = "public",
  val rlsEnabled: Boolean = false,
  val columns: List<SupabaseColumn> = emptyList()
)

/**
 * Definição de coluna em uma tabela.
 */
data class SupabaseColumn(
  val name: String,
  val dataType: String,
  val isNullable: Boolean = true,
  val isPrimaryKey: Boolean = false
)

/**
 * Definição de uma política de Row Level Security (RLS).
 */
data class SupabaseRlsPolicy(
  val policyName: String,
  val tableName: String,
  val command: String = "ALL", // SELECT, INSERT, UPDATE, DELETE, ALL
  val roles: List<String> = listOf("authenticated", "anon"),
  val usingExpression: String = "true",
  val withCheckExpression: String? = null
)

/**
 * Resultado da execução de uma migração ou comando SQL.
 */
data class SupabaseMigrationResult(
  val success: Boolean,
  val message: String,
  val affectedRows: Int = 0,
  val errorDetails: String? = null
)

/**
 * Registro de log ou erro capturado do banco de dados Supabase.
 */
data class SupabaseDatabaseLog(
  val id: String,
  val timestamp: Long,
  val level: String, // error, warn, info
  val eventMessage: String,
  val query: String? = null
)

/**
 * Ação gerada pelo Gemini para operações de nuvem no Supabase.
 */
data class SupabaseToolAction(
  val type: String, // SQL_MIGRATION, CREATE_RLS, ENABLE_RLS, INSPECT_LOGS
  val sql: String,
  val description: String,
  val tableName: String? = null
)

/**
 * Diagnóstico automático de erro de RLS com proposta de correção.
 */
data class SupabaseRlsDiagnostic(
  val errorCode: String,
  val tableName: String,
  val attemptedOperation: String,
  val rootCause: String,
  val recommendedSqlFix: String,
  val isAutoFixable: Boolean = true
)
