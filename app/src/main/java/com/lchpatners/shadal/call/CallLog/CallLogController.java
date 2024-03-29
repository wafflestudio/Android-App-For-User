package com.lchpatners.shadal.call.CallLog;

import android.app.Activity;

import com.lchpatners.shadal.call.RecentCallController;
import com.lchpatners.shadal.campus.CampusController;
import com.lchpatners.shadal.dao.CallLog;
import com.lchpatners.shadal.restaurant.category.CategoryController;
import com.lchpatners.shadal.util.Preferences;
import com.lchpatners.shadal.util.RetrofitConverter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by YoungKim on 2015. 8. 27..
 */
public class CallLogController {
    private static final String BASE_URL = "http://www.shadal.kr:3000";

    public static void sendCallLog(final Activity activity, final int restaurant_id) {
        CallLog callLog = new CallLog();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new RetrofitConverter().createBasicConverter()))
                .setEndpoint(BASE_URL) // The base API endpoint.
                .build();

        CallLogAPI callLogAPI = restAdapter.create(CallLogAPI.class);

        callLog.setRestaurant_id(restaurant_id);
        callLog.setCategory_id(CategoryController.getRestaurantCategory(activity, restaurant_id));
        callLog.setCampus_id(CampusController.getCurrentCampus(activity).getId());
        callLog.setUuid(Preferences.getDeviceUuid(activity));
        callLog.setNumber_of_calls(RecentCallController.getRecentCallCountByRestaurantId(activity, restaurant_id));
        callLog.setHas_recent_call(RecentCallController.checkHasRecentCall(activity, restaurant_id));

        callLogAPI.sendCallLog(callLog, new Callback<CallLog>() {
            @Override
            public void success(CallLog callLog, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {
//                if (error.toString().contains("java.io.EOFException")) {
                    sendCallLog(activity, restaurant_id);
                error.printStackTrace();
//                } else {
//                    Toast.makeText(activity, "실패했어요! 다시 시도해 주세요", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }
}
