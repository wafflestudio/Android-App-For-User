package com.lchpatners.shadal.call;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.lchpatners.shadal.R;
import com.lchpatners.shadal.Restaurant;
import com.lchpatners.shadal.restaurant.RestaurantInfoActivity;

/**
 * Created by eunhyekim on 2015. 8. 22..
 */
public class RecentCallFragment extends Fragment {
    private Activity activity;
    private ListView listView;
    private RecentCallAdapter mAdapter;
    private String orderBy;

    private ImageView iv_call;
    private ImageView iv_name;

    public RecentCallFragment() {
        this.orderBy = RecentCallController.ORDER_BY_CALL_RECENT;
    }

    public static RecentCallFragment newInstance() {
        return new RecentCallFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        //default is ORDER_BY_RECENT
        this.mAdapter = new RecentCallAdapter(activity, RecentCallController.ORDER_BY_CALL_RECENT);
    }

    @Override
    public void onResume() {
        mAdapter.loadData(RecentCallController.ORDER_BY_CALL_RECENT);
        super.onResume();
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
        View view = inflater.inflate(R.layout.fragment_recent_call, container, false);

        listView = (ListView) view.findViewById(R.id.call_list_view);
        TextView textView = (TextView) view.findViewById(android.R.id.empty);
        listView.setEmptyView(textView);

        TextView name = (TextView) view.findViewById(R.id.title_name);
        TextView call = (TextView) view.findViewById(R.id.title_call);

        iv_name = (ImageView) view.findViewById(R.id.iv_name_order);
        iv_call = (ImageView) view.findViewById(R.id.iv_order);

        name.setOnClickListener(orderListener);
        call.setOnClickListener(orderListener);

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapter.getItem(position) instanceof Restaurant) {
                    Restaurant restaurant = (Restaurant) mAdapter.getItem(position);

//                    AnalyticsHelper helper = new AnalyticsHelper(getActivity().getApplication());
//                    helper.sendEvent("UX", "res_in_favorite_clicked", restaurant.getName());

                    Intent intent = new Intent(activity, RestaurantInfoActivity.class);
                    intent.putExtra("RESTAURANT", restaurant);
                    intent.putExtra("REFERRER", "CallListFragment");
                    startActivity(intent);
                    mAdapter.notifyDataSetChanged();
                    listView.deferNotifyDataSetChanged();
                }
            }
        });
        return view;
    }

    View.OnClickListener orderListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.title_name) {
                mAdapter.loadData(RecentCallController.ORDER_BY_NAME);
                iv_name.setVisibility(View.VISIBLE);
                iv_call.setVisibility(View.INVISIBLE);
            } else if (view.getId() == R.id.title_call) {
                mAdapter.loadData(RecentCallController.ORDER_BY_CALL_RECENT);
                iv_name.setVisibility(View.INVISIBLE);
                iv_call.setVisibility(View.VISIBLE);
            }
        }
    };
}
