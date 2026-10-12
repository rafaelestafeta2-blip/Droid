package com.example.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectItem
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun ProjectCard(
  project: ProjectItem,
  onOpenProject: (ProjectItem) -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .border(1.dp, IdeBorder, RoundedCornerShape(12.dp))
      .clickable { onOpenProject(project) }
      .testTag("project_card_${project.id}"),
    colors = CardDefaults.cardColors(containerColor = IdeSurface)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .background(
              if (project.isGitRepo) IdePurple.copy(alpha = 0.2f) else IdeCyan.copy(alpha = 0.2f),
              RoundedCornerShape(8.dp)
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (project.isGitRepo) Icons.Default.Source else Icons.Default.Folder,
            contentDescription = null,
            tint = if (project.isGitRepo) IdePurple else IdeCyan,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = project.name,
            style = MaterialTheme.typography.titleMedium.copy(
              color = IdeTextPrimary,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          )
          Text(
            text = project.description,
            style = MaterialTheme.typography.bodySmall.copy(
              color = IdeTextMuted,
              fontSize = 12.sp
            )
          )
        }

        FilledTonalButton(
          onClick = { onOpenProject(project) },
          modifier = Modifier.testTag("open_project_btn_${project.id}")
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp), tint = IdeGreen)
          Spacer(modifier = Modifier.width(4.dp))
          Text("Abrir", fontSize = 12.sp, color = IdeGreen, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Code, contentDescription = null, tint = IdeTextMuted, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${project.fileCount} arquivos",
            style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted, fontSize = 11.sp)
          )
        }

        Text(
          text = if (project.isGitRepo) "Git branch: ${project.branch}" else "Template Nativo",
          style = MaterialTheme.typography.bodySmall.copy(
            color = if (project.isGitRepo) IdePurple else IdeCyan,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
          )
        )
      }
    }
  }
}
