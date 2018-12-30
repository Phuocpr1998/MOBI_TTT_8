package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.hcmus.dreamers.foodmap.AsyncTask.DownloadImageTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.Dish;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.adapter.DishInfoListAdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

public class RestaurantInfoActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RestAcitvity";
    private static final int CM_ID = 1009;
    private CallbackManager callbackManager;
    private ShareDialog shareDialog;
    private boolean isGuestLogin = false;

    private TextView txtRestName;
    private TextView txtNCheckIn;
    private TextView txtNComment;
    private TextView txtNFavorite;
    private TextView txtNShare;
    private TextView txtNRate;
    private TextView txtStatus;
    private TextView txtOpenTime;
    private TextView txtLocation;
    private TextView txtDescription;
    private TextView txtPrice;
    private ImageView imgDescription;
    private ListView lstDish;
    private LinearLayout lnrCheckIn;
    private LinearLayout lnrComment;
    private LinearLayout lnrFavorite;
    private LinearLayout lnrRate;
    private LinearLayout lnrShare;
    private FloatingActionButton fabMoreInfo;
    private FloatingActionButton fabOffer;
    private FloatingActionButton fabLocation;

    private ImageView imgHeart;
    private Button btnContact;
    private Restaurant restaurant;
    private boolean isMoreInfoClick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());
        setContentView(R.layout.activity_restaurant_info);

        //set header toolbar in the layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlbRestInfo);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //init View
        txtRestName = (TextView) findViewById(R.id.txtRestName);
        txtNCheckIn = (TextView) findViewById(R.id.txtNCheckIn);
        txtNComment = (TextView) findViewById(R.id.txtNComment);
        txtNFavorite = (TextView) findViewById(R.id.txtNFavorite);
        txtNShare = (TextView) findViewById(R.id.txtNShare);
        txtNRate = (TextView) findViewById(R.id.txtNRate);
        txtStatus = (TextView) findViewById(R.id.txtStatus);
        txtOpenTime = (TextView) findViewById(R.id.txtOpenTime);
        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtDescription = (TextView) findViewById(R.id.txtDescription);
        txtPrice = (TextView) findViewById(R.id.txtPrice);
        imgDescription = (ImageView) findViewById(R.id.imgDescription);
        lstDish = (ListView) findViewById(R.id.lstDish);
        lstDish.setFocusable(false);
        lnrCheckIn = (LinearLayout) findViewById(R.id.lnrCheckIn);
        lnrComment = (LinearLayout) findViewById(R.id.lnrComment);
        lnrFavorite = (LinearLayout)findViewById(R.id.lnrFavourite);
        lnrRate = (LinearLayout)findViewById(R.id.lnrRate);
        lnrShare = (LinearLayout)findViewById(R.id.lnrShare);
        btnContact = (Button) findViewById(R.id.btnContact);
        imgHeart = (ImageView) findViewById(R.id.imgHeart);
        fabMoreInfo = (FloatingActionButton) findViewById(R.id.fabMoreInfo);
        fabOffer= (FloatingActionButton) findViewById(R.id.fabOffer);
        fabLocation = (FloatingActionButton) findViewById(R.id.fabLocation);
        //get Restaurant
        int restID;
        Intent intent = this.getIntent();
        restID = intent.getIntExtra("restID", -1);
        restaurant = FoodMapManager.findRestaurant(restID);

        if(restaurant == null)
        {
            Log.i(TAG,"can't get restaurant data");
            Toast.makeText(this,"can't get restaurant data", Toast.LENGTH_LONG).show();
        }
        else
        {
            isGuestLogin = FoodMapApiManager.isGuestLogin();
            setLayoutInfo();

            //set phone call event
            PhoneStateListener phoneStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);


            btnContact.setOnClickListener(this);
            lnrRate.setOnClickListener(this);
            lnrShare.setOnClickListener(this);
            lnrFavorite.setOnClickListener(this);
            lnrComment.setOnClickListener(this);
            lnrCheckIn.setOnClickListener(this);
            fabMoreInfo.setOnClickListener(this);
            fabOffer.setOnClickListener(this);
            fabLocation.setOnClickListener(this);
        }

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

    private void setLayoutInfo() {
        //set status favorite
        if(isGuestLogin) {
            if (Guest.getInstance().isFavoriteRestaurant(restaurant.getId())) {
                imgHeart.setImageResource(R.drawable.ic_red_heart);
            }
        }

        //set Description Image
        DownloadImageTask taskDownload = new DownloadImageTask(imgDescription, getApplicationContext());
        taskDownload.loadImageFromUrl(restaurant.getUrlImage());

        //set Restaurant name
        txtRestName.setText(restaurant.getName());

        //Set a number of  check-in
        txtNCheckIn.setText(Integer.toString(restaurant.getNum_checkin()));

        // Set a number of comments
        txtNComment.setText(Integer.toString(restaurant.getComments().size()));

        // Set a number of favorites
        txtNFavorite.setText(Integer.toString(restaurant.getnFavorites()));

        //Set a number of shares
        txtNShare.setText(Integer.toString(restaurant.getnShare()));

        //Set a number of rates
        double averageRate = 0;
        for(Map.Entry<String, Integer> kvp : restaurant.getRanks().entrySet()) {
            averageRate += kvp.getValue();
        }
        if(restaurant.getRanks().size() != 0) {
            txtNRate.setText(String.format("%.1f", averageRate / restaurant.getRanks().size()));
        }

        //set Time and Status
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
        txtDescription.setText((restaurant.getDescription()));

        //Set Price range of restaurant
        int minPrice = 0;
        int maxPrice = 0;
        try {
            minPrice = Collections.min(restaurant.getDishes(), new Comparator<Dish>() {
                @Override
                public int compare(Dish o1, Dish o2) {
                    return ((Integer) o1.getPrice()).compareTo((Integer) o2.getPrice());
                }
            }).getPrice();

            maxPrice = Collections.max(restaurant.getDishes(), new Comparator<Dish>() {
                @Override
                public int compare(Dish o1, Dish o2) {
                    return ((Integer) o1.getPrice()).compareTo((Integer) o2.getPrice());
                }
            }).getPrice();
        }catch (Exception e){
            minPrice = 0;
            maxPrice = 0;
        }

        if(minPrice == maxPrice)
            txtPrice.setText(Integer.toString(minPrice) + " VND");
        else
            txtPrice.setText(String.format("%d VND - %d VND", minPrice, maxPrice));

        //set Menu
        DishInfoListAdapter dishInfoList = new DishInfoListAdapter(this, R.layout.adapter_dish_info_list, restaurant.getDishes());
        lstDish.setAdapter(dishInfoList);
        //make the list view don't have scroll
        justifyListViewHeightBasedOnChildren(lstDish);

        lstDish.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(RestaurantInfoActivity.this, DishDetailActivity.class);
                intent.putExtra("dish", restaurant.getDishes().get(position));
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.lnrCheckIn:
                clickOnCheckInEvent();
                break;
            case R.id.lnrComment:
                clickOnCommentEvent();
                break;
            case R.id.lnrFavourite:
                clickOnFavoriteEvent();
                break;
            case R.id.lnrRate:
                clickOnRateEvent();
                break;
            case R.id.lnrShare:
                clickOnShareEvent();
                break;
            case R.id.btnContact:
                clickOnContactEvent();
                break;
            case R.id.fabMoreInfo:
                if(isMoreInfoClick)
                {
                    fabOffer.hide();
                    fabLocation.hide();
                    isMoreInfoClick = false;
                }
                else {
                    fabOffer.show();
                    fabLocation.show();
                    isMoreInfoClick = true;
                }
                break;
            case R.id.fabOffer:
                //add offer here
                Intent intentOfferAct = new Intent(RestaurantInfoActivity.this, DiscountListInfoActivity.class);
                intentOfferAct.putExtra("rest", (Serializable) restaurant);
                startActivity(intentOfferAct);
                break;
            case R.id.fabLocation:
                Intent intent = new Intent(RestaurantInfoActivity.this, MapActivity.class);
                intent.putExtra("endPoint", (Serializable) restaurant.getLocation());
                startActivity(intent);
                break;
        }
    }

    private void clickOnCheckInEvent() {
        if(isGuestLogin) {
            Intent intent = new Intent(RestaurantInfoActivity.this, CheckInActivity.class);
            intent.putExtra("restID", restaurant.getId());
            startActivity(intent);
        }
        else{
            Toast.makeText(this, "You must login first", Toast.LENGTH_LONG).show();
        }
    }

    private void clickOnCommentEvent(){
        Intent intent = new Intent(RestaurantInfoActivity.this, CommentActivity.class);
        intent.putExtra("rest", restaurant);
        startActivityForResult(intent, CM_ID);
    }

    private void clickOnFavoriteEvent() {
        //check login
        if (isGuestLogin) {
            lnrFavorite.setClickable(false);

            //kiem tra da ton tai trong ds yeu thich chua
            if (!Guest.getInstance().isFavoriteRestaurant(restaurant.getId())) {
                FoodMapApiManager.addFavorite(Guest.getInstance().getEmail(), restaurant.getId(), new TaskCompleteCallBack() {
                    @Override
                    public void OnTaskComplete(Object response) {
                        int code = (int) response;

                        if (code == ConstantCODE.SUCCESS) {
                            Guest.getInstance().getFavRestaurant().add(restaurant);
                            imgHeart.setImageResource(R.drawable.ic_red_heart);
                            //reset a number of favorites
                            restaurant.setnFavorites(restaurant.getnFavorites() + 1);
                            txtNFavorite.setText(Integer.toString(restaurant.getnFavorites()));

                            Log.e(TAG, "Add favorite restaurant successfully");
                            Toast.makeText(RestaurantInfoActivity.this, "This restaurant has been your favorites", Toast.LENGTH_LONG).show();

                        } else {
                            Log.e(TAG, "Error transfer data");
                            Toast.makeText(RestaurantInfoActivity.this, "Add error " + Integer.toString(code), Toast.LENGTH_LONG).show();
                        }

                        lnrFavorite.setClickable(true);
                    }
                });

            } else {
                FoodMapApiManager.deleteFavorite(Guest.getInstance().getEmail(), restaurant.getId(), new TaskCompleteCallBack() {
                    @Override
                    public void OnTaskComplete(Object response) {
                        int code = (int) response;

                        if (code == ConstantCODE.SUCCESS) {
                            Guest.getInstance().removeFavoriteRestaurant(restaurant.getId());
                            restaurant.setnFavorites(restaurant.getnFavorites() - 1);
                            imgHeart.setImageResource(R.drawable.ic_heart);
                            //set nFavorite
                            txtNFavorite.setText(Integer.toString(restaurant.getnFavorites()));

                            Log.e(TAG, "Delete favorite restaurant successfully");
                            Toast.makeText(RestaurantInfoActivity.this, "This restaurant was removed out your favorites", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Error transfer data");
                            Toast.makeText(RestaurantInfoActivity.this, "Delete error", Toast.LENGTH_LONG).show();
                        }

                        lnrFavorite.setClickable(true);
                    }
                });
            }
        } else {
            Toast.makeText(this, "You must login first", Toast.LENGTH_LONG).show();
        }
    }

    private void clickOnRateEvent() {
        //check login
        if (isGuestLogin) {

            final Dialog dialog = new Dialog(RestaurantInfoActivity.this);
            dialog.setContentView(R.layout.dialog_rating);
            dialog.setCanceledOnTouchOutside(true);
            dialog.show();

            final RatingBar rtbRate = (RatingBar) dialog.findViewById(R.id.rtbRate);
            //set rank that rated by this user
            try {
                rtbRate.setRating(restaurant.getRanks().get(Guest.getInstance().getEmail()));
            }catch (Exception e){
                rtbRate.setRating(0);
            }
            Button btnRateSubmit = (Button) dialog.findViewById(R.id.btnRateSubmit);
            btnRateSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    FoodMapApiManager.addRank(Guest.getInstance().getEmail(), restaurant.getId(),  (int) rtbRate.getRating(),
                            new TaskCompleteCallBack() {
                                @Override
                                public void OnTaskComplete(Object response) {
                                    int code = (int) response;

                                    if (code == ConstantCODE.SUCCESS) {
                                        //update data to local database
                                        FoodMapManager.addRankRestaurant(restaurant.getId(), Guest.getInstance().getEmail(), (int) rtbRate.getRating());

                                        //reset rate average
                                        restaurant.getRanks().put(Guest.getInstance().getEmail(), (int) rtbRate.getRating());
                                        double averageRate = 0;
                                        for(Map.Entry<String, Integer> kvp : restaurant.getRanks().entrySet()) {
                                            averageRate += kvp.getValue();
                                        }
                                        if(restaurant.getRanks().size() != 0) {
                                            txtNRate.setText(String.format("%.1f", averageRate / restaurant.getRanks().size()));
                                        }
                                        Log.e(TAG, "Submit rate successfully");
                                        Toast.makeText(RestaurantInfoActivity.this, "Thank you for your submit", Toast.LENGTH_LONG).show();

                                    } else {
                                        Log.e(TAG, "Error transfer data");
                                        Toast.makeText(RestaurantInfoActivity.this, "Submit error " + Integer.toString(code), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    dialog.dismiss();
                }
            });
        } else {
            Toast.makeText(this, "You must login first", Toast.LENGTH_LONG).show();
        }
    }

    private void clickOnShareEvent(){
        if(isGuestLogin) {

            //Init Facebook
            callbackManager = CallbackManager.Factory.create();
            shareDialog = new ShareDialog(this);

            shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {
                    //reset a number of shares
                    restaurant.setnShare(restaurant.getnShare() + 1);
                    FoodMapApiManager.addShare(restaurant.getId(), Guest.getInstance().getEmail(), new TaskCompleteCallBack() {
                        @Override
                        public void OnTaskComplete(Object response) {
                            int code = (int)response;
                            if(code == FoodMapApiManager.SUCCESS){
                                txtNShare.setText(Integer.toString(restaurant.getnShare()));
                                FoodMapManager.addShare(restaurant.getId());
                            }
                        }
                    });
                }

                @Override
                public void onCancel() {
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(RestaurantInfoActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            //Make the target
            Target target = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    SharePhoto sharePhoto = new SharePhoto.Builder()
                            .setBitmap(bitmap)
                            .build();

                    ShareHashtag shareHashtag = new ShareHashtag.Builder()
                            .setHashtag("#whatever")
                            .build();

                    SharePhotoContent shareContent = new SharePhotoContent.Builder()
                            .addPhoto(sharePhoto)
                            .setShareHashtag(shareHashtag)
                            .build();


                    if (ShareDialog.canShow(SharePhotoContent.class)) {
                        shareDialog.show(RestaurantInfoActivity.this, shareContent);

                    }
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }


                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            //load image on facebook
            Picasso.get()
                    .load(restaurant.getUrlImage())
                    .into(target);
        }
        else
        {
            Toast.makeText(this, "You must login first", Toast.LENGTH_LONG).show();
        }
    }

    private void clickOnContactEvent(){
        Toast.makeText(this, restaurant.getPhoneNumber(), Toast.LENGTH_LONG).show();
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(callIntent);
    }

    //region
    //
    // This method is get the adapter of the listview and calculate it size when all items are show
    // Then we will have some thing like *ListView without Scrolling*
    //
    // Source: https://stackoverflow.com/questions/4338185/how-to-get-a-non-scrollable-listview
    //endregion
    // TODO REMEMBER to call this method every time the ListView adapter has changed (updated/deleted)
    public void justifyListViewHeightBasedOnChildren (ListView listView) {

        ListAdapter adapter = listView.getAdapter();

        if (adapter == null) {
            return;
        }
        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1)) + 230;
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CM_ID){
            if (resultCode == Activity.RESULT_OK){
                boolean isRefesh = data.getBooleanExtra("isRefesh", false);
                if (isRefesh){
                    int id_rest = restaurant.getId();
                    restaurant = FoodMapManager.findRestaurant(id_rest);
                    if (restaurant == null)
                        RestaurantInfoActivity.this.finish();
                }
            }
        }
        try {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        //reset a number of check in
        txtNCheckIn.setText(Integer.toString(restaurant.getNum_checkin()));

        //reset  a number of comments
        txtNComment.setText(Integer.toString(restaurant.getComments().size()));

    }
}