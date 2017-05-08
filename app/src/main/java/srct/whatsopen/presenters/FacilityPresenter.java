package srct.whatsopen.presenters;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Path;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.Schedule;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.views.FacilityView;

public class FacilityPresenter {

    private FacilityView mFacilityView;

    public void attachView(FacilityView view) {
        mFacilityView = view;
    }

    public void detachView() {
        mFacilityView = null;
    }

    // Asynchronously updates the Realm object's favorite status
    // and updates the favorite status in SharedPreferences
    public void toggleFavorite(Facility facility) {
        final boolean status = !facility.isFavorited();

        mFacilityView.changeFavoriteIcon(status);

        Context context = mFacilityView.getContext();
        Resources res = context.getResources();
        int formatName = status ? R.string.toast_set_favorite : R.string.toast_unset_favorite;
        String msg = String.format(res.getString(formatName), facility.getName());
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();

        // Get Realm instance and SharedPreferences
        Realm realm = Realm.getDefaultInstance();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mFacilityView.getContext());

        final SharedPreferences.Editor editor = pref.edit();

        final String facilityName = facility.getName();
        realm.executeTransactionAsync(bgRealm -> {
            // have to re-query for the object as it was created on a separate thread
            Facility f = bgRealm.where(Facility.class)
                    .equalTo("mName", facilityName).findFirst();

            f.setFavorited(status);
            editor.putBoolean(facilityName+"FavoriteStatus", status);
            editor.apply();
        }, null, null);

        realm.close();
    }



    // Parses the schedule into an HTML string.
    // Kind of a hacky approach. That being said, this is certainly a lot simpler to test
    // than the alternative.
    public String getScheduleText(Schedule schedule, Calendar now) {
        RealmList<OpenTimes> openTimesList = schedule.getOpenTimesList();
        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;

        if(openTimesList.size() == 0)
           return "No schedule available";

        if(facilityDoesNotClose(openTimesList))
            return "This facility is always open";

        StringBuilder scheduleString = new StringBuilder();
        boolean first = true;
        for(OpenTimes o : openTimesList) {
            if(first)
                first = false;
            else
                scheduleString.append("<br/>");

            // the current day's schedule should be highlighted
            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                scheduleString.append("<strong>");

            scheduleString.append("<b>");
            scheduleString.append(parseIntToDay(o.getStartDay()));
            scheduleString.append("</b>: ");
            scheduleString.append(parseTo12HourTime(o.getStartTime()));
            scheduleString.append(" - ");
            scheduleString.append(parseTo12HourTime(o.getEndTime()));

            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                scheduleString.append("</strong>");
        }

        return scheduleString.toString();
    }

    public static boolean facilityDoesNotClose(List<OpenTimes> openTimesList) {
        boolean doesNotClose = false;

        int counter = 0;
        for(OpenTimes o : openTimesList) {
            if(o.getStartTime().equals("00:00:00") && o.getEndTime().equals("23:59:59")
                    || o.getEndTime().equals("00:00:00")) {
                doesNotClose = true;

                counter++;
            }
        }

        if(counter != openTimesList.size()) {
            doesNotClose = false;
        }

        return doesNotClose;
    }

    // Parses 24 hour formatted time String to 12 hour formatted time String
    public static String parseTo12HourTime(String time) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            final Date date = sdf.parse(time);
            return new SimpleDateFormat("h:mm a").format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return "";
        }
    }

    // Parses String with format YYYY-MM-DD to MM/DD
    public static String parseYMDtoMDY(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            final Date date = sdf.parse(time);
            return new SimpleDateFormat("MM/dd/yy").format(date);
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }
    }

    // Parses an integer to a String of the day of the week
    public static String parseIntToDay(int day) {
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

    // Returns the active schedule given the current date
    public Schedule getActiveSchedule(Facility facility, Calendar now) {
        Schedule schedule = facility.getMainSchedule();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date currentDate = now.getTime();

            for(SpecialSchedule s : facility.getSpecialSchedules()) {
                Date startDate = sdf.parse(s.getValidStart());
                Date endDate = sdf.parse(s.getValidEnd());

                if(currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
                    return s;
                }
            }
        } catch (ParseException pe) {
            pe.printStackTrace();
            return null;
        }

        return schedule;
    }
}
