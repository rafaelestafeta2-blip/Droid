package com.example.ui.components.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun ResetTemplateConfirmDialog(
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = IdeSurface,
      modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
      Column(modifier = Modifier.padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = IdeGreen, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Novo Projeto (Template)",
            style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Deseja inicializar um novo projeto a partir do template padrão? Isso carregará a estrutura inicial com Jetpack Compose, Retrofit e Room.",
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted),
          fontSize = 13.sp,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          OutlinedButton(onClick = onDismiss) {
            Text("Cancelar", color = IdeTextMuted)
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onConfirm()
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(containerColor = IdeGreen)
          ) {
            Text("Iniciar Novo Projeto", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
