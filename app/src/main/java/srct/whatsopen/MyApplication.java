package srct.whatsopen;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set up LeakCanary for memory leak detection
        if(LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        // Default Realm config. Might want to change this later
        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder().build();
        Realm.deleteRealm(realmConfig);
        Realm.setDefaultConfiguration(realmConfig);
    }
}
