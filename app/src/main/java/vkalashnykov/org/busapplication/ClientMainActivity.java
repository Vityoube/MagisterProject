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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.BusInformation;
import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.api.domain.Position;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.components.CreateRequestPanel;
import vkalashnykov.org.busapplication.fragment.ClientMapFragment;
import vkalashnykov.org.busapplication.components.DriverBusCurrentDetails;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity
        implements OnMapReadyCallback, android.location.LocationListener {
    // TODO: Add possibilty to add Request to Driver
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference routesRef;
    private static final String BUS = "BusApplication";
    private String userEmail;
    String userKey;
    private String selectedRouteKey;
    private GoogleMap mMap;
    private ArrayList<String> routes;
    private int currentRouteOnList = 0;
    Route selectedRoute;
    private LinearLayout routesList;
    private int routesListSize;
    private Marker currentDriverPositionMarker;
    private String currentDriverKey;
    private Route currentRoute;
    private ArrayList<com.google.android.gms.maps.model.Polyline> currentRouteLines;
    private DatabaseReference selectedRouteRef;
    private Position driverPosition;
    private ValueEventListener createMarkerListener;
    private ValueEventListener updateMarkerListener;
    private ValueEventListener updateRouteListener;
    private ClientMapFragment mapFragment;
    private Button createRequestButton;
    private EditText locationOnRequest;
    //    private LatLng placeRequestLatLng;
    private Marker requestLocationMarker;
    private LocationManager locationManager;
    private FirebaseListAdapter<Driver> driversAdapter;
    private DatabaseReference driverRef;
    private Marker driverPositionMarker;
    private boolean selectionOrigin=true;
    private boolean selectionDestination=true;
    private Marker startRequestMarker;
    private Marker finishRequestMarker;
    private CreateRequestPanel createRequestPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);
        TextView welcome = (TextView) findViewById(R.id.welcome2);
        String welcomeMessage = welcome.getText().toString() + ", " + getIntent().getStringExtra("NAME") + "!";
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        userKey = getIntent().getStringExtra("USER_KEY");
        welcome.setText(welcomeMessage);
        routes = new ArrayList<>();
        currentDriverPositionMarker = null;
        currentRouteLines = new ArrayList<>();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        createRequestPanel=findViewById(R.id.createRequestPanel);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapClientMain);
        mapFragment.getMapAsync(this);
        initializeListView();
        selectionOrigin=true;
        selectionDestination=true;

    }

    private void initializeListView() {
        final ListView driverListView = findViewById(R.id.driverList);
        setAdapterForListView(driverListView);
        listViewSetOnItemClickListener(driverListView, new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < parent.getCount(); i++) {
                    if (i == position) {
                        parent.getChildAt(i).setBackgroundColor(Color.GREEN);
                        driversAdapter.getRef(position).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                Driver driver=dataSnapshot.getValue(Driver.class);
                                if (driver.getRoutes()!=null && !driver.getRoutes().isEmpty()){
                                    if (getCurrentRoute()==null ){
                                        setCurrentRoute(driver.getRoutes()
                                                .get(driver.getRoutes().size()-1));
                                        drawRouteOnMap(getCurrentRoute().getPoints());
                                    } else if (getCurrentRoute()!=null &&
                                            !(getCurrentRoute().equals(
                                                    driver.getRoutes().get(driver.getRoutes().size()-1)))){
                                        setCurrentRoute(driver.getRoutes()
                                                .get(driver.getRoutes().size()-1));
                                        drawRouteOnMap(getCurrentRoute().getPoints());
                                    }
                                }
                                if (driver.getCurrentPosition()!=null){
                                    if (driverPosition==null ){
                                        updateDriverMarker(driver.getCurrentPosition());
                                    } else if (driverPosition!=null
                                            && !(driverPosition.equals(driver.getCurrentPosition()))){
                                        updateDriverMarker(driver.getCurrentPosition());
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                    } else {
                        parent.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }
        });
    }

    private void updateDriverMarker(Position currentPosition) {
        if (driverPositionMarker!=null)
            driverPositionMarker.remove();
        driverPosition=currentPosition;
        MarkerOptions markerOptions=new MarkerOptions();
        markerOptions.position(new LatLng(currentPosition.getLatitude(),driverPosition.getLongitude()));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_bus));
        driverPositionMarker=mMap.addMarker(markerOptions);
    }

    private void listViewSetOnItemClickListener(ListView driverListView,
                                                AdapterView.OnItemClickListener onItemClickListener) {
        driverListView.setOnItemClickListener(onItemClickListener);
    }

    private void setAdapterForListView(ListView driverListView) {
        Query driversQuery = FirebaseDatabase.getInstance()
                .getReference().child("drivers")
                .orderByKey();
        FirebaseListOptions listOptions = new FirebaseListOptions.Builder<Driver>()
                .setLayout(R.layout.driver_item)
                .setQuery(driversQuery, Driver.class)
                .build();
        driversAdapter = new FirebaseListAdapter<Driver>(listOptions) {
            @Override
            protected void populateView(View v, Driver model, int position) {
                TextView driverName = v.findViewById(R.id.driverName);
                driverName.setText(model.getFirstName() + " " + model.getLastName());
                final DriverBusCurrentDetails busDetails = v.findViewById(R.id.busInfo);

                BusInformation busInformation = model.getBusInformation();
                busDetails.setSeats(busInformation.getBusSize());
                busDetails.setSeatsOccupied(busInformation.getOccupiedSeats());
                busDetails.setTrunk(busInformation.getTrunkCapacity());
                busDetails.setTrunkOccupied(busInformation.getOccupiedTrunk());
                busDetails.setSalonTrunk(busInformation.getSalonCapacity());
                busDetails.setSalonTrunkOccupied(busInformation.getOccupiedSalonTrunk());

            }

        };
        driverListView.setAdapter(driversAdapter);
    }

    private void drawRouteOnMap(ArrayList<Position> points) {
        mMap.clear();
        for (Polyline polyline :currentRouteLines)
            polyline.remove();
        currentRouteLines.clear();
        if (points.isEmpty())
            return;
        final LatLng origin = new LatLng(
                points.get(0).getLatitude(),
                points.get(0).getLongitude()
        );
        final LatLng destination = new LatLng(
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
            mMap.addMarker(markerOptions);
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
                            com.akexorcist.googledirection.model.Route route = direction.getRouteList()
                                    .get(0);
                            LatLngBounds.Builder cameraBounds=new LatLngBounds.Builder();
                            cameraBounds.include(origin);
                            cameraBounds.include(destination);
                            for (Leg leg : route.getLegList()) {

                                List<Step> directionPoints = leg.getStepList();
                                ArrayList<PolylineOptions> polylinesOptions =
                                        DirectionConverter.createTransitPolyline(
                                                ClientMainActivity.this,
                                                directionPoints,
                                                5, Color.GREEN,
                                                5, Color.GREEN);
                                for (PolylineOptions polylineOptions : polylinesOptions)
                                    currentRouteLines.add(mMap.addPolyline(polylineOptions));
                                mMap.moveCamera(CameraUpdateFactory
                                        .newLatLngBounds(cameraBounds.build(),100));
                            }
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });
    }

    public void userDetails(View view) {
        Intent userDetailsIntent = new Intent(this, ClientUserDetailsActivity.class);
        userDetailsIntent.putExtra("USER_EMAIL", userEmail);
        userDetailsIntent.putExtra("USER_KEY", userKey);
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

//    @Override
//    public void passRouteToMap(String routeKey) {
//        final ClientMapFragment mapFragment =
//                (ClientMapFragment) getFragmentManager().findFragmentById(R.id.mapCientMain);
//        if (selectedRouteRef != null) {
//            selectedRouteRef.child("currentPosition").removeEventListener(updateMarkerListener);
//            selectedRouteRef.child("currentPosition").removeEventListener(createMarkerListener);
//            selectedRouteRef.child("route").removeEventListener(updateRouteListener);
//
//        }
//        createMarkerListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Position driverPosition = dataSnapshot.getValue(Position.class);
////                    mapFragment.createDriverPositionMarker(driverPosition);
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("ClientMapDriverPosition", databaseError.getMessage());
//
//            }
//        };
//        updateMarkerListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                driverPosition = (Position) dataSnapshot.getValue(Position.class);
////                    mapFragment.updateDriverPosition(driverPosition);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.e("ClientMapDriverPosition", databaseError.getMessage());
//            }
//        };
////            updateRouteListener=new ValueEventListener() {
////                @Override
////                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
////                    mapFragment.setRouteRef(selectedRouteRef);
////                    ArrayList<Position> route=new ArrayList<Position>();
////                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
////                        LatLng point=snapshot.getValue(LatLng.class);
////                        route.add(point);
////                    }
////                    mapFragment.updateRoute(route);
////
////                }
////
////                @Override
////                public void onCancelled(@NonNull DatabaseError databaseError) {
////                    Log.e("ClientMapRoute",databaseError.getMessage());
////                }
////            };
//        selectedRouteRef = FirebaseDatabase.getInstance().getReference("routes").child(routeKey);
//        selectedRouteRef.child("currentPosition").
//                addListenerForSingleValueEvent(createMarkerListener);
//        selectedRouteRef.child("currentPosition").addValueEventListener(updateMarkerListener);
//
//        selectedRouteRef.child("route").addValueEventListener(updateRouteListener);
//    }


//    @Override
//    public void onSubmitClick(DialogFragment dialogFragment) {
//
//    }
//
//    @Override
//    public void clickOnMap(Marker marker) {
//        if (selectedRouteRef != null) {
////                placeRequestLatLng=latLng;
//            requestLocationMarker = marker;
//
//            createRequestButton = findViewById(R.id.createRequest);
//            createRequestButton.setEnabled(true);
//            createRequestButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CreateRequestFragment createRequestFragment = new CreateRequestFragment();
//                    createRequestFragment.show(getFragmentManager(), "request");
//                }
//            });
//        }
//    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 15));
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
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        final LatLngBounds bounds = new LatLngBounds(
                new LatLng(49.452007, 14.362359),
                new LatLng(54.410894, 22.843805)
        );
        mMap.setLatLngBoundsForCameraTarget(bounds);
        mMap.setMyLocationEnabled(true);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, this,
                    Looper.myLooper());
        else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, this,
                    Looper.myLooper());
        else {
            LatLng centerPoland = new LatLng(52.0693234, 19.4781172);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(centerPoland, 7));
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (selectionOrigin){
                    setOriginMarker(latLng);
                    selectionOrigin=false;
                } else if (selectionDestination){
                    setDestinationMarker(latLng);
                    selectionDestination=false;
                    createRequestPanel.setVisibility(View.VISIBLE);
                    calculateDriveTime();
                }
            }
        });
    }

    private void calculateDriveTime() {
        List<LatLng> waypoints=new ArrayList<>();
//        for (Polyline polyline :currentRouteLines){
//            if (PolyUtil.isLocationOnPath(startRequestMarker.getPosition(),
//                    polyline.getPoints(),true,100)
//                    && !PolyUtil.isLocationOnPath(finishRequestMarker.getPosition(),polyline.getPoints(),
//                    true,100)
//                ){
//                    waypoints.add(polyline.getPoints().get(polyline.getPoints().size()-1));
//                    int nextPolylineIndex=currentRouteLines.indexOf(polyline)+1;
//                    for (int i=nextPolylineIndex;i<currentRouteLines.size();i++){
//                        Polyline currentPolyline=currentRouteLines.get(i);
//                        if(!PolyUtil.isLocationOnPath(finishRequestMarker.getPosition(),
//                                currentPolyline.getPoints(),
//                                true,100) ){
//                                waypoints.add(currentPolyline.getPoints().get(currentPolyline.getPoints().size()-1));
//                        }
//
//                    }
//            }
//        }
        DirectionDestinationRequest directionRequest = GoogleDirection
                .withServerKey(getString(R.string.api_key))
                .from(startRequestMarker.getPosition());
        if (!waypoints.isEmpty())
            directionRequest.and(waypoints);
        directionRequest
                .to(finishRequestMarker.getPosition())
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback(){

                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()){
                            long driveTime=0;
                            for (Leg leg : direction.getRouteList().get(0).getLegList()){
                                driveTime+=Long.parseLong(leg.getDuration().getValue());
                            }
                            createRequestPanel.setDriveTime(driveTime);
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });

    }

    private void setDestinationMarker(LatLng latLng){
        if (finishRequestMarker!=null)
            finishRequestMarker.remove();
        for (Polyline polyline : currentRouteLines){
            if (PolyUtil.isLocationOnPath(latLng,polyline.getPoints(),true,100) ){
                MarkerOptions destinationOptions=new MarkerOptions();
                destinationOptions.icon(BitmapDescriptorFactory.defaultMarker());
                destinationOptions.position(latLng);
                destinationOptions.title(getString(R.string.finish));
                finishRequestMarker=mMap.addMarker(destinationOptions);
            }
        }

    }

    private void setOriginMarker(LatLng latLng) {
        if (startRequestMarker!=null)
            startRequestMarker.remove();
        for (Polyline polyline : currentRouteLines){
            if (PolyUtil.isLocationOnPath(latLng,polyline.getPoints(),true,100) ){
                MarkerOptions originOptions=new MarkerOptions();
                originOptions.icon(BitmapDescriptorFactory.defaultMarker());
                originOptions.position(latLng);
                originOptions.title(getString(R.string.start));
                startRequestMarker=mMap.addMarker(originOptions);
            }
        }

    }


    @Override
    public void onStart() {
        super.onStart();
        driversAdapter.startListening();
    }


    @Override
    public void onStop() {
        super.onStop();
        driversAdapter.stopListening();
    }

    public void cancelCreateRequest(View view) {
        createRequestPanel.setVisibility(View.INVISIBLE);
        startRequestMarker.remove();
        finishRequestMarker.remove();
        selectionOrigin=true;
        selectionDestination=true;
    }


//        @Override
//        public void onMapReady(GoogleMap googleMap) {
//            this.mMap = googleMap;
//            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
//
//            }
//            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//                @Override
//                public void onMapClick(LatLng latLng) {
//                    placeRequestLatLng=latLng;
//                    Toast.makeText(ClientMainActivity.this,"Selected location: "+
//                    placeRequestLatLng.toString(),Toast.LENGTH_SHORT);
//                }
//            });
//        }

    public void setCurrentRoute(Route route){
        currentRoute=route;
    }

    public Route getCurrentRoute(){
        return currentRoute;
    }
}
