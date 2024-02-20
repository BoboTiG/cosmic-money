package net.eneiluj.moneybuster.theme

import android.widget.TextView
import com.google.android.material.materialswitch.MaterialSwitch
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase
import com.nextcloud.android.common.ui.util.buildColorStateList
import scheme.Scheme

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
        withScheme(textView.context) {scheme ->
            textView.setLinkTextColor(scheme.primary)
        }
    }
}
