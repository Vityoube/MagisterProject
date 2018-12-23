package vkalashnykov.org.busapplication;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.api.domain.Position;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.fragment.ClientMapFragment;
import vkalashnykov.org.busapplication.fragment.CreateRequestFragment;
import vkalashnykov.org.busapplication.fragment.OnChooseRouteFromListListener;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity implements OnChooseRouteFromListListener,
        CreateRequestFragment.CreateRequestFragmentListener,
        ClientMapFragment.ClientMapClickListener
    {
        // TODO: Add possibilty to add Request to Driver
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
    private String selectedRouteKey;
    private GoogleMap mMap;
    private ArrayList<String> routes;
    private int currentRouteOnList=0;
    Route selectedRoute;
    private LinearLayout routesList;
    private int routesListSize;
    private Marker currentDriverPosition;
    private String currentDriverKey;
    private ArrayList<Marker> currentRoute;
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
        currentDriverPosition=null;
        currentRoute=new ArrayList<>();
        currentRouteLines=new ArrayList<>();
        mapFragment=(ClientMapFragment) getFragmentManager().findFragmentById(R.id.mapCientMain);
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

        @Override
        public void passRouteToMap(String routeKey) {
            final ClientMapFragment mapFragment=
                    (ClientMapFragment) getFragmentManager().findFragmentById(R.id.mapCientMain);
            if (selectedRouteRef!=null){
                selectedRouteRef.child("currentPosition").removeEventListener(updateMarkerListener);
                selectedRouteRef.child("currentPosition").removeEventListener(createMarkerListener);
                selectedRouteRef.child("route").removeEventListener(updateRouteListener);

            }
            createMarkerListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Position driverPosition=dataSnapshot.getValue(Position.class);
//                    mapFragment.createDriverPositionMarker(driverPosition);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ClientMapDriverPosition",databaseError.getMessage());

                }
            };
            updateMarkerListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    driverPosition=(Position) dataSnapshot.getValue(Position.class);
//                    mapFragment.updateDriverPosition(driverPosition);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ClientMapDriverPosition",databaseError.getMessage());
                }
            };
//            updateRouteListener=new ValueEventListener() {
//                @Override
//                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                    mapFragment.setRouteRef(selectedRouteRef);
//                    ArrayList<Position> route=new ArrayList<Position>();
//                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                        LatLng point=snapshot.getValue(LatLng.class);
//                        route.add(point);
//                    }
//                    mapFragment.updateRoute(route);
//
//                }
//
//                @Override
//                public void onCancelled(@NonNull DatabaseError databaseError) {
//                    Log.e("ClientMapRoute",databaseError.getMessage());
//                }
//            };
            selectedRouteRef=FirebaseDatabase.getInstance().getReference("routes").child(routeKey);
            selectedRouteRef.child("currentPosition").
                    addListenerForSingleValueEvent(createMarkerListener);
            selectedRouteRef.child("currentPosition").addValueEventListener(updateMarkerListener);

            selectedRouteRef.child("route").addValueEventListener(updateRouteListener);
        }


        @Override
        public void onSubmitClick(DialogFragment dialogFragment) {

        }

        @Override
        public void clickOnMap(Marker marker) {
            if (selectedRouteRef!=null){
//                placeRequestLatLng=latLng;
                requestLocationMarker=marker;

                createRequestButton=findViewById(R.id.createRequest);
                createRequestButton.setEnabled(true);
                createRequestButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CreateRequestFragment createRequestFragment=new CreateRequestFragment();
                        createRequestFragment.show(getFragmentManager(),"request");
                    }
                });
            }
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
    }
