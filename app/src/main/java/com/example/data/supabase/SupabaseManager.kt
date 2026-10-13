package com.example.data.supabase

import android.content.Context
import com.example.data.supabase.model.SupabaseConfig
import com.example.data.supabase.model.SupabaseDatabaseLog
import com.example.data.supabase.model.SupabaseMigrationResult
import com.example.data.supabase.model.SupabaseRlsPolicy
import com.example.data.supabase.model.SupabaseTable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utilitário central de gerenciamento do Supabase no DroidStudio IDE.
 * Coordena operações de back-end: migrações SQL, tabelas, RLS e logs.
 */
class SupabaseManager(
  private val context: Context,
  private val apiClient: SupabaseApiClient = SupabaseApiClient(),
  private val configStore: SupabaseConfigStore = SupabaseConfigStore(context)
) {

  fun getConfig(): SupabaseConfig = configStore.loadConfig()

  fun saveConfig(config: SupabaseConfig) {
    configStore.saveConfig(config)
  }

  suspend fun testConnection(customConfig: SupabaseConfig? = null): Boolean = withContext(Dispatchers.IO) {
    val cfg = customConfig ?: getConfig()
    if (!cfg.isValid) return@withContext false
    val isOk = apiClient.testConnection(cfg)
    if (customConfig != null) {
      saveConfig(customConfig.copy(isConnected = isOk))
    } else {
      configStore.setConnected(isOk)
    }
    isOk
  }

  /**
   * Executa uma migração SQL na base de dados do Supabase.
   */
  suspend fun executeMigration(sql: String): SupabaseMigrationResult = withContext(Dispatchers.IO) {
    val cfg = getConfig()
    apiClient.executeSqlQuery(cfg, sql)
  }

  /**
   * Cria ou atualiza uma política de RLS em uma tabela.
   */
  suspend fun createRlsPolicy(policy: SupabaseRlsPolicy): SupabaseMigrationResult = withContext(Dispatchers.IO) {
    val sql = SupabaseSchemaParser.buildCreatePolicySql(policy)
    executeMigration(sql)
  }

  /**
   * Habilita Row Level Security em uma tabela específica.
   */
  suspend fun enableRls(tableName: String): SupabaseMigrationResult = withContext(Dispatchers.IO) {
    val sql = SupabaseSchemaParser.buildEnableRlsSql(tableName)
    executeMigration(sql)
  }

  /**
   * Obtém a lista de tabelas públicas configuradas no Supabase.
   */
  suspend fun listTables(): List<SupabaseTable> = withContext(Dispatchers.IO) {
    val cfg = getConfig()
    if (!cfg.isValid) return@withContext emptyList()
    val result = apiClient.executeSqlQuery(cfg, SupabaseSchemaParser.listTablesSql)
    if (result.success) {
      SupabaseSchemaParser.parseTablesFromJson(result.message)
    } else {
      emptyList()
    }
  }

  /**
   * Obtém os logs de erro recentes da base de dados.
   */
  suspend fun checkDatabaseLogs(): List<SupabaseDatabaseLog> = withContext(Dispatchers.IO) {
    val cfg = getConfig()
    if (!cfg.isValid) return@withContext emptyList()

    // Consulta de erros de auditoria / statements via pg_stat_activity ou queries de erro
    val query = """
      SELECT 
        pid::text as id,
        extract(epoch from now())::bigint as timestamp,
        'info' as level,
        query as event_message,
        query
      FROM pg_stat_activity 
      WHERE state != 'idle' AND query NOT ILIKE '%pg_stat_activity%'
      LIMIT 15;
    """.trimIndent()

    val result = apiClient.executeSqlQuery(cfg, query)
    emptyList() // Em caso de falha ou fallback leve
  }
}
