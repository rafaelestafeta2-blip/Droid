package com.example.data.api

import com.example.data.supabase.model.SupabaseToolAction
import com.example.model.AiFileChange
import com.example.model.AiFixSuggestion
import com.example.model.FileChangeAction

/**
 * Gerador inteligente de arquivos para o Agente IA quando operando
 * offline ou com fallback de alta fidelidade para Jetpack Compose.
 */
object AiSmartGenerator {

  fun generateSuggestionForPrompt(
    fileName: String,
    currentCode: String,
    query: String
  ): AiFixSuggestion {
    val lower = query.lowercase().trim()
    val isCloud = GeminiPromptBuilder.isCloudPersistenceRequested(query)

    return when {
      lower.contains("login") || lower.contains("loguin") || lower.contains("autentic") -> {
        generateLoginSuite(fileName, currentCode, isCloud)
      }
      lower.contains("cadastro") || lower.contains("registro") || lower.contains("register") -> {
        generateRegisterSuite(fileName, currentCode)
      }
      lower.contains("todo") || lower.contains("tarefa") || lower.contains("lista") -> {
        generateTodoListSuite(fileName, currentCode, isCloud)
      }
      else -> {
        generateGenericComponent(fileName, currentCode, query, isCloud)
      }
    }
  }

  private fun generateLoginSuite(
    activeFileName: String,
    currentCode: String,
    isCloud: Boolean
  ): AiFixSuggestion {
    val loginContent = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdePurple
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun LoginScreen(
  onLoginSuccess: (email: String) -> Unit = {},
  onNavigateToRegister: () -> Unit = {}
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isLoading by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(IdeBackground)
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = IdeSurface,
      tonalElevation = 8.dp,
      modifier = Modifier.fillMaxWidth().wrapContentHeight()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Bem-vindo de volta",
          style = MaterialTheme.typography.headlineSmall.copy(
            color = IdeTextPrimary,
            fontWeight = FontWeight.Bold
          )
        )
        Text(
          text = "Entre na sua conta para continuar",
          style = MaterialTheme.typography.bodySmall.copy(color = IdeTextMuted),
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("E-mail") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = IdeCyan) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary,
            focusedBorderColor = IdeCyan,
            unfocusedBorderColor = IdeBackground
          )
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Senha") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IdePurple) },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null,
                tint = IdeTextMuted
              )
            }
          },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = IdeTextPrimary,
            unfocusedTextColor = IdeTextPrimary,
            focusedBorderColor = IdePurple,
            unfocusedBorderColor = IdeBackground
          )
        )

        errorMessage?.let { err ->
          Text(text = err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = {
            if (email.isBlank() || password.isBlank()) {
              errorMessage = "Preencha todos os campos."
            } else {
              isLoading = true
              onLoginSuccess(email)
            }
          },
          modifier = Modifier.fillMaxWidth().height(48.dp),
          colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
          shape = RoundedCornerShape(12.dp),
          enabled = !isLoading
        ) {
          if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
          } else {
            Text("Entrar", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToRegister) {
          Text("Não tem uma conta? Cadastre-se", color = IdeCyan, fontSize = 13.sp)
        }
      }
    }
  }
}
""".trimIndent()

    val changes = listOf(
      AiFileChange(
        filePath = "app/src/main/java/com/example/ui/screens/LoginScreen.kt",
        action = FileChangeAction.CREATE,
        explanation = "Criação da tela completa de autenticação com validação e Jetpack Compose.",
        fullContent = loginContent,
        originalContent = ""
      )
    )

    return AiFixSuggestion(
      issueTitle = "Implementação: Tela de Login",
      explanation = "Criei o componente completo LoginScreen.kt com campos de email, senha com toggle de visibilidade e suporte a validação.",
      fullFixedContent = loginContent,
      changes = changes,
      supabaseActions = if (isCloud) listOf(
        SupabaseToolAction(
          type = "SQL_MIGRATION",
          sql = "CREATE TABLE IF NOT EXISTS public.profiles (id uuid references auth.users on delete cascade primary key, email text, updated_at timestamptz default now());",
          description = "Tabela de perfis para autenticação",
          tableName = "profiles"
        )
      ) else emptyList()
    )
  }

  private fun generateRegisterSuite(activeFileName: String, currentCode: String): AiFixSuggestion {
    return generateLoginSuite(activeFileName, currentCode, false)
  }

  private fun generateTodoListSuite(activeFileName: String, currentCode: String, isCloud: Boolean): AiFixSuggestion {
    return generateLoginSuite(activeFileName, currentCode, isCloud)
  }

  private fun generateGenericComponent(
    fileName: String,
    code: String,
    query: String,
    isCloud: Boolean
  ): AiFixSuggestion {
    val dynamicCode = com.example.data.agent.AgentScreenTemplatesExtended.getDynamicCustomScreenCode(query)
    val mainCode = com.example.data.agent.AgentMainActivityRewriter.buildMainActivityCode("CustomFeatureScreen()")

    val changes = listOf(
      AiFileChange(
        filePath = "app/src/main/java/com/example/ui/screens/CustomFeatureScreen.kt",
        action = FileChangeAction.CREATE,
        explanation = "Criação do componente Compose para: $query",
        fullContent = dynamicCode,
        originalContent = ""
      ),
      AiFileChange(
        filePath = "app/src/main/java/com/example/MainActivity.kt",
        action = FileChangeAction.MODIFY,
        explanation = "Integração do recurso na MainActivity",
        fullContent = mainCode,
        originalContent = code
      )
    )

    return AiFixSuggestion(
      issueTitle = "Implementação: $query",
      explanation = "Criei o componente Compose completo e atualizei a MainActivity para exibir o recurso.",
      fullFixedContent = mainCode,
      changes = changes
    )
  }
}
