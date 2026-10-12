package com.example.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LivePreviewState
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextPrimary
import kotlin.math.roundToInt

@Composable
fun FloatingPreviewPanel(
  state: LivePreviewState,
  onDrag: (Float, Float) -> Unit,
  onResize: (Float, Float) -> Unit,
  onToggleMinimize: () -> Unit,
  onToggleOpacity: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  if (!state.isVisible) return

  Box(
    modifier = modifier
      .offset { IntOffset(state.offsetX.roundToInt(), state.offsetY.roundToInt()) }
      .width(state.widthDp.dp)
      .height(if (state.isMinimized) 38.dp else state.heightDp.dp)
      .alpha(state.opacity)
      .shadow(12.dp, RoundedCornerShape(12.dp))
      .clip(RoundedCornerShape(12.dp))
      .border(1.5.dp, IdeBorder, RoundedCornerShape(12.dp))
      .background(IdeSurface)
      .testTag("floating_preview_panel")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header arrastável
      PreviewHeader(
        title = state.previewTitle,
        isMinimized = state.isMinimized,
        opacity = state.opacity,
        onDrag = onDrag,
        onToggleMinimize = onToggleMinimize,
        onToggleOpacity = onToggleOpacity,
        onClose = onClose
      )

      // Corpo da tela (quando não minimizado)
      if (!state.isMinimized) {
        Box(modifier = Modifier.fillMaxSize().weight(1f)) {
          PreviewCanvasRenderer(
            elements = state.elements,
            modifier = Modifier.fillMaxSize()
          )

          // Alça de Redimensionamento no canto inferior direito
          ResizeHandle(
            onResize = onResize,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .testTag("preview_resize_handle")
          )
        }
      }
    }
  }
}

@Composable
private fun PreviewHeader(
  title: String,
  isMinimized: Boolean,
  opacity: Float,
  onDrag: (Float, Float) -> Unit,
  onToggleMinimize: () -> Unit,
  onToggleOpacity: () -> Unit,
  onClose: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(38.dp)
      .background(Color(0xFF1E1F29))
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          onDrag(dragAmount.x, dragAmount.y)
        }
      }
      .padding(horizontal = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Smartphone, contentDescription = null, tint = IdeCyan, modifier = Modifier.size(16.dp))
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = title,
        color = IdeTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      // Botão Opacidade / Transparência
      IconButton(
        onClick = onToggleOpacity,
        modifier = Modifier.size(26.dp).testTag("preview_opacity_button")
      ) {
        Icon(
          imageVector = Icons.Default.Opacity,
          contentDescription = "Alternar Transparência",
          tint = if (opacity < 0.9f) IdeCyan else Color.Gray,
          modifier = Modifier.size(15.dp)
        )
      }

      // Botão Minimizar
      IconButton(
        onClick = onToggleMinimize,
        modifier = Modifier.size(26.dp).testTag("preview_minimize_button")
      ) {
        Icon(
          imageVector = if (isMinimized) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
          contentDescription = "Minimizar Preview",
          tint = IdeTextPrimary,
          modifier = Modifier.size(16.dp)
        )
      }

      // Botão Fechar
      IconButton(
        onClick = onClose,
        modifier = Modifier.size(26.dp).testTag("preview_close_button")
      ) {
        Icon(
          imageVector = Icons.Default.Close,
          contentDescription = "Fechar Preview",
          tint = Color(0xFFFF5555),
          modifier = Modifier.size(15.dp)
        )
      }
    }
  }
}

@Composable
private fun ResizeHandle(
  onResize: (Float, Float) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .size(24.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, dragAmount ->
          change.consume()
          onResize(dragAmount.x, dragAmount.y)
        }
      }
      .padding(4.dp),
    contentAlignment = Alignment.BottomEnd
  ) {
    Icon(
      imageVector = Icons.Default.FitScreen,
      contentDescription = "Redimensionar Janela",
      tint = IdeCyan.copy(alpha = 0.8f),
      modifier = Modifier.size(14.dp)
    )
  }
}
