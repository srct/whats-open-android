package srct.whatsopen.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

import butterknife.ButterKnife;

import io.realm.Realm;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import srct.whatsopen.R;
import srct.whatsopen.service.WhatsOpenClient;
import srct.whatsopen.service.WhatsOpenService;
import srct.whatsopen.model.Facility;

public class MainActivity extends AppCompatActivity {

    private Realm mRealm;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get Realm singleton
        mRealm = Realm.getDefaultInstance();

        // Get WhatsOpenClient singleton
        WhatsOpenService service = WhatsOpenClient.getInstance();
        callWhatsOpenAPI(service);

        // Set up view
        mRecyclerView = ButterKnife.findById(this, R.id.rvFacilities);
        setUpRecyclerView();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    // Handles set up for the Recycler View
    private void setUpRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new FacilityListAdapter(this,
                mRealm.where(Facility.class).findAllAsync()));

        // Speeds things up for static lists
        mRecyclerView.setHasFixedSize(true);

        // Adds dividers between items
        mRecyclerView.addItemDecoration(new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }

    // Gets a Call from the given Retrofit service, then asynchronously executes it
    // On success, copies the resulting facility list to the Realm DB
    private void callWhatsOpenAPI(WhatsOpenService service) {
        Call<List<Facility>> call = service.facilityList();
        call.enqueue(new Callback<List<Facility>>() {
            @Override
            public void onResponse(Call<List<Facility>> call, Response<List<Facility>> response) {
                List<Facility> facilities = response.body();
                mRealm.beginTransaction();
                mRealm.copyToRealmOrUpdate(facilities);
                mRealm.commitTransaction();
            }

            @Override
            public void onFailure(Call<List<Facility>> call, Throwable t) {
                // do some stuff
            }
        });
    }
}

