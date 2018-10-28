package com.hcmus.dreamers.foodmap;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hcmus.dreamers.foodmap.Model.Restaurant;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RestaurantInfo extends AppCompatActivity {

    TextView txtRestName;
    TextView txtStatus;
    TextView txtOpenTime;
    TextView txtLocation;
    TextView txtDescription;
    ImageView imgDescription;
    ListView lstDish;
    Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.restaurant_info);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar1);
        setSupportActionBar(toolbar);

        txtRestName = (TextView) findViewById(R.id.txtResName);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtOpenTime = (TextView) findViewById(R.id.txtOpenTime);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        imgDescription = (ImageView)findViewById(R.id.imgDescription);

        lstDish = (ListView) findViewById(R.id.lstDish);

        /*LinearLayout lnrCheckIn = (LinearLayout)findViewById(R.id.lnrCheckIn);

        lnrCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/
    }

    public void setRestaurant(Restaurant restaurant){
        this.restaurant = restaurant;
    }

    @SuppressLint("SetTextI18n")
    private void setLayoutInfo()
    {
        txtRestName.setText(restaurant.getName());

        //set Time and Status
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm");
        txtOpenTime.setText(simpleDateFormat.format(restaurant.getTimeOpen()) + " - " + simpleDateFormat.format(restaurant.getTimeClose()));

        Date date = Calendar.getInstance().getTime();
        if(!restaurant.getTimeOpen().after(date) && !restaurant.getTimeClose().before(date))
        {
            txtStatus.setText("OPENING");
        }
        else
        {
            txtStatus.setText("CLOSING");
        }
        txtLocation.setText(restaurant.getAddress());
        txtDescription.setText((restaurant.getDescription()));

        //set Description Image
        {new DownloadImageTask(imgDescription).execute(restaurant.getUrlImage());}

        //set menu
        DishInfoList dishInfoList = new DishInfoList(this, R.layout.row_dish_info, restaurant.getDishes());
        lstDish.setAdapter(dishInfoList);
    }
}


class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    @SuppressLint("StaticFieldLeak")
    private ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        bmImage.setImageBitmap(bitmap);
    }

    @Override
    protected Bitmap doInBackground(String... urls) {
        Bitmap mIcon = null;
        try {
            InputStream in = new java.net.URL(urls[0]).openStream();
            mIcon = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mIcon;
    }
}
