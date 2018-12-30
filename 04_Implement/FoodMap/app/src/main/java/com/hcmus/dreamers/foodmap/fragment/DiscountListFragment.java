// http://www.technotalkative.com/contextual-action-bar-cab-android/

package com.hcmus.dreamers.foodmap.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AddDiscountActivity;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Discount;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.R;
import com.hcmus.dreamers.foodmap.adapter.DiscountListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.common.ResponseJSON;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;

import org.json.JSONException;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class DiscountListFragment extends Fragment implements AdapterView.OnItemLongClickListener, AbsListView.MultiChoiceModeListener{
    private static final String TAG = "DiscountListFragment";
    Restaurant restaurant;

    private ListView lvDiscount;
    private FloatingActionButton fabAddDiscount;
    private DiscountListAdapter discountListAdapter;
    private List<Discount> discounts, discountAdapter;
    private List<Dish> dishes;
    private int id_rest;

    Context context = null;
    CoordinatorLayout rootLayout;

    private int nSelected = 0;

    public DiscountListFragment() {

    }

    public void setId_rest(int id_rest) {
        this.id_rest = id_rest;
    }

    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

    public Restaurant getRestaurant() { return this.restaurant; }

    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        FoodMapApiManager.getDiscount(id_rest, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String resp = response.toString();
                try {
                    ResponseJSON responseJSON = ParseJSON.parseFromAllResponse(resp);
                    if(responseJSON.getCode() == ConstantCODE.SUCCESS){

                        discounts = ParseJSON.parseDiscount(resp);
                        discountAdapter = new ArrayList<>(discounts);
                        discountListAdapter = new DiscountListAdapter(context, R.layout.item_discount_list, discountAdapter, restaurant);
                        lvDiscount.setAdapter(discountListAdapter);
                        discountListAdapter.notifyDataSetChanged();

                    }else if(responseJSON.getCode() == ConstantCODE.NOTFOUND){
                        Toast.makeText(context, "NOT FOUND!", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(context, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(context, "NOT INTERNET!", Toast.LENGTH_SHORT).show();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootLayout =(CoordinatorLayout) inflater.inflate(R.layout.fragment_manage_discount, container, false);

        references();
        Log.d(TAG, "onCreateView");
        fabAddDiscount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Dish> dishes = restaurant.getDishes();
				if (restaurant == null || dishes == null || dishes.isEmpty()) {
                    Toast.makeText(context, "Quán chưa có món ăn nào, không thể tạo Discount!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(context, AddDiscountActivity.class);
                intent.putExtra("rest", (Serializable) restaurant);
                startActivity(intent);
            }
        });

        return rootLayout;
    }

    private void references(){
        lvDiscount = rootLayout.findViewById(R.id.lv_discount);
        lvDiscount.setOnItemLongClickListener(this);
        lvDiscount.setMultiChoiceModeListener(this);
        lvDiscount.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        fabAddDiscount = rootLayout.findViewById(R.id.fabAddDiscount);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
/*        if (lvDiscount.getChoiceMode() != ListView.CHOICE_MODE_MULTIPLE_MODAL) {
            lvDiscount.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            //getActivity().invalidateOptionsMenu();
            //lvDiscount.setItemChecked(position, !discountListAdapter.isPositionChecked(position));
            if (currentActionMode != null) { return false; }
            //currentListItemIndex = position;
            currentActionMode = lvDiscount.startActionMode(modeCallBack);
            view.setSelected(true);
            return true;
            //onItemClick(parent, view, position, id);
        }
        return true;*/
        lvDiscount.setItemChecked(position, !discountListAdapter.isPositionChecked(position));
        return false;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (checked) {
            nSelected++;
            discountListAdapter.setNewSelection(position, checked);
        } else {
            nSelected--;
            discountListAdapter.removeSelection(position);
        }
        mode.setTitle(nSelected + " selected");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        nSelected = 0;
        mode.setTitle("Selected Items");
        mode.getMenuInflater().inflate(R.menu.delete_item_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_delete:

                new AlertDialog.Builder(context)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Xóa Discount")
                    .setMessage(String.format("Xác nhận xóa? (%d Discount)", nSelected))
                    .setPositiveButton("Có", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            List<Integer> indexes = discountListAdapter.getSelectedIndex();

                            for (final Integer index : indexes) {

                                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.setMessage("Deleting Discount");
                                progressDialog.show();

                                final Discount discount = discountAdapter.get(index.intValue());
                                FoodMapApiManager.deleteDiscount(restaurant.getId(), discount.getId(), new TaskCompleteCallBack() {
                                    @Override
                                    public void OnTaskComplete(Object response) {
                                        progressDialog.dismiss();
                                        if((int)response == ConstantCODE.SUCCESS){
                                            Discount discount = discountAdapter.get(index.intValue());
                                            int i = discounts.indexOf(discount);
                                            discounts.remove(i);
                                            discountAdapter.remove(index.intValue());
                                            discountListAdapter.notifyDataSetChanged();
                                            Toast.makeText(context, "Xóa Discount thành công!", Toast.LENGTH_SHORT).show();
                                        }else if((int) response == ConstantCODE.NOTFOUND){
                                            Toast.makeText(context, "Lỗi: Discount không tồn tại hoặc đang được sử dụng!", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
                                        }else{
                                            Toast.makeText(context, "Lỗi: Không có kết nối internet, xin kiểm tra lại!", Toast.LENGTH_SHORT).show();
                                            Toast.makeText(context, "Xóa thất bại!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }

                            nSelected = 0;
                            discountListAdapter.notifyDataSetChanged();
                            mode.finish();
                        }
                    })
                    .setNegativeButton("Không", null)
                    .show();


                break;

            default:
                nSelected = 0;
                discountListAdapter.clearSelection();
                mode.finish();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        discountListAdapter.clearSelection();
        nSelected = 0;
    }
}
