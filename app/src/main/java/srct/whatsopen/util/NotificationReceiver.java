package srct.whatsopen.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, NotificationService.class);

        i.putExtra("title", intent.getStringExtra("title"));
        i.putExtra("text", intent.getStringExtra("text"));

        context.startService(i);
    }
}
