
package ca.mudar.mtlaucasou.ui.widgets;

import ca.mudar.mtlaucasou.provider.PlacemarkContract.PlacemarkColumns;
import ca.mudar.mtlaucasou.R;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Location;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PlacemarksCursorAdapter extends SimpleCursorAdapter {
    protected static final String TAG = "HistorySimpleCursorAdapter";

    private Location mLocation;

    public PlacemarksCursorAdapter(Context context, int layout, Cursor c, String[] from,
            int[] to, int flags) {
        // TODO: flags is a deprecated argument
        super(context, layout, c, from, to);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return super.newView(context, cursor, parent);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);

        // Can't calculate the distance if we don't know the current location
        if (mLocation == null) {
            return;
        }

        Resources res = context.getResources();
        TextView vDistance = (TextView) view.findViewById(R.id.placemark_distance);

        Double geoLat = cursor.getDouble(cursor.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LAT));
        Double geoLng = cursor.getDouble(cursor.getColumnIndex(PlacemarkColumns.PLACEMARK_GEO_LNG));

        float[] results = new float[1];
        android.location.Location.distanceBetween(geoLat, geoLng,
                (mLocation.getLatitude()),
                (mLocation.getLongitude()), results);

        float fDistance = (results[0] / 1000);

        // Log.v(TAG, "geoLat = "+ geoLat + "geoLng = "+ geoLng );
        //
        String sDistance;
        if (fDistance <= 1) {
            sDistance = res.getString(R.string.placemark_distance_min);
        }
        else {
            sDistance = String.format(res.getString(R.string.placemark_distance), fDistance);
        }

        // res.getString(R.string.placemark_distance);
        vDistance.setText(sDistance);

        // int transcationType =
        // cursor.getInt(cursor.getColumnIndex(Transactions.TRANSACTION_SBM_TRANSACTION_TYPE));
        // double amount =
        // cursor.getDouble(cursor.getColumnIndex(Transactions.TRANSACTION_AMOUNT));
        // String comment =
        // cursor.getString(cursor.getColumnIndex(Transactions.TRANSACTION_COMMENT));
        //
        // String sType;
        // String sAmount;
        // if (type == Const.API_TRANSACTION_TYPE_CREDIT) {
        // sAmount =
        // String.format(res.getString(R.string.transaction_type_amount_credit),
        // amount);
        // sType = res.getString(R.string.transaction_type_credit);
        // comment = sType + ". " + comment;
        // } else if (transcationType ==
        // Const.ApiTransactionType.GIFTCARD_PAYMENT) {
        // sAmount = res.getString(R.string.transaction_type_gift);
        // } else {
        // sAmount =
        // String.format(res.getString(R.string.transaction_type_amount_debit),
        // amount);
        // sType = res.getString(R.string.transaction_type_debit);
        // }
        //
        // if (vAmount != null) {
        // vAmount.setText(sAmount);
        // }
        //
        // if (vComment != null && type == Const.API_TRANSACTION_TYPE_CREDIT) {
        // vComment.setText("abc def");
        // }

    }

    public void setLocation(Location mLocation) {
        this.mLocation = mLocation;
    }
}
