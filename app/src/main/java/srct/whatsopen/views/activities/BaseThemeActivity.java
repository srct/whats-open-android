package srct.whatsopen.views.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import srct.whatsopen.R;

public abstract class BaseThemeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle bundle) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dark = prefs.getBoolean("dark_theme", false);
        // look at the udacity course. In this section you might want to reload,
        // but this would mean every activity would have to set the right theme

        if(dark){
            setTheme(R.style.darkTheme);
            Log.e("Mike's log", "BaseThemeActivity: dark");
        }else{
            setTheme(R.style.AppTheme);
            Log.e("Mike's log", "BaseThemeActivity: light");
        }
        super.onCreate(bundle);
    }
}
