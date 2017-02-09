package srct.whatsopen.util;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.WakefulBroadcastReceiver;

import srct.whatsopen.R;


public class NotificationService extends IntentService {

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int id = intent.getIntExtra("id", 0);

        displayNotification(title, text, id);
        setVibration();

        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void displayNotification(String title, String text, int id) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_access_time_black_24dp)
                        .setContentTitle(title)
                        .setContentText(text);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, builder.build());
    }

    private void setVibration() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean vibrationsOff = preferences.getBoolean("turn_off_vibrations_preference", false);

        if(!vibrationsOff) {
            Vibrator v = (Vibrator) this.getApplicationContext()
                    .getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(400);
        }
    }
}
