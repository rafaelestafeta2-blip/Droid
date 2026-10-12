package com.example.model

/**
 * Estado e modelos do Live Preview Flutuante.
 */
data class LivePreviewState(
  val isVisible: Boolean = false,
  val isMinimized: Boolean = false,
  val opacity: Float = 0.95f,
  val offsetX: Float = 24f,
  val offsetY: Float = 60f,
  val widthDp: Float = 240f,
  val heightDp: Float = 360f,
  val previewTitle: String = "App Preview",
  val elements: List<PreviewElement> = emptyList()
)

sealed class PreviewElement {
  data class ScreenHeader(val title: String, val subtitle: String? = null) : PreviewElement()
  data class TextItem(val text: String, val isHeading: Boolean = false, val isSubtitle: Boolean = false) : PreviewElement()
  data class ActionButton(val label: String, val isPrimary: Boolean = true) : PreviewElement()
  data class InputField(val label: String, val placeholder: String = "") : PreviewElement()
  data class CardItem(val title: String, val description: String, val icon: String? = null) : PreviewElement()
  data class IconItem(val name: String, val tintColorHex: String? = null) : PreviewElement()
  data class ToggleItem(val label: String, val isChecked: Boolean = false) : PreviewElement()
  data class ListItem(val title: String, val detail: String? = null) : PreviewElement()
  object DividerLine : PreviewElement()
  data class EmptyState(val message: String) : PreviewElement()
}
