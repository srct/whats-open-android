package srct.whatsopen.ui.adapters;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.ui.activities.DetailActivity;

/**
 * Basic RecyclerView boilerplate, with some added Realm stuff
 */

public class FacilityListAdapter extends
        RealmRecyclerViewAdapter<Facility, FacilityListAdapter.ViewHolder> {

    public FacilityListAdapter(Context context,
                               OrderedRealmCollection<Facility> data) {

        super(context, data, true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View facilityView = inflater.inflate(R.layout.item_facility, parent, false);

        ViewHolder viewHolder = new ViewHolder(facilityView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Facility facility = getData().get(position);

        if(facility.isOpen()) {
            // set the RV cell to be highlighted
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.facilityOpen));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.facilityClosed));
        }

        if(facility.isFavorited()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_on_24dp);
        }
        else {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_off_24dp);
        }

        holder.data = facility;
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    // Asynchronously updates the Realm object's favorite status
    // and updates the favorite status in SharedPreferences
    // Would block the favorite button redrawing if done on the UI thread
    public static void toggleFavoriteAsync(Context context, Facility facility,
                                           final boolean status) {

        Realm realm = Realm.getDefaultInstance();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = pref.edit();

        final String facilityName = facility.getName();
        realm.executeTransactionAsync(new Realm.Transaction() {
                                  @Override
                                  public void execute(Realm bgRealm) {
                // have to requery for the object as it was created on a separate thread
                Facility facility = bgRealm.where(Facility.class)
                        .equalTo("mName", facilityName).findFirst();

                facility.setFavorited(status);
                editor.putBoolean(facilityName, status);
                editor.apply();
            }
        }, null, null);

        realm.close();
    }

    // Set up for the Recycler View cells
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.facility_name)
        TextView nameTextView;
        @BindView(R.id.favorite_button)
        ImageButton favoriteButton;

        public Facility data;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        // transitions to the facility's detail view
        @OnClick(R.id.text_layout)
        public void expandFacilityView() {
            Intent i = new Intent(context, DetailActivity.class);
            i.putExtra("name", data.getName());

            context.startActivity(i);
        }

        // toggles favorite status
        @OnClick(R.id.favorite_button)
        public void setFavorite(ImageButton favoriteButton) {
            if (data.isFavorited()) {
                favoriteButton.setImageResource(R.drawable.ic_fav_button_off_24dp);
                toggleFavoriteAsync(context, data, false);
            } else {
                favoriteButton.setImageResource(R.drawable.ic_fav_button_on_24dp);
                toggleFavoriteAsync(context, data, true);
            }
        }
    }
}

