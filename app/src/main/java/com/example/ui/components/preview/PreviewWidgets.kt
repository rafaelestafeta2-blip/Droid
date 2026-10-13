package com.example.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun CalculatorWidgetRender() {
  var display by remember { mutableStateOf("0") }
  Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
    Surface(
      shape = RoundedCornerShape(8.dp),
      color = Color(0xFF1E1F29),
      modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
    ) {
      Text(display, color = IdeCyan, fontSize = 22.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End, modifier = Modifier.padding(8.dp).fillMaxWidth())
    }
    val rows = listOf(
      listOf("7", "8", "9", "/"),
      listOf("4", "5", "6", "*"),
      listOf("1", "2", "3", "-"),
      listOf("C", "0", "=", "+")
    )
    rows.forEach { row ->
      Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        row.forEach { btn ->
          val isOp = btn in listOf("/", "*", "-", "+", "=")
          Button(
            onClick = {
              display = when (btn) {
                "C" -> "0"
                "=" -> "42"
                else -> if (display == "0") btn else display + btn
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = if (isOp) IdePurple else Color(0xFF282A36)),
            shape = RoundedCornerShape(6.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.weight(1f).height(32.dp)
          ) {
            Text(btn, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isOp) Color.Black else Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun CounterWidgetRender(initialCount: Int = 0) {
  var count by remember { mutableIntStateOf(initialCount) }
  Column(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
    Text("Contador", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
    Surface(shape = CircleShape, color = Color(0xFF1E1F29), modifier = Modifier.size(72.dp).padding(vertical = 6.dp)) {
      Box(contentAlignment = Alignment.Center) {
        Text("$count", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = IdeGreen)
      }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Button(onClick = { if (count > 0) count-- }, colors = ButtonDefaults.buttonColors(containerColor = IdeRed), shape = RoundedCornerShape(6.dp), modifier = Modifier.height(30.dp)) {
        Text("-1", fontSize = 11.sp, color = Color.White)
      }
      Button(onClick = { count = 0 }, colors = ButtonDefaults.buttonColors(containerColor = IdeBorder), shape = RoundedCornerShape(6.dp), modifier = Modifier.height(30.dp)) {
        Text("0", fontSize = 11.sp, color = Color.White)
      }
      Button(onClick = { count++ }, colors = ButtonDefaults.buttonColors(containerColor = IdeGreen), shape = RoundedCornerShape(6.dp), modifier = Modifier.height(30.dp)) {
        Text("+1", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
      }
    }
  }
}

@Composable
fun TodoWidgetRender(tasksList: List<String>) {
  var tasks by remember { mutableStateOf(tasksList.map { it to false }) }
  Column(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
    Text("Lista de Tarefas", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
    tasks.forEachIndexed { index, (task, isDone) ->
      Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFF1E1F29), modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(modifier = Modifier.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
          Checkbox(checked = isDone, onCheckedChange = { checked ->
            tasks = tasks.toMutableList().also { it[index] = task to checked }
          }, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(task, fontSize = 10.sp, color = if (isDone) Color(0xFF6272A4) else Color.White)
        }
      }
    }
  }
}

@Composable
fun WeatherWidgetRender(city: String, temp: String, condition: String) {
  Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E1F29), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(city, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = IdeCyan)
      Icon(Icons.Default.WbSunny, contentDescription = null, tint = IdeYellow, modifier = Modifier.size(36.dp).padding(vertical = 4.dp))
      Text(temp, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
      Text(condition, fontSize = 11.sp, color = IdeTextMuted)
    }
  }
}

@Composable
fun ProfileWidgetRender(name: String, bio: String) {
  Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFF1E1F29), modifier = Modifier.fillMaxWidth().padding(4.dp)) {
    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(IdePurple), contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Person, contentDescription = null, tint = Color.Black, modifier = Modifier.size(24.dp))
      }
      Text(name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
      Text(bio, fontSize = 10.sp, color = IdeTextMuted, textAlign = TextAlign.Center)
    }
  }
}
