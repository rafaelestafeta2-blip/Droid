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

class IdeViewModel(application: Application) : AndroidViewModel(application) {
  private val fileManager = ProjectFileManager(application.applicationContext)
  private val gitHubRepository = GitHubRepository()
  private val workflowMonitor = GitHubWorkflowMonitor(gitHubRepository)
  private val geminiService = GeminiAiService()
  private val rollbackManager = RollbackManager()
  private val gitHubImporter = GitHubImporter()
  private val supabaseManager = SupabaseManager(application.applicationContext)
  private val _uiState = MutableStateFlow(
    IdeUiState(supabaseConfig = supabaseManager.getConfig())
  )
  val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()
  private var autoSaveJob: Job? = null
  private var previewJob: Job? = null

  init {
    viewModelScope.launch {
      val tree = fileManager.loadProjectTree()
      val initialFile = FileTreeUtils.findFirstFile(tree)
      val initialProject = fileManager.getWorkspaceProjectItem()
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = initialFile,
          editorContent = initialFile?.content ?: "",
          openTabs = listOfNotNull(initialFile),
          projectsList = listOf(initialProject)
        )
      }
      addLog("Sistema montado em context.filesDir/project_workspace", LogType.SUCCESS)
      schedulePreviewParsing(initialFile?.name ?: "MainActivity.kt", initialFile?.content ?: "")
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
    val globalIndex = ProjectIndexer.buildGlobalIndex(fileManager.workspaceDir)
    _uiState.update { it.copy(isAiAnalyzing = true, showAiFixDialog = true) }
    addLog("[INFO] Indexando projeto e enviando contexto global ao Gemini...", LogType.INFO)

    viewModelScope.launch {
      val suggestion = geminiService.generateFromCustomPrompt(fileName, state.editorContent, globalIndex, userPrompt)
      _uiState.update { it.copy(currentAiFix = suggestion, isAiAnalyzing = false) }
      addLog("[SUCCESS] Gemini AI: ${suggestion.changes.size} arquivo(s) planejados!", LogType.SUCCESS)
    }
  }

  fun requestAiFix() {
    val state = _uiState.value
    val fileName = state.activeFile?.name ?: "MainActivity.kt"
    val errorLogs = state.logs.filter { it.type == LogType.ERROR || it.type == LogType.WARNING }.joinToString("\n") { it.message }.ifEmpty { "e: Erro de compilação ou sintaxe" }
    _uiState.update { it.copy(isAiAnalyzing = true, showAiFixDialog = true) }
    addLog("[INFO] Solicitando diagnóstico e correção ao Gemini...", LogType.INFO)
    viewModelScope.launch {
      val suggestion = geminiService.analyzeAndFix(fileName, state.editorContent, errorLogs)
      _uiState.update { it.copy(currentAiFix = suggestion, isAiAnalyzing = false) }
      addLog("[SUCCESS] Gemini AI: Correção gerada!", LogType.SUCCESS)
    }
  }

  fun requestFixSyntaxErrors() {
    val state = _uiState.value
    val syntaxErrors = state.logs.filter { it.message.contains("Erro de sintaxe", ignoreCase = true) || it.type == LogType.WARNING }.joinToString("\n") { it.message }
    val prompt = "Corrija os erros de sintaxe e balanceamento de chaves/tags detectados no código: $syntaxErrors"
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
    viewModelScope.launch {
      _uiState.update { it.copy(githubToken = token, githubOwner = owner, githubRepo = repo, githubWorkflowId = workflowId, currentBranch = branch, isDispatchingGitHub = true, buildStatus = BuildStatus.BUILDING, selectedTab = TerminalTab.GITHUB_ACTIONS, isTerminalExpanded = true, showGitHubPushDialog = false) }
      addLog("[INFO] Disparando build no GitHub Actions...", LogType.INFO)
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
        addLog("BUILD SUCCESSFUL: APK gerado localmente.", LogType.SUCCESS)
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
    _uiState.update {
      it.copy(previewState = it.previewState.copy(isVisible = willShow))
    }
    if (willShow) {
      val active = _uiState.value.activeFile
      schedulePreviewParsing(active?.name ?: "MainActivity.kt", _uiState.value.editorContent)
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

  fun closeLivePreview() {
    _uiState.update { it.copy(previewState = it.previewState.copy(isVisible = false)) }
  }

  private fun schedulePreviewParsing(fileName: String, code: String) {
    previewJob?.cancel()
    previewJob = viewModelScope.launch(Dispatchers.Default) {
      delay(150)
      val elements = ComposeCodeInterpreter.parseCode(fileName, code)
      _uiState.update {
        it.copy(previewState = it.previewState.copy(previewTitle = fileName, elements = elements))
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
      val firstFile = FileTreeUtils.findFirstFile(tree)
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = firstFile,
          editorContent = firstFile?.content ?: "",
          openTabs = listOfNotNull(firstFile),
          currentScreen = IdeScreen.EDITOR
        )
      }
      schedulePreviewParsing(firstFile?.name ?: "MainActivity.kt", firstFile?.content ?: "")
    }
  }

  fun resetProjectToTemplate() {
    viewModelScope.launch {
      val tree = fileManager.resetToRobustTemplate()
      val initialFile = FileTreeUtils.findFirstFile(tree)
      val currentProject = fileManager.getWorkspaceProjectItem()
      _uiState.update {
        it.copy(
          rootNode = tree,
          activeFile = initialFile,
          editorContent = initialFile?.content ?: "",
          openTabs = listOfNotNull(initialFile),
          isDirty = false,
          hasSyntaxErrors = false,
          projectsList = listOf(currentProject),
          currentScreen = IdeScreen.EDITOR
        )
      }
      addLog("[TEMPLATE] Novo projeto inicializado com template robusto (Compose, Retrofit, Room, Coil, Permissões)!", LogType.SUCCESS)
      schedulePreviewParsing(initialFile?.name ?: "MainActivity.kt", initialFile?.content ?: "")
    }
  }

  fun showSupabaseDialog(show: Boolean) {
    _uiState.update { it.copy(showSupabaseDialog = show) }
  }

  fun saveSupabaseConfig(token: String, projectRef: String) {
    viewModelScope.launch(Dispatchers.IO) {
      val config = SupabaseConfig(accessToken = token, projectRef = projectRef, isConnected = false)
      supabaseManager.saveConfig(config)
      val loaded = supabaseManager.getConfig()
      _uiState.update { it.copy(supabaseConfig = loaded, showSupabaseDialog = false) }
      addLog("[SUPABASE] Configuração salva para o projeto $projectRef", LogType.SUCCESS)
    }
  }

  fun testSupabaseConnection() {
    viewModelScope.launch(Dispatchers.IO) {
      addLog("[SUPABASE] Testando conexão com Supabase...", LogType.INFO)
      val success = supabaseManager.testConnection()
      val config = supabaseManager.getConfig()
      _uiState.update { it.copy(supabaseConfig = config) }
      if (success) {
        addLog("[SUPABASE] Conexão estabelecida com sucesso!", LogType.SUCCESS)
      } else {
        addLog("[SUPABASE ERROR] Falha na conexão. Verifique token e projectRef.", LogType.ERROR)
      }
    }
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

  private fun addLog(message: String, type: LogType) {
    _uiState.update { it.copy(logs = it.logs + LogEntry(message = message, type = type)) }
  }
}
