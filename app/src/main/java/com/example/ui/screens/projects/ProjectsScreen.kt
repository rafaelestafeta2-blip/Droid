package com.example.ui.screens.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProjectItem
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
  projects: List<ProjectItem>,
  onOpenProject: (ProjectItem) -> Unit,
  onOpenCloneDialog: () -> Unit,
  onCreateNewTemplate: () -> Unit,
  onNavigateToEditor: () -> Unit,
  modifier: Modifier = Modifier
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = IdeCyan)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "DroidStudio • Projetos",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = IdeTextPrimary
              )
            )
          }
        },
        actions = {
          OutlinedButton(
            onClick = onNavigateToEditor,
            modifier = Modifier.padding(end = 8.dp).testTag("open_editor_direct_button")
          ) {
            Icon(Icons.Default.Code, contentDescription = null, tint = IdeCyan, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Abrir Editor", color = IdeCyan, fontSize = 12.sp)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = IdeSurface)
      )
    }
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(IdeBackground)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Action banner with 'Clonar do GitHub' and 'Novo Projeto'
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onOpenCloneDialog,
          modifier = Modifier.weight(1f).testTag("clone_github_button"),
          colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Clonar do GitHub", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onCreateNewTemplate,
          modifier = Modifier.weight(1f).testTag("new_project_template_screen_btn"),
          colors = ButtonDefaults.buttonColors(containerColor = IdeGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint = IdeBackground)
          Spacer(modifier = Modifier.width(6.dp))
          Text("Novo Projeto", fontSize = 13.sp, color = IdeBackground, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "PROJETOS RECENTES",
        style = MaterialTheme.typography.labelSmall.copy(
          color = IdeTextMuted,
          fontWeight = FontWeight.SemiBold,
          letterSpacing = 1.1.sp
        )
      )

      Spacer(modifier = Modifier.height(10.dp))

      if (projects.isEmpty()) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            tint = IdeTextMuted,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Nenhum projeto aberto",
            style = MaterialTheme.typography.titleSmall.copy(color = IdeTextPrimary)
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Clone um repositório do GitHub ou inicie um novo projeto com o template robusto.",
            style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted)
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxWidth().weight(1f),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(projects) { project ->
            ProjectCard(
              project = project,
              onOpenProject = onOpenProject
            )
          }
        }
      }
    }
  }
}
