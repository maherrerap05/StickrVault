package com.example.myapplication.presentation.scanner

import com.google.mlkit.vision.text.Text

object OcrCodeExtractor {

    // Códigos Panini tipo "MEX 1", "ARG 12", "BRA 45"
    private val PANINI_CODE = Regex("""^[A-Z]{2,4}\s+\d{1,3}$""")

    fun extractFromText(rawText: String): String? {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        lines.firstOrNull { PANINI_CODE.matches(it) }?.let { return it }

        return lines
            .filter { line ->
                line.length in 3..10 &&
                    line.any { it.isLetter() } &&
                    line.any { it.isDigit() }
            }
            .minByOrNull { it.length }
    }

    fun extractFromMlKit(text: Text, imageWidth: Int, imageHeight: Int): String? {
        if (imageWidth <= 0 || imageHeight <= 0) return extractFromText(text.text)

        val topRightLines = text.textBlocks
            .filter { block ->
                val box = block.boundingBox ?: return@filter false
                val centerX = box.centerX().toFloat() / imageWidth
                val centerY = box.centerY().toFloat() / imageHeight
                centerX > 0.55f && centerY < 0.42f
            }
            .flatMap { block -> block.lines }
            .map { it.text.trim() }
            .filter { it.isNotBlank() }

        topRightLines.firstOrNull { PANINI_CODE.matches(it) }?.let { return it }

        topRightLines
            .mapNotNull { line -> PANINI_CODE.find(line)?.value }
            .firstOrNull()
            ?.let { return it }

        return extractFromText(topRightLines.joinToString("\n").ifBlank { text.text })
    }
}
