package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WorkflowDispatchPayload(
  @param:Json(name = "ref") val ref: String = "main",
  @param:Json(name = "inputs") val inputs: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class WorkflowRunsResponse(
  @param:Json(name = "total_count") val totalCount: Int = 0,
  @param:Json(name = "workflow_runs") val workflowRuns: List<WorkflowRunDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowRunDto(
  @param:Json(name = "id") val id: Long,
  @param:Json(name = "name") val name: String? = null,
  @param:Json(name = "status") val status: String? = null,
  @param:Json(name = "conclusion") val conclusion: String? = null,
  @param:Json(name = "html_url") val htmlUrl: String? = null,
  @param:Json(name = "head_branch") val headBranch: String? = null,
  @param:Json(name = "run_number") val runNumber: Int? = null
)

@JsonClass(generateAdapter = true)
data class WorkflowJobsResponse(
  @param:Json(name = "total_count") val totalCount: Int = 0,
  @param:Json(name = "jobs") val jobs: List<WorkflowJobDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowJobDto(
  @param:Json(name = "id") val id: Long,
  @param:Json(name = "name") val name: String? = null,
  @param:Json(name = "status") val status: String? = null,
  @param:Json(name = "conclusion") val conclusion: String? = null,
  @param:Json(name = "steps") val steps: List<WorkflowStepDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class WorkflowStepDto(
  @param:Json(name = "name") val name: String? = null,
  @param:Json(name = "status") val status: String? = null,
  @param:Json(name = "conclusion") val conclusion: String? = null,
  @param:Json(name = "number") val number: Int? = null
)

@JsonClass(generateAdapter = true)
data class ArtifactsResponse(
  @param:Json(name = "total_count") val totalCount: Int = 0,
  @param:Json(name = "artifacts") val artifacts: List<ArtifactDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArtifactDto(
  @param:Json(name = "id") val id: Long,
  @param:Json(name = "name") val name: String,
  @param:Json(name = "size_in_bytes") val sizeInBytes: Long = 0,
  @param:Json(name = "archive_download_url") val archiveDownloadUrl: String? = null,
  @param:Json(name = "expired") val expired: Boolean = false
)
