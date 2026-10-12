package com.example.data.importer

import com.example.util.ProjectIndexer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream

data class CloneResult(
  val success: Boolean,
  val repoName: String,
  val extractedFilesCount: Int = 0,
  val targetDir: File,
  val architectureIndex: String = "",
  val errorMessage: String? = null
)

/**
 * Serviço de importação e clonagem de repositórios do GitHub diretamente no dispositivo.
 */
class GitHubImporter(
  private val client: OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()
) {

  suspend fun cloneRepository(
    repoUrlOrSlug: String,
    targetWorkspace: File,
    token: String? = null,
    branch: String? = null,
    onProgress: (String) -> Unit = {}
  ): CloneResult = withContext(Dispatchers.IO) {
    try {
      val (owner, repo) = parseOwnerAndRepo(repoUrlOrSlug)
        ?: return@withContext CloneResult(
          success = false,
          repoName = repoUrlOrSlug,
          targetDir = targetWorkspace,
          errorMessage = "URL do GitHub inválida. Formato esperado: https://github.com/owner/repo"
        )

      onProgress("Conectando à API do GitHub para $owner/$repo...")

      // Limpa workspace de destino ou prepara diretório
      if (targetWorkspace.exists()) {
        targetWorkspace.deleteRecursively()
      }
      targetWorkspace.mkdirs()

      val zipUrl = if (!branch.isNullOrBlank()) {
        "https://api.github.com/repos/$owner/$repo/zipball/$branch"
      } else {
        "https://api.github.com/repos/$owner/$repo/zipball"
      }

      val requestBuilder = Request.Builder()
        .url(zipUrl)
        .header("Accept", "application/vnd.github+json")
        .header("User-Agent", "DroidStudio-Mobile-IDE")

      if (!token.isNullOrBlank()) {
        requestBuilder.header("Authorization", "Bearer ${token.trim()}")
      }

      onProgress("Baixando arquivo ZIP do repositório...")
      val response = client.newCall(requestBuilder.build()).execute()

      if (!response.isSuccessful) {
        val code = response.code
        val msg = if (code == 404) {
          "Repositório não encontrado ou privado. Se for privado, informe um Token de Acesso."
        } else if (code == 401 || code == 403) {
          "Acesso negado (Código $code). Verifique se o Token de Acesso possui permissão de leitura."
        } else {
          "Falha ao descarregar repositório: HTTP $code"
        }
        return@withContext CloneResult(
          success = false,
          repoName = repo,
          targetDir = targetWorkspace,
          errorMessage = msg
        )
      }

      val responseBody = response.body
        ?: return@withContext CloneResult(
          success = false,
          repoName = repo,
          targetDir = targetWorkspace,
          errorMessage = "Resposta vazia recebida do GitHub."
        )

      onProgress("Extraindo arquivos de forma segura...")
      val extractedCount = extractZipSafely(responseBody.byteStream(), targetWorkspace)

      onProgress("Indexando arquitetura do projeto importado...")
      val indexSummary = ProjectIndexer.buildGlobalIndex(targetWorkspace)

      onProgress("Clonagem concluída com sucesso! ($extractedCount arquivos)")
      CloneResult(
        success = true,
        repoName = repo,
        extractedFilesCount = extractedCount,
        targetDir = targetWorkspace,
        architectureIndex = indexSummary
      )
    } catch (e: Exception) {
      e.printStackTrace()
      CloneResult(
        success = false,
        repoName = repoUrlOrSlug,
        targetDir = targetWorkspace,
        errorMessage = "Erro durante a importação: ${e.localizedMessage ?: e.message}"
      )
    }
  }

  private fun parseOwnerAndRepo(input: String): Pair<String, String>? {
    val trimmed = input.trim()
      .removeSuffix(".git")
      .removeSuffix("/")
    val clean = when {
      trimmed.startsWith("https://github.com/") -> trimmed.removePrefix("https://github.com/")
      trimmed.startsWith("http://github.com/") -> trimmed.removePrefix("http://github.com/")
      trimmed.startsWith("github.com/") -> trimmed.removePrefix("github.com/")
      else -> trimmed
    }
    val parts = clean.split("/").filter { it.isNotBlank() }
    if (parts.size >= 2) {
      return Pair(parts[0], parts[1])
    }
    return null
  }

  private fun extractZipSafely(inputStream: java.io.InputStream, targetDir: File): Int {
    var count = 0
    val canonicalTargetPath = targetDir.canonicalPath

    ZipInputStream(inputStream).use { zis ->
      var entry = zis.nextEntry
      while (entry != null) {
        val name = entry.name
        // O GitHub coloca todos os arquivos dentro de uma pasta raiz como "owner-repo-hash/"
        val relativePath = if (name.contains("/")) name.substringAfter("/") else name

        if (relativePath.isNotBlank()) {
          val destFile = File(targetDir, relativePath)
          val canonicalDest = destFile.canonicalPath

          // Proteção contra Zip Slip Vulnerability
          if (!canonicalDest.startsWith(canonicalTargetPath)) {
            throw SecurityException("Tentativa de travessia de diretório detectada no ZIP: $name")
          }

          if (entry.isDirectory) {
            destFile.mkdirs()
          } else {
            destFile.parentFile?.mkdirs()
            FileOutputStream(destFile).use { fos ->
              zis.copyTo(fos)
            }
            count++
          }
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
    return count
  }
}
