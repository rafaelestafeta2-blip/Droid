package com.example.data.agent

/**
 * Templates estendidos de Jetpack Compose: Clima, Perfil de Usuário,
 * E-commerce e Gerador Dinâmico Customizado.
 */
object AgentScreenTemplatesExtended {

  fun getWeatherScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherScreen() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Text("Previsão do Tempo", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text("São Paulo, Brasil", fontSize = 14.sp, color = Color(0xFF8BE9FD), modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

    Surface(
      shape = RoundedCornerShape(20.dp),
      color = Color(0xFF1A1D2D),
      modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color(0xFFF1FA8C), modifier = Modifier.size(72.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("28°C", fontSize = 52.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text("Ensolarado com poucas nuvens", fontSize = 14.sp, color = Color(0xFF6272A4))
      }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
      Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2D)), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Umidade", color = Color(0xFF8BE9FD), fontSize = 12.sp)
          Text("65%", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        }
      }
      Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1D2D)), modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Text("Vento", color = Color(0xFF50FA7B), fontSize = 12.sp)
          Text("18 km/h", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        }
      }
    }
  }
}
""".trimIndent()

  fun getUserProfileScreenCode(): String = """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserProfileScreen() {
  var name by remember { mutableStateOf("Desenvolvedor Android") }
  var bio by remember { mutableStateOf("Criando aplicativos com Kotlin e Jetpack Compose") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Surface(
      shape = CircleShape,
      color = Color(0xFFBD93F9),
      modifier = Modifier.size(96.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(54.dp))
      }
    }

    Spacer(modifier = Modifier.height(16.dp))
    Text(name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Text(bio, fontSize = 13.sp, color = Color(0xFF6272A4), modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))

    OutlinedTextField(
      value = name,
      onValueChange = { name = it },
      label = { Text("Nome Completo") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(12.dp))

    OutlinedTextField(
      value = bio,
      onValueChange = { bio = it },
      label = { Text("Biografia") },
      modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(24.dp))

    Button(
      onClick = {},
      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF50FA7B)),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.fillMaxWidth().height(48.dp)
    ) {
      Text("Salvar Perfil", color = Color.Black, fontWeight = FontWeight.Bold)
    }
  }
}
""".trimIndent()

  fun getDynamicCustomScreenCode(topic: String): String {
    val cleanTopic = topic.replace("\"", "").trim()
    return """
package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CustomFeatureScreen() {
  var isActivated by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0F111A))
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF1A1D2D),
      tonalElevation = 6.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Icon(
          imageVector = Icons.Default.AutoAwesome,
          contentDescription = null,
          tint = Color(0xFF8BE9FD),
          modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
          text = "$cleanTopic",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )

        Text(
          text = "Recurso construído pelo Agente Autônomo com Jetpack Compose Material 3",
          fontSize = 13.sp,
          color = Color(0xFFBD93F9),
          modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Button(
          onClick = { isActivated = !isActivated },
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isActivated) Color(0xFF50FA7B) else Color(0xFFBD93F9)
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (isActivated) "Status: Ativo" else "Interagir com o Recurso",
            color = Color.Black,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}
""".trimIndent()
  }
}
