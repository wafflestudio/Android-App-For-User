package com.lchpatners.shadal.util.System;

import com.lchpatners.shadal.dao.Device;
import com.lchpatners.shadal.util.RetrofitConverter;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by YoungKim on 2015. 8. 28..
 */
public class DeviceController {
    private static final String BASE_URL = "http://www.shadal.kr:3000";

    public static void sendDeviceInfo(final int campud_id, final String uuid) {
        Device device = new Device();
        device.setCampus_id(campud_id);
        device.setUuid(uuid);

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setConverter(new GsonConverter(new RetrofitConverter().createBasicConverter()))
                .setEndpoint(BASE_URL) // The base API endpoint.
                .build();

        SystemAPI systemAPI = restAdapter.create(SystemAPI.class);

        systemAPI.sendDeviceInfo(device, new Callback<Device>() {
            @Override
            public void success(Device device, Response response) {

            }

            @Override
            public void failure(RetrofitError error) {
//                if (error.toString().contains("java.io.EOFException")) {
                    sendDeviceInfo(campud_id, uuid);
//                }
                error.printStackTrace();
            }
        });
    }
}
