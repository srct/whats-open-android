package srct.whatsopen.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.ui.adapters.FacilityListAdapter;


public class DetailActivity extends AppCompatActivity {

    @BindView(R.id.facility_name) TextView nameTextView;

    MenuItem mFavoriteMenuItem;

    private Facility mFacility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getFacility(getIntent().getStringExtra("name"));

        // Set up layout
        ButterKnife.bind(this);
        configureToolbar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        mFavoriteMenuItem = menu.findItem(R.id.miFavorite);

        if(mFacility.isFavorited())
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
        else
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.miFavorite:
                toggleFavoriteStatus();
                return true;
            case R.id.miOptions:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Updates the UI and Realm data for mFacility
    private void toggleFavoriteStatus() {
        if(mFacility.isFavorited()) {
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_white_24dp);
            FacilityListAdapter.toggleFavoriteAsync(this, mFacility, false);
        }
        else {
            mFavoriteMenuItem.setIcon(R.drawable.ic_fav_button_on_24dp);
            FacilityListAdapter.toggleFavoriteAsync(this, mFacility, true);
        }
    }

    // Queries Realm for the facility matching the key
    private void getFacility(String key) {
        Realm realm = Realm.getDefaultInstance();
        mFacility = realm.where(Facility.class).equalTo("mName", key).findFirst();
        realm.close();
    }

    // Configures the toolbar title, actions, etc
    private void configureToolbar() {
        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(mFacility.getName());

        // Display back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
