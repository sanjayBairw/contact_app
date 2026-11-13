package com.snj.contactly.utils

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import kotlin.math.abs

object AvatarUtils {

    data class AvatarPalette(
        val backgroundColor: Int,
        val textColor: Int
    )

    private val palettes = listOf(
        AvatarPalette(0xFFE0E7FF.toInt(), 0xFF3730A3.toInt()), // Indigo
        AvatarPalette(0xFFDBEAFE.toInt(), 0xFF1E40AF.toInt()), // Blue
        AvatarPalette(0xFFD1FAE5.toInt(), 0xFF065F46.toInt()), // Emerald
        AvatarPalette(0xFFFEF3C7.toInt(), 0xFF92400E.toInt()), // Amber
        AvatarPalette(0xFFFCE7F3.toInt(), 0xFF9D174D.toInt()), // Pink
        AvatarPalette(0xFFEDE9FE.toInt(), 0xFF5B21B6.toInt()), // Violet
        AvatarPalette(0xFFCCFBF1.toInt(), 0xFF115E59.toInt())  // Teal
    )

    fun extractInitials(name: String): String {
        val parts = name.trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }

        return when {
            parts.isEmpty() -> "?"
            parts.size == 1 -> parts[0].take(1).uppercase()
            else -> "${parts.first().take(1)}${parts.last().take(1)}".uppercase()
        }
    }

    fun paletteFor(name: String): AvatarPalette {
        val index = abs(name.trim().lowercase().hashCode()) % palettes.size
        return palettes[index]
    }

    fun bindAvatar(
        context: Context,
        name: String,
        photoUri: String?,
        imageView: ImageView,
        initialsView: TextView
    ) {
        val loaded = tryLoadPhoto(context, photoUri, imageView)
        if (loaded) {
            imageView.visibility = View.VISIBLE
            initialsView.visibility = View.GONE
        } else {
            imageView.setImageDrawable(null)
            imageView.visibility = View.GONE
            initialsView.visibility = View.VISIBLE
            initialsView.text = extractInitials(name)
            val palette = paletteFor(name)
            initialsView.setTextColor(palette.textColor)
            initialsView.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(palette.backgroundColor)
            }
        }
    }

    private fun tryLoadPhoto(
        context: Context,
        photoUri: String?,
        imageView: ImageView
    ): Boolean {
        if (photoUri.isNullOrBlank()) return false
        return try {
            val uri = Uri.parse(photoUri)
            context.contentResolver.openInputStream(uri)?.use {
                // Verify stream is readable before setting URI
            } ?: return false
            imageView.setImageURI(uri)
            imageView.drawable != null
        } catch (_: Exception) {
            false
        }
    }
}

