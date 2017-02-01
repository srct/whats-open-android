package srct.whatsopen.views.adapters;


import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.views.FacilityView;
import srct.whatsopen.views.activities.DetailActivity;
import srct.whatsopen.presenters.FacilityPresenter;

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

            // show the duration that the facility will be open
            setItemPaddingInDp(holder.textLayout, 8);
            holder.durationTextView.setVisibility(View.VISIBLE);
            //holder.nameTextView.setTypeface(null, Typeface.BOLD);
        } else {
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(context, R.color.facilityClosed));

            setItemPaddingInDp(holder.textLayout, 15);
            holder.durationTextView.setVisibility(View.GONE);
            //holder.nameTextView.setTypeface(null, Typeface.NORMAL);
        }

        if(facility.isFavorited()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_on_24dp);
        }
        else {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_off_24dp);
        }

        holder.setData(facility);
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    // Helper method to set the facility item layout's padding
    // Have to convert from pixels to dp
    private void setItemPaddingInDp(LinearLayout layout, int paddingPx) {
        float scale = context.getResources().getDisplayMetrics().density;
        int paddingTop = (int) (paddingPx*scale + 0.5f);
        int paddingBottom = (int) (paddingPx*scale + 0.5f);

        layout.setPadding(0, paddingTop, 0, paddingBottom);
    }

    // Set up for the Recycler View cells
    public class ViewHolder extends RecyclerView.ViewHolder implements FacilityView {

        @BindView(R.id.facility_name) TextView nameTextView;
        @BindView(R.id.favorite_button) ImageButton favoriteButton;
        @BindView(R.id.facility_duration) TextView durationTextView;
        @BindView(R.id.text_layout) LinearLayout textLayout;

        private FacilityPresenter mPresenter;
        private Facility data;
        private String duration;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setData(Facility facility) {
            data = facility;

            // Set up presenter
            mPresenter = new FacilityPresenter();
            mPresenter.attachView(this);

            duration = mPresenter.getStatusDuration(data, Calendar.getInstance());
            durationTextView.setText(duration);
        }

        // transitions to the facility's detail view
        @OnClick(R.id.text_layout)
        public void expandFacilityView() {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("name", data.getName());

            context.startActivity(intent);
        }

        // toggles favorite status
        @OnClick(R.id.favorite_button)
        public void setFavorite() {
            mPresenter.toggleFavorite(data);
        }

        @Override
        public void changeFavoriteIcon(boolean isFavorited) {
            if (isFavorited)
                favoriteButton.setImageResource(R.drawable.ic_fav_button_off_24dp);
            else
                favoriteButton.setImageResource(R.drawable.ic_fav_button_on_24dp);
        }

        @Override
        public Context getContext() {
            return context;
        }
    }
}
