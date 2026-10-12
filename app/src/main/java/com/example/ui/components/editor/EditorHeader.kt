package com.example.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildStatus
import com.example.model.FileNode
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary

@Composable
fun EditorHeader(
  tabs: List<FileNode>,
  activeFile: FileNode?,
  isDirty: Boolean,
  buildStatus: BuildStatus,
  isPreviewOpen: Boolean = false,
  onTabClick: (FileNode) -> Unit,
  onCloseTab: (FileNode) -> Unit,
  onRunBuild: () -> Unit,
  onTogglePreview: () -> Unit = {},
  onGitHubPush: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(44.dp)
      .background(IdeSurface)
      .padding(horizontal = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Scrollable Tabs
    Row(
      modifier = Modifier
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      verticalAlignment = Alignment.CenterVertically
    ) {
      tabs.forEach { file ->
        val isActive = file.id == activeFile?.id
        EditorTabItem(
          file = file,
          isActive = isActive,
          showDirty = isActive && isDirty,
          onClick = { onTabClick(file) },
          onClose = { onCloseTab(file) }
        )
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    // Live Preview Button
    FilledTonalButton(
      onClick = onTogglePreview,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = if (isPreviewOpen) IdePurple else IdeSurface,
        contentColor = if (isPreviewOpen) Color.White else IdeCyan
      ),
      modifier = Modifier
        .height(34.dp)
        .testTag("toggle_preview_button")
    ) {
      Icon(Icons.Default.Smartphone, contentDescription = "Live Preview", modifier = Modifier.size(15.dp))
      Spacer(modifier = Modifier.width(3.dp))
      Text("Preview", fontSize = 12.sp)
    }

    Spacer(modifier = Modifier.width(4.dp))

    // Build action button
    FilledTonalButton(
      onClick = onRunBuild,
      enabled = buildStatus != BuildStatus.BUILDING,
      colors = ButtonDefaults.filledTonalButtonColors(
        containerColor = IdeGreen.copy(alpha = 0.2f),
        contentColor = IdeGreen
      ),
      modifier = Modifier
        .height(34.dp)
        .testTag("run_build_button")
    ) {
      if (buildStatus == BuildStatus.BUILDING) {
        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = IdeGreen)
      } else {
        Icon(Icons.Default.PlayArrow, contentDescription = "Rodar Build", modifier = Modifier.size(16.dp))
      }
      Spacer(modifier = Modifier.width(4.dp))
      Text("Build", fontSize = 12.sp)
    }

    Spacer(modifier = Modifier.width(4.dp))

    // GitHub Push button
    IconButton(
      onClick = onGitHubPush,
      modifier = Modifier
        .size(34.dp)
        .testTag("github_push_button")
    ) {
      Icon(Icons.Default.Upload, contentDescription = "Enviar para o GitHub", tint = IdeCyan, modifier = Modifier.size(18.dp))
    }
  }
}

@Composable
private fun EditorTabItem(
  file: FileNode,
  isActive: Boolean,
  showDirty: Boolean,
  onClick: () -> Unit,
  onClose: () -> Unit
) {
  val bg = if (isActive) IdeBackground else IdeSurface
  Row(
    modifier = Modifier
      .height(44.dp)
      .background(bg)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = file.name,
      style = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        color = if (isActive) IdeTextPrimary else IdeTextSecondary
      )
    )
    Spacer(modifier = Modifier.width(6.dp))
    if (showDirty) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(IdeCyan)
      )
      Spacer(modifier = Modifier.width(4.dp))
    }
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Fechar aba",
      tint = IdeTextSecondary,
      modifier = Modifier
        .size(14.dp)
        .clickable(onClick = onClose)
    )
  }
}
