package com.example.model

import com.example.data.supabase.model.SupabaseConfig
import com.example.data.supabase.model.SupabaseRlsDiagnostic
import com.example.data.supabase.model.SupabaseToolAction

enum class BuildStatus {
  IDLE,
  BUILDING,
  SUCCESS,
  FAILED
}

enum class TerminalTab(val label: String) {
  GITHUB_ACTIONS("GitHub Actions"),
  BUILD_OUTPUT("Build Output"),
  TERMINAL("Terminal")
}

data class AiFixSuggestion(
  val issueTitle: String,
  val errorLine: Int = 0,
  val explanation: String,
  val originalSnippet: String = "",
  val fixedSnippet: String = "",
  val fullFixedContent: String = "",
  val changes: List<AiFileChange> = emptyList(),
  val supabaseActions: List<SupabaseToolAction> = emptyList(),
  val supabaseDiagnostic: SupabaseRlsDiagnostic? = null
)

data class IdeUiState(
  val rootNode: FileNode = ProjectFileTreeSample.createSampleProject(),
  val activeFile: FileNode? = null,
  val editorContent: String = "",
  val openTabs: List<FileNode> = emptyList(),
  val isDirty: Boolean = false,
  val isSidebarOpen: Boolean = false,
  val isTerminalExpanded: Boolean = true,
  val selectedTab: TerminalTab = TerminalTab.GITHUB_ACTIONS,
  val buildStatus: BuildStatus = BuildStatus.IDLE,
  val logs: List<LogEntry> = emptyList(),
  val showAiFixDialog: Boolean = false,
  val currentAiFix: AiFixSuggestion? = null,
  val showGitHubPushDialog: Boolean = false,
  val currentBranch: String = "main",
  val githubToken: String = "",
  val githubOwner: String = "",
  val githubRepo: String = "",
  val githubWorkflowId: String = "android-ci.yml",
  val isDispatchingGitHub: Boolean = false,
  val isAiAnalyzing: Boolean = false,
  val isApkReady: Boolean = false,
  val isDownloadingApk: Boolean = false,
  val downloadedApkPath: String? = null,
  val canRollback: Boolean = false,
  val rollbackDescription: String? = null,
  val hasSyntaxErrors: Boolean = false,
  val previewState: LivePreviewState = LivePreviewState(),
  val currentScreen: IdeScreen = IdeScreen.EDITOR,
  val projectsList: List<ProjectItem> = emptyList(),
  val showCloneDialog: Boolean = false,
  val cloneState: CloneRepoState = CloneRepoState(),
  val supabaseConfig: SupabaseConfig = SupabaseConfig(),
  val showSupabaseDialog: Boolean = false,
  val activeRlsDiagnostic: SupabaseRlsDiagnostic? = null,
  val isApplyingSupabaseAction: Boolean = false
)
