package com.example.data.agent

/**
 * Templates robustos de Jetpack Compose para geração autônoma pelo Agente.
 * Contém telas de Login, Tarefas, Contador e Calculadora.
 */
object AgentScreenTemplates {

  fun getLoginScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(onLoginSuccess: (String) -> Unit = {}) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(24.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF1A1D2D),
      tonalElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text("Bem-vindo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Faça login para continuar", fontSize = 13.sp, color = Color(0xFF8BE9FD), modifier = Modifier.padding(top = 4.dp, bottom = 20.dp))

        OutlinedTextField(
          value = email,
          onValueChange = { email = it },
          label = { Text("E-mail") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF8BE9FD)) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = password,
          onValueChange = { password = it },
          label = { Text("Senha") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFBD93F9)) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        statusMessage?.let {
          Text(it, color = Color(0xFFFF5555), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
          onClick = {
            if (email.isNotBlank() && password.isNotBlank()) {
              statusMessage = "Login realizado com sucesso!"
              onLoginSuccess(email)
            } else {
              statusMessage = "Preencha todos os campos."
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBD93F9)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Text("Entrar", fontWeight = FontWeight.Bold, color = Color.Black)
        }
      }
    }
  }
}
""".trimIndent()

  fun getTodoListScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TaskItem(val id: Int, val title: String, val isDone: Boolean = false)

@Composable
fun TodoListScreen() {
  var tasks by remember {
    mutableStateOf(
      listOf(
        TaskItem(1, "Criar aplicativo Android com Agente IA", true),
        TaskItem(2, "Testar Live Preview em tempo real", false),
        TaskItem(3, "Configurar componentes Material 3", false)
      )
    )
  }
  var newTaskText by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(16.dp)
  ) {
    Text("Minhas Tarefas", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text("${'$'}{tasks.count { it.isDone }} de ${'$'}{tasks.size} concluídas", fontSize = 13.sp, color = Color(0xFF50FA7B), modifier = Modifier.padding(bottom = 16.dp))

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = newTaskText,
        onValueChange = { newTaskText = it },
        placeholder = { Text("Nova tarefa...") },
        modifier = Modifier.weight(1f),
        singleLine = true
      )
      Spacer(modifier = Modifier.width(8.dp))
      IconButton(
        onClick = {
          if (newTaskText.isNotBlank()) {
            val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
            tasks = tasks + TaskItem(nextId, newTaskText)
            newTaskText = ""
          }
        },
        modifier = Modifier.background(Color(0xFFBD93F9), RoundedCornerShape(12.dp))
      ) {
        Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color.Black)
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    LazyColumn(modifier = Modifier.fillMaxSize()) {
      items(tasks, key = { it.id }) { task ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2D)),
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = task.isDone,
              onCheckedChange = { checked ->
                tasks = tasks.map { if (it.id == task.id) it.copy(isDone = checked) else it }
              }
            )
            Text(
              text = task.title,
              color = if (task.isDone) Color(0xFF6272A4) else Color.White,
              modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
            )
            IconButton(onClick = { tasks = tasks.filter { it.id != task.id } }) {
              Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5555))
            }
          }
        }
      }
    }
  }
}
""".trimIndent()

  fun getCounterScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CounterScreen() {
  var count by remember { mutableIntStateOf(0) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text("Contador Interativo", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text("Toque nos botões para alterar o valor", fontSize = 13.sp, color = Color(0xFF8BE9FD), modifier = Modifier.padding(top = 4.dp, bottom = 32.dp))

    Surface(
      shape = CircleShape,
      color = Color(0xFF1A1D2D),
      tonalElevation = 6.dp,
      modifier = Modifier.size(160.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(text = "${'$'}count", fontSize = 54.sp, fontWeight = FontWeight.Bold, color = Color(0xFF50FA7B))
      }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
      Button(
        onClick = { if (count > 0) count-- },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5555)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.size(70.dp, 50.dp)
      ) {
        Text("-1", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }

      Button(
        onClick = { count = 0 },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6272A4)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.size(80.dp, 50.dp)
      ) {
        Text("Zerar", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
      }

      Button(
        onClick = { count++ },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.size(70.dp, 50.dp)
      ) {
        Text("+1", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
      }
    }
  }
}
""".trimIndent()

  fun getCalculatorScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalculatorScreen() {
  var display by remember { mutableStateOf("0") }
  var operand1 by remember { mutableDoubleOf(0.0) }
  var operation by remember { mutableStateOf<String?>(null) }
  var isNewOp by remember { mutableStateOf(true) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(16.dp)
  ) {
    Spacer(modifier = Modifier.weight(1f))
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color(0xFF1A1D2D),
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Text(
        text = display,
        fontSize = 42.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF8BE9FD),
        textAlign = TextAlign.End,
        modifier = Modifier.padding(20.dp).fillMaxWidth()
      )
    }

    val buttons = listOf(
      listOf("C", "+/-", "%", "/"),
      listOf("7", "8", "9", "*"),
      listOf("4", "5", "6", "-"),
      listOf("1", "2", "3", "+"),
      listOf("0", ".", "=", "")
    )

    buttons.forEach { row ->
      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        row.filter { it.isNotEmpty() }.forEach { label ->
          val isOperator = label in listOf("/", "*", "-", "+", "=")
          Button(
            onClick = {
              when (label) {
                "C" -> { display = "0"; operand1 = 0.0; operation = null; isNewOp = true }
                "+", "-", "*", "/" -> {
                  operand1 = display.toDoubleOrNull() ?: 0.0
                  operation = label
                  isNewOp = true
                }
                "=" -> {
                  val op2 = display.toDoubleOrNull() ?: 0.0
                  val res = when (operation) {
                    "+" -> operand1 + op2
                    "-" -> operand1 - op2
                    "*" -> operand1 * op2
                    "/" -> if (op2 != 0.0) operand1 / op2 else 0.0
                    else -> op2
                  }
                  display = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                  operation = null
                  isNewOp = true
                }
                else -> {
                  if (isNewOp) { display = label; isNewOp = false }
                  else { display += label }
                }
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isOperator) Color(0xFFBD93F9) else Color(0xFF282A36)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(54.dp)
          ) {
            Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (isOperator) Color.Black else Color.White)
          }
        }
      }
    }
  }
}
""".trimIndent()
}
