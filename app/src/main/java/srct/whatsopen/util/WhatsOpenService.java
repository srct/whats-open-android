package srct.whatsopen.util;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton for Retrofit instance
  */
public class WhatsOpenService {

    private static volatile Retrofit sRetrofit = null;
    private static WhatsOpenApi mService;

    private static final String BASE_URL = "https://api.srct.gmu.edu/whatsopen/v2/";

    public WhatsOpenService() {
    }

    // Returns a singleton of WhatsOpenApi
    public static WhatsOpenApi getInstance() {
        if(mService == null) {
            synchronized (WhatsOpenService.class) {
                if(mService == null) {
                    mService = getRetrofit().create(WhatsOpenApi.class);
                }
            }
        }
        return mService;
    }

    // Configures Retrofit's JSON parsing logic
    private synchronized static Retrofit getRetrofit() {
        if(sRetrofit == null) {
            synchronized(WhatsOpenService.class) {
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
                            .create();
                    sRetrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return sRetrofit;
    }

}
