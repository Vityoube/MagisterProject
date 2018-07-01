package vkalashnykov.org.busapplication;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vkalashnykov.org.busapplication.domain.Client;
import vkalashnykov.org.busapplication.domain.Driver;
import vkalashnykov.org.busapplication.domain.Point;
import vkalashnykov.org.busapplication.domain.Route;

@SuppressWarnings("deprecation")
public class ClientMainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {
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
    private GoogleMap mMap;
    private ArrayList<String> routes;
    private Route currentRoute;
    private int currentRouteOnList=0;
    Route currentSelectedRoute;
    private LinearLayout routesList;
    private int routesListSize;
    private Marker currentDriverPosition;
    private String currentDriverKey;

    public ClientMainActivity() {
        currentSelectedRoute = null;
    }

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
        routesList= (LinearLayout) findViewById(R.id.routesList);
        currentSelectedRoute=null;
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);
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
        routesRef=database.getReference().child("routes");





    }


    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
        routesListSize=0;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(BUS, "Location services connected.");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ClientMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } else {
            handleNewLocation(location);
        }
    }



    private void handleNewLocation(Location location) {
        Log.d(BUS, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(BUS, "Location services suspended. Please reconnect.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(BUS, "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(ClientMainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        mMap.setMyLocationEnabled(true);
        routesRef.addChildEventListener( new ChildEventListener() {
            @Override
            public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
                final Route addedRoute=dataSnapshot.getValue(Route.class);
                final String routeKey=dataSnapshot.getKey();
                routes.add(dataSnapshot.getKey());

                Toast.makeText(ClientMainActivity.this,"Added routes: "+routes.toString(),
                        Toast.LENGTH_SHORT).show();
                final LinearLayout routeLayout=new LinearLayout(ClientMainActivity.this);
                routeLayout.setOrientation(LinearLayout.HORIZONTAL);
                TextView driverName=new TextView(ClientMainActivity.this);
                driverName.setTypeface(Typeface.MONOSPACE,Typeface.BOLD_ITALIC);
                driverName.setText(addedRoute.getDriverName());
                TextViewCompat.setTextAppearance(driverName, R.style.TextAppearance_AppCompat_Body2);
                LinearLayout.LayoutParams textParam=new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT,0.8f);
                LinearLayout.LayoutParams buttonParam=new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT,0.2f);
                Button sendMessage=new Button(ClientMainActivity.this);
                sendMessage.setText(R.string.send_message);
                routeLayout.addView(driverName,textParam);
                routeLayout.addView(sendMessage,textParam);
                routesListSize++;
                routeLayout.setId(routesListSize);
                routeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (routeLayout.getSolidColor()!=Color.BLUE){

                            for (int i=0;i<routesList.getChildCount();i++){
                                LinearLayout currentLayout=(LinearLayout)routesList.getChildAt(i);
                                currentLayout.setBackgroundColor(Color.TRANSPARENT);
                            }
                            routeLayout.setBackgroundColor(Color.BLUE);
                            currentDriverKey=routeKey;
                            routesRef.child(routeKey).child("currentPosition").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (currentDriverKey.equals(routeKey)){
                                        Point changedDriverPosition=dataSnapshot.getValue(Point.class);
                                        if(currentDriverPosition!=null){
                                            currentDriverPosition.remove();
                                        }
                                        LatLng driverLatLng=new LatLng(changedDriverPosition.getLatitude(),
                                                changedDriverPosition.getLongitude());
                                        MarkerOptions busMarker=new MarkerOptions();
                                        busMarker.position(driverLatLng);
                                        busMarker.icon(BitmapDescriptorFactory.fromResource(R.mipmap.bus_icon));
                                        currentDriverPosition=mMap.addMarker(busMarker);
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    Toast.makeText(ClientMainActivity.this,R.string.databaseError,
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });

                routesList.addView(routeLayout);
//                routeLayout.addView(sendMessage, );
//                routesList.get

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                final Route changedRoute=dataSnapshot.getValue(Route.class);
//                final String routeKey=dataSnapshot.getKey();
//                int indexOfRoute=routes.indexOf(routeKey);
//                final LinearLayout routeToUpdate = (LinearLayout)routesList.getChildAt(indexOfRoute);
//                TextView drivernameText = (TextView)routeToUpdate.getChildAt(0);
//
//
//
//                Toast.makeText(ClientMainActivity.this,"Updated route: #"+indexOfRoute+
//                                " with key: "+routeKey,
//                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String routeKey = dataSnapshot.getKey();
                int layoutToRemove = routes.indexOf(routeKey);
                routes.remove(routeKey);
                LinearLayout layout= (LinearLayout) routesList.getChildAt(layoutToRemove);
                routesList.removeView(layout);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ClientMainActivity.this,R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });
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

    private List<String> getDirectionsUrl(ArrayList<vkalashnykov.org.busapplication.domain.Point> markerPoints) {
        List<String> mUrls = new ArrayList<>();
        if (markerPoints.size() > 1) {
            String str_origin = markerPoints.get(0).getLatitude() + "," + markerPoints.get(0).getLongitude();
            String str_dest = markerPoints.get(1).getLatitude()  + "," + markerPoints.get(1).getLongitude();

            String sensor = "sensor=false";
            String parameters = "origin=" + str_origin + "&destination=" + str_dest + "&" + sensor;
            String output = "json";
            String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

            mUrls.add(url);
            for (int i = 2; i < markerPoints.size(); i++)//loop starts from 2 because 0 and 1 are already printed
            {
                str_origin = str_dest;
                str_dest = markerPoints.get(i).getLatitude()  + "," + markerPoints.get(i).getLongitude();
                parameters = "origin=" + str_origin + "&destination=" + str_dest + "&" + sensor;
                url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
                mUrls.add(url);
            }
        }

        return mUrls;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String,String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String,String>>> result) {

            for (int i = 0; i < result.size(); i++) {
                ArrayList points = new ArrayList();
                PolylineOptions lineOptions= new PolylineOptions();

                List<HashMap<String,String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }



                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);
                mMap.addPolyline(lineOptions);
            }


        }
    }

    public String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
