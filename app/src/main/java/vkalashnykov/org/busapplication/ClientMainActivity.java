package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.domain.Client;
import vkalashnykov.org.busapplication.domain.Driver;
import vkalashnykov.org.busapplication.domain.Point;
import vkalashnykov.org.busapplication.domain.Route;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final FirebaseDatabase database=FirebaseDatabase.getInstance();
    private DatabaseReference routesRef;
    private static final String BUS = "BusApplication";
    private String userEmail;
    String userKey;
    private GoogleMap mMap;
    private ArrayList<Route> routes;
    private int currentRouteOnList=0;
    private LinearLayout routesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        TextView welcome= (TextView) findViewById(R.id.welcome2);
        String welcomeMessage=welcome.getText().toString()+", "+getIntent().getStringExtra("NAME")+"!";
        userEmail=getIntent().getStringExtra("USER_EMAIL");
        userKey=getIntent().getStringExtra("USER_KEY");
        welcome.setText(welcomeMessage);
        routes=new ArrayList<>();
        routesList= (LinearLayout) findViewById(R.id.routesList);
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
        routesRef=database.getReference().child("routes");




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
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(BUS, "Location services connected.");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ClientMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

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
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
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
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ClientMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        mMap.setMyLocationEnabled(true);

        routesRef.addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Route addedRoute=dataSnapshot.getValue(Route.class);
                routes.add(addedRoute);
                Toast.makeText(ClientMainActivity.this,"Added routes: "+routes.toString(),
                        Toast.LENGTH_SHORT).show();
                LinearLayout routeLayout=new LinearLayout(ClientMainActivity.this);
                routeLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView driverName=new TextView(ClientMainActivity.this);
                driverName.setTypeface(Typeface.MONOSPACE,Typeface.BOLD_ITALIC);
                driverName.setText(addedRoute.getDriverName());
                TextViewCompat.setTextAppearance(driverName, R.style.TextAppearance_AppCompat_Body2);
                LinearLayout.LayoutParams textParam=new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT,0.8f);
                LinearLayout.LayoutParams buttonParam=new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT,0.2f);
                Button sendMessage=new Button(ClientMainActivity.this);
                sendMessage.setText(R.string.send_message);
                routeLayout.addView(driverName,textParam);
                routeLayout.addView(sendMessage,textParam);
                routesList.addView(routeLayout);
//                routeLayout.addView(sendMessage, );
//                routesList.get

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Driver changedDriver=dataSnapshot.getValue(Driver.class);
                routes.add(new Route(
                        changedDriver.getFirstName()+" "+changedDriver.getLastName(),
                        changedDriver.getRoute()));
                Toast.makeText(ClientMainActivity.this,"Updated routes: "+routes.toString(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Driver removedDriver=dataSnapshot.getValue(Driver.class);
                Route route=new Route(
                        removedDriver.getFirstName()+" "+removedDriver.getLastName(),
                        removedDriver.getRoute());
                int routeToRemove=-1;
                for (Route currentRoute : routes){
                    if (!route.getDriverName().equals(currentRoute.getDriverName())){
                        continue;
                    } else{
                        for (int pointIndex=0,currentPointIndex=0;
                             pointIndex<route.getRoute().size() &&
                                     currentPointIndex<currentRoute.getRoute().size();
                             pointIndex++,currentPointIndex++){
                            Point pointToCompare=route.getRoute().get(pointIndex);
                            Point currentPoint=route.getRoute().get(currentPointIndex);
                            if (pointToCompare.getLongitude()==currentPoint.getLongitude()
                                    && pointToCompare.getLatitude()==currentPoint.getLatitude()
                                    && pointIndex==currentPointIndex){
                                routeToRemove=currentPointIndex;
                            }
                        }
                    }
                }
                if (routeToRemove!=-1){
                    routes.remove(routeToRemove);
                }

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ClientMainActivity.this,R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void userDetails(View view) {
        Intent userDetailsIntent=new Intent(this,ClientUserDetailsActivity.class);
        userDetailsIntent.putExtra("USER_EMAIL",userEmail);
        userDetailsIntent.putExtra("USER_KEY",userKey);
        startActivity(userDetailsIntent);
    }

    public void signout(View view) {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(this, R.string.logout_success,
                Toast.LENGTH_SHORT).show();
    }
}
