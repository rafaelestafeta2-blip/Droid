package com.example.data

import android.content.Context
import com.example.model.FileNode
import com.example.model.FileType
import com.example.model.ProjectTemplateDefaults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProjectFileManager(private val context: Context) {
  val workspaceDir: File
    get() = File(context.filesDir, "project_workspace")

  fun ensureWorkspaceInitialized(forceReset: Boolean = false) {
    if (forceReset && workspaceDir.exists()) {
      workspaceDir.deleteRecursively()
    }
    if (!workspaceDir.exists() || (workspaceDir.listFiles()?.isEmpty() == true)) {
      workspaceDir.mkdirs()
      seedInitialProject(workspaceDir)
    } else {
      ensureEdgeToEdgeInWorkspace()
    }
  }

  private fun ensureEdgeToEdgeInWorkspace() {
    val mainActivity = File(workspaceDir, "app/src/main/java/com/example/MainActivity.kt")
    if (mainActivity.exists()) {
      val text = mainActivity.readText()
      if (!text.contains("enableEdgeToEdge")) {
        mainActivity.writeText(ProjectTemplateDefaults.DEFAULT_MAIN_ACTIVITY_KT)
      }
    }
  }

  suspend fun resetToRobustTemplate(): FileNode = withContext(Dispatchers.IO) {
    ensureWorkspaceInitialized(forceReset = true)
    buildNodeFromFile(workspaceDir, isRoot = true)
  }

  suspend fun loadProjectTree(): FileNode = withContext(Dispatchers.IO) {
    ensureWorkspaceInitialized()
    buildNodeFromFile(workspaceDir, isRoot = true)
  }

  suspend fun getWorkspaceProjectItem(customName: String? = null, isGit: Boolean = false, branch: String = "main"): com.example.model.ProjectItem = withContext(Dispatchers.IO) {
    val count = if (workspaceDir.exists()) workspaceDir.walkTopDown().filter { it.isFile }.count() else 0
    com.example.model.ProjectItem(
      id = "current_workspace",
      name = customName ?: if (isGit) "Repositorio-GitHub" else "DroidStudio-Workspace",
      path = workspaceDir.absolutePath,
      fileCount = count,
      lastModified = if (workspaceDir.exists()) workspaceDir.lastModified() else System.currentTimeMillis(),
      description = if (isGit) "Repositório clonado do GitHub ($branch)" else "Projeto Android Nativo com Compose M3",
      isGitRepo = isGit,
      branch = branch
    )
  }

  fun resolvePath(rawPath: String): String {
    val clean = rawPath.removePrefix("/").removePrefix("./")
    return if (rawPath.startsWith(workspaceDir.absolutePath)) rawPath else File(workspaceDir, clean).absolutePath
  }

  suspend fun createOrUpdateFile(rawPath: String, content: String): Boolean = withContext(Dispatchers.IO) {
    val targetPath = resolvePath(rawPath)
    saveFile(targetPath, content)
  }

  suspend fun saveFile(path: String, content: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val file = File(path)
      file.parentFile?.mkdirs()
      file.writeText(content)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  suspend fun readFile(path: String): String = withContext(Dispatchers.IO) {
    try {
      val file = File(path)
      if (file.exists() && file.isFile) file.readText() else ""
    } catch (e: Exception) {
      e.printStackTrace()
      ""
    }
  }

  private fun buildNodeFromFile(file: File, isRoot: Boolean = false): FileNode {
    val isDir = file.isDirectory
    val type = if (isDir) FileType.DIRECTORY else resolveFileType(file.name)
    val children = if (isDir) {
      file.listFiles()
        ?.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
        ?.map { buildNodeFromFile(it) } ?: emptyList()
    } else emptyList()

    val content = if (!isDir && file.exists()) {
      try { file.readText() } catch (e: Exception) { "" }
    } else ""

    return FileNode(
      id = file.absolutePath,
      name = if (isRoot) "DroidStudio-Project" else file.name,
      path = file.absolutePath,
      type = type,
      content = content,
      isExpanded = isRoot || isDir,
      children = children
    )
  }

  private fun resolveFileType(fileName: String): FileType {
    val lower = fileName.lowercase()
    return when {
      lower.endsWith(".kt") -> FileType.KOTLIN
      lower.endsWith(".xml") -> FileType.XML
      lower.endsWith(".gradle.kts") || lower.endsWith(".gradle") -> FileType.GRADLE
      lower.endsWith(".json") -> FileType.JSON
      lower.endsWith(".properties") -> FileType.PROPERTIES
      else -> FileType.OTHER
    }
  }

  private fun seedInitialProject(root: File) {
    val appDir = File(root, "app").apply { mkdirs() }
    val mainJavaDir = File(appDir, "src/main/java/com/example").apply { mkdirs() }
    val resValuesDir = File(appDir, "src/main/res/values").apply { mkdirs() }

    File(mainJavaDir, "MainActivity.kt").writeText(ProjectTemplateDefaults.DEFAULT_MAIN_ACTIVITY_KT)
    File(resValuesDir, "strings.xml").writeText(ProjectTemplateDefaults.DEFAULT_STRINGS_XML)
    File(appDir, "src/main/AndroidManifest.xml").writeText(ProjectTemplateDefaults.DEFAULT_MANIFEST_XML)
    File(appDir, "build.gradle.kts").writeText(ProjectTemplateDefaults.DEFAULT_BUILD_GRADLE_KTS)
    File(root, "settings.gradle.kts").writeText(ProjectTemplateDefaults.DEFAULT_SETTINGS_GRADLE_KTS)
  }
}
