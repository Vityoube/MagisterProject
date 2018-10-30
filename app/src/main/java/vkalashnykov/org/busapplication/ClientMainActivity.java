package vkalashnykov.org.busapplication;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.fragment.ClientMapFragment;
import vkalashnykov.org.busapplication.fragment.CreateRequestFragment;
import vkalashnykov.org.busapplication.fragment.OnChooseRouteFromListListener;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity implements OnChooseRouteFromListListener,
        CreateRequestFragment.CreateRequestFragmentListener
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
    private Point driverPosition;
    private ValueEventListener createMarkerListener;
    private ValueEventListener updateMarkerListener;
    private ValueEventListener updateRouteListener;
    private ClientMapFragment mapFragment;
    private Button createRequestButton;
    int PLACE_PICKER_REQUEST=1;

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
                    Point driverPosition=dataSnapshot.getValue(Point.class);
                    mapFragment.createDriverPositionMarker(driverPosition);
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

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ClientMapDriverPosition",databaseError.getMessage());

                }
            };
            updateMarkerListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    driverPosition=(Point) dataSnapshot.getValue(Point.class);
                    mapFragment.updateDriverPosition(driverPosition);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ClientMapDriverPosition",databaseError.getMessage());
                }
            };
            updateRouteListener=new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<Point> route=new ArrayList<Point>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Point point=snapshot.getValue(Point.class);
                        route.add(point);
                    }
                    mapFragment.updateRoute(route);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("ClientMapRoute",databaseError.getMessage());
                }
            };
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
        public void onSelectLocationClick() {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            try {
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                Log.e("ClientPlaces","GooglePlayServicesRepairableException",e);
            } catch (GooglePlayServicesNotAvailableException e) {
                Log.e("ClientPlaces","GooglePlayServicesNotAvailableException",e);
            }
        }


    }
