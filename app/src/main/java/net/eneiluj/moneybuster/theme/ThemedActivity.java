package net.eneiluj.moneybuster.theme;

import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import net.eneiluj.moneybuster.util.ColorUtils;


public abstract class ThemedActivity extends AppCompatActivity implements Themed {

    @Override
    protected void onStart() {
        super.onStart();

        // FIXME: Use a LiveData to constantly observe this and react to changes? -> See Nextcloud Deck
        int color = ColorUtils.primaryColor(this);
        applyTheme(color);
    }
}
