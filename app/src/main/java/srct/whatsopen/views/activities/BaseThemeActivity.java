package srct.whatsopen.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import srct.whatsopen.R;

public abstract class BaseThemeActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle bundle) {
        setTheme(R.style.darkTheme);
        super.onCreate(bundle);
    }
}
