package com.example.data.repository

import kotlinx.coroutines.delay

class GitHubWorkflowMonitor(private val repository: GitHubRepository) {
  suspend fun pollWorkflow(
    token: String,
    owner: String,
    repo: String,
    runId: Long,
    onStatusUpdate: (status: String) -> Unit,
    onJobUpdate: (jobName: String, stepName: String, isFailure: Boolean) -> Unit,
    onComplete: (isSuccess: Boolean, failedSteps: List<String>) -> Unit
  ) {
    var isFinished = false
    var attempts = 0
    val maxAttempts = 10 // 50 segundos no total

    while (!isFinished && attempts < maxAttempts) {
      delay(5000)
      attempts++

      val run = repository.getLatestWorkflowRun(token, owner, repo)
      val currentStatus = run?.status ?: "in_progress"
      onStatusUpdate(currentStatus)

      val jobs = repository.getWorkflowJobs(token, owner, repo, runId)
      val failedSteps = mutableListOf<String>()

      jobs.forEach { job ->
        job.steps.forEach { step ->
          val stepName = step.name ?: "Execução"
          val jobName = job.name ?: "Build"
          if (step.status == "in_progress") {
            onJobUpdate(jobName, stepName, false)
          } else if (step.conclusion == "failure") {
            failedSteps.add("$jobName > $stepName")
            onJobUpdate(jobName, stepName, true)
          }
        }
      }

      val conclusion = run?.conclusion
      if (currentStatus == "completed" || conclusion != null) {
        isFinished = true
        val isSuccess = conclusion == "success"
        onComplete(isSuccess, failedSteps)
      }
    }

    if (!isFinished) {
      onComplete(false, listOf("Timeout aguardando conclusão do GitHub Actions"))
    }
  }
}
