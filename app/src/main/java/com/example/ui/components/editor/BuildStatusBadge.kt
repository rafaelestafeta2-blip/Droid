package com.example.ui.components.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildStatus
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeYellow

@Composable
fun BuildStatusBadge(
  status: BuildStatus,
  modifier: Modifier = Modifier
) {
  val (color, text) = when (status) {
    BuildStatus.IDLE -> IdeCyan to "PRONTO"
    BuildStatus.BUILDING -> IdeYellow to "BUILDING"
    BuildStatus.SUCCESS -> IdeGreen to "SUCESSO"
    BuildStatus.FAILED -> IdeRed to "FALHA"
  }
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.padding(end = 8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(7.dp)
        .clip(CircleShape)
        .background(color)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      color = color,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = FontWeight.Bold
    )
  }
}
