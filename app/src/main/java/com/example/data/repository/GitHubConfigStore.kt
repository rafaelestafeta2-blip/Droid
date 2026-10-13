package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences

data class SavedGitHubConfig(
  val token: String = "",
  val owner: String = "",
  val repo: String = "",
  val workflowId: String = "main.yml",
  val branch: String = "main"
)

class GitHubConfigStore(context: Context) {
  private val prefs: SharedPreferences = context.getSharedPreferences("github_ide_config", Context.MODE_PRIVATE)

  fun saveConfig(config: SavedGitHubConfig) {
    prefs.edit()
      .putString("token", config.token)
      .putString("owner", config.owner)
      .putString("repo", config.repo)
      .putString("workflowId", config.workflowId)
      .putString("branch", config.branch)
      .apply()
  }

  fun loadConfig(): SavedGitHubConfig {
    return SavedGitHubConfig(
      token = prefs.getString("token", "") ?: "",
      owner = prefs.getString("owner", "") ?: "",
      repo = prefs.getString("repo", "") ?: "",
      workflowId = prefs.getString("workflowId", "main.yml") ?: "main.yml",
      branch = prefs.getString("branch", "main") ?: "main"
    )
  }
}
