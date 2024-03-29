package com.lchpatners.shadal.request;

import android.app.Activity;
import android.widget.Toast;

import com.lchpatners.shadal.dao.Request;
import com.lchpatners.shadal.util.Preferences;
import com.lchpatners.shadal.util.RetrofitConverter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by eunhyekim on 2015. 8. 30..
 */
public class RequestController {

    private static final String BASE_URL = "http://www.shadal.kr:3000";

    public static void sendUserRequest(final Activity activity, final String email, final String details) {
        Request request = new Request();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new RetrofitConverter().createBasicConverter()))
                .setEndpoint(BASE_URL) // The base API endpoint.
                .build();

        RequestAPI requestAPI = restAdapter.create(RequestAPI.class);
        request.setUuid(Preferences.getDeviceUuid(activity));
        request.setDetails(details);
        request.setEmail(email);
        requestAPI.sendUserRequest(request, new Callback<Request>() {
            @Override
            public void success(Request request, Response response) {
                activity.finish();
                Toast.makeText(activity, "문의해주셔서 감사합니다! 메일로 답변 드리겠습니다", Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(RetrofitError error) {
//                if (error.toString().contains("java.io.EOFException")) {
                sendUserRequest(activity, email, details);
                error.printStackTrace();
//                } else {
//                    Toast.makeText(activity, "실패했어요! 다시 시도해 주세요", Toast.LENGTH_LONG).show();
//                }
            }
        });
    }
}
