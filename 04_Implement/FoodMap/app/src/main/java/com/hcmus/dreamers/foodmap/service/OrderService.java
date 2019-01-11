package com.hcmus.dreamers.foodmap.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.github.nkzawa.socketio.client.Socket;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.EditRestaurantActivity;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Offer;
import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.OrderListActivity;
import com.hcmus.dreamers.foodmap.R;
import com.hcmus.dreamers.foodmap.RestaurantManageActivity;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;
import com.hcmus.dreamers.foodmap.websocket.OrderEmitterListener;
import com.hcmus.dreamers.foodmap.websocket.OrderSocket;

import org.json.JSONException;
import org.json.JSONObject;

public class OrderService extends Service {
    private static final String CHANNEL_ID = "notifyfoodmapservice";
    private Socket socket;
    private String name_rest;
    private int id_rest;
    private OrderEmitterListener receiceOrder = new OrderEmitterListener(new TaskCompleteCallBack() {
        @Override
        public void OnTaskComplete(Object response) {
            try {
                final Offer offer = ParseJSON.parseOfferObject(response.toString());
                JSONObject resp = new JSONObject(response.toString());
                name_rest = resp.getString("name_rest");
                id_rest = resp.getInt("id_rest");
                FoodMapApiManager.addOrder(offer, resp.getInt("id_discount"), new TaskCompleteCallBack() {
                    @Override
                    public void OnTaskComplete(Object response) {
                        if((int)response == ConstantCODE.SUCCESS){
                            //NotificationBuilder.ShowNotification(OrderService.this, "Thông báo", offer.getGuestEmail() + " vừa mới đặt hàng.");
                            notification("Quán ăn \"" + name_rest + "\"", offer.getGuestEmail() + " vừa mới đặt hàng.");
                            String content = "{\"email_guest\":\"" + offer.getGuestEmail() + "\", \"status\":200, \"message\":\"Đặt hàng thành công!\"}";
                            socket.emit("send_result", content);
                        }else{
                            String content = "{\"email_guest\":\"" + offer.getGuestEmail() + "\", \"status\":500, \"message\":\"Đặt hàng thất bại!\"}";
                            socket.emit("send_result", content);
                        }
                    }
                });
            } catch (JSONException e) {
            }
        }
    });



    private OrderEmitterListener receiveResult = new OrderEmitterListener(new TaskCompleteCallBack() {
        @Override
        public void OnTaskComplete(Object response) {
            try {
                JSONObject resp = new JSONObject(response.toString());
                notification("Thông báo", resp.getString("message"));
//                if((int)response == ConstantCODE.REFUSE){
//                    //NotificationBuilder.ShowNotification(OrderService.this, "Thông báo", offer.getGuestEmail() + " vừa mới đặt hàng.");
//                    notification("Thông báo", resp.getString("message"));
//                }else if((int)response == ConstantCODE.SUCCESS){
//                    notification("Thông báo", resp.getString("message"));
//                }else if((int)response == ConstantCODE.FAILED){
//                    notification("Thông báo", resp.getString("message"));
//                }else{
//
//                }
            } catch (JSONException e) {
            }
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        socket = OrderSocket.getInstance();
        socket.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String email = intent.getStringExtra("email");
        socket.emit("register", email);
        if(Owner.getInstance() != null && !Owner.getInstance().getUsername().equals(""))
            socket.on("receive_order", receiceOrder);
        else if(Guest.getInstance() != null && !Guest.getInstance().getEmail().equals(""))
            socket.on("receive_result", receiveResult);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        socket.disconnect();
        OrderSocket.setSocket(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void notification(String title, String content){
        Uri soundNotify = Uri.parse("android.resource://com.hcmus.dreamers.foodmap/" + R.raw.messenger_sound);
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, "notifyfoodmapservice")
                        .setSmallIcon(R.drawable.ic_notifications)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setSound(soundNotify)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        createNotificationChannel();
        int NOTIFICATION_ID = 12345;

        Intent targetIntent = new Intent(this, RestaurantManageActivity.class);
        targetIntent.putExtra("id_rest", id_rest);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);

            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            Uri soundNotify = Uri.parse("android.resource://com.hcmus.dreamers.foodmap/" + R.raw.messenger_sound);
            AudioAttributes audioAttribute = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .build();
            channel.setSound(soundNotify, audioAttribute);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

}
