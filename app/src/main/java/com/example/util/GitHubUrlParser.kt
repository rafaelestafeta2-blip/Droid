package com.example.util

data class ParsedGitHubRepo(
  val owner: String,
  val repo: String,
  val isValid: Boolean = true
)

object GitHubUrlParser {

  fun parse(input: String): ParsedGitHubRepo {
    val clean = input.trim()
      .removePrefix("https://")
      .removePrefix("http://")
      .removePrefix("git@")
      .removePrefix("github.com:")
      .removePrefix("github.com/")
      .removeSuffix(".git")
      .removeSuffix("/")

    val parts = clean.split("/")
    if (parts.size >= 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) {
      return ParsedGitHubRepo(
        owner = parts[0].trim(),
        repo = parts[1].trim(),
        isValid = true
      )
    }

    return ParsedGitHubRepo(
      owner = "",
      repo = "",
      isValid = false
    )
  }
}
