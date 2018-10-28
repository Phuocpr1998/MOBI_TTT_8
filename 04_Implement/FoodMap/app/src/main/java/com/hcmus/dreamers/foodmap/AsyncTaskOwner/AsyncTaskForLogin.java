package com.hcmus.dreamers.foodmap.AsyncTaskOwner;

import android.os.AsyncTask;

import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.common.GenerateRequest;
import com.hcmus.dreamers.foodmap.common.SendRequest;

import java.io.IOException;

import okhttp3.Request;

public class AsyncTaskForLogin extends AsyncTask<Owner, Void, String> {

    private String respond;

    public String getRespond() {
        return respond;
    }

    @Override
    protected void onPostExecute(String s) {
        respond = s;
    }

    @Override
    protected String doInBackground(Owner...owners) {

        Request request = null;
        try {
            request = GenerateRequest.checkLogin(owners[0]);
            String response = SendRequest.send(request);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}

