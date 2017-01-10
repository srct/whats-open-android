package srct.whatsopen.service;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

import rx.Observable;
import srct.whatsopen.model.Facility;

// Interface for Retrofit's Http request
public interface WhatsOpenService {

    @GET("facilities")
    Observable<List<Facility>> facilityList();
}
