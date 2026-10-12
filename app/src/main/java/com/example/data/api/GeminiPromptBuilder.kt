package com.example.data.api

/**
 * Construtor modular de prompts estruturados para o Gemini no DroidStudio IDE.
 * Inclui suporte a Tool Calling / Multi-Action para Supabase e Jetpack Compose.
 */
object GeminiPromptBuilder {

  fun buildCustomPrompt(
    userPrompt: String,
    globalProjectIndex: String,
    fileName: String,
    currentCode: String,
    hasSupabaseConfig: Boolean
  ): String {
    val cloudInstruction = if (hasSupabaseConfig || isCloudPersistenceRequested(userPrompt)) {
      """
      SUPABASE & PERSISTÊNCIA EM NUVEM (TOOL CALLING / MULTI-ACTION):
      O usuário está solicitando recursos com persistência ou backend na nuvem.
      Você DEVE gerar tanto as ações de banco de dados (tabelas, colunas, RLS) quanto o código Kotlin correspondente para Android.
      Adicione no JSON o array "supabaseActions" com as migrações SQL necessárias:
      "supabaseActions": [
        {
          "type": "SQL_MIGRATION",
          "tableName": "nome_tabela",
          "description": "Cria tabela e política de RLS",
          "sql": "CREATE TABLE IF NOT EXISTS public.nome_tabela (id uuid primary key default gen_random_uuid(), created_at timestamptz default now()); ALTER TABLE public.nome_tabela ENABLE ROW LEVEL SECURITY; CREATE POLICY \"Permitir acesso\" ON public.nome_tabela FOR ALL TO authenticated, anon USING (true) WITH CHECK (true);"
        }
      ]
      """.trimIndent()
    } else ""

    return """
Você é um Arquiteto de Software Android especialista em Kotlin, Jetpack Compose e Supabase.
O desenvolvedor solicitou: "$userPrompt"

CONTEXTO GLOBAL DO PROJETO:
$globalProjectIndex

ARQUIVO ATIVO ATUAL ($fileName):
$currentCode

$cloudInstruction

DIRETRIZES OBRIGATÓRIAS:
1. SAFE AREA & WINDOW INSETS:
   - Respeite WindowInsets: utilize Modifier.windowInsetsPadding(WindowInsets.safeDrawing) ou Scaffold(contentWindowInsets = WindowInsets.safeDrawing).
2. MULTI-ACTION:
   - Gere o código Kotlin (com pacotes corretos) e, quando necessário, as ações de SQL para Supabase.
3. Responda ESTRITAMENTE em formato JSON com o seguinte esquema:
{
  "issueTitle": "Título da ação",
  "explanation": "Descrição das alterações",
  "changes": [
    {
      "filePath": "caminho/do/arquivo.kt",
      "action": "CREATE" ou "MODIFY",
      "explanation": "Descrição",
      "fullContent": "Código Kotlin completo"
    }
  ],
  "supabaseActions": []
}
""".trimIndent()
  }

  fun buildCompilationFixPrompt(
    fileName: String,
    currentCode: String,
    errorLogs: String
  ): String {
    return """
Você é um Arquiteto Android sênior.
Analise a falha de compilação ou log de execução de '$fileName'.
Gere o patch de correção em JSON com:
{
  "issueTitle": "Título da correção",
  "explanation": "Explicação do erro",
  "changes": [
    { "filePath": "$fileName", "action": "MODIFY", "explanation": "Correção aplicada", "fullContent": "Código corrigido" }
  ]
}
LOGS: $errorLogs
CÓDIGO: $currentCode
""".trimIndent()
  }

  fun isCloudPersistenceRequested(query: String): Boolean {
    val q = query.lowercase()
    return q.contains("supabase") || q.contains("nuvem") || q.contains("banco") ||
      q.contains("tabela") || q.contains("cloud") || q.contains("database") ||
      q.contains("backend") || q.contains("persist") || q.contains("rls")
  }
}
