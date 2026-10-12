package com.example.data.supabase

import com.example.data.supabase.model.SupabaseColumn
import com.example.data.supabase.model.SupabaseDatabaseLog
import com.example.data.supabase.model.SupabaseRlsPolicy
import com.example.data.supabase.model.SupabaseTable
import org.json.JSONArray
import org.json.JSONObject

/**
 * Utilitário para montagem de queries de metadados e conversão de resultados
 * retornados pela Management API do Supabase.
 */
object SupabaseSchemaParser {

  val listTablesSql: String = """
    SELECT 
      t.table_name,
      t.table_schema,
      c.relrowsecurity as rls_enabled
    FROM information_schema.tables t
    JOIN pg_class c ON c.relname = t.table_name
    WHERE t.table_schema = 'public'
    ORDER BY t.table_name;
  """.trimIndent()

  val listPoliciesSql: String = """
    SELECT 
      policyname,
      tablename,
      cmd,
      roles::text,
      qual,
      with_check
    FROM pg_policies
    WHERE schemaname = 'public'
    ORDER BY tablename, policyname;
  """.trimIndent()

  fun buildCreatePolicySql(policy: SupabaseRlsPolicy): String {
    val rolesList = policy.roles.joinToString(", ")
    val checkClause = if (!policy.withCheckExpression.isNullOrBlank()) {
      " WITH CHECK (${policy.withCheckExpression})"
    } else ""

    return """
      ALTER TABLE public.${policy.tableName} ENABLE ROW LEVEL SECURITY;
      DROP POLICY IF EXISTS "${policy.policyName}" ON public.${policy.tableName};
      CREATE POLICY "${policy.policyName}" ON public.${policy.tableName}
        FOR ${policy.command}
        TO $rolesList
        USING (${policy.usingExpression})$checkClause;
    """.trimIndent()
  }

  fun buildEnableRlsSql(tableName: String): String {
    return "ALTER TABLE public.$tableName ENABLE ROW LEVEL SECURITY;"
  }

  fun parseTablesFromJson(jsonArrayStr: String): List<SupabaseTable> {
    val result = mutableListOf<SupabaseTable>()
    try {
      val array = JSONArray(jsonArrayStr)
      for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        result.add(
          SupabaseTable(
            name = item.optString("table_name", "tabela"),
            schema = item.optString("table_schema", "public"),
            rlsEnabled = item.optBoolean("rls_enabled", false)
          )
        )
      }
    } catch (_: Exception) {}
    return result
  }

  fun parsePoliciesFromJson(jsonArrayStr: String): List<SupabaseRlsPolicy> {
    val result = mutableListOf<SupabaseRlsPolicy>()
    try {
      val array = JSONArray(jsonArrayStr)
      for (i in 0 until array.length()) {
        val item = array.getJSONObject(i)
        result.add(
          SupabaseRlsPolicy(
            policyName = item.optString("policyname", ""),
            tableName = item.optString("tablename", ""),
            command = item.optString("cmd", "ALL"),
            usingExpression = item.optString("qual", "true"),
            withCheckExpression = item.optString("with_check", null)
          )
        )
      }
    } catch (_: Exception) {}
    return result
  }
}
