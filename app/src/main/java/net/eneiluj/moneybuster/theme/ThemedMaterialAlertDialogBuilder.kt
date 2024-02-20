package net.eneiluj.moneybuster.theme

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class ThemedMaterialAlertDialogBuilder(context: Context) : MaterialAlertDialogBuilder(context) {

    private val utils = ThemeUtils.of(context)

    override fun show(): AlertDialog {
        utils.dialog.colorMaterialAlertDialogBackground(context, this)
        val dialog = super.show()
        utils.moneybuster.colorDialogButtons(dialog)
        return dialog
    }
}
