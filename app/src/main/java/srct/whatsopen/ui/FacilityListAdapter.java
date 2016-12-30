package srct.whatsopen.ui;


import android.content.Context;
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
import io.realm.RealmList;
import io.realm.RealmRecyclerViewAdapter;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;

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

        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();
        holder.data = facility;
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    // Set up for the Recycler View cells
    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.facility_name) TextView nameTextView;
        @BindView(R.id.favorite_button) ImageButton favoriteButton;

        public Facility data;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.favorite_button)
        public void setFavorite(ImageButton favoriteButton) {
            // this works very weirdly when the view is scrolled down. not sure why
            //favoriteButton.setImageResource(R.drawable.ic_star_black_24dp);
        }
    }
}

