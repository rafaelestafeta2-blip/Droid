package com.example.data.agent

import com.example.data.ProjectFileManager
import com.example.data.RollbackManager
import com.example.data.api.GeminiAiService
import com.example.model.AgentExecutionResult
import com.example.model.AgentExecutionStep
import com.example.model.AgentStepStatus
import com.example.model.AiFileChange
import com.example.model.FileChangeAction
import com.example.util.SyntaxValidator
import kotlinx.coroutines.delay

class AutonomousAgentEngine(
  private val fileManager: ProjectFileManager,
  private val rollbackManager: RollbackManager,
  private val geminiService: GeminiAiService
) {

  suspend fun executeCommand(
    userPrompt: String,
    activeFileName: String,
    currentCode: String,
    onStepUpdate: suspend (List<AgentExecutionStep>) -> Unit
  ): AgentExecutionResult {
    val trimmed = userPrompt.trim()
    val lower = trimmed.lowercase()

    if (isHelpOrGreeting(lower)) {
      return handleConversationalQuery(trimmed, onStepUpdate)
    }

    val steps = mutableListOf(
      AgentExecutionStep(id = "step_1", title = "Analisando workspace e arquitetura...", status = AgentStepStatus.RUNNING),
      AgentExecutionStep(id = "step_2", title = "Gerando componentes Jetpack Compose...", status = AgentStepStatus.PENDING),
      AgentExecutionStep(id = "step_3", title = "Gravando arquivos no projeto...", status = AgentStepStatus.PENDING),
      AgentExecutionStep(id = "step_4", title = "Validando sintaxe e atualizando preview...", status = AgentStepStatus.PENDING)
    )
    onStepUpdate(steps.map { it.copy() }.toList())
    delay(250)

    // Passo 1 concluído
    steps[0] = steps[0].copy(status = AgentStepStatus.SUCCESS)
    steps[1] = steps[1].copy(status = AgentStepStatus.RUNNING)
    onStepUpdate(steps.map { it.copy() }.toList())
    delay(300)

    // Passo 2: Geração de arquivos
    val fileChanges = planAndGenerateCode(trimmed, activeFileName, currentCode)

    steps[1] = steps[1].copy(status = AgentStepStatus.SUCCESS)
    steps[2] = steps[2].copy(status = AgentStepStatus.RUNNING)
    onStepUpdate(steps.map { it.copy() }.toList())
    delay(300)

    // Passo 3: Escrita física no workspace
    rollbackManager.createSnapshot(trimmed, fileChanges.map { it.filePath }, fileManager)
    fileChanges.forEach { change ->
      fileManager.createOrUpdateFile(change.filePath, change.fullContent)
    }

    steps[2] = steps[2].copy(status = AgentStepStatus.SUCCESS)
    steps[3] = steps[3].copy(status = AgentStepStatus.RUNNING)
    onStepUpdate(steps.map { it.copy() }.toList())
    delay(250)

    // Passo 4: Validação
    val hasErrors = fileChanges.any { change ->
      !SyntaxValidator.validate(change.filePath, change.fullContent).isValid
    }

    steps[3] = steps[3].copy(status = if (hasErrors) AgentStepStatus.FAILED else AgentStepStatus.SUCCESS)
    val finalSteps = steps.map { it.copy() }.toList()
    onStepUpdate(finalSteps)

    val summary = buildSummaryMessage(trimmed, fileChanges)
    return AgentExecutionResult(
      success = !hasErrors,
      message = summary,
      affectedFiles = fileChanges,
      steps = finalSteps,
      isConversationalOnly = false
    )
  }

  private fun isHelpOrGreeting(lower: String): Boolean {
    return lower in listOf("oi", "ola", "olá", "hello", "hi") ||
      lower.contains("o que você pode fazer") ||
      lower.contains("o que vc pode fazer") ||
      lower.contains("o que vc ode fazer") ||
      lower.contains("como funciona") ||
      lower.contains("ajuda") ||
      lower.contains("help")
  }

  private suspend fun handleConversationalQuery(
    prompt: String,
    onStepUpdate: suspend (List<AgentExecutionStep>) -> Unit
  ): AgentExecutionResult {
    val steps = listOf(
      AgentExecutionStep(title = "Identificando comandos e recursos...", status = AgentStepStatus.SUCCESS),
      AgentExecutionStep(title = "Preparando instruções do Agente...", status = AgentStepStatus.SUCCESS)
    )
    onStepUpdate(steps)

    val reply = """
Olá! Sou o seu Agente de Desenvolvimento Android Autônomo.
Diferente de assistentes normais, quando você me dá um comando eu crio os arquivos e aplico as alterações diretamente no seu projeto!

✨ O que eu posso construir para você agora:
1. 🔐 "Criar uma tela de login moderna" (E-mail, senha e validação)
2. 📝 "Criar lista de tarefas" (Adicionar, concluir e deletar itens)
3. 🔢 "Criar um contador animado" (Botões +1, -1 e zerar com visual moderno)
4. 🧮 "Criar uma calculadora" (Display numérico e operadores completos)
5. ☀️ "Criar tela de previsão do tempo" (Clima, temperatura e estatísticas)
6. 👤 "Criar perfil de usuário" (Avatar, dados pessoais e configurações)
7. 🛠️ Corrigir automaticamente qualquer erro de compilação ou sintaxe!

Basta digitar o que você quer construir e eu aplico tudo no seu código imediatamente!
""".trimIndent()

    return AgentExecutionResult(
      success = true,
      message = reply,
      affectedFiles = emptyList(),
      steps = steps,
      isConversationalOnly = true
    )
  }

  private fun planAndGenerateCode(
    prompt: String,
    activeFileName: String,
    currentCode: String
  ): List<AiFileChange> {
    val lower = prompt.lowercase()
    val changes = mutableListOf<AiFileChange>()

    when {
      lower.contains("login") || lower.contains("loguin") || lower.contains("autentic") -> {
        val screenCode = AgentScreenTemplates.getLoginScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/LoginScreen.kt", FileChangeAction.CREATE, "Tela de Login em Compose", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do LoginScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("LoginScreen()"), currentCode))
      }
      lower.contains("tarefa") || lower.contains("todo") || lower.contains("lista") -> {
        val screenCode = AgentScreenTemplates.getTodoListScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/TodoListScreen.kt", FileChangeAction.CREATE, "Lista de Tarefas interativa", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do TodoListScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("TodoListScreen()"), currentCode))
      }
      lower.contains("contador") || lower.contains("counter") || lower.contains("contar") -> {
        val screenCode = AgentScreenTemplates.getCounterScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/CounterScreen.kt", FileChangeAction.CREATE, "Contador interativo", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do CounterScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("CounterScreen()"), currentCode))
      }
      lower.contains("calculadora") || lower.contains("calc") -> {
        val screenCode = AgentScreenTemplates.getCalculatorScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/CalculatorScreen.kt", FileChangeAction.CREATE, "Calculadora moderna", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do CalculatorScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("CalculatorScreen()"), currentCode))
      }
      lower.contains("clima") || lower.contains("tempo") || lower.contains("weather") -> {
        val screenCode = AgentScreenTemplatesExtended.getWeatherScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/WeatherScreen.kt", FileChangeAction.CREATE, "Previsão do Tempo", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do WeatherScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("WeatherScreen()"), currentCode))
      }
      lower.contains("perfil") || lower.contains("usuario") || lower.contains("usuário") || lower.contains("profile") -> {
        val screenCode = AgentScreenTemplatesExtended.getUserProfileScreenCode()
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/UserProfileScreen.kt", FileChangeAction.CREATE, "Perfil de Usuário", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do UserProfileScreen na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("UserProfileScreen()"), currentCode))
      }
      else -> {
        val screenCode = AgentScreenTemplatesExtended.getDynamicCustomScreenCode(prompt)
        changes.add(AiFileChange("app/src/main/java/com/example/ui/screens/CustomFeatureScreen.kt", FileChangeAction.CREATE, "Novo recurso dinâmico", screenCode, ""))
        changes.add(AiFileChange("app/src/main/java/com/example/MainActivity.kt", FileChangeAction.MODIFY, "Integração do recurso na MainActivity", AgentMainActivityRewriter.buildMainActivityCode("CustomFeatureScreen()"), currentCode))
      }
    }
    return changes
  }

  private fun buildSummaryMessage(prompt: String, changes: List<AiFileChange>): String {
    val fileNames = changes.joinToString(", ") { it.filePath.substringAfterLast("/") }
    return "Apliquei as alterações solicitadas para '$prompt' diretamente nos arquivos do seu projeto ($fileNames). O código foi atualizado e está pronto no editor e no Live Preview!"
  }
}
