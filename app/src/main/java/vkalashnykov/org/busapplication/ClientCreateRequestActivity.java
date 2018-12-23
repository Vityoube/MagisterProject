package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import vkalashnykov.org.busapplication.api.domain.Position;
import vkalashnykov.org.busapplication.api.domain.Request;

public class ClientCreateRequestActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener, OnMapReadyCallback {

    private  GoogleMap googleMap;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private static final String BUS = "BusApplication";
    private Position selectedLocation;
    private String comments;
    private String intentDriverName;
    private String clientKey,driverKey;
    private com.google.android.gms.maps.model.LatLng currentPlaceSelection;
    private ArrayList<Marker> currentRoute;
    private ArrayList<com.google.android.gms.maps.model.Polyline> currentRouteLines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_create_request);
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
        TextView driverName= (TextView) findViewById(R.id.driverName);
        intentDriverName=getIntent().getStringExtra("DRIVER_NAME");
        driverKey=getIntent().getStringExtra("DRIVER_KEY");
        clientKey=getIntent().getStringExtra("CLIENT_KEY");
        driverName.setText(intentDriverName);
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapCientMain);
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

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap=googleMap;
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        googleMap.setMyLocationEnabled(true);
        DatabaseReference routeReference=database.getReference().
                child("routes").child(driverKey);
        routeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (currentRoute!=null && !currentRoute.isEmpty()){
                    while (!currentRoute.isEmpty()){
                        Marker marker=currentRoute.get(0);
                        marker.remove();
                        currentRoute.remove(marker);
                    }
                }
                currentRoute=new ArrayList<>();
//                Route driverRoute=dataSnapshot.getValue(Route.class);
//                for (Position point: driverRoute.getRoute()){
//                    MarkerOptions routeMarker =new MarkerOptions();
//                    routeMarker.position(new com.google.android.gms.maps.model.LatLng(point.getLatitude(),point.getLongitude()));
//                    currentRoute.add(googleMap.addMarker(routeMarker));
//                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ClientCreateRequestActivity.this,R.string.databaseError,
                        Toast.LENGTH_SHORT).show();
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(com.google.android.gms.maps.model.LatLng latLng) {
                String apiKey="AIzaSyAcwyEytYneiCAeth4iXI8iMyatyHUkN5U";
                String placeUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                        latLng.latitude+","+latLng.longitude+"&radius=10&type=bus_station&key="+apiKey;
                currentPlaceSelection=latLng;
                CallPlacesAPI callPlacesAPI=new CallPlacesAPI();
                callPlacesAPI.execute(placeUrl);

            }
        });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }

    private void handleNewLocation(Location location) {
        Log.d(BUS, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        com.google.android.gms.maps.model.LatLng latLng = new com.google.android.gms.maps.model.LatLng(currentLatitude, currentLongitude);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    @Override
    public void onConnectionSuspended(int i) {

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
    public void onLocationChanged(Location location) {

    }

    public void sendRequest(View view) {
        if (selectedLocation!=null){
            EditText commentsText = (EditText) findViewById(R.id.comments);
            comments= commentsText.getText().toString();
            SimpleDateFormat df=new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
            String currentDate=df.format(new Date());
            ArrayList<vkalashnykov.org.busapplication.api.domain.Message> messages=new ArrayList<>();
            if (!comments.isEmpty()){
                messages.add(new vkalashnykov.org.busapplication.api.domain.Message(comments,currentDate));
            }
            DatabaseReference requestsReference=database.getReference().child("requests");

            Request request=new Request(clientKey,driverKey,messages, currentDate,Request.STATUS.RAISED);
            requestsReference.push().setValue(request);
            finish();
            Toast.makeText(this,R.string.request_success,Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,R.string.provide_location,Toast.LENGTH_SHORT).show();
        }

    }

    private class CallPlacesAPI extends AsyncTask<String,String,String> {



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
                if(!"ZERO_RESULTS".equals(placeResponse.get("status")) ) {
                    googleMap.clear();
                    MarkerOptions positionMarker=new MarkerOptions();
                    positionMarker.position(currentPlaceSelection);
                    googleMap.addMarker(positionMarker);
                    selectedLocation=new Position(currentPlaceSelection.latitude,currentPlaceSelection.longitude);
                }
            } catch (JSONException e) {
                e.printStackTrace();
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
