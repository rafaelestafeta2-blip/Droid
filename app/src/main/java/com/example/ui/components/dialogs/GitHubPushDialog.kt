package com.example.ui.components.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.*
import com.example.util.GitHubUrlParser

@Composable
fun GitHubPushDialog(
  initialToken: String,
  initialOwner: String,
  initialRepo: String,
  initialWorkflowId: String,
  initialBranch: String,
  isDispatching: Boolean,
  onTriggerDispatch: (token: String, owner: String, repo: String, workflowId: String, branch: String) -> Unit,
  onRunLocalBuild: () -> Unit = {},
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  var selectedTab by remember { mutableIntStateOf(0) }
  var repoUrlInput by remember { mutableStateOf(if (initialOwner.isNotBlank() && initialRepo.isNotBlank()) "https://github.com/$initialOwner/$initialRepo" else "") }
  var token by remember { mutableStateOf(initialToken) }
  var owner by remember { mutableStateOf(initialOwner.ifEmpty { "android-developer" }) }
  var repo by remember { mutableStateOf(initialRepo.ifEmpty { "my-android-app" }) }
  var workflowId by remember { mutableStateOf(initialWorkflowId.ifEmpty { "main.yml" }) }
  var branch by remember { mutableStateOf(initialBranch.ifEmpty { "main" }) }

  LaunchedEffect(repoUrlInput) {
    if (repoUrlInput.isNotBlank()) {
      val parsed = GitHubUrlParser.parse(repoUrlInput)
      if (parsed.isValid) {
        owner = parsed.owner
        repo = parsed.repo
      }
    }
  }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).testTag("github_push_dialog"),
      color = IdeSurface, tonalElevation = 6.dp
    ) {
      Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Text(text = "GitHub Actions CI/CD", style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = IdeTextSecondary)
          }
        }

        // Tabs
        TabRow(selectedTabIndex = selectedTab, containerColor = IdeBackground, modifier = Modifier.padding(vertical = 10.dp)) {
          Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Conexão Fácil (URL)", fontSize = 11.sp, color = if (selectedTab == 0) IdeCyan else IdeTextSecondary) })
          Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Avançado / Manual", fontSize = 11.sp, color = if (selectedTab == 1) IdePurple else IdeTextSecondary) })
        }

        // Card explicativo: Build Local Disponível
        Surface(shape = RoundedCornerShape(8.dp), color = IdeGreen.copy(alpha = 0.12f), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
          Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Terminal, contentDescription = null, tint = IdeGreen, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text("Compilar Localmente (Zero Configuração)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IdeGreen)
              Text("Não precisa de conta ou Token para testar seu app!", fontSize = 10.sp, color = IdeTextSecondary)
            }
            TextButton(onClick = { onRunLocalBuild(); onDismiss() }, contentPadding = PaddingValues(horizontal = 8.dp)) {
              Text("Build Local", fontSize = 11.sp, color = IdeGreen, fontWeight = FontWeight.Bold)
            }
          }
        }

        if (selectedTab == 0) {
          // Input de URL única
          OutlinedTextField(
            value = repoUrlInput,
            onValueChange = { repoUrlInput = it },
            label = { Text("URL ou dono/repositório", fontSize = 11.sp) },
            placeholder = { Text("Ex: https://github.com/usuario/meu-app", fontSize = 11.sp, color = IdeTextMuted) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          if (owner.isNotBlank() && repo.isNotBlank()) {
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Check, contentDescription = null, tint = IdeCyan, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Dono: $owner | Repositório: $repo", fontSize = 10.sp, color = IdeCyan, fontFamily = FontFamily.Monospace)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Input de Token
          OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("GitHub Token (PAT)", fontSize = 11.sp) },
            placeholder = { Text("ghp_xxxxxxxxxxxx", fontSize = 11.sp, color = IdeTextMuted) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          // Link de ajuda para criar token
          Row(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text("Necessário para disparar o Actions", fontSize = 10.sp, color = IdeTextMuted)
            TextButton(
              onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/settings/tokens/new?scopes=repo,workflow"))
                context.startActivity(intent)
              },
              contentPadding = PaddingValues(0.dp)
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(12.dp), tint = IdeCyan)
              Spacer(modifier = Modifier.width(3.dp))
              Text("Gerar Token no GitHub", fontSize = 10.sp, color = IdeCyan)
            }
          }
        } else {
          // Modo Manual Avançado
          OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("GitHub Token (PAT)") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = owner, onValueChange = { owner = it }, label = { Text("Dono") }, singleLine = true, modifier = Modifier.weight(1f))
            OutlinedTextField(value = repo, onValueChange = { repo = it }, label = { Text("Repositório") }, singleLine = true, modifier = Modifier.weight(1f))
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = workflowId, onValueChange = { workflowId = it }, label = { Text("Workflow (.yml)") }, singleLine = true, modifier = Modifier.weight(1.2f))
            OutlinedTextField(value = branch, onValueChange = { branch = it }, label = { Text("Branch") }, singleLine = true, modifier = Modifier.weight(0.8f))
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botões de ação
        Row(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("Cancelar", color = IdeTextSecondary, fontSize = 12.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = { onTriggerDispatch(token, owner, repo, workflowId, branch) },
            enabled = !isDispatching && token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = IdeCyan),
            modifier = Modifier.weight(1.6f).testTag("confirm_github_push_button")
          ) {
            if (isDispatching) {
              CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = IdeBackground)
            } else {
              Icon(Icons.Default.PlayArrow, contentDescription = "Disparar", tint = IdeBackground, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Disparar Build", color = IdeBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
