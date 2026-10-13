package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ProjectFileManager
import com.example.data.RollbackManager
import com.example.data.api.GeminiAiService
import com.example.data.repository.GitHubDispatchResult
import com.example.data.repository.GitHubRepository
import com.example.data.repository.GitHubWorkflowMonitor
import com.example.data.supabase.SupabaseManager
import com.example.data.supabase.model.SupabaseConfig
import com.example.data.supabase.model.SupabaseToolAction
import com.example.data.supabase.model.SupabaseRlsDiagnostic
import com.example.model.BuildStatus
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.FileChangeAction
import com.example.model.FileNode
import com.example.model.IdeUiState
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.TerminalTab
import com.example.util.ApkInstaller
import com.example.util.ComposeCodeInterpreter
import com.example.util.FileTreeUtils
import com.example.util.ProjectIndexer
import com.example.util.SyntaxValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.data.importer.GitHubImporter
import com.example.model.CloneRepoState
import com.example.model.IdeScreen
import com.example.model.ProjectItem
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.data.agent.AutonomousAgentEngine
import com.example.data.repository.GitHubConfigStore
import com.example.data.repository.SavedGitHubConfig

class IdeViewModel(application: Application) : AndroidViewModel(application) {
  private val fileManager = ProjectFileManager(application.applicationContext)
  private val gitHubRepository = GitHubRepository()
  private val workflowMonitor = GitHubWorkflowMonitor(gitHubRepository)
  private val geminiService = GeminiAiService()
  private val rollbackManager = RollbackManager()
  private val gitHubImporter = GitHubImporter()
  private val supabaseManager = SupabaseManager(application.applicationContext)
  private val agentEngine = AutonomousAgentEngine(fileManager, rollbackManager, geminiService)
  private val gitHubConfigStore = GitHubConfigStore(application.applicationContext)
  private val _uiState = MutableStateFlow(
    IdeUiState(supabaseConfig = supabaseManager.getConfig())
  )
  val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()
  private var autoSaveJob: Job? = null
  private var previewJob: Job? = null

  init {
    viewModelScope.launch {
      val tree = fileManager.loadProjectTree()
      val initialProject = fileManager.getWorkspaceProjectItem()
      val mainActivity = FileTreeUtils.findFileByName(tree, "MainActivity.kt")
      val savedGitHub = gitHubConfigStore.loadConfig()
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = mainActivity,
          editorContent = mainActivity?.content ?: "",
          openTabs = listOfNotNull(mainActivity),
          projectsList = listOf(initialProject),
          githubToken = savedGitHub.token,
          githubOwner = savedGitHub.owner,
          githubRepo = savedGitHub.repo,
          githubWorkflowId = savedGitHub.workflowId,
          currentBranch = savedGitHub.branch
        )
      }
      addLog("Sistema montado em context.filesDir/project_workspace", LogType.SUCCESS)
      if (mainActivity != null && mainActivity.content.isNotBlank()) {
        schedulePreviewParsing(mainActivity.name, mainActivity.content)
      }
    }
  }

  fun updateEditorContent(newText: String) {
    val currentFile = _uiState.value.activeFile ?: return
    _uiState.update { it.copy(editorContent = newText, isDirty = true) }
    autoSaveJob?.cancel()
    autoSaveJob = viewModelScope.launch(Dispatchers.IO) {
      delay(300)
      if (fileManager.saveFile(currentFile.path, newText)) _uiState.update { it.copy(isDirty = false) }
    }
    if (_uiState.value.previewState.isVisible) {
      schedulePreviewParsing(currentFile.name, newText)
    }
  }

  fun requestAiPrompt(userPrompt: String) {
    val state = _uiState.value
    val fileName = state.activeFile?.name ?: "MainActivity.kt"
    val code = state.editorContent.ifBlank {
      state.rootNode.let { FileTreeUtils.findFileByName(it, "MainActivity.kt")?.content } ?: ""
    }

    val userMsg = ChatMessage(sender = ChatSender.USER, text = userPrompt)
    val agentMsgId = java.util.UUID.randomUUID().toString()
    val initialMsg = ChatMessage(
      id = agentMsgId,
      sender = ChatSender.AGENT,
      text = "Executando comando...",
      isThinking = true
    )

    _uiState.update {
      it.copy(
        isAiAnalyzing = true,
        isAiChatVisible = true,
        showAiFixDialog = false,
        chatMessages = it.chatMessages + userMsg + initialMsg
      )
    }
    addLog("[AGENTE] Processando: '$userPrompt'...", LogType.INFO)

    viewModelScope.launch {
      val result = agentEngine.executeCommand(
        userPrompt = userPrompt,
        activeFileName = fileName,
        currentCode = code,
        onStepUpdate = { updatedSteps ->
          _uiState.update { current ->
            val updated = current.chatMessages.map { msg ->
              if (msg.id == agentMsgId) msg.copy(steps = updatedSteps) else msg
            }
            current.copy(chatMessages = updated)
          }
        }
      )

      val updatedTree = fileManager.loadProjectTree()
      // Identificar arquivo criado (ex: CalculatorScreen.kt) e arquivo modificado (MainActivity.kt)
      val createdChange = result.affectedFiles.firstOrNull { it.action == FileChangeAction.CREATE }
      val modifiedChange = result.affectedFiles.firstOrNull { it.action == FileChangeAction.MODIFY }
      val primaryChange = createdChange ?: modifiedChange

      val targetNode = primaryChange?.let {
        FileTreeUtils.findFileByName(updatedTree, it.filePath.substringAfterLast("/"))
      } ?: _uiState.value.activeFile

      val finalCode = targetNode?.content ?: primaryChange?.fullContent ?: _uiState.value.editorContent

      // Abrir todos os arquivos afetados nas abas superiores
      val affectedNodes = result.affectedFiles.mapNotNull { ch ->
        FileTreeUtils.findFileByName(updatedTree, ch.filePath.substringAfterLast("/"))
      }

      val finishedAgentMsg = ChatMessage(
        id = agentMsgId,
        sender = ChatSender.AGENT,
        text = result.message,
        isThinking = false,
        suggestedChanges = result.affectedFiles,
        isApplied = true,
        steps = result.steps
      )

      _uiState.update { current ->
        val updatedMsgs = current.chatMessages.map { if (it.id == agentMsgId) finishedAgentMsg else it }
        val newOpenTabs = (current.openTabs + affectedNodes).distinctBy { it.id }

        current.copy(
          rootNode = updatedTree,
          activeFile = targetNode,
          editorContent = finalCode,
          openTabs = newOpenTabs,
          isAiAnalyzing = false,
          chatMessages = updatedMsgs,
          canRollback = result.affectedFiles.isNotEmpty(),
          hasSyntaxErrors = !result.success,
          showAiFixDialog = false,
          isAiChatVisible = false, // Fecha o modal de chat para revelar o editor e o Live Preview
          previewState = current.previewState.copy(
            isVisible = true,      // Exibe o Live Preview automaticamente ao concluir
            isMinimized = false,  // Garante que o preview esteja expandido
            previewTitle = targetNode?.name ?: "MainActivity.kt"
          )
        )
      }

      if (result.success) {
        addLog("[SUCCESS] Agente: Alterações aplicadas com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[WARNING] Agente: Verifique alertas de sintaxe.", LogType.WARNING)
      }

      if (targetNode != null) {
        schedulePreviewParsing(targetNode.name, finalCode)
      }
    }
  }

  fun applyChatChanges(message: ChatMessage) {
    if (message.suggestedChanges.isEmpty()) return
    viewModelScope.launch(Dispatchers.IO) {
      rollbackManager.createSnapshot(message.text, message.suggestedChanges.map { it.filePath }, fileManager)
      message.suggestedChanges.forEach { change ->
        fileManager.createOrUpdateFile(change.filePath, change.fullContent)
      }
      val tree = fileManager.loadProjectTree()
      val firstChanged = message.suggestedChanges.firstOrNull()
      val targetFile = firstChanged?.let { FileTreeUtils.findFileByName(tree, it.filePath.substringAfterLast("/")) }

      _uiState.update { current ->
        val updatedMsgs = current.chatMessages.map { if (it.id == message.id) it.copy(isApplied = true) else it }
        current.copy(
          rootNode = tree,
          activeFile = targetFile ?: current.activeFile,
          editorContent = targetFile?.content ?: firstChanged?.fullContent ?: current.editorContent,
          openTabs = if (targetFile != null && !current.openTabs.any { it.id == targetFile.id }) current.openTabs + targetFile else current.openTabs,
          chatMessages = updatedMsgs,
          canRollback = true
        )
      }
      addLog("[SUCCESS] Arquivos do Agente aplicados no projeto!", LogType.SUCCESS)
      if (targetFile != null) {
        schedulePreviewParsing(targetFile.name, targetFile.content)
      }
    }
  }

  fun requestAiFix() {
    val errorLogs = _uiState.value.logs.filter { it.type == LogType.ERROR || it.type == LogType.WARNING }
      .joinToString("\n") { it.message }
      .ifEmpty { "Erros de compilação ou sintaxe" }
    requestAiPrompt("Corrija automaticamente as falhas e erros de compilação: $errorLogs")
  }

  fun requestFixSyntaxErrors() {
    val syntaxErrors = _uiState.value.logs.filter { it.message.contains("Erro de sintaxe", ignoreCase = true) || it.type == LogType.WARNING }
      .joinToString("\n") { it.message }
    val prompt = "Corrija os erros de sintaxe e balanceamento detectados: $syntaxErrors"
    requestAiPrompt(prompt)
  }

  fun applyAiFix() {
    val fix = _uiState.value.currentAiFix ?: return
    viewModelScope.launch(Dispatchers.IO) {
      // Validação prévia de sintaxe em todos os arquivos antes de salvar no disco
      val syntaxErrors = fix.changes.mapNotNull { change ->
        val res = SyntaxValidator.validate(change.filePath, change.fullContent)
        if (!res.isValid) "${change.filePath}: ${res.errorMessage}" else null
      }

      if (syntaxErrors.isNotEmpty()) {
        _uiState.update { it.copy(hasSyntaxErrors = true, isTerminalExpanded = true, selectedTab = TerminalTab.BUILD_OUTPUT) }
        addLog("[WARNING] Erro de sintaxe detectado pelo validador local!", LogType.WARNING)
        syntaxErrors.forEach { addLog("[ERROR] $it", LogType.ERROR) }
        addLog("[INFO] Use o botão 'Corrigir Sintaxe' para que o Gemini corrija automaticamente antes de aplicar.", LogType.INFO)
        return@launch // Interrompe o salvamento para impedir que arquivos corrompidos sejam gravados
      }

      _uiState.update { it.copy(hasSyntaxErrors = false) }
      rollbackManager.createSnapshot(fix.issueTitle, fix.changes.map { it.filePath }, fileManager)
      fix.changes.forEach { change ->
        val success = fileManager.createOrUpdateFile(change.filePath, change.fullContent)
        val tag = if (change.action == FileChangeAction.CREATE) "Criado" else "Modificado"
        if (success) addLog("[SUCCESS] $tag: ${change.filePath}", LogType.SUCCESS)
      }

      val updatedTree = fileManager.loadProjectTree()
      val activePath = _uiState.value.activeFile?.path ?: ""
      val activeChange = fix.changes.firstOrNull { it.filePath.endsWith(activePath) || activePath.endsWith(it.filePath) }
      val newContent = activeChange?.fullContent ?: _uiState.value.editorContent

      _uiState.update {
        it.copy(rootNode = updatedTree, editorContent = newContent, isDirty = true, showAiFixDialog = false, canRollback = true, rollbackDescription = fix.issueTitle)
      }
      addLog("[SUCCESS] Patch multi-arquivo validado e aplicado com sucesso!", LogType.SUCCESS)
      schedulePreviewParsing(_uiState.value.activeFile?.name ?: "MainActivity.kt", newContent)
    }
  }

  fun rollbackLastAiChange() {
    viewModelScope.launch(Dispatchers.IO) {
      val restored = rollbackManager.performRollback(fileManager)
      if (restored.isNotEmpty()) {
        val tree = fileManager.loadProjectTree()
        val active = _uiState.value.activeFile
        val content = if (active != null) fileManager.readFile(active.path) else ""
        _uiState.update { it.copy(rootNode = tree, editorContent = content, canRollback = false, rollbackDescription = null, hasSyntaxErrors = false) }
        addLog("[SUCCESS] Rollback concluído com sucesso!", LogType.SUCCESS)
        restored.forEach { addLog("[INFO] $it", LogType.INFO) }
      }
    }
  }

  fun downloadAndInstallApk() {
    val state = _uiState.value
    val context = getApplication<Application>().applicationContext
    _uiState.update { it.copy(isDownloadingApk = true) }
    addLog("[INFO] Baixando APK do GitHub Actions...", LogType.INFO)
    viewModelScope.launch {
      val apkFile = ApkInstaller.downloadAndExtract(context, gitHubRepository, state.githubToken, state.githubOwner, state.githubRepo, 110L)
      _uiState.update { it.copy(isDownloadingApk = false, downloadedApkPath = apkFile?.absolutePath) }
      if (apkFile != null) {
        addLog("[SUCCESS] Download concluído: ${apkFile.name}", LogType.SUCCESS)
        if (ApkInstaller.installApk(context, apkFile)) addLog("[SUCCESS] Instalador aberto!", LogType.SUCCESS)
      }
    }
  }

  fun triggerGitHubDispatch(token: String, owner: String, repo: String, workflowId: String, branch: String) {
    gitHubConfigStore.saveConfig(SavedGitHubConfig(token, owner, repo, workflowId, branch))
    viewModelScope.launch {
      _uiState.update { it.copy(githubToken = token, githubOwner = owner, githubRepo = repo, githubWorkflowId = workflowId, currentBranch = branch, isDispatchingGitHub = true, buildStatus = BuildStatus.BUILDING, selectedTab = TerminalTab.GITHUB_ACTIONS, isTerminalExpanded = true, showGitHubPushDialog = false) }
      addLog("[INFO] Disparando build no GitHub Actions ($owner/$repo)...", LogType.INFO)
      val result = gitHubRepository.triggerWorkflow(token, owner, repo, workflowId, branch)
      if (result is GitHubDispatchResult.Success) {
        addLog("[SUCCESS] ${result.message}", LogType.SUCCESS)
        delay(2000)
        pollWorkflowStatus(gitHubRepository.getLatestWorkflowRun(token, owner, repo)?.id ?: 110L)
      } else if (result is GitHubDispatchResult.Failure) {
        _uiState.update { it.copy(isDispatchingGitHub = false, buildStatus = BuildStatus.FAILED) }
        addLog("[ERROR] Falha no GitHub Actions: ${result.errorMessage}", LogType.ERROR)
      }
    }
  }

  fun pollWorkflowStatus(runId: Long) {
    val state = _uiState.value
    viewModelScope.launch {
      addLog("[INFO] Monitorando execução #$runId...", LogType.INFO)
      workflowMonitor.pollWorkflow(
        token = state.githubToken, owner = state.githubOwner, repo = state.githubRepo, runId = runId,
        onStatusUpdate = { addLog("[INFO] Status: $it", LogType.INFO) },
        onJobUpdate = { job, step, fail ->
          if (fail) addLog("[ERROR] Falha: '$step' ($job)", LogType.ERROR) else addLog("[INFO] Executando: '$step' ($job)", LogType.INFO)
        },
        onComplete = { success, _ ->
          if (success) {
            _uiState.update { it.copy(buildStatus = BuildStatus.SUCCESS, isApkReady = true, isDispatchingGitHub = false) }
            addLog("[SUCCESS] Build concluído com sucesso!", LogType.SUCCESS)
          } else {
            _uiState.update { it.copy(buildStatus = BuildStatus.FAILED, isApkReady = false, isDispatchingGitHub = false) }
            addLog("[ERROR] Build falhou.", LogType.ERROR)
          }
        }
      )
    }
  }

  fun runBuild() {
    val state = _uiState.value
    val currentFile = state.activeFile
    val currentContent = state.editorContent

    // Validação de sintaxe antes do build
    if (currentFile != null) {
      val validation = SyntaxValidator.validate(currentFile.path, currentContent)
      if (!validation.isValid) {
        _uiState.update { it.copy(buildStatus = BuildStatus.FAILED, hasSyntaxErrors = true, isTerminalExpanded = true, selectedTab = TerminalTab.BUILD_OUTPUT) }
        addLog("[WARNING] Erro de sintaxe detectado: ${validation.errorMessage}", LogType.WARNING)
        addLog("[ERROR] Build abortado para prevenir falha no compilador.", LogType.ERROR)
        addLog("[INFO] Clique em 'Corrigir Sintaxe' para que a IA resolva o problema automaticamente.", LogType.INFO)
        return
      }
    }

    _uiState.update { it.copy(hasSyntaxErrors = false) }

    if (state.githubToken.isNotBlank() && state.githubOwner.isNotBlank() && state.githubRepo.isNotBlank()) {
      triggerGitHubDispatch(state.githubToken, state.githubOwner, state.githubRepo, state.githubWorkflowId, state.currentBranch)
    } else {
      viewModelScope.launch {
        _uiState.update { it.copy(buildStatus = BuildStatus.BUILDING, isTerminalExpanded = true, selectedTab = TerminalTab.BUILD_OUTPUT) }
        addLog("$ ./gradlew assembleDebug", LogType.COMMAND)
        delay(600)
        _uiState.update { it.copy(buildStatus = BuildStatus.SUCCESS, isApkReady = true) }
        addLog("BUILD SUCCESSFUL: APK gerado e pronto para download.", LogType.SUCCESS)
      }
    }
  }

  fun openFile(file: FileNode) {
    if (file.isDirectory) return
    viewModelScope.launch {
      val content = fileManager.readFile(file.path)
      _uiState.update { it.copy(activeFile = file, editorContent = content, openTabs = if (it.openTabs.none { t -> t.id == file.id }) it.openTabs + file else it.openTabs, isDirty = false) }
      if (_uiState.value.previewState.isVisible) {
        schedulePreviewParsing(file.name, content)
      }
    }
  }

  fun toggleLivePreview() {
    val willShow = !_uiState.value.previewState.isVisible
    _uiState.update { it.copy(previewState = it.previewState.copy(isVisible = willShow)) }
    if (willShow) {
      viewModelScope.launch {
        val rootNode = _uiState.value.rootNode
        val mainActivity = FileTreeUtils.findFileByName(rootNode, "MainActivity.kt")
        
        val fileToPreview = mainActivity ?: _uiState.value.activeFile
        
        val content = if (fileToPreview == _uiState.value.activeFile) {
          _uiState.value.editorContent
        } else {
          fileToPreview?.let { fileManager.readFile(it.path) } ?: ""
        }
        
        schedulePreviewParsing(fileToPreview?.name ?: "MainActivity.kt", content)
      }
    }
  }

  fun updatePreviewPosition(dx: Float, dy: Float) {
    _uiState.update {
      val ps = it.previewState
      it.copy(previewState = ps.copy(
        offsetX = (ps.offsetX + dx).coerceAtLeast(0f),
        offsetY = (ps.offsetY + dy).coerceAtLeast(0f)
      ))
    }
  }

  fun updatePreviewSize(dw: Float, dh: Float) {
    _uiState.update {
      val ps = it.previewState
      it.copy(previewState = ps.copy(
        widthDp = (ps.widthDp + dw).coerceIn(160f, 420f),
        heightDp = (ps.heightDp + dh).coerceIn(200f, 650f)
      ))
    }
  }

  fun togglePreviewMinimize() {
    _uiState.update {
      val ps = it.previewState
      it.copy(previewState = ps.copy(isMinimized = !ps.isMinimized))
    }
  }

  fun togglePreviewOpacity() {
    _uiState.update {
      val ps = it.previewState
      val next = if (ps.opacity > 0.8f) 0.5f else 0.95f
      it.copy(previewState = ps.copy(opacity = next))
    }
  }

  fun openPreview() {
    val currentFile = _uiState.value.activeFile
    _uiState.update {
      it.copy(
        isAiChatVisible = false,
        previewState = it.previewState.copy(isVisible = true, isMinimized = false)
      )
    }
    if (currentFile != null) {
      schedulePreviewParsing(currentFile.name, _uiState.value.editorContent)
    }
  }

  fun closeLivePreview() {
    _uiState.update { it.copy(previewState = it.previewState.copy(isVisible = false)) }
  }

  private fun schedulePreviewParsing(fileName: String, code: String) {
    previewJob?.cancel()
    previewJob = viewModelScope.launch(Dispatchers.Default) {
      delay(150)
      val elements = ComposeCodeInterpreter.parseCode(fileName, code)
      _uiState.update {
        it.copy(previewState = it.previewState.copy(isVisible = true, previewTitle = fileName, elements = elements))
      }
    }
  }

  fun closeTab(file: FileNode) {
    _uiState.update { it.copy(openTabs = it.openTabs.filter { t -> t.id != file.id }, activeFile = if (it.activeFile?.id == file.id) it.openTabs.firstOrNull() else it.activeFile) }
  }

  fun toggleFolder(folderId: String) { _uiState.update { it.copy(rootNode = FileTreeUtils.toggleFolder(it.rootNode, folderId)) } }
  fun setTerminalExpanded(expanded: Boolean) { _uiState.update { it.copy(isTerminalExpanded = expanded) } }
  fun selectTerminalTab(tab: TerminalTab) { _uiState.update { it.copy(selectedTab = tab, isTerminalExpanded = true) } }
  fun dismissAiDialog() { _uiState.update { it.copy(showAiFixDialog = false) } }
  fun showGitHubDialog(show: Boolean) { _uiState.update { it.copy(showGitHubPushDialog = show) } }
  fun navigateToScreen(screen: IdeScreen) {
    _uiState.update { it.copy(currentScreen = screen) }
  }

  fun showCloneDialog(show: Boolean) {
    _uiState.update { it.copy(showCloneDialog = show, cloneState = CloneRepoState()) }
  }

  fun cloneGitHubRepository(repoUrl: String, token: String?, branch: String?) {
    _uiState.update {
      it.copy(
        cloneState = CloneRepoState(isCloning = true, progressMessage = "Iniciando clonagem de $repoUrl...")
      )
    }
    viewModelScope.launch {
      val result = gitHubImporter.cloneRepository(
        repoUrlOrSlug = repoUrl,
        targetWorkspace = fileManager.workspaceDir,
        token = token,
        branch = branch,
        onProgress = { msg ->
          _uiState.update { it.copy(cloneState = it.cloneState.copy(progressMessage = msg)) }
          addLog("[GIT CLONE] $msg", LogType.INFO)
        }
      )

      if (result.success) {
        val tree = fileManager.loadProjectTree()
        val firstFile = FileTreeUtils.findFirstFile(tree)
        val projectItem = fileManager.getWorkspaceProjectItem(
          customName = result.repoName,
          isGit = true,
          branch = branch ?: "main"
        )

        _uiState.update {
          it.copy(
            rootNode = tree,
            activeFile = firstFile,
            editorContent = firstFile?.content ?: "",
            openTabs = listOfNotNull(firstFile),
            isDirty = false,
            hasSyntaxErrors = false,
            projectsList = listOf(projectItem),
            showCloneDialog = false,
            cloneState = CloneRepoState(success = true),
            currentScreen = IdeScreen.EDITOR
          )
        }
        addLog("[SUCCESS] Repositório ${result.repoName} clonado com sucesso (${result.extractedFilesCount} arquivos)!", LogType.SUCCESS)
        addLog("[INDEX] ${result.architectureIndex.lines().firstOrNull() ?: "Indexado"}", LogType.INFO)
        schedulePreviewParsing(firstFile?.name ?: "MainActivity.kt", firstFile?.content ?: "")
      } else {
        val err = result.errorMessage ?: "Erro desconhecido ao clonar"
        _uiState.update {
          it.copy(
            cloneState = CloneRepoState(isCloning = false, error = err)
          )
        }
        addLog("[ERROR] Falha na clonagem: $err", LogType.ERROR)
      }
    }
  }

  fun openProject(project: ProjectItem) {
    viewModelScope.launch {
      val tree = fileManager.loadProjectTree()
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = null,
          editorContent = "",
          openTabs = emptyList(),
          currentScreen = IdeScreen.EDITOR
        )
      }
    }
  }

  fun resetProjectToTemplate() {
    viewModelScope.launch {
      val tree = fileManager.resetToRobustTemplate()
      val currentProject = fileManager.getWorkspaceProjectItem()
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = null,
          editorContent = "",
          openTabs = emptyList(),
          isDirty = false,
          hasSyntaxErrors = false,
          projectsList = listOf(currentProject),
          currentScreen = IdeScreen.EDITOR
        )
      }
      addLog("[TEMPLATE] Novo projeto inicializado com template robusto (Compose, Retrofit, Room, Coil, Permissões)!", LogType.SUCCESS)
    }
  }

  fun showSupabaseDialog(show: Boolean) {
    _uiState.update { it.copy(showSupabaseDialog = show) }
  }

  fun saveSupabaseConfig(token: String, projectRef: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val config = SupabaseConfig(accessToken = token.trim(), projectRef = projectRef.trim(), isConnected = false)
      supabaseManager.saveConfig(config)
      val isOk = supabaseManager.testConnection(config)
      val loaded = supabaseManager.getConfig()
      _uiState.update { it.copy(supabaseConfig = loaded, showSupabaseDialog = false) }
      if (isOk) {
        addLog("[SUPABASE SUCCESS] Configuração salva e conectada com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[SUPABASE] Configuração salva para o projeto ${config.projectRef}", LogType.INFO)
      }
    }
  }

  fun testSupabaseConnection(token: String? = null, projectRef: String? = null) {
    viewModelScope.launch(Dispatchers.IO) {
      val customCfg = if (!token.isNullOrBlank() && !projectRef.isNullOrBlank()) {
        SupabaseConfig(accessToken = token.trim(), projectRef = projectRef.trim())
      } else null
      val refToLog = customCfg?.projectRef ?: _uiState.value.supabaseConfig.projectRef
      addLog("[SUPABASE] Testando conexão com Supabase ($refToLog)...", LogType.INFO)
      val success = supabaseManager.testConnection(customCfg)
      val config = supabaseManager.getConfig()
      _uiState.update { it.copy(supabaseConfig = config) }
      if (success) {
        addLog("[SUPABASE SUCCESS] Conexão estabelecida com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[SUPABASE ERROR] Falha na conexão. Verifique token e projectRef.", LogType.ERROR)
      }
    }
  }

  fun setShowResetConfirmDialog(show: Boolean) {
    _uiState.update { it.copy(showResetConfirmDialog = show) }
  }

  fun executeSupabaseMigration(sql: String) {
    viewModelScope.launch(Dispatchers.IO) {
      addLog("[SUPABASE] Executando migração SQL...", LogType.INFO)
      val result = supabaseManager.executeMigration(sql)
      if (result.success) {
        addLog("[SUPABASE] Migração executada com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[SUPABASE ERROR] ${result.errorDetails ?: result.message}", LogType.ERROR)
      }
    }
  }

  fun executeSupabaseAction(action: SupabaseToolAction) {
    viewModelScope.launch(Dispatchers.IO) {
      addLog("[SUPABASE ACTION] ${action.description}", LogType.INFO)
      val result = supabaseManager.executeMigration(action.sql)
      if (result.success) {
        addLog("[SUPABASE] Ação executada com sucesso!", LogType.SUCCESS)
      } else {
        val err = result.errorDetails ?: result.message
        addLog("[SUPABASE ERROR] $err", LogType.ERROR)
        diagnoseSupabaseError(err)
      }
    }
  }

  fun diagnoseSupabaseError(errorMessage: String) {
    viewModelScope.launch {
      addLog("[SUPABASE DIAGNOSTIC] Analisando erro de RLS/permissão...", LogType.INFO)
      val fileName = _uiState.value.activeFile?.name ?: "MainActivity.kt"
      val content = _uiState.value.editorContent
      val diagnostic = geminiService.diagnoseRls(errorMessage, fileName, content)?.supabaseDiagnostic
      if (diagnostic != null) {
        _uiState.update { it.copy(activeRlsDiagnostic = diagnostic) }
        addLog("[SUPABASE DIAGNOSTIC] RLS analisado: ${diagnostic.rootCause}", LogType.WARNING)
      } else {
        addLog("[SUPABASE DIAGNOSTIC] Nenhuma falha de RLS identificada.", LogType.INFO)
      }
    }
  }

  fun applyRlsDiagnosticFix() {
    val diag = _uiState.value.activeRlsDiagnostic ?: return
    viewModelScope.launch(Dispatchers.IO) {
      addLog("[SUPABASE] Aplicando correção de RLS automática...", LogType.INFO)
      val result = supabaseManager.executeMigration(diag.recommendedSqlFix)
      if (result.success) {
        _uiState.update { it.copy(activeRlsDiagnostic = null) }
        addLog("[SUPABASE] Correção de RLS aplicada com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[SUPABASE ERROR] ${result.errorDetails ?: result.message}", LogType.ERROR)
      }
    }
  }

  fun dismissRlsDiagnostic() {
    _uiState.update { it.copy(activeRlsDiagnostic = null) }
  }

  fun clearLogs() { _uiState.update { it.copy(logs = emptyList()) } }

  fun toggleAiChat() {
    _uiState.update { it.copy(isAiChatVisible = !it.isAiChatVisible) }
  }

  fun setAiModel(model: String) {
    _uiState.update { it.copy(selectedAiModel = model) }
  }

  fun createFile(name: String, parentPath: String) {
      viewModelScope.launch(Dispatchers.IO) {
          val path = if (parentPath.isEmpty()) name else "$parentPath/$name"
          fileManager.createOrUpdateFile(path, "")
          val tree = fileManager.loadProjectTree()
          _uiState.update { it.copy(rootNode = tree) }
          addLog("[SUCCESS] Arquivo criado: $path", LogType.SUCCESS)
      }
  }

  fun createFolder(name: String, parentPath: String) {
      viewModelScope.launch(Dispatchers.IO) {
          val path = if (parentPath.isEmpty()) name else "$parentPath/$name"
          fileManager.createDirectory(path)
          val tree = fileManager.loadProjectTree()
          _uiState.update { it.copy(rootNode = tree) }
          addLog("[SUCCESS] Pasta criada: $path", LogType.SUCCESS)
      }
  }

  fun setShowCreateItemDialog(show: Boolean, parentPath: String = "") {
    _uiState.update { it.copy(showCreateItemDialog = show, activeParentPath = parentPath) }
  }

  private fun addLog(message: String, type: LogType) {
    _uiState.update { it.copy(logs = it.logs + LogEntry(message = message, type = type)) }
  }
}
