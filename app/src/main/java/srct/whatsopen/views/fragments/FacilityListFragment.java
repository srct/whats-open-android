package srct.whatsopen.views.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.Sort;
import srct.whatsopen.R;
import srct.whatsopen.model.Facility;
import srct.whatsopen.presenters.MainPresenter;
import srct.whatsopen.views.MainView;
import srct.whatsopen.views.decorations.DividerItemDecoration;
import srct.whatsopen.views.adapters.FacilityListAdapter;


public class FacilityListFragment extends android.support.v4.app.Fragment implements MainView {
    public static final String ARG_MODE = "ARG_MODE";

    private String mMode;
    private Realm mRealm;
    private MainPresenter mPresenter;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeContainer;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

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
        mPresenter = new MainPresenter();
        mPresenter.attachView(this);

        setPreferenceChangeListener();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        mRealm.close();

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_facility_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRecyclerView = ButterKnife.findById(view, R.id.rv_facilities);
        mSwipeContainer = ButterKnife.findById(view, R.id.swipe_container);
        setUpRecyclerView(view);

        mSwipeContainer.setOnRefreshListener(() -> mPresenter.loadFacilities());
        mSwipeContainer.setColorSchemeColors(getResources().getColor(R.color.colorPrimaryDark));
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

    @Override
    public void showProgressBar() {
        // shouldn't do anything
    }

    @Override
    public void dismissProgressBar() {
        mSwipeContainer.setRefreshing(false);
    }

    @Override
    public Context getContext() {
        return getActivity();
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    // Redraws RecyclerView if the settings have changed for it
    private void setPreferenceChangeListener() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(key.equals("list_view_information_preference")) {
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        };

        preferences.registerOnSharedPreferenceChangeListener(mListener);
    }
}
