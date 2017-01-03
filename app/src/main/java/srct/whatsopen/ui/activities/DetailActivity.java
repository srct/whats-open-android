package srct.whatsopen.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;


public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.facility_name) TextView nameTextView;

    private Facility mFacility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ButterKnife.bind(this);
        setUpViews(getIntent().getStringExtra("name"));
    }

    private void setUpViews(String key) {
        // Get facility from Realm
        Realm realm = Realm.getDefaultInstance();
        mFacility = realm.where(Facility.class).equalTo("mName", key).findFirst();
        realm.close();

        nameTextView.setText(mFacility.getName());
    }
}
