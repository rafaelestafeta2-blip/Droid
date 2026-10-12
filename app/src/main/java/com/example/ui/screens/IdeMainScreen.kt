package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.BuildStatus
import com.example.model.IdeScreen
import com.example.ui.components.dialogs.AiFixDialog
import com.example.ui.components.dialogs.CloneRepositoryDialog
import com.example.ui.components.dialogs.GitHubPushDialog
import com.example.ui.components.dialogs.CreateItemDialog
import com.example.ui.components.dialogs.SupabaseConfigDialog
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.components.editor.BuildStatusBadge
import com.example.ui.components.editor.CodeEditorArea
import com.example.ui.components.editor.EditorAccessoryBar
import com.example.ui.components.editor.EditorHeader
import com.example.ui.components.preview.FloatingPreviewPanel
import com.example.ui.components.chat.AiChatPanel
import com.example.ui.components.sidebar.ProjectSidebarDrawer
import com.example.ui.components.terminal.TerminalPanel
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeYellow
import com.example.viewmodel.IdeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeMainScreen(
  viewModel: IdeViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()

  BackHandler(enabled = drawerState.isOpen) {
    scope.launch { drawerState.close() }
  }

  BackHandler(enabled = uiState.currentScreen == IdeScreen.EDITOR && !drawerState.isOpen) {
    viewModel.navigateToScreen(IdeScreen.DASHBOARD)
  }

  if (uiState.currentScreen == IdeScreen.DASHBOARD) {
    ProjectsScreen(
      projects = uiState.projectsList,
      onOpenProject = { viewModel.openProject(it) },
      onOpenCloneDialog = { viewModel.showCloneDialog(true) },
      onCreateNewTemplate = { viewModel.resetProjectToTemplate() },
      onNavigateToEditor = { viewModel.navigateToScreen(IdeScreen.EDITOR) },
      modifier = modifier
    )
  } else {
    ModalNavigationDrawer(
      drawerState = drawerState,
      drawerContent = {
        ModalDrawerSheet(
          drawerContainerColor = IdeSurface,
          modifier = Modifier.width(300.dp)
        ) {
          ProjectSidebarDrawer(
            rootNode = uiState.rootNode,
            selectedFileId = uiState.activeFile?.id,
            onFileSelect = { file ->
              viewModel.openFile(file)
              scope.launch { drawerState.close() }
            },
            onToggleFolder = { folderId -> viewModel.toggleFolder(folderId) },
            onResetTemplate = {
              viewModel.resetProjectToTemplate()
              scope.launch { drawerState.close() }
            },
            onCloneGitHub = {
              viewModel.showCloneDialog(true)
              scope.launch { drawerState.close() }
            },
            onOpenDashboard = {
              viewModel.navigateToScreen(IdeScreen.DASHBOARD)
              scope.launch { drawerState.close() }
            },
            onOpenSupabaseSettings = {
              viewModel.showSupabaseDialog(true)
              scope.launch { drawerState.close() }
            },
            onCreateNewItem = {
              viewModel.setShowCreateItemDialog(true)
              scope.launch { drawerState.close() }
            },
            selectedAiModel = uiState.selectedAiModel,
            onModelSelect = { viewModel.setAiModel(it) }
          )
        }
      }
    ) {
      Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
          TopAppBar(
            title = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { viewModel.navigateToScreen(IdeScreen.DASHBOARD) },
                  modifier = Modifier.size(32.dp).testTag("open_dashboard_button")
                ) {
                  Icon(
                    Icons.Default.FolderShared,
                    contentDescription = "Projetos",
                    tint = IdeCyan,
                    modifier = Modifier.size(18.dp)
                  )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "DroidStudio",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = IdeTextPrimary,
                    fontSize = 15.sp
                  )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = uiState.activeFile?.name ?: "",
                  style = MaterialTheme.typography.bodySmall.copy(
                    color = IdeCyan,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                  )
                )
                Spacer(modifier = Modifier.weight(1f))
                BuildStatusBadge(uiState.buildStatus)
              }
            },
            navigationIcon = {
              IconButton(
                onClick = { scope.launch { drawerState.open() } },
                modifier = Modifier.testTag("open_sidebar_button")
              ) {
                Icon(Icons.Default.Menu, contentDescription = "Abrir Explorador de Arquivos", tint = IdeTextPrimary)
              }
            },
            colors = TopAppBarDefaults.topAppBarColors(
              containerColor = IdeSurface
            )
          )
        }
      ) { innerPadding ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
          .background(IdeBackground)
      ) {
        // Tab Header and quick actions
        EditorHeader(
          tabs = uiState.openTabs,
          activeFile = uiState.activeFile,
          isDirty = uiState.isDirty,
          buildStatus = uiState.buildStatus,
          isPreviewOpen = uiState.previewState.isVisible,
          onTabClick = { viewModel.openFile(it) },
          onCloseTab = { viewModel.closeTab(it) },
          onRunBuild = { viewModel.runBuild() },
          onTogglePreview = { viewModel.toggleLivePreview() },
          onGitHubPush = { viewModel.showGitHubDialog(true) },
          viewModel = viewModel
        )

        // Main Code Editor Area com Live Preview Flutuante Overlay
        Box(modifier = Modifier.weight(1f)) {
          CodeEditorArea(
            activeFile = uiState.activeFile,
            content = uiState.editorContent,
            onContentChange = { viewModel.updateEditorContent(it) }
          )

          // Live Preview Flutuante e Redimensionável (PiP Overlay)
          FloatingPreviewPanel(
            state = uiState.previewState,
            onDrag = { dx, dy -> viewModel.updatePreviewPosition(dx, dy) },
            onResize = { dw, dh -> viewModel.updatePreviewSize(dw, dh) },
            onToggleMinimize = { viewModel.togglePreviewMinimize() },
            onToggleOpacity = { viewModel.togglePreviewOpacity() },
            onClose = { viewModel.closeLivePreview() }
          )

          // AI Chat Panel Overlay
          if (uiState.isAiChatVisible) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                AiChatPanel(
                    onClose = { viewModel.toggleAiChat() },
                    onSendPrompt = { prompt -> viewModel.requestAiPrompt(prompt) }
                )
            }
          }
        }

        // Typing accessory bar (shortcuts for code symbols)
        EditorAccessoryBar(
          onInsertToken = { token ->
            viewModel.updateEditorContent(uiState.editorContent + token)
          }
        )

        // Bottom Retractable Terminal & GitHub Actions Panel
        TerminalPanel(
          logs = uiState.logs,
          isExpanded = uiState.isTerminalExpanded,
          selectedTab = uiState.selectedTab,
          buildStatus = uiState.buildStatus,
          isApkReady = uiState.isApkReady,
          isDownloadingApk = uiState.isDownloadingApk,
          canRollback = uiState.canRollback,
          hasSyntaxErrors = uiState.hasSyntaxErrors,
          onToggleExpand = { viewModel.setTerminalExpanded(it) },
          onSelectTab = { viewModel.selectTerminalTab(it) },
          onAiFixClick = { viewModel.requestAiFix() },
          onFixSyntaxClick = { viewModel.requestFixSyntaxErrors() },
          onInstallApkClick = { viewModel.downloadAndInstallApk() },
          onRollbackClick = { viewModel.rollbackLastAiChange() },
          onClearLogs = { viewModel.clearLogs() }
        )
      }
    }
  }
}

  // Dialogs
  if (uiState.showAiFixDialog) {
    AiFixDialog(
      suggestion = uiState.currentAiFix,
      isAnalyzing = uiState.isAiAnalyzing,
      onApplyFix = { viewModel.applyAiFix() },
      onRequestPrompt = { prompt -> viewModel.requestAiPrompt(prompt) },
      onRequestAutoDiagnose = { viewModel.requestAiFix() },
      onDismiss = { viewModel.dismissAiDialog() }
    )
  }

  if (uiState.showGitHubPushDialog) {
    GitHubPushDialog(
      initialToken = uiState.githubToken,
      initialOwner = uiState.githubOwner,
      initialRepo = uiState.githubRepo,
      initialWorkflowId = uiState.githubWorkflowId,
      initialBranch = uiState.currentBranch,
      isDispatching = uiState.isDispatchingGitHub,
      onTriggerDispatch = { token, owner, repo, workflowId, branch ->
        viewModel.triggerGitHubDispatch(token, owner, repo, workflowId, branch)
      },
      onDismiss = { viewModel.showGitHubDialog(false) }
    )
  }

  if (uiState.showCloneDialog) {
    CloneRepositoryDialog(
      isCloning = uiState.cloneState.isCloning,
      progressMessage = uiState.cloneState.progressMessage,
      errorMessage = uiState.cloneState.error,
      onClone = { url, token, branch ->
        viewModel.cloneGitHubRepository(url, token, branch)
      },
      onDismiss = { viewModel.showCloneDialog(false) }
    )
  }

  if (uiState.showCreateItemDialog) {
    CreateItemDialog(
      parentPath = uiState.activeParentPath,
      onDismiss = { viewModel.setShowCreateItemDialog(false) },
      onCreateFile = { name, path -> viewModel.createFile(name, path) },
      onCreateFolder = { name, path -> viewModel.createFolder(name, path) }
    )
  }

  if (uiState.showSupabaseDialog) {
    SupabaseConfigDialog(
      initialConfig = uiState.supabaseConfig,
      onSaveConfig = { token, ref -> viewModel.saveSupabaseConfig(token, ref) },
      onTestConnection = { _, _ -> viewModel.testSupabaseConnection() },
      onDismiss = { viewModel.showSupabaseDialog(false) }
    )
  }
}
