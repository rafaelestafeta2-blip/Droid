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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.supabase.model.SupabaseConfig
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun SupabaseConfigDialog(
  initialConfig: SupabaseConfig,
  isTestingConnection: Boolean = false,
  onSaveConfig: (String, String) -> Unit,
  onTestConnection: (String, String) -> Unit,
  onDismiss: () -> Unit
) {
  var token by remember { mutableStateOf(initialConfig.accessToken) }
  var projectRef by remember { mutableStateOf(initialConfig.projectRef) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = IdeSurface,
      modifier = Modifier.fillMaxWidth().padding(12.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Cloud, contentDescription = null, tint = IdeGreen, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Supabase Back-End",
            style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Configure as credenciais da Management API do Supabase para migrações e RLS.",
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = projectRef,
          onValueChange = { projectRef = it },
          label = { Text("Project Reference (ex: abcdefghijklm)") },
          placeholder = { Text("ID do projeto Supabase") },
          modifier = Modifier.fillMaxWidth().testTag("supabase_project_ref_input"),
          singleLine = true,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary,
            focusedBorderColor = IdeGreen,
            unfocusedBorderColor = IdeBackground
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = token,
          onValueChange = { token = it },
          label = { Text("Access Token (sbp_...)") },
          placeholder = { Text("Token de acesso pessoal") },
          modifier = Modifier.fillMaxWidth().testTag("supabase_token_input"),
          singleLine = true,
          leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = IdeCyan) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary,
            focusedBorderColor = IdeGreen,
            unfocusedBorderColor = IdeBackground
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status indicator
        Row(verticalAlignment = Alignment.CenterVertically) {
          val isConnected = initialConfig.isConnected
          Icon(
            imageVector = if (isConnected) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (isConnected) IdeGreen else IdeTextMuted,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = if (isConnected) "Conectado ao Supabase Cloud" else "Não conectado ou credenciais pendentes",
            fontSize = 12.sp,
            color = if (isConnected) IdeGreen else IdeTextMuted
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { onTestConnection(token, projectRef) },
            modifier = Modifier.weight(1f).testTag("test_supabase_connection_button"),
            enabled = token.isNotBlank() && projectRef.isNotBlank() && !isTestingConnection
          ) {
            if (isTestingConnection) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), color = IdeCyan, strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.Sync, contentDescription = null, tint = IdeCyan, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Testar", fontSize = 12.sp, color = IdeCyan)
            }
          }

          Button(
            onClick = { onSaveConfig(token, projectRef) },
            modifier = Modifier.weight(1f).testTag("save_supabase_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IdeGreen),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text("Salvar", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IdeBackground)
          }
        }
      }
    }
  }
}
