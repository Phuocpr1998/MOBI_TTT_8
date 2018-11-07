package com.hcmus.dreamers.foodmap;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.Model.Restaurant;

import org.osmdroid.util.GeoPoint;

import java.util.Calendar;
import java.util.Date;

public class RegisterRestaurantActivity extends AppCompatActivity implements View.OnClickListener{

    private final int IPC_ID = 1009;
    private final int CLA_ID = 1008;

    private EditText edtName;
    private EditText edtAddress;
    private EditText edtDesciption;

    private Button btnRegister;
    private Button btnLocation;

    private ImageView igvUpload;

    private TextView txvOpen;
    private TextView txvClose;

    private Toolbar toolbar;

    private Restaurant restaurant;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_register);

        restaurant = new Restaurant();

        edtName = (EditText)findViewById(R.id.edt_name_restaurant);
        edtAddress = (EditText)findViewById(R.id.edt_address_restaurant);
        edtDesciption = (EditText)findViewById(R.id.edt_description_restaurant);

        btnLocation = (Button)findViewById(R.id.btnLocation);
        btnLocation.setOnClickListener(this);

        btnRegister = (Button)findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(this);

        igvUpload = (ImageView) findViewById(R.id.igv_upload);
        igvUpload.setOnClickListener(this);

        txvOpen = (TextView) findViewById(R.id.txvOpenTime);
        txvOpen.setOnClickListener(this);

        txvClose = (TextView) findViewById(R.id.txvCloseTime);
        txvClose.setOnClickListener(this);

        toolbar = (Toolbar)findViewById(R.id.register_restaurant_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            RegisterRestaurantActivity.this.finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.txvOpenTime || id == R.id.txvCloseTime){
            Calendar mcurrentTime = Calendar.getInstance();
            int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
            int minute = mcurrentTime.get(Calendar.MINUTE);
            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(RegisterRestaurantActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    String time = String.valueOf(selectedHour) + " : " + String.valueOf(selectedMinute);
                    Date date = new Date();
                    date.setHours(selectedHour);
                    date.setMinutes(selectedMinute);
                    if (id == R.id.txvOpenTime){
                        txvOpen.setText(time);
                        restaurant.setTimeOpen(date);
                    }
                    else {
                        txvClose.setText(time);
                        restaurant.setTimeClose(date);
                    }
                }
            }, hour, minute, true);//Yes 24 hour time

            if (id == R.id.txvOpenTime){
                mTimePicker.setTitle("Giờ mở cửa");
            }
            else {
                mTimePicker.setTitle("Giờ đóng cửa");
            }
            mTimePicker.show();
        }
        else if (id == R.id.igv_upload){
            Intent editDish_pickImage = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(editDish_pickImage, IPC_ID);
        }
        else if (id == R.id.btnRegister){
            Toast.makeText(RegisterRestaurantActivity.this, "btnRegister", Toast.LENGTH_LONG).show();
        }
        else if (id == R.id.btnLocation){

            String address = edtAddress.getText().toString();

            if (address.equals("")){
                Toast.makeText(RegisterRestaurantActivity.this, "Vui lòng nhập địa chỉ", Toast.LENGTH_LONG).show();
            }
            else{
                Intent intent = new Intent(RegisterRestaurantActivity.this, ChooseLocationActivity.class);
                intent.putExtra("address", address);
                startActivityForResult(intent, CLA_ID);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == IPC_ID){
            if (resultCode == Activity.RESULT_OK){
                // Chuỗi URI trả về có dạng content://<path>
                String imageURI = data.getDataString();
                igvUpload.setImageURI(Uri.parse(imageURI));
            }
        }
        else if (requestCode == CLA_ID){
            if (resultCode == Activity.RESULT_OK){
                double lat = data.getDoubleExtra("lat", -1000.0);
                double lon = data.getDoubleExtra("lon", -1000.0);
                String address = data.getStringExtra("address");
                if (lat != -1000.0 && lon != -1000.0){
                    restaurant.setLocation(new GeoPoint(lat, lon));
                }
                if (address != null){
                    edtAddress.setText(address);
                }
            }
        }
    }
}
