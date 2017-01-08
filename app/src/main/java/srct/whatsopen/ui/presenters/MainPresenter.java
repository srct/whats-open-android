package srct.whatsopen.ui.presenters;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.service.WhatsOpenClient;
import srct.whatsopen.service.WhatsOpenService;
import srct.whatsopen.ui.activities.MainActivity;

public class MainPresenter {

    private MainActivity mainView;

    public void attachView(MainActivity view) {
        this.mainView = view;
    }

    public void detachView() {
        this.mainView = null;
    }

    // Gets a Call from the given Retrofit service, then asynchronously executes it
    // On success, copies the resulting facility list to the Realm DB
    public void loadFacilities() {
        // Get WhatsOpenClient singleton
        WhatsOpenService service = WhatsOpenClient.getInstance();

        // Get Realm and SharedPreference instances
        final Realm realm = Realm.getDefaultInstance();
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mainView);

        Call<List<Facility>> call = service.facilityList();

        call.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                List<Facility> facilities = response.body();

                for(Facility facility : facilities) {
                    // Query SharedReferences for each Facility's favorite status. defaults to false
                    facility.setFavorited(pref.getBoolean(facility.getName(), false));
                    facility.setOpen(getOpenStatus(facility, Calendar.getInstance()));
                }

                realm.beginTransaction();
                realm.copyToRealmOrUpdate(facilities);
                realm.commitTransaction();

                realm.close();
            }

            @Override
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                // do some stuff
                realm.close();
            }
        });
    }

    // Uses the device time to determine which facilities should be open
    public boolean getOpenStatus(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();

        // have to mess with the current day value, as Calender.DAY_OF_WEEK
        // starts with Sunday as 1 and the Whats Open Api starts with Monday at 0
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
            pe.printStackTrace();
            return false;
        }
    }
}
