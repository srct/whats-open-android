package srct.whatsopen.presenters;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.HashSet;
import java.util.Set;

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
        mNotificationSettings = pref.getStringSet(name+"NotificationSettings", null);

        boolean opening = false, closing = false, interval_on = false, interval_15 = false,
                interval_30 = false, interval_hour = false;
        if(mNotificationSettings != null) {
            for (String s : mNotificationSettings) {
                switch (s) {
                    case "opening":
                        opening = true;
                        break;
                    case "closing":
                        closing = true;
                        break;
                    case "interval_on":
                        interval_on = true;
                        break;
                    case "interval_15":
                        interval_15 = true;
                        break;
                    case "interval_30":
                        interval_30 = true;
                        break;
                    case "interval_hour":
                        interval_hour = true;
                        break;
                }
            }
        }

        mNotificationView.setNotificationChecks(opening, closing, interval_on,
                interval_15, interval_30, interval_hour);
    }

    // Saves the notification settings to SharedPreferences and schedules the Notifications
    public void saveNotifications(String name, boolean inEditMode, boolean opening, boolean closing,
                                  boolean interval_on, boolean interval_15,
                                  boolean interval_30, boolean interval_hour) {

        if(inEditMode) {
            editNotifications(name, opening, closing, interval_on, interval_15, interval_30,
                    interval_hour);
        } else {
            setNotifications("Notifications set.", name, opening, closing, interval_on,
                    interval_15, interval_30, interval_hour);
        }
    }

    // If no checkboxes are set, removes Notifications. Else, sets new Notifications
    private void editNotifications(String name, boolean opening, boolean closing,
                                   boolean interval_on, boolean interval_15,
                                   boolean interval_30, boolean interval_hour) {

        if(!opening && !closing && !interval_on && !interval_15 && !interval_30 && !interval_hour) {
            removeNotifications(name);
        } else {
            setNotifications("Notifications edited.", name, opening, closing, interval_on,
                    interval_15, interval_30, interval_hour);
        }

    }

    // Removes the Notification settings from SharedPreferences
    private void removeNotifications(String name) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(name + "NotificationSettings", null);
        editor.apply();

        Toast.makeText(mNotificationView.getContext(),
                "Notifications removed.", Toast.LENGTH_SHORT).show();

        mNotificationView.dismiss();
    }

    // Saves the Notification settings to SharedPreferences
    private void setNotifications(String message, String name, boolean opening, boolean closing,
                                  boolean interval_on, boolean interval_15,
                                  boolean interval_30, boolean interval_hour) {

        // if the Notifications are valid (i.e. one of each category is checked)
        if((opening || closing) && (interval_on || interval_15 || interval_30 || interval_hour)) {

            Set<String> set = setFromBooleans(opening, closing, interval_on, interval_15,
                    interval_30, interval_hour);

            SharedPreferences.Editor editor = pref.edit();
            editor.putStringSet(name + "NotificationSettings", set);
            editor.apply();

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
    private Set<String> setFromBooleans(boolean opening, boolean closing,
                                  boolean interval_on, boolean interval_15,
                                  boolean interval_30, boolean interval_hour) {

        Set<String> set = new HashSet<>(6);
        if(opening) set.add("opening");
        if(closing) set.add("closing");
        if(interval_on) set.add("interval_on");
        if(interval_15) set.add("interval_15");
        if(interval_30) set.add("interval_30");
        if(interval_hour) set.add("interval_hour");

        return set;
    }
}
