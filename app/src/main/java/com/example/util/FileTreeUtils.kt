package com.example.util

import com.example.model.FileNode

object FileTreeUtils {
  fun toggleFolder(node: FileNode, folderId: String): FileNode {
    if (node.id == folderId) return node.copy(isExpanded = !node.isExpanded)
    return node.copy(children = node.children.map { toggleFolder(it, folderId) })
  }

  fun findFirstFile(node: FileNode): FileNode? {
    if (!node.isDirectory) return node
    for (child in node.children) {
      val found = findFirstFile(child)
      if (found != null) return found
    }
    return null
  }

  fun findFileByName(node: FileNode, name: String): FileNode? {
    if (!node.isDirectory && node.name == name) return node
    for (child in node.children) {
      val found = findFileByName(child, name)
      if (found != null) return found
    }
    return null
  }

  fun buildProjectTreeSummary(node: FileNode, indent: String = ""): String {
    val builder = StringBuilder()
    val prefix = if (node.isDirectory) "$indent📁 ${node.name}/\n" else "$indent📄 ${node.name}\n"
    builder.append(prefix)
    if (node.isDirectory) {
      for (child in node.children) {
        builder.append(buildProjectTreeSummary(child, "$indent  "))
      }
    }
    return builder.toString()
  }
}
