package com.hcmus.dreamers.foodmap.event;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import org.osmdroid.api.IMapController;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class LocationChange implements LocationListener {

    private MapView mMap;
    private MyLocationNewOverlay mLocationOverlay;
    private IMapController mapController;

    public LocationChange(MapView mMap, MyLocationNewOverlay mLocationOverlay, IMapController mapController) {
        this.mMap = mMap;
        this.mLocationOverlay = mLocationOverlay;
        this.mapController = mapController;
    }

    @Override
    public void onLocationChanged(Location location) {
        double mLatitude = location.getLatitude();
        double mLongtitude = location.getLongitude();

        GeoPoint gpt = new GeoPoint(mLatitude, mLongtitude);
        mapController.setCenter(gpt);
        // xóa vị trí cũ
        mMap.getOverlays().remove(this.mLocationOverlay);
        //mMap.getOverlays().clear();
        // add vị trí mới
        mMap.getOverlays().add(this.mLocationOverlay);
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
}
