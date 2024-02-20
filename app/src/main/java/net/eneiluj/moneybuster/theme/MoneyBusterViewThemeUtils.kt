package net.eneiluj.moneybuster.theme

import android.content.res.ColorStateList
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionBarContextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.preference.PreferenceCategory
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch
import com.nextcloud.android.common.ui.theme.MaterialSchemes
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase
import com.nextcloud.android.common.ui.theme.utils.MaterialViewThemeUtils
import com.nextcloud.android.common.ui.util.buildColorStateList
import net.eneiluj.moneybuster.R


// TODO: upstream these to android-common:ui library
class MoneyBusterViewThemeUtils(
    val material: MaterialViewThemeUtils,
    schemes: MaterialSchemes,
) : ViewThemeUtilsBase(schemes) {

    fun themeMaterialCheckBox(materialCheckBox: MaterialCheckBox) {
        withScheme(materialCheckBox.context) { scheme ->
            materialCheckBox.buttonTintList = buildColorStateList(
                android.R.attr.state_checked to scheme.primary,
                -android.R.attr.state_checked to scheme.outline,
            )

            materialCheckBox.buttonIconTintList = buildColorStateList(
                android.R.attr.state_checked to scheme.onPrimary,
                -android.R.attr.state_checked to android.R.color.transparent,
            )
        }
    }

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

    /**
     * Call this AFTER `dialog.show()`, otherwise the buttons will be null!
     */
    fun colorDialogButtons(dialog: AlertDialog) {
        val buttons = listOf(
            AlertDialog.BUTTON_NEGATIVE,
            AlertDialog.BUTTON_NEUTRAL,
            AlertDialog.BUTTON_POSITIVE,
        )
        buttons.forEach { buttonInt ->
            dialog.getButton(buttonInt)?.let { button ->
                if (button is MaterialButton) {
                    this.material.colorMaterialButtonText(button)
                    this.material.colorMaterialTextButton(button)
                }
            }
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

    fun themePreferenceCategory(category: PreferenceCategory) {
        withScheme(category.context) { scheme ->
            val text: Spannable = SpannableString(category.title)
            text.setSpan(
                ForegroundColorSpan(scheme.primary),
                0,
                text.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            category.title = text
        }
    }
}
