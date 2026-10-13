package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.ChatSender
import com.example.model.FileChangeAction
import androidx.compose.material.icons.filled.Smartphone
import com.example.ui.theme.*

@Composable
fun ChatMessageItem(
  message: ChatMessage,
  onOpenPreview: () -> Unit = {},
  onRollback: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val isUser = message.sender == ChatSender.USER

  Row(
    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp),
    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
  ) {
    if (!isUser) {
      Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(IdePurple.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.SmartToy, contentDescription = null, tint = IdePurple, modifier = Modifier.size(18.dp))
      }
      Spacer(modifier = Modifier.width(8.dp))
    }

    Column(
      modifier = Modifier
        .widthIn(max = 300.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(if (isUser) IdePurple.copy(alpha = 0.25f) else IdeSurface)
        .padding(12.dp)
    ) {
      Text(
        text = if (isUser) "Você" else "Agente Autônomo DroidStudio",
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = if (isUser) IdeCyan else IdePurple
      )

      Spacer(modifier = Modifier.height(4.dp))

      if (message.steps.isNotEmpty()) {
        AgentExecutionStepsView(steps = message.steps)
      }

      if (message.isThinking && message.steps.isEmpty()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = IdePurple)
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = message.text, color = IdeTextMuted, fontSize = 12.sp)
        }
      } else {
        Text(
          text = message.text,
          color = IdeTextPrimary,
          fontSize = 13.sp,
          lineHeight = 18.sp
        )

        if (message.suggestedChanges.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = IdeBackground,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Check, contentDescription = null, tint = IdeGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Alterações aplicadas no projeto", fontSize = 11.sp, color = IdeGreen, fontWeight = FontWeight.Bold)
              }

              Spacer(modifier = Modifier.height(4.dp))

              message.suggestedChanges.forEach { change ->
                val isCreate = change.action == FileChangeAction.CREATE
                Row(modifier = Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Description, contentDescription = null, tint = if (isCreate) IdeGreen else IdeCyan, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "${if (isCreate) "[+ Novo]" else "[~ Modificado]"} ${change.filePath.substringAfterLast("/")}",
                    fontSize = 11.sp,
                    color = if (isCreate) IdeGreen else IdeTextPrimary
                  )
                }
              }

              Spacer(modifier = Modifier.height(8.dp))

              Button(
                onClick = onOpenPreview,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
                shape = RoundedCornerShape(6.dp)
              ) {
                Icon(Icons.Default.Smartphone, contentDescription = null, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Abrir Live Preview", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
              }

              Spacer(modifier = Modifier.height(4.dp))

              FilledTonalButton(
                onClick = onRollback,
                modifier = Modifier.fillMaxWidth().height(28.dp),
                colors = ButtonDefaults.filledTonalButtonColors(containerColor = IdeOrange.copy(alpha = 0.2f), contentColor = IdeOrange),
                shape = RoundedCornerShape(6.dp)
              ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Desfazer Alterações (Rollback)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    if (isUser) {
      Spacer(modifier = Modifier.width(8.dp))
      Box(
        modifier = Modifier.size(32.dp).clip(CircleShape).background(IdeCyan.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Person, contentDescription = null, tint = IdeCyan, modifier = Modifier.size(18.dp))
      }
    }
  }
}
