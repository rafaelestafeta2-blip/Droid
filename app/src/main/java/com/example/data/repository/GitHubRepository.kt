package com.example.data.repository

import com.example.data.api.ArtifactDto
import com.example.data.api.GitHubApiClient
import com.example.data.api.WorkflowDispatchPayload
import com.example.data.api.WorkflowJobDto
import com.example.data.api.WorkflowRunDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import java.io.IOException

sealed class GitHubDispatchResult {
  data class Success(val message: String) : GitHubDispatchResult()
  data class Failure(val code: Int?, val errorMessage: String) : GitHubDispatchResult()
}

class GitHubRepository {
  private val apiService = GitHubApiClient.service

  suspend fun triggerWorkflow(
    token: String, owner: String, repo: String, workflowId: String, ref: String
  ): GitHubDispatchResult = withContext(Dispatchers.IO) {
    if (token.isBlank() || owner.isBlank() || repo.isBlank()) {
      return@withContext GitHubDispatchResult.Failure(400, "Token, Dono e Repositório são obrigatórios.")
    }
    val authHeader = formatAuthHeader(token)
    try {
      val response = apiService.triggerWorkflowDispatch(authHeader, owner.trim(), repo.trim(), workflowId.trim(), WorkflowDispatchPayload(ref.trim()))
      if (response.code() == 204 || response.isSuccessful) {
        GitHubDispatchResult.Success("Build disparado no GitHub Actions com sucesso para '$ref'!")
      } else {
        GitHubDispatchResult.Failure(response.code(), "Erro HTTP ${response.code()}: ${response.message()}")
      }
    } catch (e: Exception) {
      GitHubDispatchResult.Failure(null, "Falha de conexão: ${e.localizedMessage ?: "Erro interno."}")
    }
  }

  suspend fun getLatestWorkflowRun(token: String, owner: String, repo: String): WorkflowRunDto? = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getWorkflowRuns(formatAuthHeader(token), owner.trim(), repo.trim(), perPage = 1)
      if (response.isSuccessful) response.body()?.workflowRuns?.firstOrNull() else null
    } catch (e: Exception) { null }
  }

  suspend fun getWorkflowJobs(token: String, owner: String, repo: String, runId: Long): List<WorkflowJobDto> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getWorkflowJobs(formatAuthHeader(token), owner.trim(), repo.trim(), runId)
      if (response.isSuccessful) response.body()?.jobs ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
  }

  suspend fun getRunArtifacts(token: String, owner: String, repo: String, runId: Long): List<ArtifactDto> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getRunArtifacts(formatAuthHeader(token), owner.trim(), repo.trim(), runId)
      if (response.isSuccessful) response.body()?.artifacts ?: emptyList() else emptyList()
    } catch (e: Exception) { emptyList() }
  }

  suspend fun downloadArtifact(token: String, owner: String, repo: String, artifactId: Long): ResponseBody? = withContext(Dispatchers.IO) {
    try {
      val response = apiService.downloadArtifactZip(formatAuthHeader(token), owner.trim(), repo.trim(), artifactId)
      if (response.isSuccessful) response.body() else null
    } catch (e: Exception) { null }
  }

  private fun formatAuthHeader(token: String): String =
    if (token.startsWith("Bearer ") || token.startsWith("token ")) token else "Bearer $token"
}
