package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.DoingTask;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.AsyncTask.TaskRequest;
import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.adapter.PlaceAutoCompleteApdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapManager;
import com.hcmus.dreamers.foodmap.common.GenerateRequest;
import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.jsonapi.ParseJSON;

import org.json.JSONException;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChooseLocationActivity extends AppCompatActivity implements View.OnClickListener {

    String address;
    GeoPoint point;
    private static final int PERMISSION_CODEREQUEST = 9001;

    AutoCompleteTextView atclSearch;
    ImageView igvDone;
    ImageView igvMyLocation;

    Toolbar toolbar;

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private IMapController mapController;
    private boolean isPermissionOK;
    private ArrayList<OverlayItem> markers;

    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        mMap = (MapView)findViewById(R.id.map);
        isPermissionOK = false;
        mapInit();

        igvDone = (ImageView) findViewById(R.id.igv_done);
        igvDone.setOnClickListener(this);

        igvMyLocation = (ImageView)findViewById(R.id.igv_mylocation);
        igvMyLocation.setOnClickListener(this);

        searchAutoCompleteSupportInit();

        toolbar = (Toolbar)findViewById(R.id.choose_loaction_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getData();
    }

    void getData(){
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        if (address != null){
            atclSearch.setText(address);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMap.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMap.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            ChooseLocationActivity.this.finish();
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.igv_done){
            address = atclSearch.getText().toString();

            Intent intent = new Intent();
            intent.putExtra("lat", point.getLatitude());
            intent.putExtra("lon", point.getLongitude());
            intent.putExtra("address", address);
            setResult(Activity.RESULT_OK, intent);

            ChooseLocationActivity.this.finish();
        }
        else if (id == R.id.igv_mylocation) {
            mapController.setZoom(17.0);
            moveCamera(mLocationOverlay.getMyLocation());
        }
    }

    // kiểm tra permission
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkPermission(){
        isPermissionOK = true;
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

        for (String permission: permissions){
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED ) {
                requestPermissions(permissions,PERMISSION_CODEREQUEST);
                isPermissionOK = false;
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        isPermissionOK = true;
        for (int i = 0; i <grantResults.length;i++)
        {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
            {
                isPermissionOK = false;
                break;
            }
        }
    }

    private void mapInit()
    {
        // cài đặt map
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(17.0);
        mMap = (MapView) findViewById(R.id.map);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        //list marker
        markers = new ArrayList<OverlayItem>();

        // check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        // cài đặt event location change
        if (!isPermissionOK)
            return;

        // cài đặt marker vị trí
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ChooseLocationActivity.this),mMap);
        Bitmap iconMyLocation = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mylocation);
        mLocationOverlay.setPersonIcon(iconMyLocation);
        mapController.setCenter(this.mLocationOverlay.getMyLocation());
        // thêm marker vào
        mMap.getOverlays().add(this.mLocationOverlay);

        point = new GeoPoint(mLocationOverlay.getMyLocation().getLatitude(), mLocationOverlay.getMyLocation().getLongitude());

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));
    }

    // thêm một marker vào map
    private ItemizedOverlayWithFocus<OverlayItem> addMarker(String title, String description, GeoPoint point){
        markers.clear();
        markers.add(new OverlayItem(title, description, point)); // Lat/Lon decimal degrees
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(ChooseLocationActivity.this, markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                GeoPoint point = new GeoPoint(overlayItem.getPoint().getLatitude(), overlayItem.getPoint().getLongitude());
                Restaurant restaurant = FoodMapManager.findRestaurant(point);

                if (restaurant != null){
                    Intent intent = new Intent(ChooseLocationActivity.this, RestaurantInfoActivity.class);
                    intent.putExtra("rest", (Serializable) restaurant);
                    startActivity(intent);
                }
                return false;
            }
        });
        mOverlay.setFocusItemsOnTap(true);

        // thêm marker vào map và chỉ một marker được tồn tại
        mMap.getOverlays().clear();
        mMap.getOverlays().add(mOverlay);
        mMap.invalidate();
        return mOverlay;
    }
    private void moveCamera(GeoPoint point){
        mapController.setCenter(point);
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
        placeAutoCompleteApdapter = new PlaceAutoCompleteApdapter(ChooseLocationActivity.this, R.layout.item_detailaddress_list, detailAddresses);
        atclSearch.setAdapter(placeAutoCompleteApdapter);
        atclSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = detailAddresses.get(position).getName();
                String address = detailAddresses.get(position).toString();
                point = detailAddresses.get(position).getPoint();

                atclSearch.setText(address);
                addMarker(name, address, point);
                moveCamera(point);
                Toast.makeText(ChooseLocationActivity.this, detailAddresses.get(position).toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    void refeshListAddressSearch(String address){
        TaskRequest taskRequest = new TaskRequest();
        taskRequest.setOnCompleteCallBack(new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String rep = response.toString();
                if (rep != null)
                {
                    try {
                        detailAddresses.clear();
                        detailAddresses.addAll(ParseJSON.parseDetailAddress(rep));
                        placeAutoCompleteApdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        taskRequest.execute(new DoingTask(GenerateRequest.getAddressForSearch(address)));
    }
}