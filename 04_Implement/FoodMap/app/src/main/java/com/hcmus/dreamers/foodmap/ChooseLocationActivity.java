package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.Model.Restaurant;
import com.hcmus.dreamers.foodmap.adapter.PlaceAutoCompleteApdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.database.FoodMapManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.map.ZoomLimitMapView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChooseLocationActivity extends AppCompatActivity implements View.OnClickListener {

    String address;
    GeoPoint restPoint;

    AutoCompleteTextView atclSearch;
    ImageView igvDone;
    ImageView igvMyLocation;
    TextView currentStreetAddress;

    Toolbar toolbar;

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private IMapController mapController;
    MapEventsOverlay OverlayEvents;// on map click event
    private Marker marker;          // the one and only one marker

    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_location);

        mapInit();

        igvDone = (ImageView) findViewById(R.id.igv_done);
        igvDone.setOnClickListener(this);

        igvMyLocation = (ImageView)findViewById(R.id.igv_mylocation);
        igvMyLocation.setOnClickListener(this);

        searchAutoCompleteSupportInit();

        toolbar = (Toolbar)findViewById(R.id.choose_loaction_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //getData();
        currentStreetAddress = findViewById(R.id.currentStreetAddress);
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
            if (restPoint != null){
                intent.putExtra("lat", restPoint.getLatitude());
                intent.putExtra("lon", restPoint.getLongitude());
            }
            intent.putExtra("address", currentStreetAddress.getText());
            setResult(Activity.RESULT_OK, intent);

            ChooseLocationActivity.this.finish();
        }
        else if (id == R.id.igv_mylocation) {
            if (mLocationOverlay.getMyLocation() != null){
                mapController.setZoom(17.0);
                moveCamera(mLocationOverlay.getMyLocation());
                restPoint = mLocationOverlay.getMyLocation();
                updateMarker(mLocationOverlay.getMyLocation());
            }
        }
    }


    private void mapInit()
    {
        mMap = (ZoomLimitMapView) findViewById(R.id.map);
        // cài đặt map
        ((ZoomLimitMapView) mMap).initZoomLimit();
        ((ZoomLimitMapView) mMap).initScaleBar();
        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(17.0);
        mMap.setTileSource(TileSourceFactory.MAPNIK);


        // cài đặt marker vị trí
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ChooseLocationActivity.this),mMap);
        Bitmap iconMyLocation = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mylocation);
        mLocationOverlay.setPersonIcon(iconMyLocation);
        mLocationOverlay.disableFollowLocation();

        if (mLocationOverlay.getMyLocation() != null)
            restPoint = new GeoPoint(mLocationOverlay.getMyLocation().getLatitude(), mLocationOverlay.getMyLocation().getLongitude());

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));

        mapController.setCenter(this.mLocationOverlay.getMyLocation());
        // thêm marker vào
        mMap.getOverlays().add(this.mLocationOverlay);
        //
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                restPoint = p;
                updateMarker(restPoint);
                moveCamera(restPoint);
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        };

        OverlayEvents = new MapEventsOverlay(mReceive);
        mMap.getOverlays().add(OverlayEvents);

        GeoPoint lastKnownPoint = new GeoPoint(mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER));
        Drawable drawable = getResources().getDrawable(R.drawable.ic_restaurant_marker);
        marker = new Marker(mMap);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setIcon(drawable);
        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                GeoPoint point = marker.getPosition();
                Restaurant restaurant = FoodMapManager.findRestaurant(point);

                if (restaurant != null){
                    Intent intent = new Intent(ChooseLocationActivity.this, RestaurantInfoActivity.class);
                    intent.putExtra("rest", (Serializable) restaurant);
                    startActivity(intent);
                }
                return true;
            }
        });
        mMap.getOverlayManager().add(marker);
        updateMarker(lastKnownPoint);
    }

    // thêm một marker vào map
    private void updateMarker(GeoPoint point) {
        marker.setPosition(point);
        showAddressFromPoint(point);
        mMap.invalidate();

    }
    private void moveCamera(GeoPoint point){
        mapController.animateTo(point);
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
                restPoint = detailAddresses.get(position).getPoint();

                atclSearch.setText(address);
                updateMarker(restPoint);
                moveCamera(restPoint);
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
                else if ((int)response != ConstantCODE.NOTINTERNET){
                    Toast.makeText(ChooseLocationActivity.this,"Kiểm tra kết nối internet của bạn", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showAddressFromPoint(GeoPoint centerPoint) {
        FoodMapApiManager.getAddressFromPoint(centerPoint, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                String streetAddress = (String) response;
                currentStreetAddress.setText("");
                if (streetAddress != null) {
                    currentStreetAddress.setText(streetAddress);
                } else {
                    Toast.makeText(ChooseLocationActivity.this, "Kiểm tra kết nối Internet của bạn", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
