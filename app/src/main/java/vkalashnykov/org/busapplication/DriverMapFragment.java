package vkalashnykov.org.busapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import vkalashnykov.org.busapplication.api.domain.Driver;
import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.api.util.DataParser;
import vkalashnykov.org.busapplication.api.util.RoutesAPI;
import vkalashnykov.org.busapplication.api.util.URLUtil;

public class DriverMapFragment extends MapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {

    private LatLng currentPlaceSelection = null;
    private ArrayList<Point> markerPoints = new ArrayList();
    private  String currentDriverKey;
    private ArrayList<Marker> markers;
    private ArrayList<Polyline> polylines=new ArrayList<>();
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    GoogleMap mMap;
    private DatabaseReference currentRouteRef;




    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this.getActivity(),android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                    this);
        } else {
            handleNewLocation(location);
            LatLng currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 14));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        handleNewLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        mMap.setMyLocationEnabled(true);
        getRouteFromDatabase();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currentPlaceSelection=latLng;
                String apiKey="AIzaSyAcwyEytYneiCAeth4iXI8iMyatyHUkN5U";
                final String placeUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                        latLng.latitude+","+latLng.longitude+"&radius=10&type=bus_station&key="+apiKey;
                CallPlacesAPI callPlacesAPI=new CallPlacesAPI();
                callPlacesAPI.execute(placeUrl);

            }
        });
    }

    private void getRouteFromDatabase() {
        currentRouteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route=dataSnapshot.getValue(Route.class);
                markerPoints=route.getRoute();
                for (Point point : markerPoints){
                    MarkerOptions options=new MarkerOptions();
                    options.position(new LatLng(point.getLatitude(),point.getLongitude()));
                    markers.add(mMap.addMarker(options));
                }

                List<String> urls = URLUtil.getDirectionsUrl(markerPoints);
                if (urls.size() > 1) {
                    for (int i = 0; i < urls.size(); i++) {
                        String url = urls.get(i);
                        DownloadTask downloadTask = new DownloadTask();
                        // Start downloading json data from Google Directions API
                        try {
                            downloadTask.execute(url).get();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverMapFragment.this.getActivity(),
                        R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this.getActivity()
        );
        settingsClient.checkLocationSettings(locationSettingsRequest);


        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        LocationServices.getFusedLocationProviderClient(this.getActivity()).
                requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                }, Looper.myLooper());
        currentDriverKey=getActivity().getIntent().getStringExtra("USER_KEY");;
        currentRouteRef= FirebaseDatabase.getInstance().getReference().child("routes").child(currentDriverKey);
    }

    @Override
    public void onPause() {
        super.onPause();
//        if (mGoogleApiClient.isConnected()) {
//            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
//            mGoogleApiClient.disconnect();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    private void handleNewLocation(Location location) {
        final double currentLatitude = location.getLatitude();
        final double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);
        currentRouteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route = dataSnapshot.getValue(Route.class);
                route.setCurrentPosition(new Point(currentLatitude, currentLongitude));
                currentRouteRef.setValue(route);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverMapFragment.this.getActivity(),
                        R.string.databaseError, Toast.LENGTH_SHORT).show();
            }
        });

    }



    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = URLUtil.downloadUrl(url[0]);
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

    public class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>>> {

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
                if(!polylines.contains(lineOptions))
                    polylines.add(mMap.addPolyline(lineOptions));
            }


        }
    }

    private class CallPlacesAPI extends AsyncTask<String,String,String>{



        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                data=URLUtil.sendRequest(strings[0]);

            } catch (IOException e) {
                Log.d("PlacesAPI",e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            try {
                JSONObject placeResponse=new JSONObject(response);
                if(!"ZERO_RESULTS".equals(placeResponse.get("status")) ){
                    vkalashnykov.org.busapplication.api.domain.Point pointToAdd=
                            new vkalashnykov.org.busapplication.api.domain.Point(
                                    currentPlaceSelection.latitude,
                                    currentPlaceSelection.longitude
                            );
                    if (markerPoints==null)
                        markerPoints=new ArrayList<>();
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(currentPlaceSelection);
                    boolean isToRemove=false;
                    int index=0;
                    DecimalFormat df = new DecimalFormat("#.###");
                    df.setRoundingMode(RoundingMode.FLOOR);
                    for(vkalashnykov.org.busapplication.api.domain.Point point : markerPoints){
                        if (df.format(point.getLatitude()).equals(df.format(pointToAdd.getLatitude()))
                                && df.format(point.getLongitude()).equals(df.format(pointToAdd.getLongitude()))){
                            isToRemove=true;
                            index=markerPoints.indexOf(point);
                        }
                    }
                    if (isToRemove) {
                        markerPoints.remove(index);
                        markers.remove(index);
                        polylines.clear();
                    }
                    else {
                        markerPoints.add(pointToAdd);
                        markers.add(mMap.addMarker(markerOptions));
                }



                    List<String> urls = URLUtil.getDirectionsUrl(markerPoints);
                    if (urls.size() > 1) {
                        for (int i = 0; i < urls.size(); i++) {
                            String url = urls.get(i);
                            DownloadTask downloadTask = new DownloadTask();
                            // Start downloading json data from Google Directions API
                            downloadTask.execute(url).get();
                        }
                    }
                }
            } catch (JSONException e){
                Log.d("PlacesAPI",e.toString());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
