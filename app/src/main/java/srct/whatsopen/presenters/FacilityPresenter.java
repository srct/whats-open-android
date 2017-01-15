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

import io.realm.Realm;
import io.realm.RealmList;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;
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

        Context context = mFacilityView.getContext();
        Resources res = context.getResources();
        int formatName = status ? R.string.toast_set_favorite : R.string.toast_unset_favorite;
        String msg = String.format(res.getString(formatName), facility.getName());
        Toast toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        toast.show();

        mFacilityView.changeFavoriteIcon(status);

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

    // Finds the next time the facility closes or opens and returns it
    public String getStatusDuration(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = getActiveSchedule(facility, now);

        if(openTimesList.size() == 0)
            return "No open time on schedule";

        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        String durationMessage;

        if(facility.isOpen()) {
            if(facilityDoesNotClose(openTimesList.first())) {
                return "Open 24/7";
            }

            String closingTime = getCurrentEndTime(openTimesList, currentDay);
            closingTime = parseTo12HourTime(closingTime);
            durationMessage = "Closes at " + closingTime;

            return durationMessage;
        }

        // Check if the facility opens later today
        if(openTimesContains(openTimesList, currentDay)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            try {
                Date currentTime = sdf.parse(sdf.format(now.getTime()));
                Date startTime = sdf.parse(getCurrentStartTime(openTimesList, currentDay));

                if(currentTime.compareTo(startTime) < 0) {
                    String openingTime = getCurrentStartTime(openTimesList, currentDay);
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

        String openingTime = getCurrentStartTime(openTimesList, nextDay);
        openingTime = parseTo12HourTime(openingTime);

        durationMessage = "Opens next on " + nextDayStr + " at " + openingTime;

        return durationMessage;
    }

    private boolean openTimesContains(RealmList<OpenTimes> openTimesList, int currentDay) {
        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                return true;
        }
        return false;
    }

    // Returns the next open day in the list of OpenTimes
    private int findNextDay(RealmList<OpenTimes> openTimesList, int current) {
        int nextDay = openTimesList.first().getStartDay();
        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() > current) {
                nextDay = o.getStartDay();
                break;
            }
        }

        return nextDay;
    }

    // Returns the end time for the current day
    private String getCurrentEndTime(RealmList<OpenTimes> openTimesList, int currentDay) {
        String endTime = "";
        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                endTime = o.getEndTime();
        }
        return endTime;
    }

    // Returns the end time for the current day
    private String getCurrentStartTime(RealmList<OpenTimes> openTimesList, int currentDay) {
        String startTime = "";
        for(OpenTimes o : openTimesList) {
            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                startTime = o.getStartTime();
        }
        return startTime;
    }

    private boolean facilityDoesNotClose(OpenTimes openTimes) {
        return (openTimes.getStartDay() == 0 && openTimes.getEndDay() == 6 &&
                openTimes.getStartTime().equals("00:00:00") &&
                openTimes.getEndTime().equals("23:59:59"));
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
    // Kind of a hacky approach
    public String getSchedule(Facility facility, Calendar now) {
        RealmList<OpenTimes> openTimesList = getActiveSchedule(facility, now);
        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;

        if(openTimesList.size() == 0)
           return "No schedule available";

        if(facilityDoesNotClose(openTimesList.first()))
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

            scheduleString.append("<b>" + parseIntToDay(o.getStartDay()) + "</b>: ");
            scheduleString.append(parseTo12HourTime(o.getStartTime()));
            scheduleString.append(" - ");
            scheduleString.append(parseTo12HourTime(o.getEndTime()));

            if(o.getStartDay() <= currentDay && o.getEndDay() >= currentDay)
                scheduleString.append("</strong>");
        }

        return scheduleString.toString();
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
