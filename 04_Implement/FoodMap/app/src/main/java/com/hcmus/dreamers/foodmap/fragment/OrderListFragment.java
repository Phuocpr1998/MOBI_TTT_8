package com.hcmus.dreamers.foodmap.fragment;


import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.OrderManagerActivity;
import com.hcmus.dreamers.foodmap.R;
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

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderListFragment extends Fragment implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {

    Restaurant restaurant;
    private static final String TAG = "OrderListFragment";

    private ListView listOffer;
    private int position;
    private OrderListAdapter adapter;
    private List<Offer> offers, offersAdapter;
    private int id_rest;
    private Calendar c = Calendar.getInstance();

    Context context = null;
    LinearLayout rootLayout;

    public OrderListFragment() {
        // Required empty public constructor
    }

    public void setId_rest(int id_rest) {
        this.id_rest = id_rest;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();

        int mYear, mMonth, mDay;
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        refreshData(true, mYear, mMonth, mDay);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        rootLayout =(LinearLayout) inflater.inflate(R.layout.fragment_order_list, container, false);

        refferences();
        return rootLayout;
    }

    private void refferences(){
        listOffer = rootLayout.findViewById(R.id.list_order);
        listOffer.setOnItemLongClickListener(this);
        listOffer.setOnItemClickListener(this);
    }


    private void refreshData(boolean filter, int year,int monthOfYear ,int dayOfMonth){
        FoodMapApiManager.getOffer(id_rest, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();
                try {
                    ResponseJSON responseJSON = ParseJSON.parseFromAllResponse(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){
                        offers = ParseJSON.parseOffer(resp);
                        offersAdapter = new ArrayList<>(offers);
                        adapter = new OrderListAdapter(context, R.layout.order_item_list, offersAdapter);
                        listOffer.setAdapter(adapter);
                        if(filter){
                            filter(year, monthOfYear, dayOfMonth);
                        }
                        adapter.notifyDataSetChanged();
                    }else if(responseJSON.getCode() == ConstantCODE.NOTFOUND){
                        Toast.makeText(context, "NOT FOUND!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
        return true;
    }


    private void showConfirmDenyDialog(final int position) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Từ chối đơn hàng")
                .setMessage("Bạn có muốn từ chối hàng này?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Offer offer = (Offer) offersAdapter.get(position);
                        ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setMessage("Processing...");
                        progressDialog.show();

                        FoodMapApiManager.updateStatusOrder(offer.getId(), -1, new TaskCompleteCallBack() {
                            @Override
                            public void OnTaskComplete(Object response) {
                                progressDialog.dismiss();
                                if ((int) response == ConstantCODE.SUCCESS) {
                                    Offer o = offersAdapter.get(position);
                                    int index = offers.indexOf(o);
                                    offersAdapter.get(position).setStatus(-1);
                                    offers.get(index).setStatus(-1);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(context, "Cập nhật đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                } else if ((int) response == ConstantCODE.NOTFOUND) {
                                    Toast.makeText(context, "Lỗi cập nhật đơn hàng không tồn tại, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Không có kết nối internet, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Không", null)
                .show();
    }

    private void showConfirmProcessDialog(final int position) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Chấp nhận đơn hàng")
                .setMessage("Bạn có muốn chấp nhận đơn hàng này?")
                .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Offer offer = (Offer) offersAdapter.get(position);
                        ProgressDialog progressDialog = new ProgressDialog(context);
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setMessage("Processing...");
                        progressDialog.show();
                        int status = 1;
                        FoodMapApiManager.updateStatusOrder(offer.getId(), status, new TaskCompleteCallBack() {
                            @Override
                            public void OnTaskComplete(Object response) {
                                progressDialog.dismiss();
                                if ((int) response == ConstantCODE.SUCCESS) {
                                    Offer o = offersAdapter.get(position);
                                    int index = offers.indexOf(o);
                                    offersAdapter.get(position).setStatus(status);
                                    offers.get(index).setStatus(status);
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(context, "Cập nhật đơn hàng thành công!", Toast.LENGTH_SHORT).show();
                                } else if ((int) response == ConstantCODE.NOTFOUND) {
                                    Toast.makeText(context, "Lỗi cập nhật đơn hàng không tồn tại, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "Không có kết nối internet, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                })
                .setNegativeButton("Không", null)
                .show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.order_group_by_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_GroupByNone:
                c = Calendar.getInstance();
                offersAdapter = new ArrayList<>(offers);
                adapter.setOffers(offersAdapter);
                adapter.notifyDataSetChanged();
            return true;

            case R.id.action_GroupByDate:
                int mYear, mMonth, mDay;
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                filter(year, monthOfYear, dayOfMonth);
                                adapter.notifyDataSetChanged();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void filter(int year,int monthOfYear,int dayOfMonth){
        Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
        c.set(year, monthOfYear, dayOfMonth);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            offersAdapter = offers.stream().filter(o -> o.compareDateOrder(date)).collect(Collectors.toList());
            adapter.setOffers(offersAdapter);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (offersAdapter.get(position).getStatus() == 1) // các đơn hàng đã xử lý sẽ không được chỉnh sửa
            return;

        OrderListFragment.this.position = position;
        // show popup menu
        PopupMenu popupMenu = new PopupMenu(context, view);
        popupMenu.inflate(R.menu.popup_menu_order_manage_owner);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                popupMenu.dismiss();
                if (item.getItemId() == R.id.btnAcceptOrder){
                    showConfirmProcessDialog(OrderListFragment.this.position);
                }
                else if (item.getItemId() == R.id.btnDenyOrder){
                    showConfirmDenyDialog(OrderListFragment.this.position);
                }
                return false;
            }
        });
        popupMenu.show();
    }
}
