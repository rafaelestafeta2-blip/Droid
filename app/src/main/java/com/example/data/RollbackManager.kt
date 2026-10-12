package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class FileSnapshot(
  val path: String,
  val existed: Boolean,
  val previousContent: String
)

data class BatchSnapshot(
  val id: String,
  val timestamp: Long = System.currentTimeMillis(),
  val description: String,
  val files: List<FileSnapshot>
)

class RollbackManager {
  private var lastSnapshot: BatchSnapshot? = null

  fun hasSnapshot(): Boolean = lastSnapshot != null
  fun getLastSnapshot(): BatchSnapshot? = lastSnapshot

  suspend fun createSnapshot(
    description: String,
    rawPaths: List<String>,
    fileManager: ProjectFileManager
  ): BatchSnapshot = withContext(Dispatchers.IO) {
    val snapshots = rawPaths.map { rawPath ->
      val resolved = fileManager.resolvePath(rawPath)
      val file = File(resolved)
      val existed = file.exists() && file.isFile
      val content = if (existed) file.readText() else ""
      FileSnapshot(path = resolved, existed = existed, previousContent = content)
    }
    val batch = BatchSnapshot(
      id = "snap_${System.currentTimeMillis()}",
      description = description,
      files = snapshots
    )
    lastSnapshot = batch
    batch
  }

  suspend fun performRollback(
    fileManager: ProjectFileManager
  ): List<String> = withContext(Dispatchers.IO) {
    val snapshot = lastSnapshot ?: return@withContext emptyList()
    val restored = mutableListOf<String>()

    snapshot.files.forEach { fileSnap ->
      val file = File(fileSnap.path)
      if (fileSnap.existed) {
        fileManager.saveFile(fileSnap.path, fileSnap.previousContent)
        restored.add("Restaurado: ${file.name}")
      } else {
        if (file.exists()) {
          file.delete()
          restored.add("Removido (novo arquivo revertido): ${file.name}")
        }
      }
    }
    lastSnapshot = null
    restored
  }
}
