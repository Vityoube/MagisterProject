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
import android.view.KeyEvent;
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
    private boolean promptExit=true;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode==KeyEvent.KEYCODE_BACK){
            if (promptExit){
                Toast.makeText(this,R.string.prompt_exit,Toast.LENGTH_SHORT).show();
                promptExit=false;
            } else {
                mAuth.signOut();
                finish();
            }
        }
        return false;
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


    public void userDetails(View view) {
        Intent userDetailsIntent=new Intent(this,UserDetailsActivity.class);
        userDetailsIntent.putExtra("USER_EMAIL",userEmail);
        startActivity(userDetailsIntent);

    }


}
