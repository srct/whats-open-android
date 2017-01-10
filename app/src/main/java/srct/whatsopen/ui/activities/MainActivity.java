package srct.whatsopen.ui.activities;

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

import srct.whatsopen.R;
import srct.whatsopen.ui.MainView;
import srct.whatsopen.ui.presenters.MainPresenter;
import srct.whatsopen.ui.adapters.FacilityListFragmentPagerAdapter;

public class MainActivity extends AppCompatActivity implements MainView {

    @BindView(R.id.progress_bar) CircularProgressBar mProgressBar;
    @BindView(R.id.list_view) LinearLayout mListView;

    private MainPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        // Set up presenter
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);

        // Get facility data
        mPresenter.loadFacilities();

        // Configure toolbar
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ViewPager and set its PagerAdapter
        ViewPager viewPager = ButterKnife.findById(this, R.id.view_pager);
        viewPager.setAdapter(new FacilityListFragmentPagerAdapter(getSupportFragmentManager()));

        // Now give the TabStrip the ViewPager
        PagerSlidingTabStrip tabStrip = ButterKnife.findById(this, R.id.tabs);
        tabStrip.setTabPaddingLeftRight(0);
        tabStrip.setViewPager(viewPager);

        viewPager.setCurrentItem(1);
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
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    // Opens the About page for the app
    private void expandAboutActivity() {
        Intent i = new Intent(this, AboutActivity.class);
        startActivity(i);
    }
}
