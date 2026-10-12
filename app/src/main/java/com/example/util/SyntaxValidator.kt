package com.example.util

data class SyntaxValidationResult(
  val isValid: Boolean,
  val errorMessage: String? = null,
  val errorType: SyntaxErrorType? = null
)

enum class SyntaxErrorType {
  UNBALANCED_BRACES,
  UNBALANCED_PARENS,
  UNBALANCED_BRACKETS,
  MISSING_PACKAGE,
  INVALID_XML,
  EMPTY_FILE
}

/**
 * Utilitário de validação estrutural e sintática para códigos Kotlin e XML.
 * Impede que arquivos com erros sintáticos evidentes corrompam o disco ou quebrem builds.
 */
object SyntaxValidator {

  /**
   * Valida o arquivo com base na extensão e conteúdo.
   */
  fun validate(filePath: String, content: String): SyntaxValidationResult {
    val trimmed = content.trim()
    if (trimmed.isEmpty()) {
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "O arquivo $filePath está vazio.",
        errorType = SyntaxErrorType.EMPTY_FILE
      )
    }

    return when {
      filePath.endsWith(".kt") -> validateKotlin(filePath, content, isScript = false)
      filePath.endsWith(".gradle.kts") -> validateKotlin(filePath, content, isScript = true)
      filePath.endsWith(".xml") -> validateXml(filePath, content)
      else -> validateGenericBrackets(filePath, content)
    }
  }

  /**
   * Valida arquivos Kotlin (.kt e .gradle.kts).
   */
  private fun validateKotlin(filePath: String, code: String, isScript: Boolean): SyntaxValidationResult {
    // 1. Checagem de balanceamento de chaves, parênteses e colchetes ignorando strings e comentários
    val balanceCheck = validateGenericBrackets(filePath, code)
    if (!balanceCheck.isValid) {
      return balanceCheck
    }

    // 2. Se for arquivo Kotlin padrão (.kt), verifica se contém declaração de package
    if (!isScript) {
      val hasPackageDeclaration = code.lines().any { line ->
        val trimmed = line.trim()
        trimmed.startsWith("package ") && trimmed.length > 8
      }

      if (!hasPackageDeclaration) {
        return SyntaxValidationResult(
          isValid = false,
          errorMessage = "Falta declaração de package obrigatória no início de $filePath",
          errorType = SyntaxErrorType.MISSING_PACKAGE
        )
      }
    }

    return SyntaxValidationResult(isValid = true)
  }

  /**
   * Valida sintaxe de tags e caracteres do XML.
   */
  private fun validateXml(filePath: String, xml: String): SyntaxValidationResult {
    val trimmed = xml.trim()
    if (!trimmed.startsWith("<") || !trimmed.endsWith(">")) {
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "Estrutura XML inválida em $filePath: deve iniciar com '<' e terminar com '>'",
        errorType = SyntaxErrorType.INVALID_XML
      )
    }

    // Checagem básica de balanceamento de tags
    var openAngle = 0
    var inComment = false
    var inString = false
    var i = 0

    while (i < xml.length) {
      val c = xml[i]
      if (!inComment && !inString && c == '<') {
        if (i + 3 < xml.length && xml.substring(i, i + 4) == "<!--") {
          inComment = true
          i += 4
          continue
        }
        openAngle++
      } else if (!inComment && !inString && c == '>') {
        openAngle--
        if (openAngle < 0) {
          return SyntaxValidationResult(
            isValid = false,
            errorMessage = "Tag de fechamento '>' inesperada sem abertura correspondente em $filePath",
            errorType = SyntaxErrorType.INVALID_XML
          )
        }
      } else if (inComment && c == '-' && i + 2 < xml.length && xml.substring(i, i + 3) == "-->") {
        inComment = false
        i += 3
        continue
      } else if (!inComment && c == '"') {
        inString = !inString
      }
      i++
    }

    if (openAngle != 0) {
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "Tags XML não fechadas ou malformadas em $filePath",
        errorType = SyntaxErrorType.INVALID_XML
      )
    }

    return SyntaxValidationResult(isValid = true)
  }

  /**
   * Valida chaves {}, parênteses () e colchetes [], desconsiderando comentários e strings literais.
   */
  private fun validateGenericBrackets(filePath: String, code: String): SyntaxValidationResult {
    var braceCount = 0
    var parenCount = 0
    var bracketCount = 0
    var inString = false
    var inSingleLineComment = false
    var inMultiLineComment = false
    var i = 0

    while (i < code.length) {
      val c = code[i]
      val next = if (i + 1 < code.length) code[i + 1] else ' '

      // Comentários de linha única //
      if (!inString && !inMultiLineComment && !inSingleLineComment && c == '/' && next == '/') {
        inSingleLineComment = true
        i += 2
        continue
      }
      if (inSingleLineComment && c == '\n') {
        inSingleLineComment = false
        i++
        continue
      }
      if (inSingleLineComment) {
        i++
        continue
      }

      // Comentários de bloco /* */
      if (!inString && !inMultiLineComment && c == '/' && next == '*') {
        inMultiLineComment = true
        i += 2
        continue
      }
      if (inMultiLineComment && c == '*' && next == '/') {
        inMultiLineComment = false
        i += 2
        continue
      }
      if (inMultiLineComment) {
        i++
        continue
      }

      // Strings literais
      if (c == '"') {
        // Checa se é escape
        var backslashCount = 0
        var backIdx = i - 1
        while (backIdx >= 0 && code[backIdx] == '\\') {
          backslashCount++
          backIdx--
        }
        if (backslashCount % 2 == 0) {
          inString = !inString
        }
        i++
        continue
      }

      if (!inString) {
        when (c) {
          '{' -> braceCount++
          '}' -> {
            braceCount--
            if (braceCount < 0) {
              return SyntaxValidationResult(
                isValid = false,
                errorMessage = "Chave de fechamento '}' excedente encontrada em $filePath",
                errorType = SyntaxErrorType.UNBALANCED_BRACES
              )
            }
          }
          '(' -> parenCount++
          ')' -> {
            parenCount--
            if (parenCount < 0) {
              return SyntaxValidationResult(
                isValid = false,
                errorMessage = "Parêntese ')' excedente encontrado em $filePath",
                errorType = SyntaxErrorType.UNBALANCED_PARENS
              )
            }
          }
          '[' -> bracketCount++
          ']' -> {
            bracketCount--
            if (bracketCount < 0) {
              return SyntaxValidationResult(
                isValid = false,
                errorMessage = "Colchete ']' excedente encontrado em $filePath",
                errorType = SyntaxErrorType.UNBALANCED_BRACKETS
              )
            }
          }
        }
      }
      i++
    }

    if (braceCount != 0) {
      val diff = if (braceCount > 0) "faltam $braceCount '}'" else "${-braceCount} '{' excedentes"
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "Chaves desbalanceadas em $filePath ($diff)",
        errorType = SyntaxErrorType.UNBALANCED_BRACES
      )
    }

    if (parenCount != 0) {
      val diff = if (parenCount > 0) "faltam $parenCount ')'" else "${-parenCount} '(' excedentes"
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "Parênteses desbalanceados em $filePath ($diff)",
        errorType = SyntaxErrorType.UNBALANCED_PARENS
      )
    }

    if (bracketCount != 0) {
      val diff = if (bracketCount > 0) "faltam $bracketCount ']'" else "${-bracketCount} '[' excedentes"
      return SyntaxValidationResult(
        isValid = false,
        errorMessage = "Colchetes desbalanceados em $filePath ($diff)",
        errorType = SyntaxErrorType.UNBALANCED_BRACKETS
      )
    }

    return SyntaxValidationResult(isValid = true)
  }
}
