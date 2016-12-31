package srct.whatsopen.ui;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.Sort;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;


public class FacilityListFragment extends android.support.v4.app.Fragment {
    public static final String ARG_MODE = "ARG_MODE";

    private String mMode;
    private Realm mRealm;
    private RecyclerView mRecyclerView;

    public static FacilityListFragment newInstance(String mode) {
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        FacilityListFragment fragment = new FacilityListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMode = getArguments().getString(ARG_MODE);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facility, container, false);
        mRecyclerView = ButterKnife.findById(view, R.id.rvFacilities);
        setUpRecyclerView(view);

        return view;
    }

    // Handles set up for the Recycler View
    private void setUpRecyclerView(View view) {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        switch(mMode) {
            case "All":
                mRecyclerView.setAdapter(new FacilityListAdapter(view.getContext(),
                        mRealm.where(Facility.class)
                              .findAllSortedAsync("isOpen", Sort.DESCENDING)));
                break;
            case "Favorites":
                mRecyclerView.setAdapter(new FacilityListAdapter(view.getContext(),
                        mRealm.where(Facility.class)
                              .equalTo("isFavorited", true)
                              .findAllSortedAsync("isOpen", Sort.DESCENDING)));
                break;
            case "Open":
                mRecyclerView.setAdapter(new FacilityListAdapter(view.getContext(),
                        mRealm.where(Facility.class)
                              .equalTo("isOpen", true)
                              .findAllSortedAsync("isOpen", Sort.DESCENDING)));
                break;
            case "Closed":
                mRecyclerView.setAdapter(new FacilityListAdapter(view.getContext(),
                        mRealm.where(Facility.class)
                              .equalTo("isOpen", false)
                              .findAllSortedAsync("isOpen", Sort.DESCENDING)));
                break;
            default:
                mRecyclerView.setAdapter(new FacilityListAdapter(view.getContext(),
                        mRealm.where(Facility.class)
                              .findAllSortedAsync("isOpen", Sort.DESCENDING)));
        }

        // Speeds things up for static lists
        mRecyclerView.setHasFixedSize(true);

        // Adds dividers between items
        Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));
    }
}
