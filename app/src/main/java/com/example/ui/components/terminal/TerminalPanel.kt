package com.example.ui.components.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildStatus
import com.example.model.LogEntry
import com.example.model.LogType
import com.example.model.TerminalTab
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeOrange
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTerminalBg
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary
import com.example.ui.theme.IdeYellow

@Composable
fun TerminalPanel(
  logs: List<LogEntry>,
  isExpanded: Boolean,
  selectedTab: TerminalTab,
  buildStatus: BuildStatus,
  isApkReady: Boolean,
  isDownloadingApk: Boolean,
  canRollback: Boolean,
  hasSyntaxErrors: Boolean = false,
  onToggleExpand: (Boolean) -> Unit,
  onSelectTab: (TerminalTab) -> Unit,
  onAiFixClick: () -> Unit,
  onFixSyntaxClick: () -> Unit = onAiFixClick,
  onInstallApkClick: () -> Unit,
  onRollbackClick: () -> Unit,
  onClearLogs: () -> Unit,
  modifier: Modifier = Modifier
) {
  val listState = rememberLazyListState()

  LaunchedEffect(logs.size) {
    if (logs.isNotEmpty()) listState.animateScrollToItem(logs.size - 1)
  }

  Column(modifier = modifier.fillMaxWidth().background(IdeSurface)) {
    HorizontalDivider(color = IdeBorder, thickness = 1.dp)

    // Tab Bar
    Row(
      modifier = Modifier.fillMaxWidth().height(42.dp).padding(horizontal = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      TerminalTab.values().forEach { tab ->
        val isSelected = tab == selectedTab
        Text(
          text = tab.label,
          color = if (isSelected) IdeCyan else IdeTextSecondary,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          modifier = Modifier
            .clickable { onSelectTab(tab) }
            .padding(horizontal = 4.dp, vertical = 6.dp)
        )
      }

      Spacer(modifier = Modifier.weight(1f))

      // Button: "Desfazer IA (Rollback)"
      if (canRollback) {
        FilledTonalButton(
          onClick = onRollbackClick,
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = IdeOrange.copy(alpha = 0.2f),
            contentColor = IdeOrange
          ),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(30.dp).testTag("rollback_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Rollback", modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Rollback IA", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      // Button: "Instalar APK"
      if (isApkReady) {
        FilledTonalButton(
          onClick = onInstallApkClick,
          enabled = !isDownloadingApk,
          colors = ButtonDefaults.filledTonalButtonColors(containerColor = IdeGreen.copy(alpha = 0.2f), contentColor = IdeGreen),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(30.dp).testTag("install_apk_button")
        ) {
          if (isDownloadingApk) CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = IdeGreen)
          else Icon(Icons.Default.Download, contentDescription = "APK", modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Instalar APK", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      // Button: "Corrigir Sintaxe com IA" (quando erro de sintaxe for detectado)
      if (hasSyntaxErrors) {
        FilledTonalButton(
          onClick = onFixSyntaxClick,
          colors = ButtonDefaults.filledTonalButtonColors(containerColor = IdeYellow.copy(alpha = 0.25f), contentColor = IdeYellow),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier.height(30.dp).testTag("fix_syntax_button")
        ) {
          Icon(Icons.Default.AutoAwesome, contentDescription = "Corrigir Sintaxe", modifier = Modifier.size(13.dp))
          Spacer(modifier = Modifier.width(3.dp))
          Text("Corrigir Sintaxe", fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(4.dp))
      }

      // Button: "Corrigir com IA / IA Agent"
      FilledTonalButton(
        onClick = onAiFixClick,
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = IdePurple.copy(alpha = 0.25f), contentColor = IdePurple),
        shape = RoundedCornerShape(6.dp),
        modifier = Modifier.height(30.dp).testTag("ai_fix_button")
      ) {
        Icon(Icons.Default.AutoAwesome, contentDescription = "IA", modifier = Modifier.size(13.dp))
        Spacer(modifier = Modifier.width(3.dp))
        Text("IA Agent", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
      }

      IconButton(onClick = onClearLogs, modifier = Modifier.size(28.dp).testTag("clear_logs_button")) {
        Icon(Icons.Default.ClearAll, contentDescription = "Limpar", tint = IdeTextMuted, modifier = Modifier.size(15.dp))
      }

      IconButton(onClick = { onToggleExpand(!isExpanded) }, modifier = Modifier.size(28.dp).testTag("toggle_terminal_button")) {
        Icon(
          imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
          contentDescription = "Expandir/Recolher",
          tint = IdeTextSecondary,
          modifier = Modifier.size(17.dp)
        )
      }
    }

    // Retractable Logs Area
    AnimatedVisibility(visible = isExpanded) {
      Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(IdeTerminalBg).padding(8.dp)) {
        if (logs.isEmpty()) {
          Text("Nenhum log registrado.", color = IdeTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
        } else {
          LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
            items(logs, key = { it.id }) { log ->
              Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp)) {
                Text("[${log.timestamp}] ", color = IdeTextMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                Text(
                  text = log.message,
                  color = when (log.type) {
                    LogType.SUCCESS -> IdeGreen
                    LogType.ERROR -> IdeRed
                    LogType.WARNING -> IdeYellow
                    LogType.COMMAND -> IdeCyan
                    LogType.INFO -> IdeTextPrimary
                  },
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
