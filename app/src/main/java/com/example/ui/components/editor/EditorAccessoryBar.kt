package com.example.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.IdeBorder
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeSurface
import com.example.ui.theme.IdeTextPrimary

@Composable
fun EditorAccessoryBar(
  onInsertToken: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val tokens = listOf(
    "Tab" to "  ",
    "{" to " { ",
    "}" to " }",
    "(" to "(",
    ")" to ")",
    "=" to " = ",
    ";" to ";",
    "\"" to "\"",
    "fun" to "fun ",
    "val" to "val ",
    "var" to "var ",
    "import" to "import ",
    "// " to "// "
  )

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(38.dp)
      .background(IdeSurface)
      .horizontalScroll(rememberScrollState())
      .padding(horizontal = 6.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    tokens.forEach { (label, value) ->
      Box(
        modifier = Modifier
          .padding(horizontal = 3.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(IdeBorder)
          .clickable { onInsertToken(value) }
          .padding(horizontal = 10.dp, vertical = 4.dp)
          .testTag("token_$label"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = label,
          color = if (label == "Tab" || label.length > 2) IdeCyan else IdeTextPrimary,
          fontSize = 12.sp,
          fontFamily = FontFamily.Monospace
        )
      }
    }
  }
}
