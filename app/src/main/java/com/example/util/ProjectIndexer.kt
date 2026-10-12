package com.example.util

import java.io.File

object ProjectIndexer {

  fun buildGlobalIndex(workspaceDir: File): String {
    if (!workspaceDir.exists()) return "Nenhum arquivo no projeto."
    val builder = StringBuilder()
    builder.append("INDEX DE ARQUITETURA DO PROJETO:\n")

    workspaceDir.walkTopDown()
      .filter { it.isFile }
      .forEach { file ->
        val relPath = file.relativeTo(workspaceDir).path
        when {
          file.name.endsWith(".kt") -> {
            val signatures = extractKotlinSignatures(file)
            builder.append("- [$relPath]: $signatures\n")
          }
          file.name == "AndroidManifest.xml" -> {
            val perms = extractXmlPermissions(file)
            builder.append("- [$relPath]: Permissões declaradas: $perms\n")
          }
          file.name == "build.gradle.kts" -> {
            val deps = extractGradleDeps(file)
            builder.append("- [$relPath]: Dependências: $deps\n")
          }
        }
      }
    return builder.toString()
  }

  private fun extractKotlinSignatures(file: File): String {
    return try {
      val lines = file.readLines()
      val pkg = lines.firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim() ?: "default"
      val items = lines.filter { line ->
        val trimmed = line.trim()
        trimmed.startsWith("class ") || trimmed.startsWith("data class ") ||
        trimmed.startsWith("interface ") || trimmed.startsWith("object ") ||
        (trimmed.startsWith("fun ") && !trimmed.contains("private "))
      }.map { it.trim().substringBefore("{").trim() }
      "pacote: $pkg | declarações: [${items.take(4).joinToString(", ")}]"
    } catch (e: Exception) {
      "leitura indisponível"
    }
  }

  private fun extractXmlPermissions(file: File): String {
    return try {
      val content = file.readText()
      Regex("""uses-permission\s+android:name="([^"]+)"""")
        .findAll(content)
        .map { it.groupValues[1].substringAfterLast(".") }
        .joinToString(", ")
        .ifEmpty { "nenhuma" }
    } catch (e: Exception) { "erro" }
  }

  private fun extractGradleDeps(file: File): String {
    return try {
      val content = file.readText()
      Regex("""implementation\(([^)]+)\)""")
        .findAll(content)
        .map { it.groupValues[1].substringAfterLast(".") }
        .take(5)
        .joinToString(", ")
        .ifEmpty { "padrão" }
    } catch (e: Exception) { "erro" }
  }
}
