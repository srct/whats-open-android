package srct.whatsopen.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.realm.Realm;
import io.realm.RealmResults;
import srct.whatsopen.model.NotificationSettings;
import srct.whatsopen.presenters.NotificationPresenter;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Resets Notifications on boot
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Realm realm = Realm.getDefaultInstance();
            RealmResults<NotificationSettings> results = realm.where(NotificationSettings.class)
                    .findAll();

            for(NotificationSettings n : results) {
                if(n != null) {
                    NotificationPresenter.createAlarmsForFacility(context, n);
                }
            }

            realm.close();
        }
    }
}
