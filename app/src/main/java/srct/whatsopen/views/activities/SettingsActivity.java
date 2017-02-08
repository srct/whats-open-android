package srct.whatsopen.views.activities;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.ButterKnife;
import srct.whatsopen.MyApplication;
import srct.whatsopen.R;
import srct.whatsopen.views.fragments.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        configureToolbar();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_settings, new SettingsFragment())
                .commit();

        setPreferenceChangeListener();

        MyApplication.setRotation(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void configureToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Settings");

        // Display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setPreferenceChangeListener() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences.OnSharedPreferenceChangeListener prefListener =
                new SharedPreferences.OnSharedPreferenceChangeListener() {
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        if(key.equals("turn_off_rotation_preference")) {
                            MyApplication.setRotation(SettingsActivity.this);
                        }
                    }
                };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(prefListener);
    }
}

