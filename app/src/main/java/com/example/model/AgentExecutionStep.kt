package com.example.model

enum class AgentStepStatus {
  PENDING,
  RUNNING,
  SUCCESS,
  FAILED
}

data class AgentExecutionStep(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val description: String = "",
  val status: AgentStepStatus = AgentStepStatus.PENDING
)

data class AgentExecutionResult(
  val success: Boolean,
  val message: String,
  val affectedFiles: List<AiFileChange> = emptyList(),
  val steps: List<AgentExecutionStep> = emptyList(),
  val isConversationalOnly: Boolean = false
)
