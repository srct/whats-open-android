package srct.whatsopen.service;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import io.realm.RealmList;
import io.realm.RealmObject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import srct.whatsopen.util.OpenTimesDeserializer;
import srct.whatsopen.util.OpenTimesListDeserializer;
import srct.whatsopen.model.OpenTimes;

/**
 * Singleton for Retrofit instance
  */
public class WhatsOpenClient {

    private static volatile Retrofit sRetrofit = null;
    private static WhatsOpenService mService;

    private static final String BASE_URL = "https://whatsopen.gmu.edu/api/";

    public WhatsOpenClient() {
    }

    // Returns a singleton of WhatsOpenService
    public static WhatsOpenService getInstance() {
        if(mService == null) {
            synchronized (WhatsOpenClient.class) {
                if(mService == null) {
                    mService = getRetrofit().create(WhatsOpenService.class);
                }
            }
        }
        return mService;
    }

    // Configures Retrofit's JSON parsing logic
    private synchronized static Retrofit getRetrofit() {
        if(sRetrofit == null) {
            synchronized(WhatsOpenClient.class) {
                if(sRetrofit == null) {
                    Gson gson = new GsonBuilder()
                            // Ensures Retrofit plays nicely with Realm
                            .setExclusionStrategies(new ExclusionStrategy() {
                                @Override
                                public boolean shouldSkipField(FieldAttributes f) {
                                    return f.getDeclaredClass().equals(RealmObject.class);
                                }

                                @Override
                                public boolean shouldSkipClass(Class<?> clazz) {
                                    return false;
                                }
                            })
                            .registerTypeAdapter(OpenTimes.class, new OpenTimesDeserializer())
                            .registerTypeAdapter(new TypeToken<RealmList<OpenTimes>>(){}.getType(),
                                    new OpenTimesListDeserializer())
                            .create();
                    sRetrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return sRetrofit;
    }

}
