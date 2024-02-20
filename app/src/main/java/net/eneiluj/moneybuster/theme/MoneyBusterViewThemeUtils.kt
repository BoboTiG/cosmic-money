package net.eneiluj.moneybuster.theme

import android.content.res.ColorStateList
import android.widget.TextView
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionBarContextView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase
import com.nextcloud.android.common.ui.util.buildColorStateList
import net.eneiluj.moneybuster.R

// TODO: upstream these to android-common:ui library
class MoneyBusterViewThemeUtils(schemes: MaterialSchemes) : ViewThemeUtilsBase(schemes) {

    fun themeMaterialSwitch(materialSwitch: MaterialSwitch) {
        withScheme(materialSwitch.context) { scheme ->
            materialSwitch.thumbTintList = buildColorStateList(
                android.R.attr.state_checked to scheme.onPrimary,
                -android.R.attr.state_checked to scheme.outline,
            )

            materialSwitch.trackTintList = buildColorStateList(
                android.R.attr.state_checked to scheme.primary,
                -android.R.attr.state_checked to scheme.surface, // XXX: specs use surfaceContainerHighest
            )

            materialSwitch.trackDecorationTintList = buildColorStateList(
                android.R.attr.state_checked to android.R.color.transparent,
                -android.R.attr.state_checked to scheme.outline,
            )
        }
    }

    fun themeTextViewLinkColor(textView: TextView) {
        withScheme(textView.context) { scheme ->
            textView.setLinkTextColor(scheme.primary)
        }
    }

    fun themeActionModeActionBar(actionBar: ActionBarContextView, vararg menuIds: Int) {
        withScheme(actionBar.context) { scheme ->
            actionBar.setBackgroundColor(scheme.primary)

            val title = actionBar.findViewById<TextView>(R.id.action_bar_title)
            title.setTextColor(scheme.onPrimary)

            val closeButton =
                actionBar.findViewById<AppCompatImageView>(R.id.action_mode_close_button)
            closeButton.imageTintList = ColorStateList.valueOf(scheme.onPrimary)

            menuIds.forEach { id ->
                val actionMenuItemView = actionBar.findViewById<ActionMenuItemView>(id)
                actionMenuItemView.compoundDrawableTintList =
                    ColorStateList.valueOf(scheme.onPrimary)
            }
        }
    }
}
