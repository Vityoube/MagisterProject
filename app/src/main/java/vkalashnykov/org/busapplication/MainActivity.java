package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vkalashnykov.org.busapplication.domain.Driver;
import vkalashnykov.org.busapplication.domain.Point;
import vkalashnykov.org.busapplication.domain.Route;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private static final String BUS = "BusApplication";
    TextView welcomeMessage;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String userEmail;
    private String driverName;
    private String driverKey;
    private LatLng currentPlaceSelection = null;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    GoogleMap mMap;
    private ArrayList<vkalashnykov.org.busapplication.domain.Point> markerPoints = new ArrayList();
    private boolean editMap = false;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference driversRef;
    DatabaseReference routesRef;
    private Location mLocation;

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    Marker mCurrLocationMarker;
    private boolean mLocationPermissionGranted = false;
    private Location mLastKnownLocation;
    private LatLng defaultLocation = new LatLng(0, 0);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_driver_main);


        welcomeMessage = (TextView) findViewById(R.id.welcome);
        userEmail = intent.getStringExtra("USER_EMAIL");
        driverName = intent.getStringExtra("NAME");
        welcomeMessage.setText(getResources().getString(R.string.welcome) + ", " + driverName + "!");
        driverKey = intent.getStringExtra("USER_KEY");
        driversRef = database.getReference().child("drivers");
        routesRef = database.getReference().child("routes");

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        LocationServices.getFusedLocationProviderClient(this).
                requestLocationUpdates(mLocationRequest,new LocationCallback(){
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                }, Looper.myLooper());
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(BUS, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(BUS, "onAuthStateChanged:signed_out");
                }
            }
        };
        routesRef.child(driverKey).child("driverName").setValue(driverName);



    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);



    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public void signout(View view) {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(this, R.string.logout_success,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation=location;
        handleNewLocation();

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(BUS, "Location services connected.");
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        }

        handleNewLocation();


    }

    private void handleNewLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            if (mLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                Log.d(BUS, mLocation.toString());

                final double currentLatitude = mLocation.getLatitude();
                final double currentLongitude = mLocation.getLongitude();
                LatLng latLng = new LatLng(currentLatitude, currentLongitude);
                routesRef.child(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Route route = dataSnapshot.getValue(Route.class);
                        route.setCurrentPosition(new Point(currentLatitude, currentLongitude));
                        routesRef.child(driverKey).setValue(route);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this,
                                R.string.databaseError, Toast.LENGTH_SHORT).show();
                    }
                });
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(BUS, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(BUS, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        mMap.setMyLocationEnabled(true);

        routesRef.child(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                        Route route=dataSnapshot.getValue(Route.class);
                        markerPoints=route.getRoute();
                        MarkerOptions markerOptions = new MarkerOptions();
                        for (vkalashnykov.org.busapplication.domain.Point point : markerPoints) {
                            LatLng marker=new LatLng(point.getLatitude(),point.getLongitude());
                            markerOptions.position(marker);
                            mMap.addMarker(markerOptions);
                        }

                        List<String> urls = getDirectionsUrl(markerPoints);
                        if (urls.size() > 1) {
                            for (int i = 0; i < urls.size(); i++) {
                                String url = urls.get(i);
                                DownloadTask downloadTask = new DownloadTask();
                                // Start downloading json data from Google Directions API
                                downloadTask.execute(url);
                            }
                        }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,
                        R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });


        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (editMap) {
                    String apiKey="AIzaSyAcwyEytYneiCAeth4iXI8iMyatyHUkN5U";
                    String placeUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                            latLng.latitude+","+latLng.longitude+"&radius=10&type=bus_station&key="+apiKey;
                    String data="";
                    currentPlaceSelection=latLng;
                    CallPlacesAPI callPlacesAPI=new CallPlacesAPI();
                    callPlacesAPI.execute(placeUrl);
                }
            }
        });

    }

    public void setEditRoute(View view) {
        Button button = (Button) findViewById(R.id.editButton);
        editMap = !editMap;
        if (editMap) {
            button.setText("Save Route");
        } else {
            saveRouteOnDatabase();

            button.setText("Edit Route");


        }


    }

    private void saveRouteOnDatabase() {
        routesRef.child(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route=dataSnapshot.getValue(Route.class);
                route.setRoute(markerPoints);
                dataSnapshot.getRef().setValue(route);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MainActivity.this,R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void userDetails(View view) {
        Intent userDetailsIntent=new Intent(this,UserDetailsActivity.class);
        userDetailsIntent.putExtra("USER_EMAIL",userEmail);
        startActivity(userDetailsIntent);

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {

            for (int i = 0; i < result.size(); i++) {
                ArrayList points = new ArrayList();
                PolylineOptions lineOptions= new PolylineOptions();

                List<HashMap<String,String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }



                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
                mMap.addPolyline(lineOptions);
            }


        }
    }



    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    private List<String> getDirectionsUrl(ArrayList<vkalashnykov.org.busapplication.domain.Point> markerPoints) {
        List<String> mUrls = new ArrayList<>();
        if (markerPoints.size() > 1) {
            String str_origin = markerPoints.get(0).getLatitude() + "," + markerPoints.get(0).getLongitude();
            String str_dest = markerPoints.get(1).getLatitude()  + "," + markerPoints.get(1).getLongitude();

            String sensor = "sensor=false";
            String parameters = "origin=" + str_origin + "&destination=" + str_dest + "&" + sensor;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

            mUrls.add(url);
            for (int i = 2; i < markerPoints.size(); i++)//loop starts from 2 because 0 and 1 are already printed
            {
                str_origin = str_dest;
                str_dest = markerPoints.get(i).getLatitude()  + "," + markerPoints.get(i).getLongitude();
                parameters = "origin=" + str_origin + "&destination=" + str_dest + "&" + sensor;
                url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
                mUrls.add(url);
            }
        }

        return mUrls;
    }
    private class CallPlacesAPI extends AsyncTask<String,String,String>{



        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                data=sendRequest(strings[0]);

            } catch (IOException e) {
                Log.d("PlacesAPI",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject placeResponse=new JSONObject(response);
                if(!"ZERO_RESULTS".equals(placeResponse.get("status")) ){
                    vkalashnykov.org.busapplication.domain.Point pointToAdd=
                            new vkalashnykov.org.busapplication.domain.Point(
                            currentPlaceSelection.latitude,
                            currentPlaceSelection.longitude
                    );
                    if (markerPoints==null)
                        markerPoints=new ArrayList<>();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentPlaceSelection);
                    Marker marker = mMap.addMarker(markerOptions);
                    boolean isToRemove=false;
                    int index=0;
                    DecimalFormat df = new DecimalFormat("#.###");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    for(vkalashnykov.org.busapplication.domain.Point point : markerPoints){
                        if (df.format(point.getLatitude()).equals(df.format(pointToAdd.getLatitude()))
                                && df.format(point.getLongitude()).equals(df.format(pointToAdd.getLongitude()))){
                            isToRemove=true;
                            index=markerPoints.indexOf(point);
                        }
                    }
                    if (isToRemove) {
                        markerPoints.remove(index);
                       mMap.clear();
                       for (vkalashnykov.org.busapplication.domain.Point point : markerPoints){
                           LatLng latLng=new LatLng(point.getLatitude(),point.getLongitude());
                           markerOptions.position(latLng);
                           mMap.addMarker(markerOptions);
                       }

                    }
                    else {
                        markerPoints.add(pointToAdd);
                    }



                    List<String> urls = getDirectionsUrl(markerPoints);
                    if (urls.size() > 1) {
                        for (int i = 0; i < urls.size(); i++) {
                            String url = urls.get(i);
                            DownloadTask downloadTask = new DownloadTask();
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url);
                        }
                    }
                }
            } catch (JSONException e){
                Log.d("PlacesAPI",e.toString());
            }
        }
    }


    public String sendRequest(String uri) throws IOException{
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        String data=null;
        try {
            URL url=new URL(uri);
            urlConnection= (HttpURLConnection) url.openConnection();
            iStream=urlConnection.getInputStream();
            StringBuffer sb=new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            String line="";
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            data=sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        Log.d("PLACES_API",data);
        return data;
    }
}
