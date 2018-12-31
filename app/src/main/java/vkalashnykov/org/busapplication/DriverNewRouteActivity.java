package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.request.DirectionDestinationRequest;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.api.domain.Position;

public class DriverNewRouteActivity extends AppCompatActivity
        implements OnMapReadyCallback, android.location.LocationListener {
    private GoogleMap map;
    private DatabaseReference driverRef;
    private LocationManager locationManager;
    private List<Marker> addedMarkers;
    private List<Polyline> polylines;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_new_route);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverNewRouteMap);
        mapFragment.getMapAsync(this);
        String driverKey = getIntent().getStringExtra("DRIVER_KEY");
        driverRef = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey);
        addedMarkers = new ArrayList<>();
        polylines=new ArrayList<>();
        GoogleDirectionConfiguration.getInstance().setLogEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        final LatLngBounds bounds = new LatLngBounds(
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
            LatLng centerPoland = new LatLng(52.0693234, 19.4781172);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoland, 7));
        }
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                if (addedMarkers.isEmpty())
                    options.icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN));
                else if (addedMarkers.size()==1)
                    options.icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_CYAN));
                else{
                    options.icon(BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_RED));
                    addedMarkers.get(addedMarkers.size()-1).
                            setIcon(BitmapDescriptorFactory.
                                    defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                }

                addedMarkers.add(map.addMarker(options));
                updateRoute();
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.remove();
                addedMarkers.remove(addedMarkers.indexOf(marker));
                for (int i = 0; i < addedMarkers.size(); i++) {
                    if (i == 0)
                        addedMarkers.get(i).setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                    else if (i == addedMarkers.size() - 1)
                        addedMarkers.get(i).setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_RED));
                    else
                        addedMarkers.get(i).setIcon(BitmapDescriptorFactory
                                .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
                }
                for (Polyline polyline : polylines)
                    polyline.remove();
                polylines.clear();
                updateRoute();
                return false;
            }
        });


    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 7));
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

    public void updateRoute() {
        if (!addedMarkers.isEmpty() && addedMarkers.size() > 1) {
            LatLng origin = addedMarkers.get(0).getPosition();
            LatLng destination = addedMarkers.get(addedMarkers.size() - 1).getPosition();
            List<LatLng> waypoints = new ArrayList<>();
            if (addedMarkers.size() > 2) {
                for (int i = 1; i < addedMarkers.size() - 1; i++) {
                    waypoints.add(addedMarkers.get(i).getPosition());
                }
            }
            DirectionDestinationRequest directionRequest = GoogleDirection
                    .withServerKey(getString(R.string.api_key))
                    .from(origin);
            if (!waypoints.isEmpty())
                directionRequest.and(waypoints);
            directionRequest
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {
                            if (direction.isOK()) {
                                Route route = direction.getRouteList().get(0);
                                for (Leg leg : route.getLegList()) {
                                    List<Step> directionPoints = leg.getStepList();
                                    ArrayList<PolylineOptions> polylinesOptions =
                                            DirectionConverter.createTransitPolyline(
                                                    DriverNewRouteActivity.this,
                                                    directionPoints,
                                                    5, Color.GREEN,
                                                    5,Color.GREEN);
                                    for (PolylineOptions polylineOptions: polylinesOptions)
                                        polylines.add(map.addPolyline(polylineOptions));
                                }
                            }
                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {

                        }
                    });


        }
    }

    public void cancel(View view) {
        Intent cancelIntent=new Intent();
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_CANCELED,cancelIntent);
        finish();
    }


    public void saveRoute(View view) {
        if (addedMarkers.size()>1){
            driverRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Driver driver=dataSnapshot.getValue(Driver.class);
                    ArrayList<Position> points=new ArrayList<>();
                    for (Marker marker : addedMarkers){
                        Position point=new Position(
                                marker.getPosition().latitude,
                                marker.getPosition().longitude
                        );
                        points.add(point);
                    }
                    String routeStatus=getString(R.string.opened);
                    vkalashnykov.org.busapplication.api.domain.Route route=
                            new vkalashnykov.org.busapplication.api.domain.Route(points,
                                    routeStatus);
                    driver.addRoute(route);
                    driverRef.setValue(driver);
                    setResult(RESULT_OK);
                    finish();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }
}
