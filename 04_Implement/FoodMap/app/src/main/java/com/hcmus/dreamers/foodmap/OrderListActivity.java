package com.hcmus.dreamers.foodmap;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.DoingTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.adapter.OrderListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.common.ResponseJSON;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

public class OrderListActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ListView listOffer;
    private OrderListAdapter adapter;
    private List<Offer> offers;
    private int id_rest;
    private Calendar c = Calendar.getInstance();

    @Override
    protected void onResume() {
        super.onResume();
        getItentFromActivity();
        int mYear, mMonth, mDay;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        refreshData( mYear, mMonth, mDay);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);
        refferences();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return true;
    }

    private void refferences(){
        listOffer = (ListView) findViewById(R.id.list_order);
        toolbar = (Toolbar)findViewById(R.id.order_toolbar);
    }


    private void getItentFromActivity(){
        Intent intent = getIntent();
        id_rest = intent.getIntExtra("id_rest", -1);
    }

    private void refreshData(int year,int monthOfYear ,int dayOfMonth){
        FoodMapApiManager.getOffer(id_rest, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();
                try {
                    ResponseJSON responseJSON = ParseJSON.parseFromAllResponse(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        offers = ParseJSON.parseOffer(resp);
                        filter(year, monthOfYear, dayOfMonth);
                        adapter = new OrderListAdapter(OrderListActivity.this, R.layout.order_item_list, offers);
                        listOffer.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    }else if(responseJSON.getCode() == ConstantCODE.NOTFOUND){
                        Toast.makeText(OrderListActivity.this, "NOT FOUND!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(OrderListActivity.this, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(OrderListActivity.this, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void filter(int year,int monthOfYear,int dayOfMonth){
        Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
        c.set(year, monthOfYear, dayOfMonth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            offers = offers.stream().filter(o -> o.compareDateOrder(date)).collect(Collectors.toList());
            adapter.setOffers(offers);
        }
    }
}
