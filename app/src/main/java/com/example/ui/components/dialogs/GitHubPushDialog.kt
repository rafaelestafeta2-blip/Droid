package com.example.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary

@Composable
fun GitHubPushDialog(
  initialToken: String,
  initialOwner: String,
  initialRepo: String,
  initialWorkflowId: String,
  initialBranch: String,
  isDispatching: Boolean,
  onTriggerDispatch: (token: String, owner: String, repo: String, workflowId: String, branch: String) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var token by remember { mutableStateOf(initialToken) }
  var owner by remember { mutableStateOf(initialOwner.ifEmpty { "android-developer" }) }
  var repo by remember { mutableStateOf(initialRepo.ifEmpty { "my-android-app" }) }
  var workflowId by remember { mutableStateOf(initialWorkflowId.ifEmpty { "android-ci.yml" }) }
  var branch by remember { mutableStateOf(initialBranch.ifEmpty { "main" }) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .testTag("github_push_dialog"),
      color = IdeSurface,
      tonalElevation = 6.dp
    ) {
      Column(
        modifier = Modifier
          .padding(18.dp)
          .verticalScroll(rememberScrollState())
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "GitHub Actions Workflow",
            style = MaterialTheme.typography.titleMedium.copy(
              color = IdeTextPrimary,
              fontWeight = FontWeight.Bold
            )
          )
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = IdeTextSecondary)
          }
        }

        Text(
          text = "Dispara POST /repos/{owner}/{repo}/actions/workflows/{workflow_id}/dispatches",
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted, fontSize = 11.sp),
          modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        // Inputs
        GitHubField(label = "GitHub Personal Access Token (PAT)", value = token, onValueChange = { token = it }, isPassword = true)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
          GitHubField(label = "Dono (Owner)", value = owner, onValueChange = { owner = it }, modifier = Modifier.weight(1f))
          Spacer(modifier = Modifier.width(8.dp))
          GitHubField(label = "Repositório (Repo)", value = repo, onValueChange = { repo = it }, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
          GitHubField(label = "Workflow ID / Arquivo", value = workflowId, onValueChange = { workflowId = it }, modifier = Modifier.weight(1.3f))
          Spacer(modifier = Modifier.width(8.dp))
          GitHubField(label = "Branch (Ref)", value = branch, onValueChange = { branch = it }, modifier = Modifier.weight(0.9f))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Buttons
        Row(modifier = Modifier.fillMaxWidth()) {
          OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
            Text("Cancelar", color = IdeTextSecondary, fontSize = 12.sp)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = { onTriggerDispatch(token, owner, repo, workflowId, branch) },
            enabled = !isDispatching,
            colors = ButtonDefaults.buttonColors(containerColor = IdeCyan),
            modifier = Modifier
              .weight(1.6f)
              .testTag("confirm_github_push_button")
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

@Composable
private fun GitHubField(
  label: String,
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  isPassword: Boolean = false
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, color = IdeTextSecondary, fontSize = 11.sp) },
    colors = TextFieldDefaults.colors(
      focusedContainerColor = IdeBackground,
      unfocusedContainerColor = IdeBackground,
      focusedTextColor = IdeTextPrimary,
      unfocusedTextColor = IdeTextPrimary,
      focusedIndicatorColor = IdeCyan,
      unfocusedIndicatorColor = IdeTextMuted
    ),
    visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
    modifier = modifier.fillMaxWidth(),
    singleLine = true
  )
}
