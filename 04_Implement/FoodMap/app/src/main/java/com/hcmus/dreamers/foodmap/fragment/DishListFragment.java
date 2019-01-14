package com.hcmus.dreamers.foodmap.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;


import com.google.gson.Gson;
import com.hcmus.dreamers.foodmap.AddDishActivity;
import com.hcmus.dreamers.foodmap.EditDishActivity;
import com.hcmus.dreamers.foodmap.EditRestaurantActivity;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.R;
import com.hcmus.dreamers.foodmap.adapter.DishInfoListAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DishListFragment extends Fragment {
    private static final String TAG = "DishListFragment";
    int selectedRow = -1;
    List<Dish> dishes;
    List<Dish> visibleDishes;
    Restaurant restaurant;

    Context context = null;
    EditRestaurantActivity editRestaurantActivity;


    ListView dishListView;
    DishInfoListAdapter adapter;
    FloatingActionButton fab;
    CoordinatorLayout rootLayout;
    Button btnLoadMoreDish;
    int lastVisibleDishIndex = -1; // includesive [0, lastVisibleDishIndex)
    final int NUMBER_OF_DISH_ITEM_LOAD = 5;     // Số món ăn mỗi lần tải thêm
    final int DEFAULT_NUMBER_DISH_DISPLAY = 5;      // Số món ăn hiển thị ban đầu

    // arbitrary interprocess communication ID (just a nickname!)
    final int IPC_ID = (int) (10001 * Math.random());
    final int AFA_ID = 1009;

    public DishListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Get host activity "EditRestaurant"
        context = getActivity();
        editRestaurantActivity = (EditRestaurantActivity) getActivity();

        if (editRestaurantActivity != null) {
            restaurant = editRestaurantActivity.restaurant;

            //Null pointer is already check in host activity
            dishes = restaurant.getDishes();

            if (dishes.size() <= DEFAULT_NUMBER_DISH_DISPLAY)
            {
                visibleDishes = new ArrayList<>(dishes) ;
                lastVisibleDishIndex = dishes.size() - 1; //inclusive

            }
            else {
                visibleDishes = new ArrayList<>();
                visibleDishes.addAll(dishes.subList(0, DEFAULT_NUMBER_DISH_DISPLAY)); // Lấy 5 item dish từ toàn bộ dish
                lastVisibleDishIndex = DEFAULT_NUMBER_DISH_DISPLAY - 1;              //inclusive
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        rootLayout = (CoordinatorLayout) inflater.inflate(
                R.layout.fragment_dish_list, container, false);

        setup();
        handleClickEvent();
        if (visibleDishes.size() == dishes.size())
        {
            btnLoadMoreDish.setVisibility(View.GONE);   // Đã nạp toàn bộ món ăn, ẩn nút bấm đi
        }
        return rootLayout;
    }

    private void setup() {
        dishListView = (ListView) rootLayout.findViewById(R.id.dish_list);
        fab = (FloatingActionButton) rootLayout.findViewById(R.id.fabAddDish);
        btnLoadMoreDish = rootLayout.findViewById(R.id.btnLoadMoreDish);

        adapter = new DishInfoListAdapter(context,
                R.layout.adapter_dish_info_list, visibleDishes
        );

        dishListView.setAdapter(adapter);
    }

    private void handleClickEvent() {
        // Chọn 1 món ăn từ danh sách
        dishListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Dish dish = visibleDishes.get(position);
                selectedRow = position;
                transferData2NextActivity(dish);
            }
        });

        // Nút Thêm món ăn mới
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, AddDishActivity.class);
                intent.putExtra("rest", (Serializable) restaurant);
                startActivityForResult(intent, AFA_ID);
            }
        });

        btnLoadMoreDish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int fromIndex  = lastVisibleDishIndex + 1;          //inclusive [
                int toIndex = fromIndex + NUMBER_OF_DISH_ITEM_LOAD; //exclusive    )

                if (toIndex > dishes.size())
                {
                    toIndex = dishes.size();
                }
                visibleDishes.addAll(dishes.subList(fromIndex, toIndex));
                lastVisibleDishIndex = toIndex - 1;
                adapter.notifyDataSetChanged();

                if (visibleDishes.size() == dishes.size())       // Đã tải toàn bộ, ẩn nút loadmore đi
                {
                    btnLoadMoreDish.setVisibility(View.GONE);
                }
            }
        });
    }

    private void transferData2NextActivity(Dish dish) {

        Bundle transferData = new Bundle();
        Gson gson = new Gson();
        Intent manageRest_manageDish = new Intent(
                context,
                EditDishActivity.class);

        transferData.putString("dishJSON", gson.toJson(dish));
        transferData.putInt("restID", restaurant.getId());

        manageRest_manageDish.putExtras(transferData);
        startActivityForResult(manageRest_manageDish, IPC_ID);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (IPC_ID == requestCode) {

                if (resultCode == Activity.RESULT_OK) {
                    Gson gson = new Gson();
                    Dish dish;

                    Boolean isDelete = data.getBooleanExtra("isDelete", false);
                    if (!isDelete) {
                        String dishJSON = data.getStringExtra("dishJSON");
                        dish = gson.fromJson(dishJSON, Dish.class);
                        visibleDishes.set(selectedRow, dish);       // danh sách các món nhìn thấy đc
                        dishes.set(selectedRow, dish);              // danh sách ban đầu
                        Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_LONG).show();
                    } else {
                        visibleDishes.remove(selectedRow);          //danh sách các món nhìn thấy được
                        dishes.remove(selectedRow);                 // danh sách ban đầu
                        Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_LONG).show();
                    }
                    adapter.notifyDataSetChanged();
                }
            } else if (requestCode == AFA_ID && resultCode == Activity.RESULT_OK) {

                Gson gson = new Gson();
                Dish dish;

                String dishJSON = data.getStringExtra("dishJSON");
                dish = gson.fromJson(dishJSON, Dish.class);

                visibleDishes.add(dish);           // danh sách các món nhìn thấy đc
                dishes.add(dish);                   // danh sách ban đầu
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        } //try
    }
}
