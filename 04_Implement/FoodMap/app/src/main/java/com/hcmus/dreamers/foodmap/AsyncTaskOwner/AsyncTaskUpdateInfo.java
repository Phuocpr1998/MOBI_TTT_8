package com.hcmus.dreamers.foodmap.AsyncTaskOwner;

import android.os.AsyncTask;

import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.common.GenerateRequest;
import com.hcmus.dreamers.foodmap.common.SendRequest;

import java.io.IOException;

import okhttp3.Request;

public class AsyncTaskUpdateInfo extends AsyncTask<Owner, Void, String> {

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
            request = GenerateRequest.updateAccount(owners[0], owners[0].getToken());
            String response = SendRequest.send(request);
            return response;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
