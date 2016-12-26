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

        mRealm = Realm.getDefaultInstance();

        WhatsOpenService service = WhatsOpenClient.getInstance();
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

        mRecyclerView = ButterKnife.findById(this, R.id.rvFacilities);
        setUpRecyclerView();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    private void setUpRecyclerView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new FacilityListAdapter(this,
                mRealm.where(Facility.class).findAllAsync()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
    }
}

