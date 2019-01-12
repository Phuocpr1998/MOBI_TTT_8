package com.hcmus.dreamers.foodmap.event;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import com.hcmus.dreamers.foodmap.R;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;

public class LocationChange implements LocationListener {

    private MapView mMap;
    private ItemizedOverlayWithFocus<OverlayItem> mLocationOverlay;
    private GeoPoint point;
    private IMapController mapController;
    private Context context;
    private boolean isFollow;

    public LocationChange(Context context, MapView mMap, IMapController mapController, boolean isFollow) {
        this.mMap = mMap;
        this.mapController = mapController;
        this.context = context;
        this.isFollow = isFollow;
        mLocationOverlay = null;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("LocationChange", "onLocationChanged " + location.toString());
        double mLatitude = location.getLatitude();
        double mLongtitude = location.getLongitude();

        point = new GeoPoint(mLatitude, mLongtitude);
        addMarker(point);
    }

    public void addMarker(GeoPoint point){
        ArrayList arr = new ArrayList<>();
        OverlayItem marker = new OverlayItem("", "", point);
        Drawable drawable = context.getResources().getDrawable(R.drawable.ic_mylocation);
        marker.setMarker(drawable);
        arr.add(marker);

        // xóa vị trí cũ
        if (this.mLocationOverlay != null)
            mMap.getOverlays().remove(this.mLocationOverlay);
        else
            mapController.setCenter(point);

        this.mLocationOverlay = new ItemizedOverlayWithFocus<OverlayItem>(context, arr, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, OverlayItem overlayItem) {
                return false;
            }
        });
        // add vị trí mới
        mMap.getOverlays().add(this.mLocationOverlay);
        if (isFollow)
            mapController.setCenter(point);
        mMap.invalidate();
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    public GeoPoint getMyLocation(){
        return point;
    }
}
