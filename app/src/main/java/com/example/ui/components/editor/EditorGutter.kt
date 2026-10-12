package com.example.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeGutterBg
import com.example.ui.theme.IdeLineNumber

@Composable
fun EditorGutter(
  lineCount: Int,
  modifier: Modifier = Modifier
) {
  val lines = if (lineCount <= 0) 1 else lineCount
  val textBuilder = StringBuilder()
  for (i in 1..lines) {
    textBuilder.append(i).append("\n")
  }

  Column(
    modifier = modifier
      .width(42.dp)
      .background(IdeGutterBg)
      .padding(horizontal = 6.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.End
  ) {
    Text(
      text = textBuilder.toString().trimEnd(),
      color = IdeLineNumber,
      fontSize = 13.sp,
      fontFamily = FontFamily.Monospace,
      lineHeight = 20.sp,
      textAlign = TextAlign.End
    )
  }
}
