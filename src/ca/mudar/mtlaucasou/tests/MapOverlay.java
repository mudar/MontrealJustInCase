
package ca.mudar.mtlaucasou.tests;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import android.util.Log;
import android.view.MotionEvent;

class MapOverlay extends com.google.android.maps.Overlay {
    protected static final String TAG = "MapOverlay";
    private boolean isPinch = false;

    @Override
    public boolean onTap(GeoPoint p, MapView map)
    {
        if (isPinch)
        {
            return false;
        }
        else
        {
            Log.i(TAG, "TAP!");
            if (p != null)
            {
                Log.e(TAG, "handled the tap closing bubble");
                return true; // We handled the tap
            }
            else
            {
                return false; // Null GeoPoint
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView)
    {
        int fingers = e.getPointerCount();
        if (e.getAction() == 0)
            isPinch = false; // touch down, don't know it's a pinch yet
        if (e.getAction() == 2 && fingers == 2)
            isPinch = true; // Two fingers, def a pinch
        return super.onTouchEvent(e, mapView);
    }

}
