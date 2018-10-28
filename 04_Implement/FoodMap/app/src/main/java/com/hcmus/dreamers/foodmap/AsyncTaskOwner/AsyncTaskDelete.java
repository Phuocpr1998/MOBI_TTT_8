package com.hcmus.dreamers.foodmap.AsyncTaskOwner;

import android.os.AsyncTask;

import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.common.GenerateRequest;
import com.hcmus.dreamers.foodmap.common.SendRequest;

import java.io.IOException;

import okhttp3.Request;

public class AsyncTaskDelete extends AsyncTask<String, Void, String> {

    private String respond;

    public String getRespond() {
        return respond;
    }

    @Override
    protected void onPostExecute(String s) {
        respond = s;
    }

    @Override
    protected String doInBackground(String... strings) {
        Request request = null;
        try {
            request = GenerateRequest.deleteAccount(strings[0], strings[1]);
            String response = SendRequest.send(request);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

}
