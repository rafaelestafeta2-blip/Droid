package com.example.data.agent

/**
 * Responsável por conectar e atualizar MainActivity.kt para exibir a tela
 * gerada pelo Agente com suporte a Edge-to-Edge e WindowInsets.safeDrawing.
 */
object AgentMainActivityRewriter {

  fun buildMainActivityCode(composableCall: String): String {
    return """
package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.screens.*

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MaterialTheme {
        Scaffold(
          modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
          contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
          $composableCall
        }
      }
    }
  }
}
""".trimIndent()
  }
}
