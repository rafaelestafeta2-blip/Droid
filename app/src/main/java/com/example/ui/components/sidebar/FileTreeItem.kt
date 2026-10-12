package com.example.ui.components.sidebar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileNode
import com.example.model.FileType
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeOrange
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary

@Composable
fun FileTreeItem(
  node: FileNode,
  depth: Int,
  isSelected: Boolean,
  onFileClick: (FileNode) -> Unit,
  onToggleFolder: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val isFolder = node.isDirectory
  val background = if (isSelected) {
    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
  } else Color.Transparent

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(36.dp)
      .padding(start = (depth * 14).dp, end = 8.dp)
      .clip(RoundedCornerShape(6.dp))
      .background(background)
      .clickable {
        if (isFolder) {
          onToggleFolder(node.id)
        } else {
          onFileClick(node)
        }
      }
      .testTag("file_tree_item_${node.id}")
      .padding(horizontal = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    if (isFolder) {
      Icon(
        imageVector = if (node.isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
        contentDescription = if (node.isExpanded) "Recolher pasta" else "Expandir pasta",
        tint = IdeTextSecondary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Icon(
        imageVector = if (node.isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
        contentDescription = "Pasta",
        tint = IdeOrange,
        modifier = Modifier.size(18.dp)
      )
    } else {
      Spacer(modifier = Modifier.width(20.dp))
      FileIcon(node.type)
    }

    Spacer(modifier = Modifier.width(8.dp))

    Text(
      text = node.name,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontSize = 13.sp,
        fontFamily = FontFamily.Monospace,
        color = if (isSelected) IdeCyan else IdeTextPrimary
      ),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

@Composable
private fun FileIcon(type: FileType) {
  val (icon, tint) = when (type) {
    FileType.KOTLIN -> Icons.Default.Code to IdePurple
    FileType.XML -> Icons.Default.DataObject to IdeGreen
    FileType.GRADLE -> Icons.Default.Settings to IdeCyan
    FileType.JSON -> Icons.Default.DataObject to IdeOrange
    FileType.PROPERTIES, FileType.OTHER -> Icons.Default.Description to IdeTextSecondary
    FileType.DIRECTORY -> Icons.Default.Folder to IdeOrange
  }

  Icon(
    imageVector = icon,
    contentDescription = type.name,
    tint = tint,
    modifier = Modifier.size(16.dp)
  )
}
