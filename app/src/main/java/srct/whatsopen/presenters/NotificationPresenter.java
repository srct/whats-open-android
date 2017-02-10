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
import io.realm.RealmResults;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.NotificationSettings;
import srct.whatsopen.model.OpenTimes;
import srct.whatsopen.model.SpecialSchedule;
import srct.whatsopen.util.NotificationReceiver;
import srct.whatsopen.views.NotificationView;

public class NotificationPresenter {

    private NotificationView mNotificationView;

    public void attachView(NotificationView view) {
        mNotificationView = view;
    }

    public void detachView() {
        mNotificationView = null;
    }

    // Gets the notification settings from SharedPreferences, parses them to booleans,
    // and passes them to the View
    public void presentNotifications(String name) {
        Realm realm = Realm.getDefaultInstance();
        NotificationSettings n = realm.where(NotificationSettings.class).equalTo("name", name)
                .findFirst();
        realm.close();

        if(n != null) {
            mNotificationView.setNotificationChecks(n);
        }
    }

    // Saves the notification settings to SharedPreferences and schedules the Notifications
    public void saveNotifications(boolean inEditMode, NotificationSettings n) {

        if(inEditMode) {
            editNotifications(n);
        } else {
            setNotifications("Notifications set", n);
        }
    }

    // If no checkboxes are set, removes Notifications. Else, sets new Notifications
    private void editNotifications(NotificationSettings n) {

        if(!n.opening && !n.closing && !n.interval_on && !n.interval_15 && !n.interval_30 &&
                !n.interval_hour) {
            removeNotifications(n.name, true);
        } else {
            removeNotifications(n.name, false);
            setNotifications("Notifications edited", n);
        }

    }

    // Removes the Notification settings from SharedPreferences
    public void removeNotifications(String name, boolean dismiss) {
        // Remove the set Notifications
        deleteNotificationsForFacility(name);

        Realm realm = Realm.getDefaultInstance();
        realm.executeTransactionAsync((bgRealm) -> {
            NotificationSettings results = bgRealm.where(NotificationSettings.class)
                    .equalTo("name", name)
                    .findFirst();

            if(results != null) {
                results.deleteFromRealm();
            }
        }, () -> { // on success
            if(dismiss) {
                Toast.makeText(mNotificationView.getContext(), "Notifications removed",
                        Toast.LENGTH_SHORT).show();

                mNotificationView.dismiss();
            }
        });

        realm.close();
    }

    // Saves the Notification settings to SharedPreferences
    private void setNotifications(String message, NotificationSettings n) {

        // if the Notifications are valid (i.e. one of each category is checked)
        if((n.opening || n.closing) &&
                (n.interval_on || n.interval_15 || n.interval_30 || n.interval_hour)) {

            Realm realm = Realm.getDefaultInstance();
            realm.executeTransactionAsync(
                    bgRealm -> bgRealm.copyToRealmOrUpdate(n),
                    () -> {
                        // TODO: make async
                        createAlarmsForFacility(mNotificationView.getContext(), n);

                        Toast.makeText(mNotificationView.getContext(),
                                message, Toast.LENGTH_SHORT).show();

                        mNotificationView.dismiss();
                    });
            realm.close();
        } else {
            // Show error message
            Toast.makeText(mNotificationView.getContext(),
                    "Invalid settings. One of each category must be selected.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // Sets Alarms for the Facility with the given name
    public static void createAlarmsForFacility(Context context,
                                               NotificationSettings notificationSettings) {
        Realm realm = Realm.getDefaultInstance();
        Facility facility = realm.where(Facility.class)
                .equalTo("mName", notificationSettings.name).findFirst();
        RealmList<OpenTimes> openTimesList = getActiveSchedule(facility, Calendar.getInstance());

        if(openTimesList == null || openTimesList.size() == 0)
            return;

        for(OpenTimes o : openTimesList) {
            setAlarmsFromOpenTimes(context, o, notificationSettings);
        }

        realm.close();
    }

    private static void setAlarmsFromOpenTimes(Context context, OpenTimes openTimes,
                          NotificationSettings n) {
        String name = n.name;

        for(int i = openTimes.getStartDay(); i <= openTimes.getEndDay(); i++) {
            // Parse Python day of week int to Calendar day of week
            int day = ((i+1)%7)+1;

            if(n.opening) {
                if(n.interval_on)
                    setAlarm(context, name, day, "Op_on", 0, openTimes.getStartTime(),
                            "Opens now");
                if(n.interval_15)
                    setAlarm(context, name, day, "Op_15", 15, openTimes.getStartTime(),
                            "Opens in 15 minutes");
                if(n.interval_30)
                    setAlarm(context, name, day, "Op_30", 30, openTimes.getStartTime(),
                            "Opens in 30 minutes");
                if(n.interval_hour)
                    setAlarm(context, name, day, "Op_hour", 60, openTimes.getStartTime(),
                            "Opens in an hour");
            }

            if(n.closing) {
                if(n.interval_on)
                    setAlarm(context, name, day, "Cl_on", 0, openTimes.getEndTime(),
                            "Closes now");
                if(n.interval_15)
                    setAlarm(context, name, day, "Cl_15", 15, openTimes.getEndTime(),
                            "Closes in 15 minutes");
                if(n.interval_30)
                    setAlarm(context, name, day, "Cl_30", 30, openTimes.getEndTime(),
                            "Closes in 30 minutes");
                if(n.interval_hour)
                    setAlarm(context, name, day, "Cl_hour", 60, openTimes.getEndTime(),
                            "Closes in an hour");
            }
        }
    }

    private static void setAlarm(Context context, String name, int day, String type,
                                 int intervalMin, String time, String message) {

        // NOTE: this doesn't set notifications based on the assumption that
        // facilities shouldn't open or close at those times, thus those must indicate
        // that the facility is open 24/7 or open past midnight
        // As of writing this (2/9/17) there is no better way (due to the way the Api is written)
        // If you're reading this and there now is an approach, please fix this
        if(time.equals("00:00:00") || time.equals("23:59:59"))
            return;

        Long alarmTime = parseTimeStringToMs(time, day, Calendar.getInstance());

        int interval = intervalMin * 60000; // parse minutes to ms
        alarmTime = alarmTime - interval;

        int id = (name+type+day).hashCode(); // unique id for editing the Notification later
        Log.i("Set hash for " + name + day + type, ""+id);

        // Construct an Intent to execute the NotificationReceiver
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", name);
        intent.putExtra("text", message);

        // Create a PendingIntent that will be triggered when the alarm goes off
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Set up alarm to repeat every week
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, 7*1440*60000, pendingIntent);
    }

    private void deleteNotificationsForFacility(String name) {
        Realm realm = Realm.getDefaultInstance();
        NotificationSettings n = realm.where(NotificationSettings.class).equalTo("name", name)
                .findFirst();
        realm.close();

        if(n == null)
            return;

        for(int i = 1; i <= 7; i++) {
            if(n.opening) {
                if(n.interval_on)
                    cancelNotificationAlarms(name, i, "Op_on");
                if(n.interval_15)
                    cancelNotificationAlarms(name, i, "Op_15");
                if(n.interval_30)
                    cancelNotificationAlarms(name, i, "Op_30");
                if(n.interval_hour)
                    cancelNotificationAlarms(name, i, "Op_hour");
            }

            if(n.closing) {
                if(n.interval_on)
                    cancelNotificationAlarms(name, i, "Cl_on");
                if(n.interval_15)
                    cancelNotificationAlarms(name, i, "Cl_15");
                if(n.interval_30)
                    cancelNotificationAlarms(name, i, "Cl_30");
                if(n.interval_hour)
                    cancelNotificationAlarms(name, i, "Cl_hour");
            }
        }
    }

    private void cancelNotificationAlarms(String name, int day, String type) {
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

    // Returns the time in ms from epoch for the given time on the next Day
    // of the week relative to the given Calendar
    private static Long parseTimeStringToMs(String timeString, int day, Calendar alarmCalendar) {

        // Determine if the time is in the past
        boolean hasPassed = timeHasPassed(timeString, day, alarmCalendar);

        // Save the current date
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
            if(hasPassed)
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
    private static RealmList<OpenTimes> getActiveSchedule(Facility facility, Calendar now) {
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
    public static boolean timeHasPassed(String time, int day, Calendar now) {
        if(now.get(Calendar.DAY_OF_WEEK) != day)
            return false;

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        try {
            Date timeDate = sdf.parse(time);
            Date currentDate = sdf.parse(sdf.format(now.getTime()));

            return currentDate.compareTo(timeDate) > 0;
        } catch (ParseException pe) {
            pe.printStackTrace();
            return false;
        }
    }
}
