package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.ui.theme.*

@Composable
fun AiChatPanel(
  messages: List<ChatMessage>,
  isAnalyzing: Boolean,
  selectedModel: String,
  onClose: () -> Unit,
  onSendPrompt: (String) -> Unit,
  onOpenPreview: () -> Unit = {},
  onRollback: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var inputPrompt by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size, messages.lastOrNull()?.steps?.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black.copy(alpha = 0.55f))
      .clickable(enabled = true, onClick = {}),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(0.94f)
        .fillMaxHeight(0.88f)
        .clip(RoundedCornerShape(16.dp))
        .testTag("ai_chat_modal"),
      color = IdeSurface,
      tonalElevation = 8.dp
    ) {
      Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IdePurple, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Agente Autônomo DroidStudio", style = MaterialTheme.typography.titleMedium.copy(color = IdeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp))
              Text("Executa e aplica no código em tempo real", fontSize = 11.sp, color = IdeGreen)
            }
          }
          IconButton(onClick = onClose, modifier = Modifier.testTag("close_ai_chat_button")) {
            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = IdeTextMuted)
          }
        }

        HorizontalDivider(color = IdeBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Messages List
        Box(modifier = Modifier.weight(1f)) {
          if (messages.isEmpty()) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IdePurple.copy(alpha = 0.6f), modifier = Modifier.size(44.dp))
              Spacer(modifier = Modifier.height(10.dp))
              Text("O que você deseja que eu crie agora?", color = IdeTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("Dê um comando e eu aplico as alterações diretamente no seu código!", color = IdeTextMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))

              Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                SuggestionChip(onClick = { onSendPrompt("Criar tela de login moderna com Jetpack Compose") }, label = { Text("Tela de Login", fontSize = 11.sp) })
                SuggestionChip(onClick = { onSendPrompt("Criar lista de tarefas com adicionar e concluir") }, label = { Text("Lista de Tarefas", fontSize = 11.sp) })
                SuggestionChip(onClick = { onSendPrompt("Criar um contador animado interativo") }, label = { Text("Contador", fontSize = 11.sp) })
                SuggestionChip(onClick = { onSendPrompt("Criar calculadora com operações matemáticas") }, label = { Text("Calculadora", fontSize = 11.sp) })
              }
            }
          } else {
            LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
              items(messages, key = { it.id }) { msg ->
                ChatMessageItem(message = msg, onOpenPreview = onOpenPreview, onRollback = onRollback)
              }
            }
          }
        }

        HorizontalDivider(color = IdeBorder, modifier = Modifier.padding(vertical = 8.dp))

        // Quick Suggestion Bar
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          AssistChip(onClick = { onSendPrompt("Criar tela de login com email e senha") }, label = { Text("🔐 Login", fontSize = 11.sp) })
          AssistChip(onClick = { onSendPrompt("Criar lista de tarefas interativa") }, label = { Text("📝 Tarefas", fontSize = 11.sp) })
          AssistChip(onClick = { onSendPrompt("Criar um contador animado") }, label = { Text("🔢 Contador", fontSize = 11.sp) })
          AssistChip(onClick = { onSendPrompt("Criar calculadora moderna") }, label = { Text("🧮 Calculadora", fontSize = 11.sp) })
          AssistChip(onClick = { onSendPrompt("Criar tela de previsão do tempo") }, label = { Text("☀️ Clima", fontSize = 11.sp) })
        }

        // Input Field
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = inputPrompt,
            onValueChange = { inputPrompt = it },
            placeholder = { Text("Ex: Crie uma tela de login ou um app de notas...", color = IdeTextMuted, fontSize = 12.sp) },
            modifier = Modifier.weight(1f).testTag("ai_chat_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = IdeTextPrimary,
              unfocusedTextColor = IdeTextPrimary,
              focusedBorderColor = IdePurple,
              unfocusedBorderColor = IdeBackground,
              focusedContainerColor = IdeBackground,
              unfocusedContainerColor = IdeBackground
            ),
            maxLines = 3,
            enabled = !isAnalyzing
          )

          Spacer(modifier = Modifier.width(8.dp))

          IconButton(
            onClick = {
              if (inputPrompt.isNotBlank() && !isAnalyzing) {
                val p = inputPrompt
                inputPrompt = ""
                onSendPrompt(p)
              }
            },
            enabled = inputPrompt.isNotBlank() && !isAnalyzing,
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(if (inputPrompt.isNotBlank() && !isAnalyzing) IdePurple else IdeBackground)
              .testTag("send_ai_chat_button")
          ) {
            if (isAnalyzing) {
              CircularProgressIndicator(modifier = Modifier.size(18.dp), color = IdePurple, strokeWidth = 2.dp)
            } else {
              Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar", tint = if (inputPrompt.isNotBlank()) Color.White else IdeTextMuted)
            }
          }
        }
      }
    }
  }
}
