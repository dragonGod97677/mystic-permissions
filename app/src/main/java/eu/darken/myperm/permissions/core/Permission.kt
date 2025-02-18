package eu.darken.myperm.permissions.core

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.core.content.ContextCompat
import eu.darken.myperm.apps.core.Pkg
import eu.darken.myperm.apps.core.getPermissionInfo2
import eu.darken.myperm.permissions.core.features.*
import eu.darken.myperm.permissions.core.known.AExtraPerm
import eu.darken.myperm.permissions.core.known.APerm
import eu.darken.myperm.permissions.core.known.APermGrp
import kotlinx.parcelize.Parcelize

interface Permission {
    val id: Id

    val tags: Collection<PermissionTag>
        get() = emptySet()
    val groupIds: Collection<PermissionGroup.Id>
        get() = emptySet()

    fun getLabel(context: Context): String? {

        APerm.values.singleOrNull { it.id == id }
            ?.labelRes
            ?.let { return context.getString(it) }

        AExtraPerm.values.singleOrNull { it.id == id }
            ?.labelRes
            ?.let { return context.getString(it) }

        val pm = context.packageManager

        pm
            .getPermissionInfo2(id, PackageManager.GET_META_DATA)
            ?.takeIf { it.labelRes != 0 }
            ?.loadLabel(pm)
            ?.takeIf { it.isNotEmpty() && it != id.value }
            ?.let { return it.toString() }

        return null
    }

    fun getDescription(context: Context): String? {

        APerm.values.singleOrNull { it.id == id }
            ?.descriptionRes
            ?.let { return context.getString(it) }

        AExtraPerm.values.singleOrNull { it.id == id }
            ?.descriptionRes
            ?.let { return context.getString(it) }

        val pm = context.packageManager

        pm
            .getPermissionInfo2(id, PackageManager.GET_META_DATA)
            ?.takeIf { it.labelRes != 0 }
            ?.loadDescription(pm)
            ?.takeIf { it.isNotEmpty() && it != id.value }
            ?.let { return it.toString() }

        return null
    }

    fun getIcon(context: Context): Drawable? {
        val pm = context.packageManager

        pm
            .getPermissionInfo2(id, PackageManager.GET_META_DATA)
            ?.takeIf { it.icon != 0 }
            ?.loadIcon(pm)
            ?.let { return it }

        APerm.values.singleOrNull { it.id == id }
            ?.iconRes
            ?.let { ContextCompat.getDrawable(context, it) }
            ?.let { return it }

        AExtraPerm.values.singleOrNull { it.id == id }
            ?.iconRes
            ?.let { ContextCompat.getDrawable(context, it) }
            ?.let { return it }

        return null
    }

    fun getAction(context: Context, pkg: Pkg): PermissionAction {
        when {
            tags.contains(RuntimeGrant) -> PermissionAction.Runtime(this, pkg)
            tags.contains(SpecialAccess) -> PermissionAction.SpecialAccess(this, pkg)
            else -> null
        }?.let { return it }

        return PermissionAction.None(this, pkg)
    }

    @Parcelize
    data class Id(val value: String) : Parcelable
}


fun Permission.Id.toKnownPermission(): APerm? =
    APerm.values.singleOrNull { it.id == this@toKnownPermission }

fun Permission.getGroup(): Collection<APermGrp> =
    groupIds.map { grpId -> APermGrp.values.single { it.id == grpId } }


fun Permission.Id.getGroupIds(): Collection<PermissionGroup.Id> =
    APerm.values.singleOrNull { it.id == this }?.groupIds ?: emptySet()


fun Permission.getGroupIds(): Collection<PermissionGroup.Id> =
    APerm.values.singleOrNull { it.id == this.id }?.groupIds ?: emptySet()

val Permission.isHighlighted
    get() = this.tags.any { it is Highlighted }