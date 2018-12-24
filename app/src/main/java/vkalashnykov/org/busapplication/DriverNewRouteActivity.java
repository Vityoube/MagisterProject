package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
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
        implements OnMapReadyCallback, android.location.LocationListener {
    private GoogleMap map;
    private DatabaseReference driverRef;
    private LocationManager locationManager;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    LatLng currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_new_route);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1600);
        }
        locationManager=(LocationManager)getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment=
                (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.driverNewRouteMap);
        mapFragment.getMapAsync(this);
        String driverKey=getIntent().getStringExtra("DRIVER_KEY");
        driverRef= FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(LocationServices.API)
//                .build();
//        mLocationRequest = LocationRequest.create()
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
//                .setFastestInterval(1 * 1000);
//        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
//        builder.addLocationRequest(mLocationRequest);
//        LocationSettingsRequest locationSettingsRequest = builder.build();
//
//        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
//        settingsClient.checkLocationSettings(locationSettingsRequest);
//        LocationServices.getFusedLocationProviderClient(this).
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
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,
                    Looper.myLooper());
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this,
                    Looper.myLooper());
        else {
            LatLng centerPoland=new LatLng(52.0693234,19.4781172);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoland,7));
        }



    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,7));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
