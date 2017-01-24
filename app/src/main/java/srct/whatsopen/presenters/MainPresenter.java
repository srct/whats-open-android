package srct.whatsopen.presenters;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;


import io.realm.RealmResults;
import rx.Observable;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.util.WhatsOpenService;
import srct.whatsopen.util.WhatsOpenApi;
import srct.whatsopen.views.MainView;

public class MainPresenter {

    private MainView mMainView;
    private SharedPreferences pref;

    public void attachView(MainView view) {
        this.mMainView = view;
        pref = PreferenceManager.getDefaultSharedPreferences(mMainView.getContext());
    }

    public void detachView() {
        this.mMainView = null;
    }

    // Gets a Call from the given Retrofit service, then asynchronously executes it
    // On success, copies the resulting facility list to the Realm DB
    public void loadFacilities() {
        mMainView.showProgressBar();

        // Get WhatsOpenApi singleton
        WhatsOpenApi service = WhatsOpenService.getInstance();

        Observable<List<Facility>> call = service.facilityList();
        call.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(facilities1 -> setStatus(facilities1))
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Facility>>() {
                    @Override
                    public void onCompleted() {
                        if(mMainView != null)
                            mMainView.dismissProgressBar();
                    }
                    @Override
                    public void onError(Throwable e) {
                        updateOpenStatus();
                        if(mMainView != null)
                            mMainView.dismissProgressBar();
                        Toast.makeText(mMainView.getContext(), "Error getting data; " +
                                "schedules may be out of date.", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onNext(List<Facility> facilities) {
                        writeToRealm(facilities);
                    }
                });
    }

    // Sets the favorite and open status of each Facility
    private List<Facility> setStatus(List<Facility> facilities) {

        for(Facility facility : facilities) {
            // Query SharedReferences for each Facility's favorite status. defaults to false
            facility.setFavorited(pref.getBoolean(facility.getName()+"FavoriteStatus", false));
            facility.setOpen(getOpenStatus(facility, Calendar.getInstance()));
        }

        return facilities;
    }

    // Asynchronously writes the facility list to Realm
    private void writeToRealm(List<Facility> facilities) {
        final Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(bgRealm -> bgRealm.copyToRealmOrUpdate(facilities));
        realm.close();

        removeDeletedFacilities(facilities);
    }

    // Removes the facilities from Realm that are no longer in the Api
    private void removeDeletedFacilities(List<Facility> facilities) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Facility> results = realm.where(Facility.class).findAll();

        // Not a pretty way to do this, but Facilities shouldn't ever have too many items anyway
        for(Facility r : results) {
            boolean deleted = true;
            for(Facility f : facilities) {
                if( r.getName().equals(f.getName()) ) {
                    deleted = false;
                }
            }

            if(deleted) {
                removeFacilityFromRealm(r);
            }
        }

        realm.close();
    }

    // Removes the given Facility from Realm
    private void removeFacilityFromRealm(Facility facility) {
        Realm realm = Realm.getDefaultInstance();

        final String name = facility.getName();
        realm.executeTransactionAsync((bgRealm) -> {
            RealmResults<Facility> results = realm.where(Facility.class).equalTo("mName", name)
                    .findAll();

            results.deleteAllFromRealm();
        });
    }

    // Sets the open status of each facility in the Realm instance
    private void updateOpenStatus() {
        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync(bgRealm -> {
            List<Facility> facilities = bgRealm.where(Facility.class).findAll();
            for(Facility f : facilities) {
                f.setOpen(getOpenStatus(f, Calendar.getInstance()));
            }
        }, null, null);
        realm.close();
    }

    // Uses the device time to determine which facilities should be open
    public boolean getOpenStatus(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = getActiveSchedule(facility, now);

        // have to mess with the current day value, as Calender.DAY_OF_WEEK
        // starts with Sunday as 1 and the Whats Open Api starts with Monday at 0
        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        OpenTimes currentOpenTimes = null;

        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
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

    // Returns the active schedule given the current date
    private RealmList<OpenTimes> getActiveSchedule(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date currentDate = now.getTime();

            for(SpecialSchedule s : facility.getSpecialSchedules()) {
                Date startDate = sdf.parse(s.getValidStart());
                Date endDate = sdf.parse(s.getValidEnd());

                if(currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                    openTimesList = s.getOpenTimesList();
                    return openTimesList;
                }
            }
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }

        return openTimesList;
    }
}
