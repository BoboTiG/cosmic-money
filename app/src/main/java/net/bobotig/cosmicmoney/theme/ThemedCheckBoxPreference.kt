package net.bobotig.cosmicmoney.theme

import android.content.Context
import android.util.AttributeSet
import androidx.preference.CheckBoxPreference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.checkbox.MaterialCheckBox
import net.bobotig.cosmicmoney.R


class ThemedCheckBoxPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.checkBoxPreferenceStyle,
    defStyleRes: Int = 0,
) : CheckBoxPreference(context, attrs, defStyleAttr, defStyleRes) {

    private val utils: ThemeUtils = ThemeUtils.of(context)

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val checkBox = holder.findViewById(R.id.checkbox)

        if (checkBox is MaterialCheckBox) {
            // we need to manually set this based on the mChecked field that our parent class keeps
            checkBox.isChecked = mChecked

            utils.material.colorMaterialCheckBox(checkBox)
        }
    }
}