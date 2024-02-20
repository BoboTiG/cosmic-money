package net.eneiluj.moneybuster.theme

import com.google.android.material.materialswitch.MaterialSwitch
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase
import com.nextcloud.android.common.ui.util.buildColorStateList
import scheme.Scheme

class MoneyBusterViewThemeUtils(schemes: MaterialSchemes) : ViewThemeUtilsBase(schemes) {

    // TODO: upstream this to android-common:ui library
    fun themeMaterialSwitch(materialSwitch: MaterialSwitch) {
        withScheme(materialSwitch.context) { scheme: Scheme ->
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
}
