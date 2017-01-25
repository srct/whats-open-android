package srct.whatsopen.presenters;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmList;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.NotificationSettings;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.util.NotificationReceiver;
import srct.whatsopen.views.NotificationView;

public class NotificationPresenter {

    private NotificationView mNotificationView;
    private Set<String> mNotificationSettings;
    private SharedPreferences pref;

    public void attachView(NotificationView view) {
        mNotificationView = view;
        pref = PreferenceManager
                .getDefaultSharedPreferences(mNotificationView.getContext());
    }

    public void detachView() {
        mNotificationView = null;
    }

    // Gets the notification settings from SharedPreferences, parses them to booleans,
    // and passes them to the View
    public void presentNotifications(String name) {
        NotificationSettings n = getNotificationsFromPreferences(name);
        mNotificationView.setNotificationChecks(n);
    }

    private NotificationSettings getNotificationsFromPreferences(String name) {
        mNotificationSettings = pref.getStringSet(name+"NotificationSettings", null);

        NotificationSettings n = new NotificationSettings();
        if(mNotificationSettings != null) {
            for (String s : mNotificationSettings) {
                switch (s) {
                    case "opening":       n.opening       = true;   break;
                    case "closing":       n.closing       = true;   break;
                    case "interval_on":   n.interval_on   = true;   break;
                    case "interval_15":   n.interval_15   = true;   break;
                    case "interval_30":   n.interval_30   = true;   break;
                    case "interval_hour": n.interval_hour = true;   break;
                }
            }
        }

        return n;
    }

    // Saves the notification settings to SharedPreferences and schedules the Notifications
    public void saveNotifications(String name, boolean inEditMode, NotificationSettings n) {

        if(inEditMode) {
            editNotifications(name, n);
        } else {
            setNotifications("Notifications set", name, n);
        }
    }

    // If no checkboxes are set, removes Notifications. Else, sets new Notifications
    private void editNotifications(String name, NotificationSettings n) {

        if(!n.opening && !n.closing && !n.interval_on && !n.interval_15 && !n.interval_30 &&
                !n.interval_hour) {
            removeNotifications(name, true);
        } else {
            removeNotifications(name, false);
            setNotifications("Notifications edited", name, n);
        }

    }

    // Removes the Notification settings from SharedPreferences
    public void removeNotifications(String name, boolean dismiss) {
        // Remove the set Notifications
        NotificationSettings n = getNotificationsFromPreferences(name);
        deleteNotificationsForFacility(name, n);

        // Remove the NotificationSettings
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(name + "NotificationSettings", null);
        editor.apply();

        if(dismiss) {
            Toast.makeText(mNotificationView.getContext(), "Notifications removed",
                    Toast.LENGTH_SHORT).show();

            mNotificationView.dismiss();
        }
    }

    // Saves the Notification settings to SharedPreferences
    private void setNotifications(String message, String name, NotificationSettings n) {

        // if the Notifications are valid (i.e. one of each category is checked)
        if((n.opening || n.closing) &&
                (n.interval_on || n.interval_15 || n.interval_30 || n.interval_hour)) {

            Set<String> set = setFromNotificationSettings(n);

            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet(name + "NotificationSettings", set);
            editor.apply();

            // TODO: make async
            createAlarmsForFacility(name, n);

            Toast.makeText(mNotificationView.getContext(), message, Toast.LENGTH_SHORT).show();

            mNotificationView.dismiss();
        } else {
            // Show error message
            Toast.makeText(mNotificationView.getContext(),
                    "Invalid settings. One of each category must be selected.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Returns a String set parsed from the given booleans
    private Set<String> setFromNotificationSettings(NotificationSettings n) {

        Set<String> set = new HashSet<>(6);
        if(n.opening) set.add("opening");
        if(n.closing) set.add("closing");
        if(n.interval_on) set.add("interval_on");
        if(n.interval_15) set.add("interval_15");
        if(n.interval_30) set.add("interval_30");
        if(n.interval_hour) set.add("interval_hour");

        return set;
    }

    // Sets Alarms for the Facility with the given name
    private void createAlarmsForFacility(String name, NotificationSettings notificationSettings) {
        Realm realm = Realm.getDefaultInstance();
        Facility facility = realm.where(Facility.class).equalTo("mName", name).findFirst();
        RealmList<OpenTimes> openTimesList = getActiveSchedule(facility, Calendar.getInstance());

        if(openTimesList == null || openTimesList.size() == 0)
            return;

        for(OpenTimes o : openTimesList) {
            setAlarmsFromOpenTimes(name, o, notificationSettings);
        }

        realm.close();
    }

    private void setAlarmsFromOpenTimes(String name, OpenTimes openTimes,
                          NotificationSettings n) {

        for(int i = openTimes.getStartDay(); i <= openTimes.getEndDay(); i++) {
            // Parse Python day of week int to Calendar day of week
            int day = ((i+1)%7)+1;

            if(n.opening) {
                if(n.interval_on)
                    setAlarm(name, day, "Op_on", 0, openTimes.getStartTime(),
                            "Opens now");
                if(n.interval_15)
                    setAlarm(name, day, "Op_15", 15, openTimes.getStartTime(),
                            "Opens in 15 minutes");
                if(n.interval_30)
                    setAlarm(name, day, "Op_30", 30, openTimes.getStartTime(),
                            "Opens in 30 minutes");
                if(n.interval_hour)
                    setAlarm(name, day, "Op_hour", 60, openTimes.getStartTime(),
                            "Opens in an hour");
            }

            if(n.closing) {
                if(n.interval_on)
                    setAlarm(name, day, "Cl_on", 0, openTimes.getEndTime(),
                            "Closes now");
                if(n.interval_15)
                    setAlarm(name, day, "Cl_15", 15, openTimes.getEndTime(),
                            "Closes in 15 minutes");
                if(n.interval_30)
                    setAlarm(name, day, "Cl_30", 30, openTimes.getEndTime(),
                            "Closes in 30 minutes");
                if(n.interval_hour)
                    setAlarm(name, day, "Cl_hour", 60, openTimes.getEndTime(),
                            "Closes in an hour");
            }
        }
    }

    private void setAlarm(String name, int day, String type, int intervalMin, String time,
                          String message) {

        Long alarmTime = parseTimeString(time, day, timeIsPassed(time, day));

        int interval = intervalMin * 60000; // parse minutes to ms
        alarmTime = alarmTime - interval;

        int id = (name+type+day).hashCode(); // unique id for editing the Notification later
        Log.i("Set hash for " + name + day + type, ""+id);

        // Construct an Intent to execute the NotificationReceiver
        Intent intent = new Intent(mNotificationView.getContext(), NotificationReceiver.class);
        intent.putExtra("title", name);
        intent.putExtra("text", message);

        // Create a PendingIntent that will be triggered when the alarm goes off
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mNotificationView.getContext(),
                id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up alarm to repeat every week
        AlarmManager alarm = (AlarmManager)
                mNotificationView.getContext().getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 7*1440*60000, pendingIntent);
    }

    private void deleteNotificationsForFacility(String name, NotificationSettings n) {
        for(int i = 1; i <= 7; i++) {
            if(n.opening) {
                if(n.interval_on)
                    deleteNotification(name, i, "Op_on");
                if(n.interval_15)
                    deleteNotification(name, i, "Op_15");
                if(n.interval_30)
                    deleteNotification(name, i, "Op_30");
                if(n.interval_hour)
                    deleteNotification(name, i, "Op_hour");
            }

            if(n.closing) {
                if(n.interval_on)
                    deleteNotification(name, i, "Cl_on");
                if(n.interval_15)
                    deleteNotification(name, i, "Cl_15");
                if(n.interval_30)
                    deleteNotification(name, i, "Cl_30");
                if(n.interval_hour)
                    deleteNotification(name, i, "Cl_hour");
            }
        }
    }

    private void deleteNotification(String name, int day, String type) {
        int id = (name+type+day).hashCode(); // unique id for editing the Notification later
        Log.i("Delete hash for " + name + day + type, ""+id);

        // Get the Intent matching the existing id
        Intent intent = new Intent(mNotificationView.getContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mNotificationView.getContext(),
                id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager)
                mNotificationView.getContext().getSystemService(Context.ALARM_SERVICE);

        alarm.cancel(pendingIntent);
    }

    private Long parseTimeString(String timeString, int day, boolean thisWeek) {
        Calendar alarmCalendar = Calendar.getInstance();
        int month = alarmCalendar.get(Calendar.MONTH);
        int dayOfMonth = alarmCalendar.get(Calendar.DAY_OF_MONTH);
        int year = alarmCalendar.get(Calendar.YEAR);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        try {
            // Set the new time
            Date time = sdf.parse(timeString);
            alarmCalendar.setTime(time);

            // Set the current day, as setTime() changes the date to epoch
            alarmCalendar.set(year, month, dayOfMonth);

            // to make sure that the alarm isn't set in the past
            if(!thisWeek)
                alarmCalendar.add(Calendar.DATE, 1);

            // set the day to the next day matching the given day of the week
            while(alarmCalendar.get(Calendar.DAY_OF_WEEK) != day) {
                alarmCalendar.add(Calendar.DATE, 1);
            }

            return alarmCalendar.getTimeInMillis();
        } catch (ParseException pe) {
            pe.printStackTrace();
            return Long.valueOf(0);
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

    // Determines if the given time is earlier in the current day
    // TODO: does not seem to work
    private boolean timeIsPassed(String time, int day) {
        Calendar now = Calendar.getInstance();

        if(now.get(Calendar.DAY_OF_WEEK) != day)
            return false;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            Date timeDate = sdf.parse(time);
            return now.getTime().compareTo(timeDate) > 0;
        } catch (ParseException pe) {
            pe.printStackTrace();
            return false;
        }
    }
}
