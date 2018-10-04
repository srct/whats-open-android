package srct.whatsopen.views.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import srct.whatsopen.R;

public abstract class BaseThemeActivity extends AppCompatActivity {
    private static SharedPreferences prefs;

    /**
     * This is overriden so the theme can be changed, but it only works statically because
     * <a href="https://developer.android.com/reference/android/view/ContextThemeWrapper#setTheme(int)">setTheme(int)</a>
     * only applies after the contentView is set.
     * @param bundle This is just your classic bundle
     */
    @Override
    protected void onCreate(Bundle bundle) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean dark = prefs.getBoolean("dark_theme", false);
        // look at the udacity course. In this section you might want to reload,
        // but this would mean every activity would have to set the right theme
        Log.d("BaseThemeActivity", ".onCreate Recreate called?");

        if(dark){
            setTheme(R.style.darkTheme);
            Log.d("BaseThemeActivity", ".onCreate() Theme is now dark");
        }else{
            setTheme(R.style.AppTheme);
            Log.d("BaseThemeActivity", ".onCreate() Theme is now light");
        }
        super.onCreate(bundle);
    }

    /**
     * This is overriden so that the theme can be changed dynamically. onStart is called, but
     * the themes are wrong, onStart will call recreate, which will destroy the app and re-create it.
     * This is a dangerous practice as it could destroy volatile data,
     * but we don't have any volatile data so who cares. It will set the theme correctly.
     */
    @Override
    protected void onStart(){
        boolean dark = prefs.getBoolean("dark_theme", false);
        int actTheme = getThemeID();

        if(actTheme == R.style.darkTheme && !dark){
            Log.d("BaseThemeActivity", ".onStart calling recreate to set to light");
            recreate();
        }else if (actTheme == R.style.AppTheme && dark){
            Log.d("BaseThemeActivity", ".onStart calling recreate to set to dark");
            recreate();
        }
        super.onStart();


    }

    /**
        gets the current theme's resid:
        <a href="https://stackoverflow.com/questions/7267852/android-how-to-obtain-the-resource-id-of-the-current-theme">
        getThemeResId()</a>
        note, the top answer on this page returns the default theme's ID, not the actual theme.

        The stackoverflow warned about using getThemeResId because it was designed to be private.
        @return returns the int resid of the current theme
    */
    private int getThemeID(){
        int themeResId = 0;
        String TAG = "BaseThemeActivity";
        try {
            Class<?> clazz = ContextThemeWrapper.class;
            Method method = clazz.getMethod("getThemeResId");
            method.setAccessible(true);
            themeResId = (Integer) method.invoke(this);
        } catch (NoSuchMethodException e) {
            Log.e(TAG, ".getThemeID Failed to get theme resource ID", e);
        } catch (IllegalAccessException e) {
            Log.e(TAG, ".getThemeID Failed to get theme resource ID", e);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, ".getThemeID Failed to get theme resource ID", e);
        } catch (InvocationTargetException e) {
            Log.e(TAG, ".getThemeID Failed to get theme resource ID", e);
        }
        return themeResId;
    }
}
