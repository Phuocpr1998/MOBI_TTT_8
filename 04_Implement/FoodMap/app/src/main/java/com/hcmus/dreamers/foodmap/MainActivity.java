package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hcmus.dreamers.foodmap.AsyncTask.DownloadImageTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.Model.Guest;
import com.hcmus.dreamers.foodmap.Model.Owner;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.adapter.PlaceAutoCompleteApdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.common.MathUtils;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.define.ConstantURL;
import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.map.ZoomLimitMapView;
import com.hcmus.dreamers.foodmap.service.OrderService;
import com.hcmus.dreamers.foodmap.websocket.OrderSocket;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private AutoCompleteTextView atclSearch;
    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;

    private ImageView igvMyLocation;

    private DrawerLayout drawerLayout;
    private NavigationView navigationMenu;

    private ZoomLimitMapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private IMapController mapController;

    private ArrayList<OverlayItem> markers;
    private ItemizedOverlayWithFocus<OverlayItem> markerTemp;

    private Button btnSearchArea;           //Nút tìm kiếm/cập nhật dữ liệu khu vực
    private LinearLayout internalWrapper;   //layout con của HorizalScrollView, chứa các layout của phần tử restaurant

    // Phục vụ việc xử lý sự kiện vuốt của HorizontalScrollView
    private HorizontalScrollView horizontalScrollView;
    private GestureDetector gestureDetector;
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private int selectedRestaurant = 0;

    // Lưu các quán ăn trong khu vực màn hình đt
    private List<Restaurant> restaurants;
    private int width;

    // Dùng để đặt tiêu chí so sánh giữa 2 nhà hàng (khoảng cách, comment,...)
    // mặc định sắp xếp theo khoảng cách
    Comparator<Restaurant> restaurantComparator = new Comparator<Restaurant>() {
        @Override
        public int compare(Restaurant o1, Restaurant o2) {
            double distanceToRest1, distanceToRest2;
            distanceToRest1 = getDistanceToUser(o1.getLocation());
            distanceToRest2 = getDistanceToUser(o2.getLocation());
            return Double.compare(distanceToRest1, distanceToRest2);
        }
    };

    private Marker searchReturnedMarker;

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if((Owner.getInstance() != null) && !Owner.getInstance().getEmail().equals("")  && OrderSocket.isNULL()){
            //start service
            Intent myIntent = new Intent(MainActivity.this, OrderService.class);
            myIntent.putExtra("email", Owner.getInstance().getUsername());
            // Gọi phương thức startService (Truyền vào đối tượng Intent)
            startService(myIntent);
        }
        navmenuToolbarInit();
        mMap.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //stop service
        Intent myIntent = new Intent(MainActivity.this, OrderService.class);
        // Gọi phương thức stopservice
        stopService(myIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // setup view
        mapInit();
        //
        navmenuToolbarInit();
        // init AutocompleteTextView
        searchAutoCompleteSupportInit();

        // button my location
        igvMyLocation = (ImageView) findViewById(R.id.igv_mylocation);
        igvMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLocationOverlay != null){
                    mapController.setZoom(17.0);
                    moveCamera(mLocationOverlay.getMyLocation());
                }
            }
        });

        // Thiết lập ẩn hiện nút Tìm kiếm dữ liệu khu vực
        btnSearchArea = findViewById(R.id.btnSearchArea);
        btnSearchArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearchArea.setVisibility(View.GONE);
                restaurants = getBoundingBoxData();     //Chuẩn bị sẵn dữ liệu, đổ dữ liệu này vào layout khi ng dùng ấn marker
                Collections.sort(restaurants, restaurantComparator);    //Sắp xếp theo tiêu chí đã chuẩn bị sẵn

                clearAllMarkers();
                addMarkerRestaurant();
            }
        });

        //Thiết lập sự kiến vuốt, click vào RestaurantOverview
        gestureDetector = new GestureDetector(this, new MyGestureDetector());
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        horizontalScrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL ){
                    int scrollX = horizontalScrollView.getScrollX();
                    int featureWidth = v.getMeasuredWidth();
                    selectedRestaurant = ((scrollX + (featureWidth/2))/featureWidth);
                    int scrollTo = selectedRestaurant*featureWidth;
                    horizontalScrollView.smoothScrollTo(scrollTo, 0);
                    return true;
                } else{
                    return false;
                }
            }
        });

        // Lấy chiều ngang màn hình đt thật
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;

        internalWrapper = findViewById(R.id.internalWrapper);

        restaurants = getBoundingBoxData();         // Lấy dữ liệu lần nạp đầu tiên

        // thêm restaurant
        addMarkerRestaurant();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    private void mapInit()
    {
        //
        mMap = (ZoomLimitMapView) findViewById(R.id.map);
        mMap.initZoomLimit();
        mMap.initScaleBar();

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));

        // cài đặt map
        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(15.0);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        //list marker
        markers = new ArrayList<OverlayItem>();

        // cài đặt marker vị trí
        this.mLocationOverlay = new MyLocationNewOverlay(mMap);
        Bitmap iconMyLocation = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mylocation);
        this.mLocationOverlay.setPersonIcon(iconMyLocation);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.disableFollowLocation();
        this.mLocationOverlay.setOptionsMenuEnabled(true);

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));

        mapController.setCenter(this.mLocationOverlay.getMyLocation());
        mMap.getOverlays().add(this.mLocationOverlay);

        // Khi người dùng di chuyển bản đồ, hiện nút cho phép người dùng cập nhật dữ liệu
        mMap.setMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent scrollEvent) {
                btnSearchArea.setVisibility(View.VISIBLE);
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent zoomEvent) {
                btnSearchArea.setVisibility(View.VISIBLE);
                return true;
            }
        });
    }

    // thêm một marker vào map (marker từ kết quả trả về của autocomplete)
    private void addMarker(String title, String description, GeoPoint point){
        if (searchReturnedMarker != null)
        {
            mMap.getOverlayManager().remove(searchReturnedMarker);
        }

        searchReturnedMarker = new Marker(mMap);
        searchReturnedMarker.setTitle(title);
        searchReturnedMarker.setSnippet(description);
        searchReturnedMarker.setPosition(point);
        searchReturnedMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        searchReturnedMarker.showInfoWindow();
        mMap.getOverlayManager().add(searchReturnedMarker);
    }

    // sử dụng để thêm nhiều marker cùng lúc
    private void addMarkers(List<OverlayItem> markers){
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(MainActivity.this, markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                GeoPoint point = (GeoPoint) overlayItem.getPoint();
                Restaurant restaurant = FoodMapManager.findRestaurant(point);

                if (restaurant != null){
                    selectedRestaurant = restaurants.indexOf(restaurant);           //Cập nhật chỉ mục của Rest Overview
                    Log.w("selectedItem",String.valueOf(selectedRestaurant));  //debug only

                    horizontalScrollView.setVisibility(View.VISIBLE);
                    updateRestaurantOverview();
                    View child = internalWrapper.getChildAt(selectedRestaurant);
                    internalWrapper.requestChildFocus(child, child);        // Di chuyển đến layout con tương ứng
                    mapController.animateTo(point);                         // Hiệu ứng trên bản đồ
                }

                return true;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                return false;
            }
        });

        // thêm marker vào map
        mMap.getOverlays().add(mOverlay);
        mMap.invalidate();
    }


    private void moveCamera(GeoPoint point){
        mapController.setCenter(point);
    }

    // navigation menu and toolbar init
    void navmenuToolbarInit(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,drawerLayout, toolbar, R.string.nav_open,R.string.nav_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (FoodMapApiManager.isGuestLogin()){
            initMenuLoginGuest();
        }
        else if (FoodMapApiManager.isLogin()){
            initMenuLoginOwner();
        }
        else{
            initMenuNotLogin();
        }
    }

    void initMenuLoginGuest(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // change header
        navigationMenu = (NavigationView)findViewById(R.id.nav_view);
        navigationMenu.removeHeaderView(navigationMenu.getHeaderView(0));
        navigationMenu.inflateHeaderView(R.layout.nav_header);

        Menu menu = navigationMenu.getMenu();
        menu.clear();
        getMenuInflater().inflate(R.menu.drawer_menu_guest, menu);

        View head = navigationMenu.getHeaderView(0);
        TextView txtName = (TextView)head.findViewById(R.id.txtName);
        TextView txtEmail = (TextView)head.findViewById(R.id.txtEmail);
        ImageView imgAvatar = (ImageView)head.findViewById(R.id.igvAvatar);

        Guest.getInstance().setName(user.getDisplayName());
        txtName.setText(user.getDisplayName());

        Guest.getInstance().setEmail(user.getEmail());
        txtEmail.setText(user.getEmail());

        Guest.getInstance().setUrlAvatar(user.getPhotoUrl());

        FoodMapApiManager.getFavorite(Guest.getInstance().getEmail(), new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                int code = (int) response;

                if(code == ConstantCODE.SUCCESS)
                {
                    Log.i(TAG, "Upload data to Guest successfully");
                }
                else{
                    Log.i(TAG, "Error: Can't upload data to Guest " + Integer.toString(code));
                }
            }
        });

        DownloadImageTask taskDownload = new DownloadImageTask(imgAvatar, getApplicationContext());
        String avatar = user.getPhotoUrl().toString();
        taskDownload.loadImageFromUrl(avatar);

        navigationMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.btnFavorite:
                        Log.d(TAG, "onClick: btnFavorite");
                        Intent intent = new Intent(MainActivity.this, FavoriteRestaurantsActivity.class);
                        startActivity(intent);
                        break;
                    case  R.id.btnFeedBack:
                        Log.d(TAG, "onClick: btnFeedBack");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConstantURL.LINKFORM));
                        startActivity(browserIntent);
                        break;
                    case  R.id.btnUpdate:
                        Log.d(TAG, "onClick: btnUpdate");
                        updateData();
                        break;
                    case R.id.btnLogout:
                        Log.d(TAG, "onClick: btnLogout");
                        Toast.makeText(MainActivity.this, "onClick: btnLogout", Toast.LENGTH_SHORT).show();
                        LoginManager.getInstance().logOut();
                        Guest.setInstance(null);
                        initMenuNotLogin();
                        break;
                    case R.id.btnAbout:
                        Log.d(TAG, "onClick: btnAbout");
                        showAboutDialog();
                        break;
                }
                return true;
            }
        });
    }

    void initMenuNotLogin(){
        // change header
        navigationMenu = (NavigationView)findViewById(R.id.nav_view);
        navigationMenu.removeHeaderView(navigationMenu.getHeaderView(0));
        navigationMenu.inflateHeaderView(R.layout.nav_header_notlogin);

        View head = navigationMenu.getHeaderView(0);
        View accountView = head.findViewById(R.id.accountView);

        accountView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginGuestActivity.class);
                startActivity(intent);
            }
        });


        Menu menu = navigationMenu.getMenu();
        menu.clear();
        getMenuInflater().inflate(R.menu.drawer_menu_notlogin, menu);

        navigationMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case  R.id.btnFeedBack:
                        Log.d(TAG, "onClick: btnFeedBack");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConstantURL.LINKFORM));
                        startActivity(browserIntent);
                        break;
                    case  R.id.btnUpdate:
                        Log.d(TAG, "onClick: btnUpdate");
                        updateData();
                        break;
                    case R.id.btnAbout:
                        Log.d(TAG, "onClick: btnAbout");
                        showAboutDialog();
                        break;
                }
                return true;
            }
        });

    }

    void initMenuLoginOwner(){
        // change header
        navigationMenu = (NavigationView)findViewById(R.id.nav_view);
        navigationMenu.removeHeaderView(navigationMenu.getHeaderView(0));
        navigationMenu.inflateHeaderView(R.layout.nav_header);

        Menu menu = navigationMenu.getMenu();
        menu.clear();
        getMenuInflater().inflate(R.menu.drawer_menu_owner, menu);

        View head = navigationMenu.getHeaderView(0);
        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ManageAccountActivity.class);
                startActivity(intent);
            }
        });

        TextView txtName = (TextView)head.findViewById(R.id.txtName);
        TextView txtEmail = (TextView)head.findViewById(R.id.txtEmail);
        ImageView imgAvatar = (ImageView)head.findViewById(R.id.igvAvatar);

        Owner owner =  Owner.getInstance();
        txtName.setText(owner.getUsername());

        txtEmail.setText(owner.getEmail());

        if (owner.getUrlImage() != null)
        {
            DownloadImageTask taskDownload = new DownloadImageTask(imgAvatar, getApplicationContext());
            taskDownload.loadImageFromUrl(owner.getUrlImage());
        }

        navigationMenu.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id){
                    case R.id.btnManager:
                        Log.d(TAG, "onClick: btnManager");
                        Intent main_manageRest = new Intent(MainActivity.this,
                                RestaurantManageActivity.class);
                        startActivity(main_manageRest);
                        break;
                    case  R.id.btnFeedBack:
                        Log.d(TAG, "onClick: btnFeedBack");
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ConstantURL.LINKFORM));
                        startActivity(browserIntent);
                        break;
                    case  R.id.btnUpdate:
                        Log.d(TAG, "onClick: btnUpdate");
                        updateData();
                        break;
                    case R.id.btnLogout:
                        Log.d(TAG, "onClick: btnLogout");
                        Owner.setInstance(null);
                        initMenuNotLogin();
                        break;
                    case R.id.btnAbout:
                        Log.d(TAG, "onClick: btnAbout");
                        showAboutDialog();
                        break;
                }
                return true;
            }
        });
    }

    //
    void searchAutoCompleteSupportInit(){
        atclSearch = (AutoCompleteTextView)findViewById(R.id.atclSearch);
        atclSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String address = s.toString();
                if (address.length() >= 3)
                    refeshListAddressSearch(address);
            }
        });

        detailAddresses = new ArrayList<DetailAddress>();
        placeAutoCompleteApdapter = new PlaceAutoCompleteApdapter(MainActivity.this, R.layout.item_detailaddress_list, detailAddresses);
        atclSearch.setAdapter(placeAutoCompleteApdapter);
        atclSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = detailAddresses.get(position).getName();
                String address = detailAddresses.get(position).toString();
                GeoPoint point = detailAddresses.get(position).getPoint();

                atclSearch.setText(address);
                atclSearch.clearFocus();
                addMarker(name, address, point);
                moveCamera(point);
            }
        });
    }

    void refeshListAddressSearch(String address){
        FoodMapApiManager.getDetailAddressFromString(address, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                if (response != null){
                    detailAddresses.clear();
                    detailAddresses.addAll((ArrayList<DetailAddress>) response);
                    placeAutoCompleteApdapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(MainActivity.this, "Kiểm tra kết nối internet", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void clearAllMarkers(){
        mMap.getOverlays().clear();
        mMap.getOverlays().add(this.mLocationOverlay);
    }

    void addMarkerRestaurant(){
        if (restaurants != null) {
            for (Restaurant rest : restaurants) {
                OverlayItem marker = new OverlayItem(rest.getName(), rest.getDescription(), rest.getLocation());
                Drawable drawable = getResources().getDrawable(R.drawable.ic_restaurant_marker);
                marker.setMarker(drawable);
                markers.add(marker);
            }
        }

        addMarkers(markers);
    }

    void showAboutDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        AlertDialog dialog = builder.create();
        View view = getLayoutInflater().inflate(R.layout.dialog_about, null);
        Button btnOk = view.findViewById(R.id.btnOK);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    void updateData(){
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Updating");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        FoodMapApiManager.getRestaurant(MainActivity.this, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                int code = (int)response;
                if (code == FoodMapApiManager.SUCCESS){
                    clearAllMarkers();
                    addMarkerRestaurant();
                    Toast.makeText(MainActivity.this, "Cập nhât thông tin thành công!", Toast.LENGTH_LONG).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Cập nhât thất bại!", Toast.LENGTH_LONG).show();
                }
                progressDialog.dismiss();
            }
        });
    }

    // Bounding Box definition:
    // https://wiki.openstreetmap.org/wiki/Bounding_Box
    //
    //              ------* (North, East)
    //              |     |
    //              |     |
    //(South, West) *------

    // Gọi phương thức này khi lần đầu vào hoặc khi người dùng ấn nút cập nhật dữ liệu khu vực

    private List<Restaurant> getBoundingBoxData(){
        double minLat, minLon;
        double maxLat, maxLon;
        minLat = mMap.getBoundingBox().getLatSouth();
        minLon = mMap.getBoundingBox().getLonWest();
        maxLat = mMap.getBoundingBox().getLatNorth();
        maxLon = mMap.getBoundingBox().getLonEast();

        List<Restaurant> result = new ArrayList<>();
        for (Restaurant restaurant: FoodMapManager.getRestaurants())
        {
            double Lat = restaurant.getLocation().getLatitude();
            double Lon = restaurant.getLocation().getLongitude();

            if ((minLat <= Lat && Lat <= maxLat) &&
                    (minLon <= Lon && Lon <= maxLon))
            {
                result.add(restaurant);
            }
        }
        return result;
    }

    private void updateRestaurantOverview() {
        //Xóa tất cả các layout con nếu có tồn tại trước
        if (internalWrapper.getChildCount() > 0)
        {
            internalWrapper.removeAllViews();
        }

        for(Restaurant restaurant: restaurants){
            final View restaurantOverview =  getLayoutInflater().inflate(R.layout.restaurant_overview, internalWrapper,false);
            restaurantOverview.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));

            // Lấy các biến đối tượng từ layout
            ImageView ovImageRestAvatar = restaurantOverview.findViewById(R.id.ovImageRestAvatar);
            TextView ovTxtRestName = restaurantOverview.findViewById(R.id.ovTxtRestName);
            TextView ovRestDistance = restaurantOverview.findViewById(R.id.ovRestDistance);
            RatingBar ovRatingbar = restaurantOverview.findViewById(R.id.ovRatingBar);
            TextView ovTxtTotalRate = restaurantOverview.findViewById(R.id.ovTxtTotalRate);
            TextView ovTxtTotalFavorite = restaurantOverview.findViewById(R.id.ovTxtTotalFavorite);
            TextView ovTxtTotalComment = restaurantOverview.findViewById(R.id.ovTxtTotalComment);
            TextView ovTxtTotalCheckin = restaurantOverview.findViewById(R.id.ovTxtTotalCheckin);
            TextView ovTxtTotalShare = restaurantOverview.findViewById(R.id.ovTxtTotalShare);

            // Nạp dữ liệu vào các Widget UI
            DownloadImageTask task = new DownloadImageTask(ovImageRestAvatar, this);
            task.loadImageFromUrl(restaurant.getUrlImage());
            ovTxtRestName.setText(restaurant.getName());
            ovRestDistance.setText(String.format("%.2f m",getDistanceToUser(restaurant.getLocation())));
            float avgRate = MathUtils.roundToHaft(restaurant.getAverageRate());
            ovRatingbar.setRating((float) restaurant.getAverageRate());
            ovTxtTotalRate.setText(String.format("(%d)", restaurant.getRanks().size()));
            ovTxtTotalFavorite.setText(String.valueOf(restaurant.getnFavorites()));
            ovTxtTotalComment.setText(String.valueOf(restaurant.getComments().size()));
            ovTxtTotalCheckin.setText(String.valueOf(restaurant.getNum_checkin()));
            ovTxtTotalShare.setText(String.valueOf(restaurant.getnShare()));

            internalWrapper.addView(restaurantOverview);            // Thêm layout của phần tử restaurant vào layout cha của nó *bắt buộc*
        }
    }

    private double computeDistance(GeoPoint src, GeoPoint dest) {
        return src.distanceToAsDouble(dest);
    }

    private double getDistanceToUser(GeoPoint src) {
        return computeDistance(mLocationOverlay.getMyLocation(), src);
    }

    // Quản lý các sự kiện vuốt, click vào restaurant overview
    // Vuốt ngang -> Di chuyển sang quán ăn tương ứng
    // Vuốt trên xuống dưới -> Ẩn đi Overview
    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (restaurants.isEmpty())
                    return false;

                //right to left
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.w("swipe","right to left");         //Debug only
                    int featureWidth = horizontalScrollView.getMeasuredWidth();
                    selectedRestaurant = (selectedRestaurant < (restaurants.size() - 1))? selectedRestaurant + 1:restaurants.size() -1;
                    horizontalScrollView.smoothScrollTo(selectedRestaurant *featureWidth, 0);

                    mapController.animateTo(restaurants.get(selectedRestaurant).getLocation());
                    return true;
                }
                //left to right
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Log.w("swipe", "left to right");    //Debug only

                    int featureWidth = horizontalScrollView.getMeasuredWidth();
                    selectedRestaurant = (selectedRestaurant > 0)? selectedRestaurant - 1:0;
                    horizontalScrollView.smoothScrollTo(selectedRestaurant *featureWidth, 0);

                    mapController.animateTo(restaurants.get(selectedRestaurant).getLocation());
                    return true;
                }
                // top to bottom
                else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY)
                {
                    Log.w("swipe", "top to bottom");    //Debug only

                    horizontalScrollView.setVisibility(View.GONE);
                    return true;
                }
            } catch (Exception e) {
                Log.e("Fling", "There was an error processing the Fling event:" + e.getMessage());
            }
            return false;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.w("onSingleTapUp",String.valueOf(selectedRestaurant));
            if (restaurants.isEmpty())
                return false;

            // Vào activity quán ăn
            Restaurant restaurant = restaurants.get(selectedRestaurant);
            Intent intent = new Intent(MainActivity.this, RestaurantInfoActivity.class);
            intent.putExtra("restID", restaurant.getId());
            startActivity(intent);
            return true;
        }

    }
}
