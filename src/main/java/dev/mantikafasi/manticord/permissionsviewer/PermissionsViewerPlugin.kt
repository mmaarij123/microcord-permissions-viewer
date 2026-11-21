package dev.mantikafasi.manticord.permissionsviewer

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.IBinder
import android.view.Gravity
import android.widget.Button
import com.discord.R
import com.discord.api.channel.isThread
import com.discord.api.permission.Permission
import com.discord.api.permission.PermissionOverwrite
import com.discord.api.role.GuildRole
import com.discord.stores.StoreStream
import com.discord.utilities.guilds.RoleUtils
import com.discord.utilities.user.UserUtils.getDisplayName
import dev.mantikafasi.manticord.plugins.api.ManticordPlugin
import dev.mantikafasi.manticord.plugins.api.PluginContextMenuSurface
import dev.mantikafasi.manticord.plugins.api.PluginScope
import dev.mantikafasi.manticord.plugins.api.pluginContextMenuItem
import dev.mantikafasi.manticord.plugins.api.pluginProfileContribution
import dev.mantikafasi.manticord.plugins.api.pluginSettings
import rx.Completable

internal const val SORT_ORDER = "roleOrder"
internal const val HIGHEST_ROLE = "highest"
internal const val LOWEST_ROLE = "lowest"
private const val VIEW_PERMISSIONS = "View Permissions"

class PermissionsViewerPlugin : ManticordPlugin {
    override val settingsSchema = pluginSettings {
        choice(
            SORT_ORDER,
            "Role used for permission attribution",
            default = HIGHEST_ROLE,
            options = listOf(HIGHEST_ROLE to "Highest role", LOWEST_ROLE to "Lowest role"),
        )
    }

    override fun start(scope: PluginScope): Completable = Completable.fromAction {
        scope.ui.registerContextMenuItem(
            pluginContextMenuItem(
                "server_permissions",
                VIEW_PERMISSIONS,
                PluginContextMenuSurface.GUILD,
                onClick = { it.guildId?.let { id -> showServer(scope, id) } },
            ) { isVisible = { it.guildId != null } },
        )
        scope.ui.registerContextMenuItem(
            pluginContextMenuItem(
                "channel_permissions",
                VIEW_PERMISSIONS,
                PluginContextMenuSurface.CHANNEL,
                PluginContextMenuSurface.THREAD,
                onClick = { it.channelId?.let { id -> showChannel(scope, id) } },
            ) { isVisible = { it.guildId != null && it.channelId != null } },
        )
        scope.ui.registerContextMenuItem(
            pluginContextMenuItem(
                "member_permissions",
                VIEW_PERMISSIONS,
                PluginContextMenuSurface.MEMBER,
                onClick = { context ->
                    val guildId = context.guildId
                    val userId = context.userId
                    if (guildId != null && userId != null) showMember(scope, guildId, userId)
                },
            ) { isVisible = { it.guildId != null && it.userId != null } },
        )
        scope.ui.registerProfile(
            pluginProfileContribution(
                "profile_permissions",
                VIEW_PERMISSIONS,
                { context ->
                    Button(context).apply {
                        text = VIEW_PERMISSIONS
                        context.discordTypeface(R.attr.font_primary_semibold)?.let { typeface = it }
                        isAllCaps = false
                        gravity = Gravity.CENTER
                        minHeight = context.dp(48)
                    }
                },
            ) {
                priority = 450
                isVisible = { it.guildId != null }
                bind = { view, profile ->
                    (view as Button).apply {
                        setTextColor(profile.primaryTextColor)
                        background = GradientDrawable().apply {
                            setColor(profile.surfaceColor)
                            setStroke(context.dp(1), profile.borderColor)
                            cornerRadius = context.dp(10).toFloat()
                        }
                        setOnClickListener {
                            profile.guildId?.let { showMember(scope, it, profile.userId) }
                        }
                    }
                }
            },
        )
    }

    private fun showServer(scope: PluginScope, guildId: Long) {
        val guilds = StoreStream.getGuilds()
        val guild = guilds.getGuild(guildId) ?: return unavailable(scope, "Server data is unavailable.")
        val roles = guilds.getRoles()[guildId].orEmpty().values.sortedWith(RoleUtils.ROLE_COMPARATOR)
        show(scope, guild.name, roles.map { it.asTarget() }, emptyText = "No server roles are available.")
    }

    private fun showMember(scope: PluginScope, guildId: Long, userId: Long) {
        val guilds = StoreStream.getGuilds()
        val guild = guilds.getGuild(guildId) ?: return unavailable(scope, "Server data is unavailable.")
        val member = guilds.getMember(guildId, userId)
            ?: return unavailable(scope, "This member's server roles are unavailable.")
        val roles = guilds.getRoles()[guildId].orEmpty()
        val memberRoleIds = (member.getRoles() + guildId).toSet()
        val memberRoles = memberRoleIds.mapNotNull(roles::get).sortedWith(RoleUtils.ROLE_COMPARATOR)
        val user = StoreStream.getUsers().getUsers()[userId]
        val name = user?.getDisplayName(member.nick) ?: member.nick?.takeIf(String::isNotBlank) ?: "Member $userId"
        val owner = PermissionTarget(userId, "Server Owner", "Owner", Permission.ALL)
            .takeIf { guild.isOwner(userId) }
        val targets = listOfNotNull(owner) + memberRoles.map { it.asTarget() }
        show(
            scope, name, targets, hasSummary = true, isOwner = owner != null,
            note = "Some of this member's roles are unavailable.".takeIf { memberRoles.size < memberRoleIds.size },
        )
    }

    private fun showChannel(scope: PluginScope, channelId: Long) {
        val channels = StoreStream.getChannels()
        val channel = channels.getChannel(channelId) ?: return unavailable(scope, "Channel data is unavailable.")
        if (channel.guildId <= 0L) return

        // Threads take their role and member overwrites from the parent channel.
        val source = if (channel.isThread()) channels.getChannel(channel.parentId) else channel
        if (source == null) return unavailable(scope, "The thread's parent channel is unavailable.")
        val guilds = StoreStream.getGuilds()
        val roles = guilds.getRoles()[channel.guildId].orEmpty()
        val users = StoreStream.getUsers().getUsers()
        val overwrites = source.permissionOverwrites.orEmpty().groupBy { it.type }
        val roleTargets = overwrites[PermissionOverwrite.Type.ROLE].orEmpty()
            .sortedWith(compareBy(RoleUtils.ROLE_COMPARATOR) { roles[it.id] })
            .map { entry ->
                val role = roles[entry.id]
                val name = role?.name ?: if (entry.id == channel.guildId) "@everyone" else "Role ${entry.id}"
                PermissionTarget(entry.id, name, "Role", entry.allow, entry.deny, role?.color ?: 0)
            }
        val memberTargets = overwrites[PermissionOverwrite.Type.MEMBER].orEmpty().map { entry ->
            val nick = guilds.getMember(channel.guildId, entry.id)?.nick?.takeIf(String::isNotBlank)
            val name = users[entry.id]?.getDisplayName(nick) ?: nick ?: "Member ${entry.id}"
            PermissionTarget(entry.id, name, "Member", entry.allow, entry.deny)
        }.sortedBy { it.name.lowercase() }
        show(
            scope,
            channel.name ?: "Channel",
            roleTargets + memberTargets,
            note = source.takeIf { channel.isThread() }?.let { "Inherited from #${it.name ?: it.id}." },
            emptyText = "This channel has no permission overwrites.",
        )
    }

    private fun show(
        scope: PluginScope,
        name: String,
        targets: List<PermissionTarget>,
        hasSummary: Boolean = false,
        isOwner: Boolean = false,
        note: String? = null,
        emptyText: String = "No permissions to display.",
    ) {
        scope.dialogs.page("Permissions", name) { context ->
            permissionsPage(context, scope.settings, targets, hasSummary, isOwner, note, emptyText)
        }
    }

    private fun unavailable(scope: PluginScope, message: String) = scope.dialogs.alert("Permissions", message)
}

internal data class PermissionTarget(
    val id: Long,
    val name: String,
    val kind: String,
    val allow: Long,
    val deny: Long? = null,
    val color: Int = Color.TRANSPARENT,
)

private fun GuildRole.asTarget() = PermissionTarget(id, name, "Role", permissions, color = color)

class PermissionsViewerDiscoveryService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
