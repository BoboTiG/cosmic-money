package net.bobotig.cosmicmoney.theme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import net.bobotig.cosmicmoney.R


class ThemedProgressDialogBuilder(context: Context) : ThemedMaterialAlertDialogBuilder(context) {

    private val view: View
    private val progressIndicator: CircularProgressIndicator
    private val messageTextView: TextView

    init {
        view = LayoutInflater.from(context).inflate(R.layout.dialog_themed_progress, null)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        messageTextView = view.findViewById(R.id.message)
        setView(view)

        utils.material.colorProgressBar(progressIndicator)
    }

    override fun setMessage(message: CharSequence?): MaterialAlertDialogBuilder {
        messageTextView.text = message
        return this
    }

}
