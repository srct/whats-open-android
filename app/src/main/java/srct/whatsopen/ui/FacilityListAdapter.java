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

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;

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

        holder.data = facility;
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.facility_name) TextView nameTextView;
        @BindView(R.id.favorite_button) ImageButton favoriteButton;

        public Facility data;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

