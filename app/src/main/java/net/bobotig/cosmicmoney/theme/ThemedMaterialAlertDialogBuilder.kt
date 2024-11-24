package net.bobotig.cosmicmoney.theme

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder


open class ThemedMaterialAlertDialogBuilder(context: Context) : MaterialAlertDialogBuilder(context) {

    protected val utils = ThemeUtils.of(context)

    override fun show(): AlertDialog {
        utils.dialog.colorMaterialAlertDialogBackground(context, this)
        val dialog = super.show()
        utils.cosmicmoney.colorDialogButtons(dialog)
        return dialog
    }
}
