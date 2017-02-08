package srct.whatsopen.views.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.astuetz.PagerSlidingTabStrip;

import butterknife.BindView;
import butterknife.ButterKnife;

import fr.castorflex.android.circularprogressbar.CircularProgressBar;

import io.realm.Realm;
import srct.whatsopen.MyApplication;
import srct.whatsopen.R;
import srct.whatsopen.views.MainView;
import srct.whatsopen.presenters.MainPresenter;
import srct.whatsopen.views.adapters.FacilityListFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity implements MainView {

    @BindView(R.id.progress_bar) CircularProgressBar mProgressBar;
    @BindView(R.id.view_pager) ViewPager mViewPager;

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

        // Get the ViewPager and set its PagerAdapter
        mViewPager.setAdapter(new FacilityListFragmentPagerAdapter(getSupportFragmentManager()));

        // Now give the TabStrip the ViewPager
        PagerSlidingTabStrip tabStrip = ButterKnife.findById(this, R.id.tabs);
        tabStrip.setTabPaddingLeftRight(0);
        tabStrip.setViewPager(mViewPager);

        mViewPager.setCurrentItem(1);
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
        switch(item.getItemId()) {
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
