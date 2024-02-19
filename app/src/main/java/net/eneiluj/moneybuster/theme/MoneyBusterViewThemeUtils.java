package net.eneiluj.moneybuster.theme;

import android.graphics.PorterDuff;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.nextcloud.android.common.ui.theme.MaterialSchemes;
import com.nextcloud.android.common.ui.theme.ViewThemeUtilsBase;

import net.eneiluj.moneybuster.R;
import net.eneiluj.moneybuster.util.ColorUtils;


public class MoneyBusterViewThemeUtils extends ViewThemeUtilsBase {

    public MoneyBusterViewThemeUtils(@NonNull MaterialSchemes schemes) {
        super(schemes);
    }

    // TODO: upstream something like this to android-common:ui library
    public void themeSwitch(@NonNull SwitchMaterial switchMaterial) {
        var context = switchMaterial.getContext();
        withScheme(context, scheme -> {
            if (switchMaterial.isChecked()) {
                switchMaterial.getTrackDrawable().setColorFilter(ColorUtils.primaryDarkColor(context), PorterDuff.Mode.SRC_IN);
                switchMaterial.getThumbDrawable().setColorFilter(ColorUtils.primaryColor(context), PorterDuff.Mode.MULTIPLY);
            } else {
                switchMaterial.getTrackDrawable().setColorFilter(ContextCompat.getColor(context, R.color.fg_default_low), PorterDuff.Mode.SRC_IN);
                switchMaterial.getThumbDrawable().setColorFilter(ContextCompat.getColor(context, R.color.fg_default_high), PorterDuff.Mode.MULTIPLY);
            }
            return switchMaterial;
        });
    }
}
