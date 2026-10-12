package com.example.model

/**
 * Representação de um projeto no Dashboard / Biblioteca de Projetos.
 */
data class ProjectItem(
  val id: String,
  val name: String,
  val path: String,
  val fileCount: Int = 0,
  val lastModified: Long = System.currentTimeMillis(),
  val description: String = "Projeto Android Nativo",
  val isGitRepo: Boolean = false,
  val branch: String = "main"
)

/**
 * Estado da operação de clonagem do GitHub.
 */
data class CloneRepoState(
  val isCloning: Boolean = false,
  val progressMessage: String = "",
  val error: String? = null,
  val success: Boolean = false
)

/**
 * Navegação de telas da IDE.
 */
enum class IdeScreen {
  DASHBOARD,
  EDITOR
}
