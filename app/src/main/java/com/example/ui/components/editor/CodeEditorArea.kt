package com.example.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileNode
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeTextMuted
import com.example.ui.theme.IdeTextPrimary

@Composable
fun CodeEditorArea(
  activeFile: FileNode?,
  content: String,
  onContentChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  if (activeFile == null) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(IdeBackground),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "Nenhum arquivo aberto.\nSelecione um arquivo na barra lateral para editar.",
        color = IdeTextMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp
      )
    }
    return
  }

  val lineCount = content.count { it == '\n' } + 1
  val verticalScrollState = rememberScrollState()
  val horizontalScrollState = rememberScrollState()

  Row(
    modifier = modifier
      .fillMaxSize()
      .background(IdeBackground)
  ) {
    // Left Gutter: synchronized line numbering
    Box(
      modifier = Modifier
        .fillMaxHeight()
        .verticalScroll(verticalScrollState)
    ) {
      EditorGutter(lineCount = lineCount)
    }

    // Code editing area
    Box(
      modifier = Modifier
        .weight(1f)
        .fillMaxHeight()
        .verticalScroll(verticalScrollState)
        .horizontalScroll(horizontalScrollState)
        .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
    ) {
      BasicTextField(
        value = content,
        onValueChange = onContentChange,
        textStyle = TextStyle(
          color = IdeTextPrimary,
          fontSize = 13.sp,
          fontFamily = FontFamily.Monospace,
          lineHeight = 20.sp
        ),
        cursorBrush = SolidColor(IdeCyan),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("code_editor_text_field"),
        decorationBox = { innerTextField ->
          if (content.isEmpty()) {
            Text(
              text = "// Digite seu código aqui...",
              color = IdeTextMuted,
              fontSize = 13.sp,
              fontFamily = FontFamily.Monospace
            )
          }
          innerTextField()
        }
      )
    }
  }
}
