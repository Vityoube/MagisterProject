package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.request.DirectionDestinationRequest;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.Position;
import vkalashnykov.org.busapplication.api.domain.Route;

public class DriverRoutesHistoryActivity extends AppCompatActivity
implements LocationListener, OnMapReadyCallback{
    private GoogleMap map;
    private DatabaseReference routesRef;
    private LocationManager locationManager;
    private ListView routesListView;
    private FirebaseListAdapter<Route> routesAdapter;
    private int selectedRoute=-1;
    private Button submitButton;
    private DatabaseReference driverRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_routes_history);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverHistoryMap);
        mapFragment.getMapAsync(this);
        routesListView=(ListView)findViewById(R.id.routesList);
        routesListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        String driverKey=getIntent().getStringExtra("DRIVER_KEY");
        submitButton=(Button)findViewById(R.id.select_route_button);
        routesRef= FirebaseDatabase.getInstance().getReference()
                .child("drivers").child(driverKey).child("routes");
        driverRef=FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey);
        Query routesQuery=routesRef.orderByKey();
        FirebaseListOptions<Route> listOptions=new FirebaseListOptions.Builder<Route>()
                .setLayout(R.layout.route)
                .setQuery(routesQuery,Route.class)
                .setLifecycleOwner(this)
                .build();
        routesAdapter=new FirebaseListAdapter<Route>(listOptions) {
            @Override
            protected void populateView(View v, final Route model, final int position) {
                CheckedTextView routeDate = v.findViewById(R.id.routeDate);
                routeDate.setText(model.getTime());
            }


        };
        routesListView.setAdapter(routesAdapter);

        routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Route route=(Route)routesAdapter.getItem(position);
                drawRoute(route.getPoints());
                submitButton.setEnabled(true);
                selectedRoute=position;
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
    }

    public void cancel(View view) {
        Intent cancelIntent=new Intent();
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        setResult(RESULT_CANCELED,cancelIntent);
        finish();
    }


    public void selectRoute(View view) {
        if (selectedRoute!=-1){
            routesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            long lastRouteInList=dataSnapshot.getChildrenCount()-1;
                            Route routeToCopy=dataSnapshot.child(String.valueOf(selectedRoute))
                                    .getValue(Route.class);
                            routesRef.child(String.valueOf(lastRouteInList+1)).setValue(routeToCopy);
                            driverRef.child("currentRoute").setValue(routeToCopy);

                            setResult(RESULT_OK);
                            finish();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        routesAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        routesAdapter.stopListening();
    }

    public void drawRoute(ArrayList<Position> points){
        map.clear();
        LatLng origin = new LatLng(
                points.get(0).getLatitude(),
                points.get(0).getLongitude()
        );
        LatLng destination = new LatLng(
                points.get(points.size() - 1).getLatitude(),
                points.get(points.size() - 1).getLongitude()
        );
        List<LatLng> waypoints = new ArrayList<>();
        if (points.size() > 2) {
            for (int i = 1; i < points.size() - 1; i++) {
                LatLng waypoint = new LatLng(
                        points.get(i).getLatitude(),
                        points.get(i).getLongitude()
                );
                waypoints.add(waypoint);
            }
        }
        for (int i = 0; i < points.size(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions
                    .position(new LatLng(
                            points.get(i).getLatitude(),
                            points.get(i).getLongitude()
                    ));
            if (i == 0)
                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            else if (i == points.size() - 1)
                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));
            else
                markerOptions.icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            map.addMarker(markerOptions);
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
                            com.akexorcist.googledirection.model.Route route = direction.getRouteList().get(0);
                            for (Leg leg : route.getLegList()) {
                                List<Step> directionPoints = leg.getStepList();
                                ArrayList<PolylineOptions> polylinesOptions =
                                        DirectionConverter.createTransitPolyline(
                                                DriverRoutesHistoryActivity.this,
                                                directionPoints,
                                                5, Color.GREEN,
                                                5, Color.GREEN);
                                for (PolylineOptions polylineOptions : polylinesOptions)
                                    map.addPolyline(polylineOptions);
                            }
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });
    }
}
