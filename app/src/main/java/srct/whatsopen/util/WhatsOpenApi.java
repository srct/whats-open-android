package srct.whatsopen.util;

import java.util.List;

import retrofit2.http.GET;

import rx.Observable;
import srct.whatsopen.model.Facility;

// Interface for Retrofit's Http request
public interface WhatsOpenApi {

    @GET("facilities")
    Observable<List<Facility>> facilityList();
}
