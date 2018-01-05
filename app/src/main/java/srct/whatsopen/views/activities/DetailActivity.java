package srct.whatsopen.views.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.transition.Slide;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import srct.whatsopen.MyApplication;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.NotificationSettings;
import srct.whatsopen.model.Schedule;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.views.FacilityView;
import srct.whatsopen.presenters.FacilityPresenter;
import srct.whatsopen.views.fragments.NotificationDialogFragment;


public class DetailActivity extends AppCompatActivity implements FacilityView,
        NotificationDialogFragment.NotificationDialogListener {

    @BindView(R.id.open_status)
    TextView openStatusTextView;
    @BindView(R.id.open_duration)
    TextView openDurationTextView;
    @BindView(R.id.location_text)
    TextView locationTextView;
    @BindView(R.id.schedule_text)
    TextView scheduleTextView;
    @BindView(R.id.special_schedule_duration_text)
    TextView specialScheduleDurationTextView;
    @BindView(R.id.notification_button)
    Button notificationButton;

    private MenuItem mFavoriteMenuItem;
    private FacilityPresenter mPresenter;
    private Facility mFacility;
    private boolean inEditMode;
    private Realm mRealm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        setUpAnimations();

        // Get Realm instance
        mRealm = Realm.getDefaultInstance();

        getFacility(getIntent().getStringExtra("name"));

        // Set up Presenter
        mPresenter = new FacilityPresenter();
        mPresenter.attachView(this);

        // Set up layout
        ButterKnife.bind(this);
        configureToolbar();
        fillTextViews();

        setNotificationStatus();
    }

    private void setUpAnimations() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.BOTTOM);
            slide.setDuration(300);
            getWindow().setEnterTransition(slide);
        }
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
        mRealm.close();
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
        switch (item.getItemId()) {
            case android.R.id.home:
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
                return true;
            case R.id.action_favorite:
                mPresenter.toggleFavorite(mFacility);
                return true;
            case R.id.action_settings:
                expandSettingsActivity();
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
        if (isFavorited)
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
        else
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);
    }

    @OnClick(R.id.notification_button)
    public void showNotificationDialog() {
        FragmentManager fm = getSupportFragmentManager();
        NotificationDialogFragment notificationDialogFragment =
                NotificationDialogFragment.newInstance(mFacility.getName(), inEditMode);
        notificationDialogFragment.show(fm, "fragment_notification_dialog");
    }

    // Queries Realm for the facility matching the key
    private void getFacility(String key) {
        mFacility = mRealm.where(Facility.class).equalTo("name", key).findFirst();
    }

    // Configures the toolbar title, actions, etc
    private void configureToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(mFacility.getName());

        // Set shared content name for transitions if Api >= 21
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getTextViewTitle(toolbar).setTransitionName("facility_name");
        }

        // Display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public static TextView getTextViewTitle(Toolbar toolbar) {
        TextView textViewTitle = null;
        for(int i = 0; i<toolbar.getChildCount(); i++) {
            View view = toolbar.getChildAt(i);
            if(view instanceof TextView) {
                textViewTitle = (TextView) view;
                break;
            }
        }
        return textViewTitle;
    }

    // Display the content to the text views
    private void fillTextViews() {
        String statusText = mFacility.isOpen() ? "Open" : "Closed";
        openStatusTextView.setText(statusText);

        openDurationTextView.setText(mFacility.getStatusDuration());

        locationTextView.setText(mFacility.getLocation().getBuilding());

        Calendar now = Calendar.getInstance();
        Schedule currentSchedule = mPresenter.getActiveSchedule(mFacility, now);
        scheduleTextView.setText(Html.fromHtml(mPresenter.getScheduleText(currentSchedule, now)));

        // Show the SpecialSchedule duration if necessary
        if(currentSchedule instanceof SpecialSchedule) {
            specialScheduleDurationTextView.setVisibility(View.VISIBLE);
            String endDate = FacilityPresenter.parseYMDtoMDY(currentSchedule.getValidEnd());
            specialScheduleDurationTextView.setText("Lasts until " + endDate);
        } else {
            specialScheduleDurationTextView.setVisibility(View.GONE);
        }
    }

    // Sets the notification button text to edit if a Notification exists
    private void setNotificationStatus() {
        NotificationSettings notificationSettings = mRealm.where(NotificationSettings.class)
                .equalTo("name", mFacility.getName()).findFirst();

        if (notificationSettings != null) {
            inEditMode = true;
            notificationButton.setText("Edit Notifications");
        } else {
            inEditMode = false;
            notificationButton.setText("Set Notifications");
        }
    }

    // Allows the NotificationDialog to refresh the view on dismiss
    @Override
    public void onSetNotification() {
        setNotificationStatus();
    }

    // Opens the About page for the app
    private void expandSettingsActivity() {
        Intent i = new Intent(this, SettingsActivity.class);
        startActivity(i);
    }
}
