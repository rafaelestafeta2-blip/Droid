package com.example.model

enum class ChatSender {
  USER,
  AGENT
}

data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val sender: ChatSender,
  val text: String,
  val timestamp: Long = System.currentTimeMillis(),
  val isThinking: Boolean = false,
  val suggestedChanges: List<AiFileChange> = emptyList(),
  val isApplied: Boolean = true,
  val steps: List<AgentExecutionStep> = emptyList()
)
