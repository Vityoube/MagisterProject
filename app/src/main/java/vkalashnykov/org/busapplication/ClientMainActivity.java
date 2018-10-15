package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.fragment.ClientMapFragment;
import vkalashnykov.org.busapplication.fragment.OnChooseRouteFromListListener;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity implements OnChooseRouteFromListListener
    {
        //TODO: fix changing the different Route from list (Now the updates from previous route are shown)
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
        mapFragment=(ClientMapFragment) getFragmentManager().findFragmentById(R.id.map);
        createMarkerListener=new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Point driverPosition=dataSnapshot.getValue(Point.class);
                mapFragment.createDriverPositionMarker(driverPosition);
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
                    (ClientMapFragment) getFragmentManager().findFragmentById(R.id.map);
            if (selectedRouteRef!=null){
                selectedRouteRef.removeEventListener(updateMarkerListener);
                selectedRouteRef.removeEventListener(createMarkerListener);
                selectedRouteRef.removeEventListener(updateRouteListener);
            }
            selectedRouteRef=FirebaseDatabase.getInstance().getReference("routes").child(routeKey);
            selectedRouteRef.child("currentPosition").
                    addListenerForSingleValueEvent(createMarkerListener);
            selectedRouteRef.child("currentPosition").addValueEventListener(updateMarkerListener);

            selectedRouteRef.child("route").addValueEventListener(updateRouteListener);
        }


}
