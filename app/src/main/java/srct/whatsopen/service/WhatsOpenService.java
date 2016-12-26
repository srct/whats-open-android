package srct.whatsopen.service;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

import srct.whatsopen.model.Facility;

public interface WhatsOpenService {

    @GET("schedules")
    Call<List<Facility>> facilityList();
}
