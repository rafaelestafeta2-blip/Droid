package com.example.ui.components.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PreviewElement
import com.example.ui.theme.IdeCyan
import com.example.ui.theme.IdeGreen
import com.example.ui.theme.IdePurple

@Composable
fun PreviewCanvasRenderer(
  elements: List<PreviewElement>,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(Color(0xFF13141C))
  ) {
    // Simulated Mini Phone Status Bar
    MiniPhoneStatusBar()

    // Scrollable Screen Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      elements.forEach { element ->
        when (element) {
          is PreviewElement.ScreenHeader -> {
            Text(
              text = element.title,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              modifier = Modifier.fillMaxWidth(),
              textAlign = TextAlign.Start
            )
          }
          is PreviewElement.IconItem -> {
            val iconVec = if (element.name.contains("Rocket")) Icons.Default.RocketLaunch else Icons.Default.Star
            Icon(
              imageVector = iconVec,
              contentDescription = null,
              tint = IdeCyan,
              modifier = Modifier.size(36.dp)
            )
          }
          is PreviewElement.TextItem -> {
            Text(
              text = element.text,
              color = if (element.isHeading) Color.White else Color(0xFFA6ACCD),
              fontWeight = if (element.isHeading) FontWeight.Bold else FontWeight.Normal,
              fontSize = if (element.isHeading) 14.sp else 11.sp,
              textAlign = TextAlign.Center
            )
          }
          is PreviewElement.ActionButton -> {
            Button(
              onClick = {},
              colors = ButtonDefaults.buttonColors(containerColor = IdePurple),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
            ) {
              Text(element.label, fontSize = 11.sp, color = Color.White)
            }
          }
          is PreviewElement.CardItem -> {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1F29))
            ) {
              Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = IdeGreen,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                  Text(element.title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                  if (element.description.isNotBlank()) {
                    Text(element.description, color = Color(0xFF676E95), fontSize = 9.sp)
                  }
                }
              }
            }
          }
          is PreviewElement.InputField -> {
            OutlinedTextField(
              value = "",
              onValueChange = {},
              readOnly = true,
              label = { Text(element.label, fontSize = 10.sp) },
              modifier = Modifier.fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = IdeCyan,
                unfocusedBorderColor = Color(0xFF44475A)
              )
            )
          }
          is PreviewElement.ToggleItem -> {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(element.label, color = Color.White, fontSize = 11.sp)
              Switch(checked = element.isChecked, onCheckedChange = {})
            }
          }
          is PreviewElement.CalculatorWidget -> {
            CalculatorWidgetRender()
          }
          is PreviewElement.CounterWidget -> {
            CounterWidgetRender(element.initialCount)
          }
          is PreviewElement.TodoWidget -> {
            TodoWidgetRender(element.tasks)
          }
          is PreviewElement.WeatherWidget -> {
            WeatherWidgetRender(element.city, element.temp, element.condition)
          }
          is PreviewElement.ProfileWidget -> {
            ProfileWidgetRender(element.name, element.bio)
          }
          is PreviewElement.EmptyState -> {
            Text(element.message, color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
          }
          else -> {}
        }
      }
    }
  }
}

@Composable
private fun MiniPhoneStatusBar() {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(18.dp)
      .background(Color(0xFF0D0E15))
      .padding(horizontal = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text("9:41", color = Color(0xFF888888), fontSize = 9.sp, fontWeight = FontWeight.Bold)
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(Color(0xFF222222))
    )
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Default.Wifi, contentDescription = null, tint = Color(0xFF888888), modifier = Modifier.size(10.dp))
      Spacer(modifier = Modifier.width(3.dp))
      Box(modifier = Modifier.width(12.dp).height(6.dp).background(IdeGreen, RoundedCornerShape(1.dp)))
    }
  }
}
