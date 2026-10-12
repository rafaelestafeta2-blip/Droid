package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.supabase.model.SupabaseRlsDiagnostic
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun SupabaseRlsDiagnosticDialog(
  diagnostic: SupabaseRlsDiagnostic,
  isApplying: Boolean = false,
  onApplyFix: (String) -> Unit,
  onDismiss: () -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = IdeSurface,
      modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = IdePurple, modifier = Modifier.size(24.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Auto-Diagnóstico de RLS",
            style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold)
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "Tabela: ${diagnostic.tableName} • Operação: ${diagnostic.attemptedOperation}",
          style = MaterialTheme.typography.labelMedium.copy(color = IdeGreen, fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = diagnostic.rootCause,
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = "SQL RECOMENDADO:",
          style = MaterialTheme.typography.labelSmall.copy(color = IdeTextMuted, fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = IdeBackground,
          modifier = Modifier.fillMaxWidth().height(110.dp)
        ) {
          Text(
            text = diagnostic.recommendedSqlFix,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = IdeTextPrimary,
            modifier = Modifier.padding(10.dp).horizontalScroll(rememberScrollState())
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).testTag("dismiss_rls_dialog_btn")
          ) {
            Text("Cancelar", fontSize = 12.sp, color = IdeTextMuted)
          }

          Button(
            onClick = { onApplyFix(diagnostic.recommendedSqlFix) },
            modifier = Modifier.weight(1.5f).testTag("apply_rls_fix_button"),
            colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
            shape = RoundedCornerShape(8.dp),
            enabled = !isApplying
          ) {
            if (isApplying) {
              CircularProgressIndicator(modifier = Modifier.size(14.dp), color = IdeTextPrimary, strokeWidth = 2.dp)
            } else {
              Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Aplicar no Supabase", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}
