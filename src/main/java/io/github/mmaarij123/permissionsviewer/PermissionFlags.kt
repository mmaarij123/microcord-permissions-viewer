package io.github.mmaarij123.permissionsviewer

import com.discord.api.permission.Permission

internal data class PermissionFlag(val bit: Long, val title: String, val description: String? = null)

// Use the host's flags for its Long-backed role and overwrite models.
private val knownFlags = listOf(
    PermissionFlag(Permission.CREATE_INSTANT_INVITE, "Create Invite", "Generate invite links for the server."),
    PermissionFlag(Permission.KICK_MEMBERS, "Kick Members", "Remove members who can still rejoin."),
    PermissionFlag(Permission.BAN_MEMBERS, "Ban Members", "Remove members and block them from rejoining."),
    PermissionFlag(Permission.ADMINISTRATOR, "Administrator", "Grants every permission and bypasses channel overwrites."),
    PermissionFlag(Permission.MANAGE_CHANNELS, "Manage Channels", "Create, edit, and delete channels."),
    PermissionFlag(Permission.MANAGE_GUILD, "Manage Server", "Change server settings and manage integrations."),
    PermissionFlag(Permission.ADD_REACTIONS, "Add Reactions", "Add a new emoji reaction to a message."),
    PermissionFlag(Permission.VIEW_AUDIT_LOG, "View Audit Log", "Read the log of administrative changes."),
    PermissionFlag(Permission.PRIORITY_SPEAKER, "Priority Speaker", "Lower other speakers' volume while using priority push-to-talk."),
    PermissionFlag(Permission.STREAM, "Video", "Use a camera or share a screen in voice."),
    PermissionFlag(Permission.VIEW_CHANNEL, "View Channel", "See a channel in the server and open it."),
    PermissionFlag(Permission.SEND_MESSAGES, "Send Messages", "Post in text channels and create posts in forums."),
    PermissionFlag(Permission.SEND_TTS_MESSAGES, "Send Text-to-Speech Messages", "Send messages with the /tts command."),
    PermissionFlag(Permission.MANAGE_MESSAGES, "Manage Messages", "Remove messages posted by other members."),
    PermissionFlag(Permission.EMBED_LINKS, "Embed Links", "Show previews for links in messages."),
    PermissionFlag(Permission.ATTACH_FILES, "Attach Files", "Upload images and other files to a channel."),
    PermissionFlag(Permission.READ_MESSAGE_HISTORY, "Read Message History", "Read messages previously posted in the channel."),
    PermissionFlag(Permission.MENTION_EVERYONE, "Mention Everyone", "Notify members with @everyone, @here, and otherwise unmentionable roles."),
    PermissionFlag(Permission.USE_EXTERNAL_EMOJIS, "Use External Emoji", "Send custom emoji from other servers."),
    PermissionFlag(Permission.VIEW_GUILD_ANALYTICS, "View Server Insights", "Open the server's analytics dashboard."),
    PermissionFlag(Permission.CONNECT, "Connect", "Join a voice or stage channel."),
    PermissionFlag(Permission.SPEAK, "Speak", "Transmit microphone audio in a voice channel."),
    PermissionFlag(Permission.MUTE_MEMBERS, "Mute Members", "Mute other members' microphones in voice."),
    PermissionFlag(Permission.DEAFEN_MEMBERS, "Deafen Members", "Prevent other members from hearing voice audio."),
    PermissionFlag(Permission.MOVE_MEMBERS, "Move Members", "Move members between voice channels or disconnect them."),
    PermissionFlag(Permission.USE_VAD, "Use Voice Activity", "Transmit when the microphone detects speech."),
    PermissionFlag(Permission.CHANGE_NICKNAME, "Change Nickname", "Change your own server nickname."),
    PermissionFlag(Permission.MANAGE_NICKNAMES, "Manage Nicknames", "Change other members' server nicknames."),
    PermissionFlag(Permission.MANAGE_ROLES, "Manage Roles", "Manage lower-ranked roles and channel permission settings."),
    PermissionFlag(Permission.MANAGE_WEBHOOKS, "Manage Webhooks", "Create, edit, and remove webhooks."),
    PermissionFlag(Permission.MANAGE_GUILD_EXPRESSIONS, "Manage Expressions", "Edit or remove server emoji, stickers, and soundboard sounds."),
    PermissionFlag(Permission.USE_APPLICATION_COMMANDS, "Use Application Commands", "Run slash commands and app actions from context menus."),
    PermissionFlag(Permission.REQUEST_TO_SPEAK, "Request to Speak", "Ask to become a speaker in a stage channel."),
    PermissionFlag(Permission.MANAGE_EVENTS, "Manage Events", "Edit or cancel scheduled events created by other members."),
    PermissionFlag(Permission.MANAGE_THREADS, "Manage Threads", "Moderate threads and see private threads."),
    PermissionFlag(Permission.CREATE_PUBLIC_THREADS, "Create Public Threads", "Start threads visible to people who can see the parent channel."),
    PermissionFlag(Permission.CREATE_PRIVATE_THREADS, "Create Private Threads", "Start threads that require an invitation."),
    PermissionFlag(Permission.USE_EXTERNAL_STICKERS, "Use External Stickers", "Send stickers from other servers."),
    PermissionFlag(Permission.SEND_MESSAGES_IN_THREADS, "Send Messages in Threads", "Reply to threads and forum posts."),
    PermissionFlag(Permission.USE_EMBEDDED_ACTIVITIES, "Use Activities", "Launch and join embedded app activities."),
    PermissionFlag(Permission.MODERATE_MEMBERS, "Time Out Members", "Temporarily restrict another member's participation."),
    PermissionFlag(Permission.VIEW_CREATOR_MONETIZATION_ANALYTICS, "View Monetization Insights", "See analytics for role subscriptions."),
    PermissionFlag(Permission.USE_SOUNDBOARD, "Use Soundboard", "Play soundboard clips in a voice channel."),
    PermissionFlag(Permission.CREATE_GUILD_EXPRESSIONS, "Create Expressions", "Add server emoji, stickers, and soundboard sounds."),
    PermissionFlag(Permission.CREATE_EVENTS, "Create Events", "Schedule events and manage events you create."),
    PermissionFlag(Permission.USE_EXTERNAL_SOUNDS, "Use External Sounds", "Play soundboard clips from other servers."),
    PermissionFlag(Permission.SEND_VOICE_MESSAGES, "Send Voice Messages", "Post recorded audio messages."),
    PermissionFlag(Permission.SET_VOICE_CHANNEL_STATUS, "Set Voice Channel Status", "Change the status displayed for a voice channel."),
    PermissionFlag(Permission.SEND_POLLS, "Create Polls", "Post messages that members can vote on."),
    PermissionFlag(Permission.USE_EXTERNAL_APPS, "Use External Apps", "Let personal apps post responses visible to the channel."),
    PermissionFlag(Permission.PIN_MESSAGES, "Pin Messages", "Add or remove pins in a channel."),
    PermissionFlag(Permission.BYPASS_SLOWMODE, "Bypass Slowmode", "Post without waiting for the channel's slowmode timer."),
    PermissionFlag(Permission.MANAGE_OFFICIAL_MESSAGES, "Manage Official Messages", "Mark messages as official server communication."),
).associateBy(PermissionFlag::bit)

internal fun permissionFlags(mask: Long): List<PermissionFlag> = buildList {
    repeat(Long.SIZE_BITS) { index ->
        val bit = 1L shl index
        if (mask and bit != 0L) {
            add(knownFlags[bit] ?: PermissionFlag(bit, "Unknown permission (bit $index)"))
        }
    }
}
