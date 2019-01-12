package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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
import com.hcmus.dreamers.foodmap.map.LocationDirection;
import com.hcmus.dreamers.foodmap.map.ZoomLimitMapView;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity{

    private MapView mMap;
    private LocationChange mLocation;
    private LocationManager mLocMgr;
    private IMapController mapController;
    private GeoPoint startPoint;
    private GeoPoint endPoint;
    private ArrayList<OverlayItem> markers;
    private List<DetailAddress> detailAddresses;
    private PlaceAutoCompleteApdapter placeAutoCompleteApdapter;
    private UpdateRoadTask updateRoadTask;

    FloatingActionButton fabSearch;

    private GeoPoint startPointEx;
    private String startName;
    private String startAddress;
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
            if (location == null)
                location = mLocMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location == null)
            {
                Toast.makeText(MapActivity.this, "Không thể lấy được vị trí hiện tại", Toast.LENGTH_LONG).show();
                finish();
            }
            double lat = location.getLatitude();
            double lon = location.getLongitude();
            startPoint = new GeoPoint(lat, lon);

            //add marker of end point
            addMarker("title","description",endPoint);
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
        ((ZoomLimitMapView) mMap).initZoomLimit();
        ((ZoomLimitMapView) mMap).initScaleBar();
        if (Build.VERSION.SDK_INT >= 16)
            mMap.setHasTransientState(true);

        mapController = mMap.getController();
        mapController.setZoom(17.0);
        mMap.setTileSource(TileSourceFactory.MAPNIK);

        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocation = new LocationChange(MapActivity.this, mMap, mapController, false);
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocation);
        mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocation);
    }

    private void addMarker(String title, String description, GeoPoint point){
        markers.clear();
        OverlayItem marker = new OverlayItem(title, description, point);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_restaurant_marker);
        marker.setMarker(drawable);
        markers.add(marker); // Lat/Lon decimal degrees
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
    }

    private void showingPath(){
        //moving camera to the start point
        moveCamera(startPoint);

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
                    if (mLocation.getMyLocation() != null)
                    {
                        moveCamera(mLocation.getMyLocation());
                    }
                }
            }
        });
        updateRoadTask.execute(pointsList);
    }

    void searchAutoCompleteSupportInit(){
        final Dialog dialog = new Dialog(MapActivity.this);
        dialog.setContentView(R.layout.dialog_input_location);
        dialog.setCanceledOnTouchOutside(true);
        int width = (int)(getResources().getDisplayMetrics().widthPixels*0.90);
        int height = (int)(getResources().getDisplayMetrics().heightPixels*0.40);
        dialog.getWindow().setLayout(width, height);
        dialog.show();

        final AutoCompleteTextView atclStart = (AutoCompleteTextView) dialog.findViewById(R.id.atclStart);
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
                if (address.length() > 1)
                    refeshListAddressSearch(address);
            }
        });

        detailAddresses = new ArrayList<DetailAddress>();
        placeAutoCompleteApdapter = new PlaceAutoCompleteApdapter(MapActivity.this,
                R.layout.item_detailaddress_list, detailAddresses);

        atclStart.setAdapter(placeAutoCompleteApdapter);

        atclStart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startName = detailAddresses.get(position).getName();
                startAddress = detailAddresses.get(position).toString();
                startPointEx = detailAddresses.get(position).getPoint();
                atclStart.setText( startAddress);
            }
        });

        btnFindWay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startPointEx != null && endPoint != null) {
                    updateRoadTask.removePolyline();
                    startPoint = startPointEx;
                    addMarker(startName, startAddress, startPointEx);
                    addMarker("Title", "Description", endPoint);
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
