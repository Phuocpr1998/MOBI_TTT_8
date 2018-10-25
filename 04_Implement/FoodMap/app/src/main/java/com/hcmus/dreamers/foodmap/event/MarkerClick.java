package com.hcmus.dreamers.foodmap.event;

import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

public class MarkerClick implements ItemizedIconOverlay.OnItemGestureListener<OverlayItem> {
    @Override
    public boolean onItemSingleTapUp(int i, OverlayItem overlayItem) {

        return false;
    }

    @Override
    public boolean onItemLongPress(int i, OverlayItem overlayItem) {

        return false;
    }
}
