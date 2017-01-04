package srct.whatsopen.ui.activities;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.ui.adapters.FacilityListAdapter;


public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.open_status) TextView openStatusTextView;
    @BindView(R.id.open_duration) TextView openDurationTextView;
    @BindView(R.id.location_text) TextView locationTextView;
    @BindView(R.id.schedule_text) TextView scheduleTextView;

    MenuItem mFavoriteMenuItem;

    private Facility mFacility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getFacility(getIntent().getStringExtra("name"));

        // Set up layout
        ButterKnife.bind(this);
        configureToolbar();
        fillTextViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        mFavoriteMenuItem = menu.findItem(R.id.miFavorite);

        if(mFacility.isFavorited())
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
        else
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.miFavorite:
                toggleFavoriteStatus();
                return true;
            case R.id.miOptions:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Updates the UI and Realm data for mFacility
    private void toggleFavoriteStatus() {
        if(mFacility.isFavorited()) {
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);
            FacilityListAdapter.toggleFavoriteAsync(this, mFacility, false);
        }
        else {
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
            FacilityListAdapter.toggleFavoriteAsync(this, mFacility, true);
        }
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

        openDurationTextView.setText(getStatusDuration());

        locationTextView.setText(mFacility.getLocation());

        scheduleTextView.setText(Html.fromHtml(getSchedule()));
    }

    // Finds the next time the facility closes or opens and returns it
    private String getStatusDuration() {
        Calendar now = Calendar.getInstance();
        RealmList<OpenTimes> openTimesList = mFacility.getMainSchedule().getOpenTimesList();

        if(openTimesList.size() == 0)
            return "No open time on schedule";

        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        String durationMessage;

        if(mFacility.isOpen()) {
            String closingTime = openTimesList.get(currentDay).getEndTime();
            closingTime = parseTo12HourTime(closingTime);
            durationMessage = "Closes at " + closingTime;

            return durationMessage;
        }

        // Check if the facility opens later today
        if(currentDay < openTimesList.size()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try {
                Date currentTime = sdf.parse(sdf.format(now.getTime()));
                Date startTime = sdf.parse(openTimesList.get(currentDay).getStartTime());

                if(currentTime.compareTo(startTime) < 0) {
                    String openingTime = openTimesList.get(currentDay).getStartTime();
                    openingTime = parseTo12HourTime(openingTime);

                    return "Opens today at " + openingTime;
                }
            } catch (ParseException pe) {
                pe.printStackTrace();
                return "";
            }
        }

        // Else return the opening time of the next day

        int nextDay = (currentDay + 1) % openTimesList.size();
        String nextDayStr = parseIntToDay(nextDay);

        String openingTime = openTimesList.get(nextDay).getStartTime();
        openingTime = parseTo12HourTime(openingTime);

        durationMessage = "Opens on " + nextDayStr + " at " + openingTime;

        return durationMessage;
    }

    // Parses 24 hour formatted time String to 12 hour formatted time String
    private String parseTo12HourTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            final Date date = sdf.parse(time);
            return new SimpleDateFormat("h:mm a").format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return "";
        }
    }

    // Parses an integer to a String of the day of the week
    private String parseIntToDay(int day) {
        switch(day) {
            case 0:
                return "Monday";
            case 1:
                return "Tuesday";
            case 2:
                return "Wednesday";
            case 3:
                return "Thursday";
            case 4:
                return "Friday";
            case 5:
                return "Saturday";
            case 6:
                return "Sunday";
            default:
                return "";
        }
    }

    // Parses the schedule into an HTML string
    private String getSchedule() {
        RealmList<OpenTimes> openTimesList = mFacility.getMainSchedule().getOpenTimesList();

        if(openTimesList.size() == 0)
           return "No schedule available";

        StringBuilder scheduleString = new StringBuilder();
        boolean first = true;
        for(OpenTimes o : openTimesList) {
            if(first)
                first = false;
            else
                scheduleString.append("<br/>");

            scheduleString.append("<b>" + parseIntToDay(o.getStartDay()) + "</b>: ");
            scheduleString.append(parseTo12HourTime(o.getStartTime()));
            scheduleString.append(" - ");
            scheduleString.append(parseTo12HourTime(o.getEndTime()));
        }

        return scheduleString.toString();
    }
}
