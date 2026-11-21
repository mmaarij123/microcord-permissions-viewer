package dev.mantikafasi.manticord.permissionsviewer

import com.discord.api.permission.Permission

internal data class MemberGrant(
    val flag: PermissionFlag,
    val target: PermissionTarget,
    val viaAdministrator: Boolean = false,
)

internal fun memberGrants(
    targets: List<PermissionTarget>,
    isOwner: Boolean,
    lowestFirst: Boolean,
): List<MemberGrant> {
    val roles = if (lowestFirst) targets.asReversed() else targets
    val explicitMask = roles.fold(0L) { mask, target -> mask or target.allow }
    if (isOwner) {
        val owner = targets.first()
        return permissionFlags(explicitMask or Permission.ALL).map { MemberGrant(it, owner) }
    }

    val administrator = roles.firstOrNull { it.allow and Permission.ADMINISTRATOR != 0L }
    val mask = if (administrator == null) explicitMask else explicitMask or Permission.ALL
    return permissionFlags(mask).mapNotNull { flag ->
        val direct = roles.firstOrNull { it.allow and flag.bit != 0L }
        direct?.let { MemberGrant(flag, it) } ?: administrator?.let { MemberGrant(flag, it, viaAdministrator = true) }
    }
}
