package srct.whatsopen.ui;


import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmAsyncTask;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.model.OpenTimes;

/**
 * Basic RecyclerView boilerplate, with some added Realm stuff
 * Also contains a bit of logic for figuring out What's Open
 */

public class FacilityListAdapter extends
        RealmRecyclerViewAdapter<Facility, FacilityListAdapter.ViewHolder> {

    Context mContext;

    public FacilityListAdapter(Context context,
                               OrderedRealmCollection<Facility> data) {

        super(context, data, true);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View facilityView = inflater.inflate(R.layout.item_facility, parent, false);

        ViewHolder viewHolder = new ViewHolder(facilityView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Facility facility = getData().get(position);

        RealmList<OpenTimes> openTimesList = facility.getMainSchedule().getOpenTimesList();
        boolean isOpen = getOpenStatus(openTimesList);

        if(isOpen) {
            // set the RV cell to be highlighted
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.facilityOpen));
        } else {
            holder.itemView.setBackgroundColor(ContextCompat
                    .getColor(mContext, R.color.facilityClosed));
        }

        if(facility.isFavorited()) {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_black_24dp);
        }
        else {
            holder.favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp);
        }

        holder.data = facility;
        TextView textView = holder.nameTextView;
        textView.setText(facility.getName());
    }

    // Uses the device time to determine which facilities should be open
    private boolean getOpenStatus(RealmList<OpenTimes> openTimesList) {
        Calendar now = Calendar.getInstance();

        // have to mess with the current day value, as Calender.DAY_OF_WEEK
        // starts with Saturday as 1 and the Whats Open Api starts with Monday
        // at 0, for some reason.
        int currentDay = (5 + now.get(Calendar.DAY_OF_WEEK)) % 7;
        RealmResults<OpenTimes> results = openTimesList.where()
                .beginGroup()
                    .equalTo("startDay", currentDay)
                    .or()
                    .equalTo("endDay", currentDay)
                .endGroup()
                .findAll();

        if(results.size() == 0)
            return false;

        OpenTimes result = results.first();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        try {
            Date startTime = sdf.parse(result.getStartTime());
            Date endTime = sdf.parse(result.getEndTime());
            // have to parse it from date to string to date. how fun
            Date currentTime = sdf.parse(sdf.format(now.getTime()));

            if(currentTime.compareTo(startTime) > 0 && currentTime.compareTo(endTime) < 0)
                return true;
            else
                return false;
        } catch (ParseException pe) {
            return false;
        }
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

        // toggles favorite status
        @OnClick(R.id.favorite_button)
        public void setFavorite(ImageButton favoriteButton) {

            if(data.isFavorited()) {
                favoriteButton.setImageResource(R.drawable.ic_star_border_black_24dp);
                toggleFavoriteAsync(false);
            }
            else {
                favoriteButton.setImageResource(R.drawable.ic_star_black_24dp);
                toggleFavoriteAsync(true);
            }
        }

        // Asynchronously updates the favorite status
        // Would block the favorite button redrawing otherwise
        void toggleFavoriteAsync(final boolean status) {
            Realm realm = Realm.getDefaultInstance();

            final String facilityName = data.getName();
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm bgRealm) {
                    // have to requery for the object as it was created on a separate thread
                    Facility facility = bgRealm.where(Facility.class)
                            .equalTo("mName", facilityName).findFirst();

                    facility.setFavorited(status);
                }
            }, null, null);

            realm.close();
        }
    }
}

