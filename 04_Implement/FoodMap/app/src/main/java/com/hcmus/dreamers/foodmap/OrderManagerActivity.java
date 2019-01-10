package com.hcmus.dreamers.foodmap;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.adapter.OrderListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;

import java.util.List;


public class OrderManagerActivity extends AppCompatActivity {

    private ListView lstOrders;
    private Toolbar toolbar;
    private List<Offer> offerList;
    private int position;

    private OrderListAdapter orderListAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_manage);

        toolbar = findViewById(R.id.manage_order_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lstOrders = findViewById(R.id.lstOrders);
        offerList = Guest.getInstance().getOfferList();
        orderListAdapter = new OrderListAdapter(OrderManagerActivity.this, R.layout.order_item_list, offerList);
        lstOrders.setAdapter(orderListAdapter);
        lstOrders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                OrderManagerActivity.this.position = position;

                // show popup menu
                PopupMenu popupMenu = new PopupMenu(OrderManagerActivity.this, view);
                popupMenu.inflate(R.menu.popup_menu_order_manage);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        popupMenu.dismiss();
                        if (item.getItemId() == R.id.btnDeleteOrder)
                        {
                            new AlertDialog.Builder(OrderManagerActivity.this)
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setTitle("Xóa Đơn hàng")
                                    .setMessage("Bạn có muốn xóa đơn hàng này?")
                                    .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            final Offer offer = (Offer) offerList.get(position);
                                            FoodMapApiManager.deleteOffer(offer.getId(), Guest.getInstance().getEmail(), new TaskCompleteCallBack() {
                                                @Override
                                                public void OnTaskComplete(Object response) {
                                                    if((int)response == ConstantCODE.SUCCESS){
                                                        Toast.makeText(OrderManagerActivity.this, "Xóa Đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                                        loadData();
                                                    }else if((int) response == ConstantCODE.NOTFOUND){
                                                        Toast.makeText(OrderManagerActivity.this, "Lỗi xóa Đơn hàng không tồn tại, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        Toast.makeText(OrderManagerActivity.this, "Không có kết nối internet, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        }
                                    })
                                    .setNegativeButton("Không", null)
                                    .show();
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        loadData();
    }

    void loadData()
    {
        if (Guest.getInstance().getEmail() == "")
            return;
        ProgressDialog progressDialog = new ProgressDialog(OrderManagerActivity.this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Load Order...");
        progressDialog.show();

        FoodMapApiManager.getOrdersGuest(Guest.getInstance().getEmail(), new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                progressDialog.dismiss();
                int code = (int)response;
                if (code == FoodMapApiManager.SUCCESS){
                    orderListAdapter.notifyDataSetChanged();
                }
                else if (code == ConstantCODE.NOTINTERNET){
                    Toast.makeText(OrderManagerActivity.this, "Kiểm tra kết nối internet", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(OrderManagerActivity.this, "Không thể lấy danh sách order", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            finish();
        }
        return false;
    }
}
