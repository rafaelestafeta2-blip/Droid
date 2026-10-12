package com.example.ui.components.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.FolderShared
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileNode
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun ProjectSidebarDrawer(
  rootNode: FileNode,
  selectedFileId: String?,
  onFileSelect: (FileNode) -> Unit,
  onToggleFolder: (String) -> Unit,
  onResetTemplate: (() -> Unit)? = null,
  onCloneGitHub: (() -> Unit)? = null,
  onOpenDashboard: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }

  Surface(
    modifier = modifier
      .fillMaxHeight()
      .width(280.dp)
      .testTag("project_sidebar_drawer"),
    color = IdeSurface,
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.fillMaxHeight()) {
      // Header
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.AccountTree,
          contentDescription = "Explorador",
          tint = IdeCyan,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "EXPLORADOR",
          style = MaterialTheme.typography.labelMedium.copy(
            color = IdeTextPrimary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          )
        )
        Spacer(modifier = Modifier.weight(1f))
        if (onCloneGitHub != null) {
          IconButton(
            onClick = onCloneGitHub,
            modifier = Modifier.size(28.dp).testTag("sidebar_clone_github_button")
          ) {
            Icon(
              imageVector = Icons.Default.CloudDownload,
              contentDescription = "Clonar do GitHub",
              tint = IdePurple,
              modifier = Modifier.size(18.dp)
            )
          }
        }
        if (onOpenDashboard != null) {
          IconButton(
            onClick = onOpenDashboard,
            modifier = Modifier.size(28.dp).testTag("sidebar_dashboard_button")
          ) {
            Icon(
              imageVector = Icons.Default.FolderShared,
              contentDescription = "Biblioteca de Projetos",
              tint = IdeCyan,
              modifier = Modifier.size(18.dp)
            )
          }
        }
        if (onResetTemplate != null) {
          IconButton(
            onClick = onResetTemplate,
            modifier = Modifier.size(28.dp).testTag("new_project_template_button")
          ) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "Novo Projeto (Template Padrão)",
              tint = IdeGreen,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      HorizontalDivider(color = IdeBorder, thickness = 1.dp)

      // Search field
      TextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Buscar arquivo...", fontSize = 12.sp, color = IdeTextMuted) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = "Buscar", tint = IdeTextMuted, modifier = Modifier.size(16.dp))
        },
        colors = TextFieldDefaults.colors(
          focusedContainerColor = IdeBackground,
          unfocusedContainerColor = IdeBackground,
          focusedTextColor = IdeTextPrimary,
          unfocusedTextColor = IdeTextPrimary,
          focusedIndicatorColor = Color.Transparent,
          unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .padding(horizontal = 10.dp, vertical = 4.dp),
        singleLine = true
      )

      HorizontalDivider(color = IdeBorder, thickness = 1.dp)

      // File Tree List
      val flattenedList = remember(rootNode, searchQuery) {
        flattenTree(rootNode, 0, searchQuery)
      }

      LazyColumn(
        modifier = Modifier
          .weight(1f)
          .padding(vertical = 4.dp)
      ) {
        items(flattenedList.size) { index ->
          val item = flattenedList[index]
          FileTreeItem(
            node = item.node,
            depth = item.depth,
            isSelected = item.node.id == selectedFileId,
            onFileClick = onFileSelect,
            onToggleFolder = onToggleFolder
          )
        }
      }
    }
  }
}

private data class FlattenedItem(val node: FileNode, val depth: Int)

private fun flattenTree(node: FileNode, depth: Int, query: String): List<FlattenedItem> {
  val result = mutableListOf<FlattenedItem>()
  val matches = query.isEmpty() || node.name.contains(query, ignoreCase = true)

  if (matches || node.isDirectory) {
    result.add(FlattenedItem(node, depth))
  }

  if (node.isExpanded || query.isNotEmpty()) {
    for (child in node.children) {
      result.addAll(flattenTree(child, depth + 1, query))
    }
  }
  return result
}
