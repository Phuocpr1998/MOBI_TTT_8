package com.hcmus.dreamers.foodmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Discount;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.adapter.DiscountListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.common.ResponseJSON;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;
import com.hcmus.dreamers.foodmap.serializer.OrderSerializer;
import com.hcmus.dreamers.foodmap.websocket.OrderEmitterListener;
import com.hcmus.dreamers.foodmap.websocket.OrderSocket;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddOrderActivity extends AppCompatActivity {

    private Toolbar tlbAddOrder;
    private EditText edtTotal;
    private Button btnSubmit;
    private Socket socket;
    private ProgressDialog progressDialog;
    private Discount discount;
    private Restaurant restaurant;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_order);
        socket = OrderSocket.getInstance();
        Intent data = getIntent();
        discount = (Discount) data.getSerializableExtra("discount");
        restaurant = (Restaurant) data.getSerializableExtra("restaurant");
        if (discount.getId() == -1) {
            finish();
        }

        references();
    }

    private void references(){
        // set header toolbar in the layout
        tlbAddOrder = (Toolbar) findViewById(R.id.tlbAddOrder);
        setSupportActionBar(tlbAddOrder);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edtTotal = (EditText) findViewById(R.id.txtTotal);

        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtTotal.getText().length() == 0) {
                    Toast.makeText(AddOrderActivity.this, "Vui lòng nhập số lượng", Toast.LENGTH_SHORT).show();
                    return;
                }


                Offer offer = new Offer();
                offer.setNameDish(discount.getNameDish());
                offer.setStatus(0);
                offer.setDiscountPercent(discount.getDiscountPercent());
                offer.setGuestEmail(Guest.getInstance().getEmail());
                offer.setTotal(Integer.parseInt(edtTotal.getText().toString()));
                offer.setDateOrder(Calendar.getInstance().getTime());

//                progressDialog = new ProgressDialog(AddOrderActivity.this);
//                progressDialog.setCanceledOnTouchOutside(false);
//                progressDialog.setMessage("Creating Order");
//                progressDialog.show();

                String orderRequest = createOrder(restaurant.getOwnerUsername(), offer.getGuestEmail(), restaurant.getName(), restaurant.getId(), discount.getId(), offer);
                socket.emit("send_order", orderRequest);
                Toast.makeText(AddOrderActivity.this, "Xin chờ trong giây lát!", Toast.LENGTH_LONG).show();
            }
        });
    }

    //set click event for toolbar
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

    private String createOrder(String email_owner, String email_guest, String name_rest, int id_rest, int id_discount, Offer offer){
        JsonObject order = new JsonObject();
        order.addProperty("email_owner", email_owner);
        order.addProperty("email_guest", email_guest);
        order.addProperty("name_rest", name_rest);
        order.addProperty("id_rest", id_rest);
        order.addProperty("id_discount", id_discount);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeHierarchyAdapter(OrderSerializer.class, new OrderSerializer());
        Gson gson = builder.create();
        String orderContent = gson.toJson(offer, Offer.class);
        order.addProperty("order", orderContent);
        return order.toString();
    }

}
