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
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.BusInformation;
import vkalashnykov.org.busapplication.api.domain.Position;
import vkalashnykov.org.busapplication.api.domain.Request;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.components.DriverBusCurrentDetails;

@SuppressWarnings("deprecation")
public class DriverMainActivity extends FragmentActivity
        implements OnMapReadyCallback, android.location.LocationListener
{

    //TODO: add possibilty to List Requests and change their status
    private static final int ADD_NEW_ROUTE_REQUEST = 1;
    private static final int CHOOSE_ROUTE_FROM_HISTORY_REQUEST = 2;
    TextView welcomeMessage;
    private LocationManager locationManager;
    private GoogleMap map;

    private FirebaseAuth mAuth;

    private String driverName="";
    private String driverKey;
    private boolean promptExit = true;
    private DatabaseReference driverRef;
    private DriverBusCurrentDetails busDetails;
    private Button updateButton;
    LinearLayout updateRoutePanel;
    private String buttonText="";
    private  static  final String START_ROUTE="startRoute";
    private  static  final String FINISH_ROUTE="finishRoute";
    private boolean routeIsStarted;


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (promptExit) {
                Toast.makeText(this, R.string.prompt_exit, Toast.LENGTH_SHORT).show();
                promptExit = false;
            } else {
                mAuth.signOut();
                finish();
            }
        }
        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        routeIsStarted=false;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        setContentView(R.layout.activity_driver_main);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.driverViewMap);
        mapFragment.getMapAsync(this);


        welcomeMessage = (TextView) findViewById(R.id.welcome);

        driverName = intent.getStringExtra("NAME");

        driverKey = intent.getStringExtra("USER_KEY");
        mAuth = FirebaseAuth.getInstance();
        driverRef = FirebaseDatabase.getInstance().getReference().child("drivers").child(driverKey);
        setDriverFirstName();
        updateRoutePanel=findViewById(R.id.routeUpdatePanel);
        updateRoutePanel.setVisibility(View.VISIBLE);
        updateButton=findViewById(R.id.updateRoute);
        busDetails=(DriverBusCurrentDetails)findViewById(R.id.driverBusCurrentDetails);
        updateBusInformation();
        updateViewFromCurrentRoute();
    }

    private void updateViewFromCurrentRoute() {
        driverRef.child("currentRoute").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Route currentRoute=dataSnapshot.getValue(Route.class);
                if (currentRoute==null ||
                        getString(R.string.finished).equals(currentRoute.getStatus())){
                        updateButton.setVisibility(View.GONE);
                        updateRoutePanel.setVisibility(View.VISIBLE);
                } else {
                    updateButton.setVisibility(View.VISIBLE);
                    if (getString(R.string.opened).equals(currentRoute.getStatus())){
                        updateRoutePanel.setVisibility(View.VISIBLE);
                        updateButton.setText(R.string.start_route);
                        updateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startRoute();
                            }
                        });
                    } else if (getString(R.string.inprogress).equals(currentRoute.getStatus())){
                        routeIsStarted=true;
                        updateRoutePanel.setVisibility(View.GONE);
                        updateButton.setText(R.string.finish_route);
                        updateButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finishRoute();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void finishRoute() {
        driverRef.child("currentRoute").child("status").setValue(getString(R.string.finished));
        routeIsStarted=false;
        driverRef.child("currentRoute").setValue(null);
        map.clear();
    }

    private void startRoute() {
        driverRef.child("currentRoute").child("status").setValue(getString(R.string.inprogress));
    }

    private void setDriverFirstName() {
        driverRef.child("firstName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String firstName=dataSnapshot.getValue(String.class);
                driverName+=firstName;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        driverRef.child("lastName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastName=dataSnapshot.getValue(String.class);
                driverName+=lastName;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        welcomeMessage.setText(getResources().getString(R.string.welcome) + ", " + driverName + "!");
    }

    private void updateBusInformation() {
        driverRef.child("busInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BusInformation busInformation=dataSnapshot.getValue(BusInformation.class);
                busDetails.setSeats(busInformation.getBusSize());
                busDetails.setSeatsOccupied(busInformation.getOccupiedSeats());
                busDetails.setTrunkOccupied(busInformation.getOccupiedTrunk());
                busDetails.setTrunk(busInformation.getTrunkCapacity());
                busDetails.setSalonTrunkOccupied(busInformation.getOccupiedSalonTrunk());
                busDetails.setSalonTrunk(busInformation.getSalonCapacity());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void signout(View view) {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        Toast.makeText(this, R.string.logout_success,
                Toast.LENGTH_SHORT).show();
        finish();
    }


    public void userDetails(View view) {
        Intent userDetailsIntent = new Intent(this, DriverUserDetailsActivity.class);
//        userDetailsIntent.putExtra("USER_EMAIL", userEmail);
        userDetailsIntent.putExtra("DRIVER_KEY",driverKey);
        startActivity(userDetailsIntent);

    }


    public void goToAddNewRoute(View view) {
        Intent goToAddNewRouteActivityIntent = new Intent(this, DriverNewRouteActivity.class);
        goToAddNewRouteActivityIntent.putExtra("DRIVER_KEY", driverKey);
        startActivityForResult(goToAddNewRouteActivityIntent, ADD_NEW_ROUTE_REQUEST);
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        driverRef.child("currentPosition").setValue(new Position(currentPosition.latitude,
                currentPosition.longitude));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15));
        if (routeIsStarted)
            compareDriverLocationWithRequestDestinations(location);
    }

    private void compareDriverLocationWithRequestDestinations(final Location driverLocation) {
        driverRef.child("currentRoute").child("acceptedRequests")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot requestSnapshot :dataSnapshot.getChildren()){
                    Request acceptedRequest=requestSnapshot.getValue(Request.class);
                    Location requestDestinationLocation=new Location("");
                    requestDestinationLocation.setLatitude(acceptedRequest.getTo().getLatitude());
                    requestDestinationLocation.setLongitude(acceptedRequest.getTo().getLongitude());
                    if (requestDestinationLocation.distanceTo(driverLocation)<=100){
                        updateDriverBusInformation(acceptedRequest);
                        removeCustomerRequestFromRoute(requestSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updateDriverBusInformation(final Request acceptedRequest) {
        driverRef.child("busInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                BusInformation busInformation=dataSnapshot.getValue(BusInformation.class);
                int requestSeats=acceptedRequest.getSeatsNumber();
                int requestTrunk=acceptedRequest.getTrunk();
                int requestSalonTrunk=acceptedRequest.getSalonTrunk();
                busInformation.setOccupiedSeats(busInformation.getOccupiedSeats()-requestSeats);
                busInformation.setOccupiedTrunk(busInformation.getOccupiedTrunk()-requestTrunk);
                busInformation.setOccupiedSalonTrunk(
                        busInformation.getOccupiedSalonTrunk()-requestSalonTrunk );
                driverRef.child("busInformation").setValue(busInformation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeCustomerRequestFromRoute(String requestKey) {
        driverRef.child("currentRoute").child("acceptedRequests").child(requestKey).removeValue();
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
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,1,
                    this,
                    Looper.myLooper());
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1,1,
                    this,
                    Looper.myLooper());
        else {
            LatLng centerPoland = new LatLng(52.0693234, 19.4781172);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoland, 7));
        }
//        if (driverRef.child("routes").child(String.valueOf(0))!=null
//                && driverRef.child("routes")!=null)
            drawRoute();


    }

//    private void updateRoutePanelSetState() {
//        driverRef.child("currentRoute").child("status").addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String status=dataSnapshot.getValue(String.class);
//                if (getString(R.string.inprogress).equals(status)) {
//                    updateRoutePanel.setVisibility(View.GONE);
//                }
//                else
//                    updateRoutePanel.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == ADD_NEW_ROUTE_REQUEST) {
            if (resultCode == RESULT_OK) {
                drawRoute();
            }
        } else if (requestCode == CHOOSE_ROUTE_FROM_HISTORY_REQUEST) {
            if (resultCode == RESULT_OK) {
                drawRoute();
            }
        }
    }

    public void drawRoute() {
        map.clear();
        driverRef.child("currentRoute")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Route currentRoute=dataSnapshot.getValue(Route.class);
                        if (currentRoute!=null){
                            ArrayList<Position> points=currentRoute.getPoints();
                            if (points.isEmpty())
                                return;
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
                                                                    DriverMainActivity.this,
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
//                        ArrayList<Position> points = new ArrayList<>();
//                        for (DataSnapshot pointSnapshot : dataSnapshot.child(
//                                String.valueOf(dataSnapshot.getChildrenCount() - 1))
//                                .child("points").getChildren()) {
//                            double latitude = pointSnapshot.child("latitude").getValue(Double.class);
//                            double longitude = pointSnapshot.child("longitude").getValue(Double.class);
//                            Position point = new Position(latitude, longitude);
//                            points.add(point);
//                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    public void goToSelectRouteFromHistory(View view) {
        Intent routesHistoryIntent = new Intent(this, DriverRoutesHistoryActivity.class);
        routesHistoryIntent.putExtra("DRIVER_KEY", driverKey);
        startActivityForResult(routesHistoryIntent, CHOOSE_ROUTE_FROM_HISTORY_REQUEST);
    }

    public void goToRequestList(View view) {
        Intent goToRequestList=new Intent(this,DriverRequestListActivity.class);
        goToRequestList.putExtra("DRIVER_KEY",driverKey);
        startActivity(goToRequestList);
    }

}
