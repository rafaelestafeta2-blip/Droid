package com.example.util

import com.example.model.PreviewElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interpretador leve e assíncrono para extrair a hierarquia visual de telas
 * Jetpack Compose e XML em tempo real com baixíssimo consumo de memória (zero lag).
 */
object ComposeCodeInterpreter {

  suspend fun parseCode(fileName: String, code: String): List<PreviewElement> = withContext(Dispatchers.Default) {
    if (code.isBlank()) {
      return@withContext listOf(PreviewElement.EmptyState("Arquivo em branco. Comece a digitar seu código."))
    }

    if (fileName.endsWith(".xml")) {
      return@withContext parseXmlLayout(code)
    }

    parseComposeCode(code)
  }

  private fun parseComposeCode(code: String): List<PreviewElement> {
    val elements = mutableListOf<PreviewElement>()

    // Extrair Título ou TopAppBar
    val topAppBarMatch = Regex("""TopAppBar\s*\([^)]*title\s*=\s*\{[^}]*Text\s*\(\s*(?:text\s*=\s*)?["']([^"']+)["']""").find(code)
    if (topAppBarMatch != null) {
      elements.add(PreviewElement.ScreenHeader(topAppBarMatch.groupValues[1]))
    }

    // Extrair componentes pelo padrão no código
    val lines = code.lines()
    var i = 0
    while (i < lines.size) {
      val line = lines[i].trim()

      // Card customizado do template: ConfigItem("Título", "Descrição")
      val configItemMatch = Regex("""ConfigItem\s*\(\s*["']([^"']+)["']\s*,\s*["']([^"']+)["']\s*\)""").find(line)
      if (configItemMatch != null) {
        elements.add(PreviewElement.CardItem(configItemMatch.groupValues[1], configItemMatch.groupValues[2], "CheckCircle"))
        i++
        continue
      }

      // Card geral
      if (line.startsWith("Card(") || line.startsWith("Card (") || line.startsWith("Card{") || line.startsWith("Card {")) {
        val cardText = extractBlockText(lines, i)
        val textMatches = Regex("""Text\s*\(\s*(?:text\s*=\s*)?["']([^"']+)["']""").findAll(cardText).toList()
        if (textMatches.isNotEmpty()) {
          val title = textMatches.first().groupValues[1]
          val desc = if (textMatches.size > 1) textMatches[1].groupValues[1] else ""
          elements.add(PreviewElement.CardItem(title, desc))
        }
      }

      // Buttons
      val buttonMatch = Regex("""(?:Button|FilledTonalButton|OutlinedButton)\s*\([^)]*\)\s*\{[^}]*Text\s*\(\s*(?:text\s*=\s*)?["']([^"']+)["']""").find(line)
      if (buttonMatch != null) {
        elements.add(PreviewElement.ActionButton(buttonMatch.groupValues[1]))
        i++
        continue
      }

      // Text Fields
      val textFieldMatch = Regex("""(?:OutlinedTextField|TextField)\s*\([^)]*label\s*=\s*\{[^}]*Text\s*\(\s*["']([^"']+)["']""").find(line)
      if (textFieldMatch != null) {
        elements.add(PreviewElement.InputField(textFieldMatch.groupValues[1]))
        i++
        continue
      }

      // Icons
      val iconMatch = Regex("""Icons\.(?:Default|Filled|Outlined)\.(\w+)""").find(line)
      if (iconMatch != null && !line.contains("navigationIcon") && !line.contains("ConfigItem")) {
        val iconName = iconMatch.groupValues[1]
        val tintHex = if (code.contains("0xFF")) Regex("""Color\s*\(\s*(0x[0-9a-fA-F]+)\s*\)""").find(line)?.groupValues?.get(1) else null
        elements.add(PreviewElement.IconItem(iconName, tintHex))
      }

      // Text solto
      val directTextMatch = Regex("""Text\s*\(\s*(?:text\s*=\s*)?["']([^"']+)["']""").find(line)
      if (directTextMatch != null && !line.contains("label =") && !line.contains("TopAppBar") && !line.contains("Button")) {
        val text = directTextMatch.groupValues[1]
        val isHeading = line.contains("fontSize = 2") || line.contains("FontWeight.Bold") || line.contains("titleLarge")
        val isSub = line.contains("fontSize = 1") || line.contains("Color(0xFFA6ACCD)") || line.contains("bodySmall")
        elements.add(PreviewElement.TextItem(text, isHeading = isHeading, isSubtitle = isSub))
      }

      // Switches ou Checkboxes
      if (line.contains("Switch(") || line.contains("Switch (")) {
        elements.add(PreviewElement.ToggleItem("Opção / Switch", isChecked = line.contains("checked = true")))
      }

      i++
    }

    if (elements.isEmpty()) {
      elements.add(PreviewElement.EmptyState("Visualizando tela base do aplicativo."))
    }

    return elements
  }

  private fun extractBlockText(lines: List<String>, startIdx: Int): String {
    val sb = StringBuilder()
    var depth = 0
    var started = false
    for (j in startIdx until minOf(lines.size, startIdx + 20)) {
      val l = lines[j]
      sb.append(l).append(" ")
      depth += l.count { it == '{' }
      depth -= l.count { it == '}' }
      if (l.contains('{')) started = true
      if (started && depth <= 0) break
    }
    return sb.toString()
  }

  private fun parseXmlLayout(code: String): List<PreviewElement> {
    val list = mutableListOf<PreviewElement>()
    val texts = Regex("""android:text\s*=\s*["']([^"']+)["']""").findAll(code)
    for (m in texts) {
      list.add(PreviewElement.TextItem(m.groupValues[1]))
    }
    val hints = Regex("""android:hint\s*=\s*["']([^"']+)["']""").findAll(code)
    for (h in hints) {
      list.add(PreviewElement.InputField(h.groupValues[1]))
    }
    if (list.isEmpty()) {
      list.add(PreviewElement.EmptyState("Layout XML detectado."))
    }
    return list
  }
}
