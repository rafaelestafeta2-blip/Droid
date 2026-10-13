package com.example.ui.components.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AgentExecutionStep
import com.example.model.AgentStepStatus

@Composable
fun AgentExecutionStepsView(
  steps: List<AgentExecutionStep>,
  modifier: Modifier = Modifier
) {
  if (steps.isEmpty()) return

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = Color(0xFF131520),
    modifier = modifier.fillMaxWidth().padding(vertical = 6.dp)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = "Ações do Agente em Tempo Real:",
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        color = Color(0xFF8BE9FD)
      )

      Spacer(modifier = Modifier.height(6.dp))

      steps.forEach { step ->
        Row(
          modifier = Modifier.padding(vertical = 3.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          when (step.status) {
            AgentStepStatus.RUNNING -> {
              CircularProgressIndicator(
                modifier = Modifier.size(12.dp),
                strokeWidth = 2.dp,
                color = Color(0xFFBD93F9)
              )
            }
            AgentStepStatus.SUCCESS -> {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF50FA7B)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Check,
                  contentDescription = null,
                  tint = Color.Black,
                  modifier = Modifier.size(10.dp)
                )
              }
            }
            AgentStepStatus.FAILED -> {
              Box(
                modifier = Modifier
                  .size(14.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFFF5555)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Close,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(10.dp)
                )
              }
            }
            AgentStepStatus.PENDING -> {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(Color(0xFF6272A4))
              )
            }
          }

          Spacer(modifier = Modifier.width(8.dp))

          val textColor by animateColorAsState(
            targetValue = when (step.status) {
              AgentStepStatus.RUNNING -> Color(0xFFF1FA8C)
              AgentStepStatus.SUCCESS -> Color(0xFFF8F8F2)
              AgentStepStatus.FAILED -> Color(0xFFFF5555)
              AgentStepStatus.PENDING -> Color(0xFF6272A4)
            },
            label = "stepColor"
          )

          Text(
            text = step.title,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = textColor
          )
        }
      }
    }
  }
}
