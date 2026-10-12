package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class LogType {
  INFO,
  SUCCESS,
  ERROR,
  WARNING,
  COMMAND
}

data class LogEntry(
  val id: String = UUID.randomUUID().toString(),
  val timestamp: String = currentTimeFormatted(),
  val message: String,
  val type: LogType = LogType.INFO
)

private fun currentTimeFormatted(): String {
  val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
  return formatter.format(Date())
}
