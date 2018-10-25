package com.hcmus.dreamers.foodmap;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hcmus.dreamers.foodmap.event.LocationChange;
import com.hcmus.dreamers.foodmap.event.MarkerClick;

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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private LocationManager mLocMgr;
    private  IMapController mapController;
    private boolean isPermissionOK;
    private ArrayList<OverlayItem> markers;

    private static final int PERMISSION_CODEREQUEST = 9001;

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
        setContentView(R.layout.activity_main);

        mMap = (MapView) findViewById(R.id.map);
        isPermissionOK = false;

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

        // cài đặt marker vị trí
        this.mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(MainActivity.this),mMap);
        Bitmap iconMyLocation = BitmapFactory.decodeResource(getResources(),R.drawable.ic_mylocation);
        mLocationOverlay.setPersonIcon(iconMyLocation);
        mapController.setCenter(this.mLocationOverlay.getMyLocation());
        // thêm marker vào
        mMap.getOverlays().add(this.mLocationOverlay);

        // check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkPermission();
        }

        // cài đặt event location change
        if (!isPermissionOK)
            return;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //
        mLocMgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 100,
                new LocationChange(mMap, mLocationOverlay, mapController));
    }

    // thêm một marker vào map
    private ItemizedOverlayWithFocus<OverlayItem> addMarker(String title, String description, GeoPoint point){
        mapController.setZoom(17.0);
        markers.add(new OverlayItem(title, description, point)); // Lat/Lon decimal degrees
        // thêm sự kiện marker click
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<OverlayItem>(MainActivity.this, markers, new MarkerClick());
        mOverlay.setFocusItemsOnTap(true);
        // thêm marker vào map
        mMap.getOverlays().add(mOverlay);
        mMap.invalidate();
        return mOverlay;
    }

    // xóa marker ra khỏi map
    private void deleteMarker(ItemizedOverlayWithFocus<OverlayItem> marker)
    {
        mMap.getOverlays().add(marker);
        mMap.invalidate();
    }


    // kiểm tra permission
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.M)
    void checkPermission(){
        isPermissionOK = true;
        String[] permissions = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissions,PERMISSION_CODEREQUEST);
            isPermissionOK = false;
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
}
