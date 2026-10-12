package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun CloneRepositoryDialog(
  isCloning: Boolean,
  progressMessage: String,
  errorMessage: String?,
  onClone: (repoUrl: String, token: String?, branch: String?) -> Unit,
  onDismiss: () -> Unit
) {
  var repoUrl by remember { mutableStateOf("") }
  var token by remember { mutableStateOf("") }
  var branch by remember { mutableStateOf("main") }

  AlertDialog(
    onDismissRequest = { if (!isCloning) onDismiss() },
    containerColor = IdeSurface,
    shape = RoundedCornerShape(14.dp),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.CloudDownload,
          contentDescription = null,
          tint = IdeCyan,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Clonar Repositório GitHub",
          style = MaterialTheme.typography.titleMedium.copy(
            color = IdeTextPrimary,
            fontWeight = FontWeight.Bold
          )
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Importe um repositório Git público ou privado diretamente para o seu workspace local.",
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted)
        )

        OutlinedTextField(
          value = repoUrl,
          onValueChange = { repoUrl = it },
          label = { Text("URL ou dono/repositório *") },
          placeholder = { Text("ex: usuario/meu-app-android", color = IdeTextMuted) },
          leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = IdeCyan) },
          singleLine = true,
          enabled = !isCloning,
          modifier = Modifier.fillMaxWidth().testTag("clone_repo_url_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = IdeBackground,
            unfocusedContainerColor = IdeBackground,
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary
          )
        )

        OutlinedTextField(
          value = token,
          onValueChange = { token = it },
          label = { Text("Token de Acesso (Opcional)") },
          placeholder = { Text("ghp_... para repositórios privados", color = IdeTextMuted) },
          leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = IdeTextMuted) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          enabled = !isCloning,
          modifier = Modifier.fillMaxWidth().testTag("clone_token_input"),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = IdeBackground,
            unfocusedContainerColor = IdeBackground,
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary
          )
        )

        OutlinedTextField(
          value = branch,
          onValueChange = { branch = it },
          label = { Text("Branch (Padrão: main)") },
          singleLine = true,
          enabled = !isCloning,
          modifier = Modifier.fillMaxWidth(),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = IdeBackground,
            unfocusedContainerColor = IdeBackground,
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary
          )
        )

        if (isCloning) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(IdeBackground, RoundedCornerShape(8.dp))
              .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = IdeCyan,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = progressMessage.ifEmpty { "Clonando repositório..." },
              style = MaterialTheme.typography.bodySmall.copy(color = IdeCyan, fontSize = 12.sp)
            )
          }
        }

        if (!errorMessage.isNullOrBlank()) {
          Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodySmall.copy(color = IdeRed, fontSize = 12.sp)
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (repoUrl.isNotBlank()) {
            onClone(repoUrl.trim(), token.trim().ifEmpty { null }, branch.trim().ifEmpty { "main" })
          }
        },
        enabled = !isCloning && repoUrl.isNotBlank(),
        colors = ButtonDefaults.buttonColors(containerColor = IdeGreen),
        modifier = Modifier.testTag("confirm_clone_button")
      ) {
        Text("Clonar", color = IdeBackground, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        enabled = !isCloning
      ) {
        Text("Cancelar", color = IdeTextMuted)
      }
    }
  )
}
