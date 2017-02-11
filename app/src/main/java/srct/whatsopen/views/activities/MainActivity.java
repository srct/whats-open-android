package srct.whatsopen.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.astuetz.PagerSlidingTabStrip;

import butterknife.BindView;
import butterknife.ButterKnife;
import srct.whatsopen.MyApplication;
import srct.whatsopen.R;
import srct.whatsopen.presenters.MainPresenter;
import srct.whatsopen.views.MainView;
import srct.whatsopen.views.adapters.FacilityListFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity implements MainView {

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind them views
        ButterKnife.bind(this);

        // Set up presenter
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);

        // Reload facility data
        mPresenter.loadFacilities();

        // Configure toolbar
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setLogo(R.drawable.wo_clock_toolbar);

        setUpTabStrip();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.setRotation(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mPresenter.loadFacilities();
                return true;
            case R.id.action_settings:
                expandSettingsActivity();
                return true;
            case R.id.action_about:
                expandAboutActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setUpTabStrip() {
        // Get the ViewPager and set its PagerAdapter
        mViewPager.setAdapter(new FacilityListFragmentPagerAdapter(getSupportFragmentManager()));

        // Now give the TabStrip the ViewPager
        PagerSlidingTabStrip tabStrip = ButterKnife.findById(this, R.id.tabs);
        tabStrip.setTabPaddingLeftRight(0);
        tabStrip.setViewPager(mViewPager);

        // Set the default tab to 'All'
        int tabNumber = getDefaultTabNumber();
        mViewPager.setCurrentItem(tabNumber);
    }

    private int getDefaultTabNumber() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String tabName = pref.getString("list_view_default_tab_preference", "default_tab_all");

        switch(tabName) {
            case "default_tab_favorites":
                return 0;
            case "default_tab_all":default:
                return 1;
            case "default_tab_open":
                return 2;
            case "default_tab_closed":
                return 3;
        }
    }

    @Override
    public void showProgressBar() {
        mViewPager.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mViewPager.setVisibility(View.VISIBLE);
    }

    @Override
    public Context getContext() {
        return this;
    }

    // Opens the About page for the app
    private void expandAboutActivity() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }

    // Opens the About page for the app
    private void expandSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
