
package ca.mudar.mtlaucasou.io;

import ca.mudar.mtlaucasou.provider.PlacemarkContract;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.FireHalls;
import ca.mudar.mtlaucasou.provider.PlacemarkContract.SyncColumns;
import ca.mudar.mtlaucasou.utils.Lists;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;
//import android.util.Log;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RemotePlacemarksHandler extends XmlHandler {
    private static final String TAG = "RemotePlacemarksHandler";

    Uri contentUri;
    Boolean bFormatting = false; // requires regex formatting

    public RemotePlacemarksHandler(Uri uri) {
        super(PlacemarkContract.CONTENT_AUTHORITY);
        contentUri = uri;
    }

    /**
     * @param uri
     * @param needsFormatting Used for FireHalls and SpvmStations to add a
     *            leading zero (using regex)
     */
    public RemotePlacemarksHandler(Uri uri, boolean needsFormatting) {
        super(PlacemarkContract.CONTENT_AUTHORITY);
        contentUri = uri;
        bFormatting = needsFormatting;
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver)
            throws XmlPullParserException, IOException {
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

        String mTag = null;
        HashMap<String, String> locationInfo = new HashMap<String, String>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(FireHalls.CONTENT_URI);

        // Log.d(TAG, "CONTENT_URI = " + contentUri);

        int eventType = parser.getEventType();

        DecimalFormat df = new DecimalFormat("00");
        Pattern p = Pattern.compile("[0-9]+");

        int fakeId = 1;
        while (eventType != XmlPullParser.END_DOCUMENT) {

            if (eventType == XmlPullParser.START_DOCUMENT) {

            } else if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals(RemoteTags.ITEM)) {
                    locationInfo = new HashMap<String, String>();
                } else {
                    mTag = parser.getName();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                // TODO: handle status != OK
                if (parser.getName().equals(RemoteTags.ITEM)) {

                    builder = ContentProviderOperation.newInsert(contentUri);

                    String coords[] = locationInfo.get(RemoteTags.COORDINATES).split(",");
                    String name = locationInfo.get(RemoteTags.NAME);

                    if (bFormatting) {
                        Matcher m = p.matcher(name);

                        while (m.find()) {
                            name = name.replace(m.group(), df.format(Integer.parseInt(m.group())));
                        }
                    }

                    // Log.v(TAG, fakeId + " " + name);
                    // TODO: handle remote/local timestamp
                    builder.withValue(SyncColumns.UPDATED, System.currentTimeMillis());
                    builder.withValue(FireHalls.PLACEMARK_ID, fakeId++);
                    builder.withValue(FireHalls.PLACEMARK_NAME, name);
                    // TODO: The description is not used currently
                    // builder.withValue(FireHalls.PLACEMARK_DESCRIPTION,
                    // locationInfo.get(RemoteTags.DESCRIPTION));
                    builder.withValue(FireHalls.PLACEMARK_ADDRESS,
                            locationInfo.get(RemoteTags.ADDRESS));
                    builder.withValue(FireHalls.PLACEMARK_GEO_LAT,
                            Double.parseDouble(coords[0]));
                    builder.withValue(FireHalls.PLACEMARK_GEO_LNG,
                            Double.parseDouble(coords[1]));

                    batch.add(builder.build());
                }
                mTag = null;
            } else if ((eventType == XmlPullParser.TEXT) && (mTag != null)) {
                locationInfo.put(mTag, parser.getText());
            }
            eventType = parser.next();
        }
        return batch;
    }

    /** Remote columns */
    private interface RemoteTags {
        // String FIRE_HALL_ID = "id";
        String ITEM = "Placemark";
        String NAME = "name";
        String DESCRIPTION = "description";
        String ADDRESS = "info_side_bar";
        // String GEO_POINT = "Point";
        String COORDINATES = "coordinates";
    }
}
