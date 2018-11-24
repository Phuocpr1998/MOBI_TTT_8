package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.Toast;

import com.hcmus.dreamers.foodmap.AsyncTask.TaskCompleteCallBack;
import com.hcmus.dreamers.foodmap.AsyncTask.UpdateRoadTask;

import com.hcmus.dreamers.foodmap.Model.DetailAddress;
import com.hcmus.dreamers.foodmap.adapter.PlaceAutoCompleteApdapter;
import com.hcmus.dreamers.foodmap.common.FoodMapApiManager;
import com.hcmus.dreamers.foodmap.define.ConstantCODE;
import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.map.ZoomLimitMapView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity{

    private ZoomLimitMapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private IMapController mapController;
    private GeoPoint startPoint;
    private GeoPoint endPoint;
    private ArrayList<OverlayItem> markers;
    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;
    private UpdateRoadTask updateRoadTask;

    FloatingActionButton fabSearch;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fabSearch = (FloatingActionButton) findViewById(R.id.fabSearch);

        //set header toolbar in the layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.tlbFindWayMap);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //list marker
        markers = new ArrayList<OverlayItem>();

        //Initialize map
        mapInit();

        //Get data (endPoint) from Intent
        Intent intent = this.getIntent();
        endPoint = (GeoPoint) intent.getSerializableExtra("endPoint");
        if(endPoint == null)
        {
            Toast.makeText(this,"Can't get data", Toast.LENGTH_SHORT).show();
        }
        else {
            //get start point
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = mLocMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            startPoint = new GeoPoint(lat, lon);

            showingPath();
        }

        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchAutoCompleteSupportInit();
            }
        });

    }
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

    private void mapInit(){
        //
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        Configuration.getInstance().setOsmdroidBasePath(new File(Environment.getExternalStorageDirectory(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(Environment.getExternalStorageDirectory(), "osmdroid/tiles"));

        // cài đặt map
        mMap = (ZoomLimitMapView) findViewById(R.id.FindWayMap);
        mMap.setBuiltInZoomControls(true);
        mMap.setMultiTouchControls(true);
        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(17.0);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MapActivity.this),mMap);
        mLocationOverlay.enableMyLocation();

        mapController.setCenter(this.mLocationOverlay.getMyLocation());
        mMap.getOverlays().add(this.mLocationOverlay);


        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));

    }

    private ItemizedOverlayWithFocus<OverlayItem> addMarker(String title, String description, GeoPoint point){

        markers.clear();
        markers.add(new OverlayItem(title, description, point)); // Lat/Lon decimal degrees
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(MapActivity.this, markers, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                return false;
            }
        });
        mOverlay.setFocusItemsOnTap(true);

        // thêm marker vào map
        mMap.getOverlays().add(mOverlay);
        mMap.invalidate();
        return mOverlay;
    }

    private void showingPath(){
        //add marker of end point
        addMarker("title","description",endPoint);

        ArrayList<GeoPoint> pointsList = new ArrayList<GeoPoint>();
        pointsList.add(startPoint);
        pointsList.add(endPoint);

        //Display the paths between 2 points
        final ProgressDialog progressDialog = new ProgressDialog(MapActivity.this);
        progressDialog.setMessage("Roading");
        progressDialog.show();

        updateRoadTask = new UpdateRoadTask(getApplicationContext(), mMap, new TaskCompleteCallBack() {
            @Override
            public void OnTaskComplete(Object response) {
                progressDialog.dismiss();

                int code = (int)response;
                if (code == ConstantCODE.NOTINTERNET){
                    Toast.makeText(MapActivity.this, "Không thể tìm được đường đi", Toast.LENGTH_LONG);
                }
                else {
                    moveCamera(mLocationOverlay.getMyLocation());
                }
            }
        });
        updateRoadTask.execute(pointsList);
    }

    void searchAutoCompleteSupportInit(){
        final Dialog dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.dialog_input_location);
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        final AutoCompleteTextView atclStart = (AutoCompleteTextView) dialog.findViewById(R.id.atclStart);
        final AutoCompleteTextView atclDestination = (AutoCompleteTextView) dialog.findViewById(R.id.atclDestination);
        Button btnFindWay = (Button) dialog.findViewById(R.id.btnFindWay);

        atclStart.addTextChangedListener(new TextWatcher() {
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

        atclDestination.addTextChangedListener(new TextWatcher() {
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
        placeAutoCompleteApdapter = new PlaceAutoCompleteApdapter(MapActivity.this,
                R.layout.item_detailaddress_list, detailAddresses);

        atclStart.setAdapter(placeAutoCompleteApdapter);
        atclDestination.setAdapter(placeAutoCompleteApdapter);

        atclStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = detailAddresses.get(position).getName();
                String address = detailAddresses.get(position).toString();
                GeoPoint point = detailAddresses.get(position).getPoint();
                startPoint = point;
                atclStart.setText(address);
                addMarker(name, address, point);
            }
        });
        atclDestination.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = detailAddresses.get(position).getName();
                String address = detailAddresses.get(position).toString();
                GeoPoint point = detailAddresses.get(position).getPoint();
                endPoint = point;
                atclDestination.setText(address);
                addMarker(name, address, point);
                mapController.setCenter(point);
            }
        });

        btnFindWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startPoint != null && endPoint != null) {
                    updateRoadTask.removePolyline();
                    showingPath();
                }

                dialog.dismiss();
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
                    Toast.makeText(MapActivity.this, "Kiểm tra kết nối internet", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    void moveCamera(GeoPoint geoPoint){
        mapController.setCenter(geoPoint);
    }
}
