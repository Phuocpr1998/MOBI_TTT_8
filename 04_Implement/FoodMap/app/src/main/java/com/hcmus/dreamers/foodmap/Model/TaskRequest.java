package com.hcmus.dreamers.foodmap.Model;

import android.os.AsyncTask;

import java.util.HashMap;

public class TaskRequest extends AsyncTask<HashMap<String, String>, Void, String> {
    public static final int GET = 0;
    public static final int POST = 0;

    private TaskCompleteCallBack onCompleteCallBack;

    private String url;
    private int type;
    public int requestCode;


    public TaskRequest(String url, int type, int requestCode) {
        this.url = url;
        this.requestCode = requestCode;
        this.type = type;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (onCompleteCallBack != null)
            onCompleteCallBack.OnTaskComplete(s);
    }

    @Override
    protected String doInBackground(HashMap<String, String>... hashMaps) {



        return null;
    }

    void setOnCompleteCallBack(TaskCompleteCallBack onCompleteCallBack)
    {
        this.onCompleteCallBack = onCompleteCallBack;
    }
}
