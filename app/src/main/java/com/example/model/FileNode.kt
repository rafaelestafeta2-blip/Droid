package com.example.model

enum class FileType {
  DIRECTORY,
  KOTLIN,
  XML,
  GRADLE,
  JSON,
  PROPERTIES,
  OTHER
}

data class FileNode(
  val id: String,
  val name: String,
  val path: String,
  val type: FileType,
  val content: String = "",
  val isExpanded: Boolean = true,
  val children: List<FileNode> = emptyList()
) {
  val isDirectory: Boolean get() = type == FileType.DIRECTORY
}

object ProjectFileTreeSample {
  fun createSampleProject(): FileNode {
    return FileNode(
      id = "root",
      name = "DroidStudio-Project",
      path = "/",
      type = FileType.DIRECTORY,
      isExpanded = true,
      children = listOf(
        FileNode(
          id = "app_dir",
          name = "app",
          path = "/app",
          type = FileType.DIRECTORY,
          isExpanded = true,
          children = listOf(
            FileNode(
              id = "src_dir",
              name = "src",
              path = "/app/src",
              type = FileType.DIRECTORY,
              isExpanded = true,
              children = listOf(
                FileNode(
                  id = "main_dir",
                  name = "main",
                  path = "/app/src/main",
                  type = FileType.DIRECTORY,
                  isExpanded = true,
                  children = listOf(
                    FileNode(
                      id = "java_dir",
                      name = "java/com/example",
                      path = "/app/src/main/java/com/example",
                      type = FileType.DIRECTORY,
                      isExpanded = true,
                      children = listOf(
                        FileNode(
                          id = "file_main_activity",
                          name = "MainActivity.kt",
                          path = "/app/src/main/java/com/example/MainActivity.kt",
                          type = FileType.KOTLIN,
                          content = ProjectTemplateDefaults.DEFAULT_MAIN_ACTIVITY_KT
                        )
                      )
                    ),
                    FileNode(
                      id = "res_dir",
                      name = "res/values",
                      path = "/app/src/main/res/values",
                      type = FileType.DIRECTORY,
                      isExpanded = false,
                      children = listOf(
                        FileNode(
                          id = "file_strings",
                          name = "strings.xml",
                          path = "/app/src/main/res/values/strings.xml",
                          type = FileType.XML,
                          content = ProjectTemplateDefaults.DEFAULT_STRINGS_XML
                        )
                      )
                    ),
                    FileNode(
                      id = "file_manifest",
                      name = "AndroidManifest.xml",
                      path = "/app/src/main/AndroidManifest.xml",
                      type = FileType.XML,
                      content = ProjectTemplateDefaults.DEFAULT_MANIFEST_XML
                    )
                  )
                )
              )
            ),
            FileNode(
              id = "file_build_gradle",
              name = "build.gradle.kts",
              path = "/app/build.gradle.kts",
              type = FileType.GRADLE,
              content = ProjectTemplateDefaults.DEFAULT_BUILD_GRADLE_KTS
            )
          )
        ),
        FileNode(
          id = "file_settings_gradle",
          name = "settings.gradle.kts",
          path = "/settings.gradle.kts",
          type = FileType.GRADLE,
          content = ProjectTemplateDefaults.DEFAULT_SETTINGS_GRADLE_KTS
        )
      )
    )
  }
}
