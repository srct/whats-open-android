package srct.whatsopen.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.ui.FacilityView;
import srct.whatsopen.ui.presenters.FacilityPresenter;


public class DetailActivity extends AppCompatActivity implements FacilityView{

    @BindView(R.id.open_status) TextView openStatusTextView;
    @BindView(R.id.open_duration) TextView openDurationTextView;
    @BindView(R.id.location_text) TextView locationTextView;
    @BindView(R.id.schedule_text) TextView scheduleTextView;

    MenuItem mFavoriteMenuItem;

    private FacilityPresenter mPresenter;
    private Facility mFacility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getFacility(getIntent().getStringExtra("name"));

        // Set up Presenter
        mPresenter = new FacilityPresenter();
        mPresenter.attachView(this, mFacility);

        // Set up layout
        ButterKnife.bind(this);
        configureToolbar();
        fillTextViews();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        mFavoriteMenuItem = menu.findItem(R.id.action_favorite);

        changeFavoriteIcon(mFacility.isFavorited());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_favorite:
                mPresenter.toggleFavorite(mFacility);
                return true;
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void changeFavoriteIcon(boolean isFavorited) {
        if(isFavorited)
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
        else
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);
    }

    // Queries Realm for the facility matching the key
    private void getFacility(String key) {
        Realm realm = Realm.getDefaultInstance();
        mFacility = realm.where(Facility.class).equalTo("mName", key).findFirst();
        realm.close();
    }

    // Configures the toolbar title, actions, etc
    private void configureToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(mFacility.getName());

        // Display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // Display the content to the text views
    private void fillTextViews() {
        String statusText = mFacility.isOpen() ? "Open" : "Closed";
        openStatusTextView.setText(statusText);

        openDurationTextView.setText(mPresenter.getStatusDuration(mFacility,
                Calendar.getInstance()));

        locationTextView.setText(mFacility.getLocation());

        scheduleTextView.setText(Html.fromHtml(mPresenter.getSchedule(mFacility)));
    }
}
