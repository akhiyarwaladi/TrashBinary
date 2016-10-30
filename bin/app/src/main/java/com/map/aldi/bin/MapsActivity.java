package com.map.aldi.bin;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener{

    LatLng myLatLng;
    LatLng shopLatLng;
    Boolean isDirectionDrawn = false;
    Toolbar toolbar;
    private ProgressDialog pDialog;
    int posisi;
    double nearlatitude, nearlongitude, minimaldistance, time, CurrentLat, CurrentLng;
    String Lokasi;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    public static final String TAG = MapsActivity.class.getSimpleName();
    private Button btnShowRoute, btnOnSearch, btnRequestDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

        btnShowRoute = (Button) findViewById(R.id.Broute);
        btnOnSearch = (Button) findViewById(R.id.Bsearch);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


        new RetrieveTask().execute();
        initToolbar();

    }

    private void initToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMap != null) {
            mMap = null;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
        ;
    }

    private void handleNewLocation(Location location) {
        Log.d(TAG, location.toString());
        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        CurrentLat = currentLatitude;
        CurrentLng = currentLongitude;
        myLatLng = new LatLng(currentLatitude, currentLongitude);
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Saya disini");
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, (float) 17.0));
    }


    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onConnectionSuspended(int i){
        Log.i(TAG, "Location services suspended. Please reconnect.");
    }

    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();
        if (mMap != null) {
            setUpMapIfNeeded();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
        }
        }else{
        Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    public void changeType(View view) {

        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    public void onSearch(View view) {
        EditText search = (EditText) findViewById(R.id.TFaddress);
        String location = search.getText().toString();
        List<Address> addressList = null;
        if (location != null || !location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }

    public void showDirection(View view){
        new LongOperation().execute("Menggambar jalur...");

    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void addMarker(LatLng latlng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.title(Lokasi);
        markerOptions.snippet(latlng.latitude + "," + latlng.longitude);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker));
        mMap.addMarker(markerOptions);
    }


    private class RetrieveTask extends AsyncTask<Void, Void, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MapsActivity.this);
            pDialog.setMessage("Menghubungkan ke server...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            String strUrl = "http://trashbinary.netai.net/retrieve.php";
            URL url = null;
            StringBuffer sb = new StringBuffer();
            try {
                url = new URL(strUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream iStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(iStream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                iStream.close();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            new ParserTask().execute(result);
        }

    }

    private class ParserTask extends AsyncTask<String, Void, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... params) {
            MarkerJSONParser markerParser = new MarkerJSONParser();
            JSONObject json = null;
            try {
                json = new JSONObject(params[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<HashMap<String, String>> markersList = markerParser.parse(json);
            return markersList;
        }

        @Override
        public void onPostExecute(List<HashMap<String, String>> result) {
            float mindist = 0;
            int pos = 0;

            for (int i = 0; i < result.size(); i++) {
                HashMap<String, String> marker = result.get(i);
                Lokasi = marker.get("lokasi");
                LatLng latlng = new LatLng(Double.parseDouble(marker.get("lat")),
                        Double.parseDouble(marker.get("lng")));
                addMarker(latlng);


                float[] distance = new float[1];
                Location.distanceBetween(CurrentLat, CurrentLng, Double.parseDouble(marker.get("lat")),
                        Double.parseDouble(marker.get("lng")), distance);
                if (i == 0) {
                    mindist = distance[0];
                } else if (mindist > distance[0]) {
                    mindist = distance[0];
                    pos = i;
                }
            }
                posisi = pos;
                minimaldistance = mindist;

                time = minimaldistance / 1.38889;
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context, "Marker terdekat: " + result.get(pos) + " " + mindist, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM, 0, 0);
                toast.show();

                HashMap<String, String> marker = result.get(posisi);
                nearlatitude = Double.parseDouble(marker.get("lat"));
                nearlongitude = Double.parseDouble(marker.get("lng"));

            pDialog.dismiss();
        }
    }

    public void showRoute(View view) {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        shopLatLng = new LatLng(nearlatitude, nearlongitude);

        Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(currentLatitude, currentLongitude), new LatLng(nearlatitude,
                        nearlongitude)).width(5).color(Color.BLUE));
        DecimalFormat df = new DecimalFormat(".##");
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage("Jarak dengan tempat sampah terdekat: " + df.format(minimaldistance) + " meter\n\n" +
                "Waktu tempuh: " + df.format(time) + " detik");
        dlgAlert.setTitle("Info");
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private class LongOperation extends AsyncTask<String, Void, PolylineOptions> {

        private PolylineOptions getDirection() {
            try {
                GMapV2Direction md = new GMapV2Direction();
                Document doc = md.getDocument(myLatLng, shopLatLng,
                        GMapV2Direction.MODE_DRIVING);
                ArrayList<LatLng> directionPoint = md.getDirection(doc);
                PolylineOptions rectLine = new PolylineOptions().width(9).color(
                        Color.RED);

                for (int i = 0; i < directionPoint.size(); i++) {
                    rectLine.add(directionPoint.get(i));
                }
                isDirectionDrawn = true;

                return rectLine;
            }
            catch (Exception e)
            {
                ///possible error:
                ///java.lang.IllegalStateException: Error using newLatLngBounds(LatLngBounds, int): Map size can't be 0. Most likely, layout has not yet occured for the map view.  Either wait until layout has occurred or use newLatLngBounds(LatLngBounds, int, int, int) which allows you to specify the map's dimensions.
                return null;
            }

        }

        @Override
        protected PolylineOptions doInBackground(String... params) {
            PolylineOptions polylineOptions = null;
            try {
                polylineOptions = getDirection();
            } catch (Exception e) {
                Thread.interrupted();
            }
            return polylineOptions;
        }

        @Override
        protected void onPostExecute(PolylineOptions result) {
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
            //mMap.clear();///TODO: clean the path only.
            //mMap.addMarker(new MarkerOptions()
            //                .position(shopLatLng)
            //                .title("Lou Lan Cha")
            //);

            mMap.addPolyline(result);

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}