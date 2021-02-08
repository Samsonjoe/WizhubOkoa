package com.wiz.okoa.Common;

import com.wiz.okoa.Remote.IGoogleAPI;
import com.wiz.okoa.Remote.RetrofitClient;

public class Common {
    public static final String baseURL = "https://maps.googleapis.com";

    public static IGoogleAPI getGoogleAPI()
    {

        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

}
