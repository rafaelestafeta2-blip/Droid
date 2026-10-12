package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.AiFixSuggestion
import com.example.model.FileChangeAction
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary

@Composable
fun AiFixDialog(
  suggestion: AiFixSuggestion?,
  isAnalyzing: Boolean,
  onApplyFix: () -> Unit,
  onRequestPrompt: (String) -> Unit,
  onRequestAutoDiagnose: () -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  var userPrompt by remember { mutableStateOf("") }
  var selectedFileChangeIndex by remember { mutableIntStateOf(0) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).testTag("ai_fix_dialog"),
      color = IdeSurface, tonalElevation = 6.dp
    ) {
      Column(modifier = Modifier.padding(18.dp).verticalScroll(rememberScrollState())) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AutoAwesome, contentDescription = "Gemini", tint = IdePurple, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Gemini Multi-File Agent", style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.weight(1f))
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = IdeTextSecondary)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TabRow(selectedTabIndex = selectedTab, containerColor = IdeBackground) {
          Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Prompt Livre", fontSize = 12.sp, color = if (selectedTab == 0) IdeCyan else IdeTextSecondary) })
          Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Diagnóstico Auto", fontSize = 12.sp, color = if (selectedTab == 1) IdePurple else IdeTextSecondary) })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
          OutlinedTextField(
            value = userPrompt, onValueChange = { userPrompt = it },
            placeholder = { Text("Ex: Crie o ViewModel de perfil e integre na MainActivity...", color = IdeTextMuted, fontSize = 12.sp) },
            label = { Text("Instrução Multi-Arquivo", color = IdeTextSecondary, fontSize = 11.sp) },
            colors = TextFieldDefaults.colors(
              focusedContainerColor = IdeBackground, unfocusedContainerColor = IdeBackground,
              focusedTextColor = IdeTextPrimary, unfocusedTextColor = IdeTextPrimary,
              focusedIndicatorColor = IdeCyan, unfocusedIndicatorColor = IdeTextMuted
            ),
            modifier = Modifier.fillMaxWidth().testTag("custom_prompt_input"), maxLines = 3
          )
          Spacer(modifier = Modifier.height(8.dp))
          Button(
            onClick = { if (userPrompt.isNotBlank()) onRequestPrompt(userPrompt) },
            enabled = !isAnalyzing && userPrompt.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = IdeCyan),
            modifier = Modifier.fillMaxWidth().testTag("send_prompt_button")
          ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = IdeBackground, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Gerar Alterações em Múltiplos Arquivos", color = IdeBackground, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = onRequestAutoDiagnose, enabled = !isAnalyzing,
            colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
            modifier = Modifier.fillMaxWidth().testTag("run_auto_diagnosis_button")
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = "Diagnosticar", modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Diagnosticar Falhas do Build", fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isAnalyzing) {
          Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = IdePurple, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gemini processando arquitetura multi-arquivo...", color = IdeTextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
          }
        } else if (suggestion != null) {
          Text(text = suggestion.issueTitle, style = MaterialTheme.typography.bodyMedium.copy(color = IdePurple, fontWeight = FontWeight.SemiBold))
          Text(text = suggestion.explanation, style = MaterialTheme.typography.bodySmall.copy(color = IdeTextSecondary, fontSize = 12.sp))
          Spacer(modifier = Modifier.height(10.dp))

          val changes = suggestion.changes
          if (changes.isNotEmpty()) {
            Text("Arquivos Afetados (${changes.size}):", fontSize = 11.sp, color = IdeTextMuted, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
              changes.forEachIndexed { idx, item ->
                val isSelected = idx == selectedFileChangeIndex
                val badgeColor = if (item.action == FileChangeAction.CREATE) IdeGreen else IdeCyan
                val label = if (item.action == FileChangeAction.CREATE) "[+ Novo]" else "[~ Edit]"
                Box(
                  modifier = Modifier
                    .padding(end = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) badgeColor.copy(alpha = 0.25f) else IdeBorder)
                    .clickable { selectedFileChangeIndex = idx }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                  Text(text = "$label ${item.filePath.substringAfterLast("/")}", fontSize = 11.sp, color = if (isSelected) badgeColor else IdeTextPrimary, fontFamily = FontFamily.Monospace)
                }
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            val currentChange = changes.getOrNull(selectedFileChangeIndex) ?: changes.first()
            AiDiffViewer(change = currentChange)
          }

          Spacer(modifier = Modifier.height(14.dp))
          Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
              Text("Cancelar", color = IdeTextSecondary, fontSize = 12.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
              onClick = onApplyFix, colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
              modifier = Modifier.weight(1.5f).testTag("apply_ai_fix_button")
            ) {
              Icon(Icons.Default.Check, contentDescription = "Aplicar", modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Aplicar no Código (${changes.size})", fontSize = 12.sp)
            }
          }
        }
      }
    }
  }
}
