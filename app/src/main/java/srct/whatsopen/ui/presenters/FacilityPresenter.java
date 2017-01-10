package srct.whatsopen.ui.presenters;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.ui.FacilityView;

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

        // Get Realm instance and SharedPreferences
        Realm realm = Realm.getDefaultInstance();
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(mFacilityView.getContext());

        final SharedPreferences.Editor editor = pref.edit();

        final String facilityName = facility.getName();
        realm.executeTransactionAsync(bgRealm -> {
            // have to requery for the object as it was created on a separate thread
            Facility f = bgRealm.where(Facility.class)
                    .equalTo("mName", facilityName).findFirst();

            f.setFavorited(status);
            editor.putBoolean(facilityName, status);
            editor.apply();
        }, null, null);

        realm.close();
    }

    // Finds the next time the facility closes or opens and returns it
    public String getStatusDuration(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();

        if(openTimesList.size() == 0)
            return "No open time on schedule";

        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        String durationMessage;

        if(facility.isOpen()) {
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

        int nextDay = findNextDay(openTimesList, currentDay);
        String nextDayStr = parseIntToDay(nextDay);

        String openingTime = openTimesList.get(nextDay).getStartTime();
        openingTime = parseTo12HourTime(openingTime);

        durationMessage = "Opens on " + nextDayStr + " at " + openingTime;

        return durationMessage;
    }

    // Returns the next open day in the list of OpenTimes
    private int findNextDay(RealmList<OpenTimes> openTimesList, int current) {
        int nextDay = openTimesList.first().getStartDay();
        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() > current)
                nextDay = o.getStartDay();
        }

        return nextDay;
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
    public String getSchedule(Facility facility) {
        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();

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
