package com.example.model

enum class FileChangeAction {
  CREATE,
  MODIFY
}

data class AiFileChange(
  val filePath: String,
  val action: FileChangeAction,
  val explanation: String,
  val fullContent: String,
  val originalContent: String = ""
)
