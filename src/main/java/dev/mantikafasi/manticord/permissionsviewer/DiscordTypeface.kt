package dev.mantikafasi.manticord.permissionsviewer

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.content.res.ResourcesCompat

internal fun Context.discordTypeface(attribute: Int): Typeface? {
    val value = TypedValue()
    if (!theme.resolveAttribute(attribute, value, true) || value.resourceId == 0) return null
    return try {
        ResourcesCompat.getFont(this, value.resourceId)
    } catch (_: Resources.NotFoundException) {
        null
    }
}
