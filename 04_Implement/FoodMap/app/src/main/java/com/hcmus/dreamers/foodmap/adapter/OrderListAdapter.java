package com.hcmus.dreamers.foodmap.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.R;

import java.util.List;

public class OrderListAdapter extends ArrayAdapter<Offer>{

    private  Context context;
    private List<Offer> offers;

    public OrderListAdapter(@NonNull Context context, int resource, @NonNull List<Offer> offers) {
        super(context, resource, offers);
        this.context = context;
        this.offers = offers;
    }

    @Override
    public int getCount() {
        return offers.size();
    }

    @Nullable
    @Override
    public Offer getItem(int position) {
        return offers.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.order_item_list, null);
        TextView dishName = (TextView) view.findViewById(R.id.namedish);
        TextView percent = (TextView) view.findViewById(R.id.discount_percent);
        TextView emailGuest = (TextView) view.findViewById(R.id.email_guest);
        TextView total = (TextView) view.findViewById(R.id.total);
        TextView status = (TextView) view.findViewById(R.id.status);
        Offer offer = offers.get(position);
        dishName.setText(offer.getNameDish());
        percent.setText(String.valueOf(offer.getDiscountPercent() + "%"));
        emailGuest.setText(offer.getGuestEmail());
        total.setText(String.valueOf(offer.getTotal()));
        if(offer.getStatus() == 0){
            status.setText("Chưa được xử lí");
            status.setTextColor(Color.RED);
        }else if (offer.getStatus() == 1){
            status.setText("Đã được xử lí");
            status.setTextColor(Color.rgb(0, 255, 196));
        } else if (offer.getStatus() == -1){
            status.setText("Đơn hàng bị từ chối");
            status.setTextColor(Color.rgb(255, 255, 0));
        } else if (offer.getStatus() == -2){
            status.setText("Đơn hàng đã bị hủy");
            status.setTextColor(Color.rgb(255, 0, 255));
        }
        return view;
    }

    public void setOffers(List<Offer> offers) {
        this.offers = offers;
    }
}
