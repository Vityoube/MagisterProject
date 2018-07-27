package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
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

import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.api.util.DataParser;
import vkalashnykov.org.busapplication.api.util.RoutesAPI;

@SuppressWarnings("deprecation")
public class MainActivity extends FragmentActivity{


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
    private ArrayList<vkalashnykov.org.busapplication.api.domain.Point> markerPoints = new ArrayList();
    private boolean editMap = false;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference routesRef;

    @Override
    protected void onPause() {
        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            mGoogleApiClient.disconnect();
//        }
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
        routesRef = database.getReference().child("routes");

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);


//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API)
//                .addApi(Places.PLACE_DETECTION_API)
//                .build();
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
//                .setFastestInterval(1 * 1000);
//
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(locationSettingsRequest);
//
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
//        }
//        LocationServices.getFusedLocationProviderClient(this).
//                requestLocationUpdates(mLocationRequest,new LocationCallback(){
//                    @Override
//                    public void onLocationResult(LocationResult locationResult) {
//                        onLocationChanged(locationResult.getLastLocation());
//                    }
//                }, Looper.myLooper());
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
//        mGoogleApiClient.connect();
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



    private void handleNewLocation(Location location) {


//        final double currentLatitude = location.getLatitude();
//        final double currentLongitude = location.getLongitude();
//        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
//        routesRef.child(driverKey).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                Route route = dataSnapshot.getValue(Route.class);
//                route.setCurrentPosition(new Point(currentLatitude, currentLongitude));
//                routesRef.child(driverKey).setValue(route);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Toast.makeText(MainActivity.this,
//                        R.string.databaseError, Toast.LENGTH_SHORT).show();
//            }
//        });

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


}
