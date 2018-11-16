package com.hcmus.dreamers.foodmap.AsyncTask;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;

import com.hcmus.dreamers.foodmap.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class DownloadImageTask {
    private static final String TAG = "DownloadImageTask";

    private ImageView imageView;
    private Context context;


    public DownloadImageTask(ImageView imageView, Context context)
    {
        this.imageView = imageView;
        this.context = context;
    }

    public void loadImageFromUrl(final String url)
    {
        Picasso.get()
                .load(url)
                .placeholder(context.getResources().getDrawable(R.mipmap.ic_launcher))
                .error(context.getResources().getDrawable(R.mipmap.ic_launcher))
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG, "onSuccess: " + url);
                    }

                    @Override
                    public void onError(Exception e) {
                        Log.e(TAG, "onError: " + url);
                    }
                });
    }

    public void loadImageFromUrl(String url, Callback callback)
    {
        Picasso.get()
                .load(url)
                .placeholder(context.getResources().getDrawable(R.mipmap.ic_launcher))
                .error(context.getResources().getDrawable(R.mipmap.ic_launcher))
                .into(imageView, callback);
    }
}
