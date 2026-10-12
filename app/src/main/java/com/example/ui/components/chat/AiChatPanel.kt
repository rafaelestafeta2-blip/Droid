package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun AiChatPanel(
    onClose: () -> Unit,
    onSendPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }

    // Backdrop sólido para evitar cliques passantes para o editor de código
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = true, onClick = {}), // Consome os cliques
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp)),
            color = IdeSurface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = IdePurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Agente Gemini Multi-File",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = IdeTextPrimary,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar Chat", tint = IdeTextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "O que você gostaria de construir ou alterar no aplicativo? O agente irá analisar o projeto inteiro e planejar as modificações nos arquivos necessários.",
                    style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted),
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Ex: Criar uma tela de login com Firebase...", color = IdeTextMuted, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = IdeTextPrimary,
                        unfocusedTextColor = IdeTextPrimary,
                        focusedBorderColor = IdePurple,
                        unfocusedBorderColor = IdeBackground,
                        focusedContainerColor = IdeBackground,
                        unfocusedContainerColor = IdeBackground
                    ),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (prompt.isNotBlank()) {
                            val p = prompt
                            prompt = ""
                            onSendPrompt(p)
                            onClose()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
                    shape = RoundedCornerShape(8.dp),
                    enabled = prompt.isNotBlank()
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gerar Alterações com IA", color = Color.White, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
