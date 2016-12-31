package srct.whatsopen.ui;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;

import io.realm.Realm;

import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import srct.whatsopen.R;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.service.WhatsOpenClient;
import srct.whatsopen.service.WhatsOpenService;
import srct.whatsopen.model.Facility;

public class MainActivity extends AppCompatActivity {

    private Realm mRealm;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Realm singleton
        mRealm = Realm.getDefaultInstance();

        // Get WhatsOpenClient singleton
        WhatsOpenService service = WhatsOpenClient.getInstance();
        callWhatsOpenAPI(service);

        // Get the ViewPager and set its PagerAdapter
        ViewPager viewPager = ButterKnife.findById(this, R.id.view_pager);
        viewPager.setAdapter(new FacilityListFragmentPagerAdapter(getSupportFragmentManager()));

        // Now give the TabStrip the ViewPager
        PagerSlidingTabStrip tabStrip = ButterKnife.findById(this, R.id.tabs);
        tabStrip.setViewPager(viewPager);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    // Gets a Call from the given Retrofit service, then asynchronously executes it
    // On success, copies the resulting facility list to the Realm DB
    private void callWhatsOpenAPI(WhatsOpenService service) {
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        Call<List<Facility>> call = service.facilityList();

        call.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                List<Facility> facilities = response.body();

                // Query SharedReferences for each Facility's favorite status. defaults to false
                for(Facility facility : facilities) {
                    facility.setOpen(getOpenStatus(facility));
                    facility.setFavorited(pref.getBoolean(facility.getName(), false));
                }

                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(facilities);
                mRealm.commitTransaction();
            }

            @Override
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                // do some stuff
            }
        });
    }

    // Uses the device time to determine which facilities should be open
    private boolean getOpenStatus(Facility facility) {
        Calendar now = Calendar.getInstance();
        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();

        // have to mess with the current day value, as Calender.DAY_OF_WEEK
        // starts with Saturday as 1 and the Whats Open Api starts with Monday
        // at 0, for some reason.
        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        OpenTimes currentOpenTimes = null;

        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() == currentDay || o.getEndDay() == currentDay)
                currentOpenTimes = o;
        }

        if(currentOpenTimes == null)
            return false;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        try {
            Date startTime = sdf.parse(currentOpenTimes.getStartTime());
            Date endTime = sdf.parse(currentOpenTimes.getEndTime());
            // have to parse it from date to string to date. how fun
            Date currentTime = sdf.parse(sdf.format(now.getTime()));

            if(currentTime.compareTo(startTime) > 0 && currentTime.compareTo(endTime) < 0)
                return true;
            else
                return false;
        } catch (ParseException pe) {
            return false;
        }
    }
}

