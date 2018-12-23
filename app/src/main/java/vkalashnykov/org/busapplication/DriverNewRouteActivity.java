package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverNewRouteActivity extends AppCompatActivity
        implements OnMapReadyCallback, LocationListener {
    private GoogleMap map;
    private DatabaseReference driverRef;
    private GoogleApiClient mGoogleApiClient;
    private Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_new_route);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1600);
        }
        LocationServices.getFusedLocationProviderClient(this).getLastLocation();
        SupportMapFragment mapFragment=
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.driverNewRouteMap);
        mapFragment.getMapAsync(this);
        String driverKey=getIntent().getStringExtra("DRIVER_KEY");
        driverRef= FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        final LatLngBounds bounds=new LatLngBounds(
                new LatLng(49.452007, 14.362359),
                new LatLng(54.410894, 22.843805)
                );
        map.setLatLngBoundsForCameraTarget(bounds);
        map.setMyLocationEnabled(true);
//        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LatLng currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,7));

    }
    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        currentLocation=location;
    }
}
