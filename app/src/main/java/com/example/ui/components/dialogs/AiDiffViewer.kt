package com.example.ui.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiFileChange
import com.example.model.FileChangeAction
import com.example.ui.theme.IdeBackground
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdeRed
import com.example.ui.theme.IdeTextPrimary
import com.example.ui.theme.IdeTextSecondary

@Composable
fun AiDiffViewer(
  change: AiFileChange,
  modifier: Modifier = Modifier
) {
  val isCreate = change.action == FileChangeAction.CREATE
  val badgeColor = if (isCreate) IdeGreen else IdeCyan
  val badgeText = if (isCreate) "CREATE" else "MODIFY"

  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 6.dp)
    ) {
      Text(
        text = badgeText,
        color = IdeBackground,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
          .clip(RoundedCornerShape(4.dp))
          .background(badgeColor)
          .padding(horizontal = 6.dp, vertical = 2.dp)
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = change.filePath,
        style = MaterialTheme.typography.bodySmall.copy(
          color = IdeTextPrimary,
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp
        )
      )
    }

    if (change.explanation.isNotBlank()) {
      Text(
        text = change.explanation,
        style = MaterialTheme.typography.bodySmall.copy(
          color = IdeTextSecondary,
          fontSize = 11.sp
        ),
        modifier = Modifier.padding(bottom = 6.dp)
      )
    }

    // Code Content Box
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(8.dp))
        .background(IdeBackground)
        .padding(10.dp)
    ) {
      Column {
        if (isCreate) {
          Text(
            text = "+ [Novo Arquivo]",
            color = IdeGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = change.fullContent.take(400) + if (change.fullContent.length > 400) "\n..." else "",
            color = IdeGreen,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          )
        } else {
          Text(
            text = "~ [Arquivo Modificado]",
            color = IdeCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = change.fullContent.take(400) + if (change.fullContent.length > 400) "\n..." else "",
            color = IdeTextPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          )
        }
      }
    }
  }
}
