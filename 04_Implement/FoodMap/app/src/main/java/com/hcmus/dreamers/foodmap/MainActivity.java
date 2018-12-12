package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
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
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.define.ConstantURL;
import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.service.OrderService;
import com.hcmus.dreamers.foodmap.websocket.OrderSocket;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private AutoCompleteTextView atclSearch;
    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;

    private ImageView igvMyLocation;

    private DrawerLayout drawerLayout;
    private NavigationView navigationMenu;

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private IMapController mapController;

    private ArrayList<OverlayItem> markers;
    private ItemizedOverlayWithFocus<OverlayItem> markerTemp;

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

        // thêm restaurant
        addMarkerRestaurant();

        //Upload data to Guest or Owner If checkLogin() == true

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
        mMap = (MapView) findViewById(R.id.map);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));

        // cài đặt map
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        mMap.setMinZoomLevel(3.0);
        mMap.setMaxZoomLevel(21.0);
        mMap.setVerticalMapRepetitionEnabled(false);
        mMap.setScrollableAreaLimitDouble(new BoundingBox(
                TileSystem.MaxLatitude, TileSystem.MaxLongitude,
                TileSystem.MinLatitude, TileSystem.MinLongitude));

        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(17.0);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        //list marker
        markers = new ArrayList<OverlayItem>();

        // cài đặt marker vị trí
        this.mLocationOverlay = new MyLocationNewOverlay(mMap);
        mMap.getOverlays().add(this.mLocationOverlay);
        Bitmap iconMyLocation = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mylocation);
        this.mLocationOverlay.setPersonIcon(iconMyLocation);
        this.mLocationOverlay.enableMyLocation();
        this.mLocationOverlay.disableFollowLocation();
        this.mLocationOverlay.setOptionsMenuEnabled(true);
        mapController.setCenter(this.mLocationOverlay.getMyLocation());

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));

    }

    // thêm một marker vào map
    private void addMarker(String title, String description, GeoPoint point){
        if (markerTemp != null){
            mMap.getOverlays().remove(markerTemp);
        }
        List<OverlayItem> overlayItems = new ArrayList<>();

        OverlayItem marker = new OverlayItem(title, description, point);
        overlayItems.add(marker); // Lat/Lon decimal degrees
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(MainActivity.this, overlayItems, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                return false;
            }
        });

        markerTemp = mOverlay;
        // thêm marker vào map
        mMap.getOverlays().add(mOverlay);
        mMap.invalidate();
    }

    // sử dụng để thêm nhiều marker cùng lúc
    private void addMarkers(List<OverlayItem> markers){
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(MainActivity.this, markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                GeoPoint point = new GeoPoint(overlayItem.getPoint().getLatitude(), overlayItem.getPoint().getLongitude());
                Restaurant restaurant = FoodMapManager.findRestaurant(point);

                if (restaurant != null){
                    Intent intent = new Intent(MainActivity.this, RestaurantInfoActivity.class);
                    intent.putExtra("restID", restaurant.getId());
                    startActivity(intent);
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
                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Updating");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        FoodMapApiManager.getRestaurant(MainActivity.this, new TaskCompleteCallBack() {
                            @Override
                            public void OnTaskComplete(Object response) {
                                progressDialog.dismiss();
                                int code = (int)response;
                                if (code == FoodMapApiManager.SUCCESS){
                                    Toast.makeText(MainActivity.this, "Cập nhât thông tin thành công!", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Cập nhât thất bại!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
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
                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Updating");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        FoodMapApiManager.getRestaurant(MainActivity.this, new TaskCompleteCallBack() {
                            @Override
                            public void OnTaskComplete(Object response) {
                                progressDialog.dismiss();
                                int code = (int)response;
                                if (code == FoodMapApiManager.SUCCESS){
                                    Toast.makeText(MainActivity.this, "Cập nhât thông tin thành công!", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Cập nhât thất bại!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
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
                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Updating");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.show();

                        FoodMapApiManager.getRestaurant(MainActivity.this, new TaskCompleteCallBack() {
                            @Override
                            public void OnTaskComplete(Object response) {
                                progressDialog.dismiss();
                                int code = (int)response;
                                if (code == FoodMapApiManager.SUCCESS){
                                    Toast.makeText(MainActivity.this, "Cập nhât thông tin thành công!", Toast.LENGTH_LONG).show();
                                }
                                else {
                                    Toast.makeText(MainActivity.this, "Cập nhât thất bại!", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
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

    void addMarkerRestaurant(){
        List<Restaurant> restaurants = FoodMapManager.getRestaurants();
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

}
