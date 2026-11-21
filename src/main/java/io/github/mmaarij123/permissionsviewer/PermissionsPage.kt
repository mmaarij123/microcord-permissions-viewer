package io.github.mmaarij123.permissionsviewer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.discord.R
import com.discord.api.permission.Permission
import com.discord.utilities.color.ColorCompat
import dev.mantikafasi.manticord.plugins.api.PluginSettings

internal fun permissionsPage(
    context: Context,
    settings: PluginSettings,
    targets: List<PermissionTarget>,
    hasSummary: Boolean,
    isOwner: Boolean,
    note: String?,
    emptyText: String,
): View = PermissionsPage(context).create(settings, targets, hasSummary, isOwner, note, emptyText)

private class PermissionsPage(private val context: Context) {
    private val primary = color(R.attr.colorHeaderPrimary)
    private val secondary = color(R.attr.colorHeaderSecondary)
    private val surface = color(R.attr.colorBackgroundSecondary)
    private val allowed = color(R.attr.colorTextPositive)
    private val denied = color(R.attr.colorTextDanger)
    private val bodyTypeface = context.discordTypeface(R.attr.font_primary_normal) ?: Typeface.DEFAULT
    private val boldTypeface = context.discordTypeface(R.attr.font_primary_bold) ?: Typeface.DEFAULT_BOLD
    private val buttonTypeface = context.discordTypeface(R.attr.font_primary_semibold) ?: boldTypeface

    fun create(
        settings: PluginSettings,
        targets: List<PermissionTarget>,
        hasSummary: Boolean,
        isOwner: Boolean,
        note: String?,
        emptyText: String,
    ): View = column().apply {
        note?.let { addView(label(it, 14, secondary)) }
        if (targets.isEmpty() && !hasSummary) {
            addView(label(emptyText, 16, primary))
            return@apply
        }

        val options = targets.map { target ->
            val name = if (target.deny != null) "${target.name} · ${target.kind}" else target.name
            sourceLabel(name, target.color)
        }
        val choices = if (hasSummary) listOf("Summary") + options else options
        val picker = Spinner(context, Spinner.MODE_DROPDOWN).apply {
            id = View.generateViewId()
            minimumHeight = context.dp(48)
            setPopupBackgroundDrawable(panel())
            adapter = object : ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, choices) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View =
                    label(choices[position], 16, primary, true)

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View =
                    label(choices[position], 16, primary).apply {
                        minHeight = context.dp(48)
                        setPadding(context.dp(14), context.dp(12), context.dp(14), context.dp(12))
                    }
            }
        }
        addView(label(if (hasSummary) "View" else "Permission source", 14, secondary).apply {
            labelFor = picker.id
            setPadding(0, context.dp(8), 0, 0)
        })
        addView(picker)

        val content = column()
        addView(content)
        picker.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                content.removeAllViews()
                if (hasSummary && position == 0) {
                    renderSummary(content, settings, targets, isOwner)
                } else {
                    val target = targets[position - if (hasSummary) 1 else 0]
                    renderDetails(content, target)
                }
            }
        }
    }

    private fun renderSummary(
        content: LinearLayout,
        settings: PluginSettings,
        targets: List<PermissionTarget>,
        isOwner: Boolean,
    ) {
        val rows = column()
        content.addView(label("Server permissions", 18, primary, true))
        val sortButton = actionButton("")
        if (!isOwner && targets.size > 1) content.addView(sortButton)
        content.addView(rows)

        fun update() {
            val lowestFirst = settings.getString(SORT_ORDER, HIGHEST_ROLE) == LOWEST_ROLE
            sortButton.text = if (lowestFirst) "Attribution: Lowest role" else "Attribution: Highest role"
            rows.removeAllViews()
            val grants = memberGrants(targets, isOwner, lowestFirst)
            if (grants.isEmpty()) rows.addView(label("No server permissions to display.", 16, secondary))
            for (grant in grants) {
                val source = if (grant.viaAdministrator) {
                    "Administrator via ${grant.target.name}"
                } else {
                    "Granted by ${grant.target.name}"
                }
                rows.addView(permissionRow(grant.flag, sourceLabel(source, grant.target.color), secondary))
            }
        }

        sortButton.setOnClickListener {
            val current = settings.getString(SORT_ORDER, HIGHEST_ROLE)
            settings.putString(SORT_ORDER, if (current == LOWEST_ROLE) HIGHEST_ROLE else LOWEST_ROLE)
            update()
        }
        update()
    }

    private fun renderDetails(content: LinearLayout, target: PermissionTarget) {
        content.addView(actionButton("Copy ${target.kind.lowercase()} ID").apply {
            setOnClickListener {
                val clipboard = context.getSystemService(ClipboardManager::class.java) ?: return@setOnClickListener
                clipboard.setPrimaryClip(ClipData.newPlainText("${target.kind} ID", target.id.toString()))
                Toast.makeText(context, "${target.kind} ID copied", Toast.LENGTH_SHORT).show()
            }
        })
        val mask = target.allow or (target.deny ?: Permission.ALL)
        if (mask == 0L) content.addView(label("This overwrite has no explicit changes.", 16, secondary))
        for (flag in permissionFlags(mask)) {
            val (status, tint) = when {
                target.allow and flag.bit != 0L -> if (target.deny == null) "Granted" to allowed else "Allowed" to allowed
                target.deny != null -> "Denied" to denied
                else -> "Unset" to secondary
            }
            content.addView(permissionRow(flag, status, tint))
        }
    }

    private fun permissionRow(flag: PermissionFlag, status: CharSequence, statusColor: Int) = column().apply {
        background = panel()
        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            .apply { topMargin = context.dp(6) }
        setPadding(context.dp(14), context.dp(10), context.dp(14), context.dp(10))
        addView(label(flag.title, 16, primary, true))
        addView(label(status, 14, statusColor))
        flag.description?.let { addView(label(it, 14, secondary)) }
    }

    private fun sourceLabel(text: String, roleColor: Int): CharSequence {
        if (roleColor == 0) return text
        return SpannableString("●  $text").apply {
            setSpan(ForegroundColorSpan(roleColor or Color.BLACK), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    private fun column() = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }

    private fun label(value: CharSequence, size: Int, tint: Int, bold: Boolean = false) = TextView(context).apply {
        text = value
        textSize = size.toFloat()
        setTextColor(tint)
        typeface = if (bold) boldTypeface else bodyTypeface
    }

    private fun actionButton(title: String) = Button(context).apply {
        text = title
        typeface = buttonTypeface
        setTextColor(primary)
        backgroundTintList = ColorStateList.valueOf(surface)
        minHeight = context.dp(48)
        isAllCaps = false
    }

    private fun panel() = GradientDrawable().apply {
        setColor(surface)
        cornerRadius = context.dp(10).toFloat()
    }

    private fun color(attribute: Int) = ColorCompat.getThemedColor(context, attribute)
}

internal fun Context.dp(value: Int) = (value * resources.displayMetrics.density + 0.5f).toInt()
