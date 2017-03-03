package srct.whatsopen.views.adapters;


import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.realm.Case;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.views.FacilityView;
import srct.whatsopen.views.activities.DetailActivity;
import srct.whatsopen.presenters.FacilityPresenter;


public class FacilityListAdapter extends
        RealmRecyclerViewAdapter<Facility, FacilityListAdapter.ViewHolder> implements
        Filterable {

    private String mMode;
    private Realm mRealm;
    private Context mContext;
    private Activity mActivity; // this is pretty much just for transition animations

    // Mode describes the filtering for the elements to be displayed
    public FacilityListAdapter(Context context, OrderedRealmCollection<Facility> data,
                               String mode, Realm realm, Activity activity) {

        super(data, true);

        mContext = context;
        mMode = mode;
        mRealm = realm;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View facilityView = inflater.inflate(R.layout.item_facility, parent, false);

        return new ViewHolder(facilityView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Facility facility = getData().get(position);

        displayStatusDurationText(facility, holder);

        // highlight the open facilities
        if (facility.isOpen()) {
            // set the RV cell to be highlighted
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.facilityOpen));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.facilityClosed));
        }

        if (facility.isFavorited()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_on_24dp);
        } else {
            holder.favoriteButton.setImageResource(R.drawable.ic_fav_button_off_24dp);
        }

        holder.setData(facility);
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    // Sets the duration text according to the user's settings
    private void displayStatusDurationText(Facility facility, ViewHolder holder) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String setting = preferences.getString("list_view_information_preference",
                "display_duration_both");

        switch (setting) {
            case "display_duration_open":
                setStatusDurationText(holder, facility.isOpen());
                break;
            case "display_duration_closed":
                setStatusDurationText(holder, !facility.isOpen());
                break;
            case "display_duration_none":
                setStatusDurationText(holder, false);
                break;
            case "display_duration_both":
            default:
                setStatusDurationText(holder, true);
                break;
        }

    }

    private void setStatusDurationText(ViewHolder holder, boolean showDuration) {
        if (showDuration) {
            // display the duration text
            setItemPaddingInDp(holder.textLayout, 8);
            holder.durationTextView.setVisibility(View.VISIBLE);
        } else {
            setItemPaddingInDp(holder.textLayout, 15);
            holder.durationTextView.setVisibility(View.GONE);
        }
    }

    // Helper method to set the facility item layout's padding
    // Have to convert from pixels to dp
    private void setItemPaddingInDp(LinearLayout layout, int paddingPx) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        int paddingTop = (int) (paddingPx * scale + 0.5f);
        int paddingBottom = (int) (paddingPx * scale + 0.5f);

        layout.setPadding(0, paddingTop, 0, paddingBottom);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                return new FilterResults();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults filterResults) {
                resetData();

                if(constraint != null && getData() != null) {
                    RealmResults<Facility> results = getData().where()
                            .contains("mName", constraint.toString(), Case.INSENSITIVE)
                            .findAll();
                    updateData(results);
                }
            }
        };
    }

    // Reloads the data between searches
    private void resetData() {
        RealmResults<Facility> results;
        switch(mMode) {
            case "All":default:
                results = mRealm.where(Facility.class)
                        .findAllSorted("isOpen", Sort.DESCENDING);
                break;
            case "Favorites":
                results = mRealm.where(Facility.class)
                        .equalTo("isFavorited", true)
                        .findAllSorted("isOpen", Sort.DESCENDING);
                break;
            case "Open":
                results = mRealm.where(Facility.class)
                        .equalTo("isOpen", true)
                        .findAllSorted("isOpen", Sort.DESCENDING);
                break;
            case "Closed":
                results = mRealm.where(Facility.class)
                        .equalTo("isOpen", false)
                        .findAllSorted("isOpen", Sort.DESCENDING);
                break;
        }
        updateData(results);
    }

    // Set up for the Recycler View cells
    public class ViewHolder extends RecyclerView.ViewHolder implements FacilityView {

        @BindView(R.id.facility_name)
        TextView nameTextView;
        @BindView(R.id.favorite_button)
        ImageButton favoriteButton;
        @BindView(R.id.facility_duration)
        TextView durationTextView;
        @BindView(R.id.text_layout)
        LinearLayout textLayout;

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

            duration = data.getStatusDuration();
            durationTextView.setText(duration);
        }

        // transitions to the facility's detail view
        @OnClick(R.id.text_layout)
        public void expandFacilityView() {
            Intent intent = new Intent(mContext, DetailActivity.class);
            intent.putExtra("name", data.getName());

            mContext.startActivity(intent);
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
            return mContext;
        }
    }
}
