package srct.whatsopen.util;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


public class NotificationReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NotificationService.class);

        i.putExtra("title", intent.getStringExtra("title"));
        i.putExtra("text", intent.getStringExtra("text"));

        //context.startService(i);
        startWakefulService(context, i);
    }
}
