package com.lchpatners.shadal.restaurant;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lchpatners.shadal.R;
import com.lchpatners.shadal.util.AnalyticsHelper;

/**
 * Created by YoungKim on 2015. 8. 25..
 */
public class RestaurantListFragment extends Fragment {
    ListView listView;

    ImageView onlyFlyer;
    ImageView onlyOpen;

    private RestaurantListAdapter mAdapter;
    private Activity mActivity;
    private boolean isCheckedOfficeHour = false;
    private boolean isCheckedHasFlyer = false;
    private String orderBy;

    View.OnClickListener checkListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.check_is_open || view.getId() == R.id.tv_open) {
                isCheckedOfficeHour = !isCheckedOfficeHour;

                AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                analyticsHelper.sendEvent("UX", "filter_by_office_hours", "");
            } else if (view.getId() == R.id.check_has_flyer || view.getId() == R.id.tv_flyer) {
                isCheckedHasFlyer = !isCheckedHasFlyer;

                AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
                analyticsHelper.sendEvent("UX", "filter_by_flyer", "");
            }

            if (isCheckedOfficeHour && isCheckedHasFlyer) {
                RestaurantController.officeHour = true;
                orderBy = RestaurantController.LIST_FLYER_OFFICE;
                mAdapter.loadData(RestaurantController.LIST_FLYER_OFFICE);
                onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_selected);
                onlyFlyer.setImageResource(R.drawable.icon_list_bar_check_box_selected);
            } else if (isCheckedOfficeHour) {
                RestaurantController.officeHour = true;
                orderBy = RestaurantController.LIST_OFFICE_HOUR;
                mAdapter.loadData(RestaurantController.LIST_OFFICE_HOUR);
                onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_selected);
                onlyFlyer.setImageResource(R.drawable.icon_list_bar_check_box_normal);
            } else if (isCheckedHasFlyer) {
                RestaurantController.officeHour = false;
                orderBy = RestaurantController.LIST_HAS_FLYER;
                mAdapter.loadData(RestaurantController.LIST_HAS_FLYER);
                onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_normal);
                onlyFlyer.setImageResource(R.drawable.icon_list_bar_check_box_selected);
            } else {
                RestaurantController.officeHour = false;
                orderBy = RestaurantController.LIST_ALL;
                mAdapter.loadData(RestaurantController.LIST_ALL);
                onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_normal);
                onlyFlyer.setImageResource(R.drawable.icon_list_bar_check_box_normal);
            }
        }
    };

    public RestaurantListFragment() {
        this.isCheckedOfficeHour = RestaurantController.officeHour;
        this.orderBy = RestaurantController.LIST_ALL;
    }

    public static RestaurantListFragment newInstance(int categoryNumber) {
        RestaurantListFragment restaurantListFragment = new RestaurantListFragment();

        Bundle args = new Bundle();
        args.putInt("mCategoryNumber", categoryNumber);
        restaurantListFragment.setArguments(args);

        return restaurantListFragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = activity;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.loadData(orderBy);
        if (getUserVisibleHint()) {
            AnalyticsHelper analyticsHelper = new AnalyticsHelper(mActivity);
            analyticsHelper.sendScreen("음식점 리스트 화면");
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            onResume();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant, container, false);

        listView = (ListView) view.findViewById(R.id.list_view);
        RelativeLayout empty = (RelativeLayout) view.findViewById(R.id.empty);
        listView.setEmptyView(empty);

        onlyFlyer = (ImageView) view.findViewById(R.id.check_has_flyer);
        onlyOpen = (ImageView) view.findViewById(R.id.check_is_open);
        TextView onlyOpenText = (TextView) view.findViewById(R.id.tv_open);
        TextView onlyFlyerText = (TextView) view.findViewById(R.id.tv_flyer);
        onlyFlyer.setOnClickListener(checkListener);
        onlyOpen.setOnClickListener(checkListener);
        onlyFlyerText.setOnClickListener(checkListener);
        onlyOpenText.setOnClickListener(checkListener);

        getCheckBox();

        int mCategoryNumber = getArguments().getInt("mCategoryNumber");
        this.mAdapter = new RestaurantListAdapter(mActivity, mCategoryNumber, orderBy);

        listView.setAdapter(mAdapter);

        return view;
    }

    public void getCheckBox() {
        isCheckedOfficeHour = RestaurantController.officeHour;
        if (isCheckedOfficeHour) {
            orderBy = RestaurantController.LIST_OFFICE_HOUR;
            onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_selected);
        } else {
            onlyOpen.setImageResource(R.drawable.icon_list_bar_check_box_normal);
        }
    }
}
