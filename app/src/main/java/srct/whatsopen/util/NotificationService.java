package srct.whatsopen.util;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_access_time_black_24dp)
                .setContentTitle(title)
                .setContentText(text);

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(id, builder.build());
    }
}
