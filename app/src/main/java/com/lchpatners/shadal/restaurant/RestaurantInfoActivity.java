package com.lchpatners.shadal.restaurant;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.lchpatners.shadal.R;
import com.lchpatners.shadal.RestaurantCorrectionActivity;
import com.lchpatners.shadal.restaurant.menu.MenuListAdapter;
import com.lchpatners.shadal.restaurant.menu.MenuListController;
import com.lchpatners.shadal.util.LogUtils;

/**
 * Created by YoungKim on 2015. 8. 25..
 */
public class RestaurantInfoActivity extends ActionBarActivity {
    private static final String TAG = LogUtils.makeTag(RestaurantInfoActivity.class);
    public static final String FLYER_URLS = "flyer_urls";
    public static final String RESTAURANT_ID = "restaurant_id";
    public static final String RESTAURANT_PHONE_NUMBER = "restaurant_phone_number";

    private Intent mIntent;
    private RestaurantInfoController mRestaurantInfoController;
    private MenuListController mMenuListController;
    private Restaurant mRestaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_list);

        mIntent = getIntent();
        mMenuListController = new MenuListController(RestaurantInfoActivity.this);
        mRestaurant = mMenuListController.getRestaurant(mIntent.getIntExtra(RESTAURANT_ID, 0));
        mRestaurantInfoController = new RestaurantInfoController(RestaurantInfoActivity.this, mRestaurant);

        //TODO : set referrer
        setView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_menu_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.pen) {
            Intent intent = new Intent(this, RestaurantCorrectionActivity.class);
            intent.putExtra(RESTAURANT_ID, mRestaurant.getId());
            startActivity(intent);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setView() {
        setRestaurantInfo();
        mRestaurantInfoController.setFlyerButtonListener();
        mRestaurantInfoController.setCallButtonListener();
        mRestaurantInfoController.setFooter();
        mRestaurantInfoController.setHeader();

        ListView listView = (ListView) findViewById(R.id.menu_list);
        MenuListAdapter adapter = new MenuListAdapter(this, mRestaurant);
        listView.setAdapter(adapter);
    }

    private void setRestaurantInfo() {
        Toolbar toolbar;
        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        toolbar.setTitle(mRestaurant.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView officeHours = (TextView) findViewById(R.id.office_hours);
        officeHours.setText(hourFormatString());

        TextView minimum = (TextView) findViewById(R.id.minimum);
        TextView labelMinimum = (TextView) findViewById(R.id.label_minimum);
        if (mRestaurant.getMinimum_price() != 0) {
            minimum.setText(mRestaurant.getMinimum_price() + "원");
        } else {
            minimum.setVisibility(View.GONE);
            labelMinimum.setVisibility(View.GONE);
        }
    }

    private String hourFormatString() {
        float open_hours = mRestaurant.getOpening_hours();
        float close_hours = mRestaurant.getClosing_hours();

        String open;
        String close;

        int open_hour = (int) open_hours;
        if (open_hour < 10 && open_hour != 0) {
            open_hour += 24;
        }
        if (open_hours > open_hour) {
            open = open_hour + ":30";
        } else {
            open = open_hour + ":00";
        }

        int close_hour = (int) close_hours;
        if (close_hour < 10 && close_hour != 0) {
            close_hour += 24;
        }
        if (close_hours > close_hour) {
            close = close_hour + ":30";
        } else {
            close = close_hour + ":00";
        }

        if (open_hours == 0 && close_hours == 0) {
            return "";
        } else {
            return open + " ~ " + close;
        }
    }
}
