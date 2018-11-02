package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.DownloadImageTask;
import com.hcmus.dreamers.foodmap.Model.Restaurant;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class RestaurantInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RestAcitvity";
    TextView txtRestName;
    TextView txtStatus;
    TextView txtOpenTime;
    TextView txtLocation;
    TextView txtDescription;
    ImageView imgDescription;
    ListView lstDish;
    Restaurant restaurant = new Restaurant();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_info);

        //set header toolbar in the layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlbRestInfo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtRestName = (TextView) findViewById(R.id.txtRestName);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtOpenTime = (TextView) findViewById(R.id.txtOpenTime);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        imgDescription = (ImageView) findViewById(R.id.imgDescription);
        lstDish = (ListView) findViewById(R.id.lstDish);

        //get Restaurant
        Intent intent = this.getIntent();
        int RestID = intent.getIntExtra("RestID", -1);
        if(RestID == -1)
        {
            Log.i(TAG,"can't get restaurant data");
            Toast.makeText(this,"can't get restaurant data", Toast.LENGTH_LONG).show();
        }
        else
        {
            //get data
            Toast.makeText(this,"ok", Toast.LENGTH_LONG).show();
        }

        //debug
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            restaurant.setTimeOpen(simpleDateFormat.parse("08:00"));
            restaurant.setTimeClose(simpleDateFormat.parse("22:00"));
        } catch (ParseException e) {
            Log.d("Time",e.toString());
            e.printStackTrace();
        }
        restaurant.setUrlImage("https://i.pinimg.com/236x/82/fa/8a/82fa8a8d0abac9e28614df1f5c45efeb.jpg");
        restaurant.setName("anbcaso");
        restaurant.setPhoneNumber("0377389063");
        restaurant.setAddress("227 Nguyen Van Cu");

        setLayoutInfo();
        //endbug


        PhoneStateListener phoneStateListener = new PhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        LinearLayout lnrContact = (LinearLayout) findViewById(R.id.lnrContact);
        lnrContact.setOnClickListener(this);




    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    @SuppressLint("SetTextI18n")
    private void setLayoutInfo() {
        //set Description Image
        DownloadImageTask taskDownload = new DownloadImageTask(imgDescription, getApplicationContext());
        taskDownload.loadImageFromUrl(restaurant.getUrlImage());

        //set Restaurant name
        txtRestName.setText(restaurant.getName());

        //Set a quality of comments, check in, saves, shares, rate


        //set Time and Status
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        txtOpenTime.setText(simpleDateFormat.format(restaurant.getTimeOpen()) + " - " + simpleDateFormat.format(restaurant.getTimeClose()));

        Date date = Calendar.getInstance().getTime();
        Date start = Calendar.getInstance().getTime();
        Date end = Calendar.getInstance().getTime();
        start.setHours(restaurant.getTimeOpen().getHours());
        start.setMinutes(restaurant.getTimeOpen().getMinutes());
        end.setHours(restaurant.getTimeClose().getHours());
        end.setMinutes(restaurant.getTimeClose().getMinutes());

        if (!start.after(date) && !end.before(date))
        {
            txtStatus.setText("OPENING");
            txtStatus.setTextColor(Color.GREEN);
        } else {
            txtStatus.setText("CLOSING");
            txtStatus.setTextColor(Color.RED);
        }


        //set Restaurant address
        txtLocation.setText(restaurant.getAddress());

        //Set Restaurant description
        //txtDescription.setText((restaurant.getDescription()));

        //Set Price range of restaurant


        //set Menu
        /*DishInfoList dishInfoList = new DishInfoList(this, R.layout.row_dish_info, restaurant.getDishes());
        lstDish.setAdapter(dishInfoList);*/
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.lnrCheckIn:
                break;
            case R.id.lnrComment:
                break;
            case R.id.lnrFavourite:
                break;
            case R.id.lnrSave:
                break;
            case R.id.lnrShare:
                break;
            case R.id.lnrRate:
                break;
            case R.id.lnrContact:
                Toast.makeText(this, restaurant.getPhoneNumber(), Toast.LENGTH_LONG).show();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                startActivity(callIntent);
                break;
            case R.id.lnrMenu:
                break;
        }
    }
}






