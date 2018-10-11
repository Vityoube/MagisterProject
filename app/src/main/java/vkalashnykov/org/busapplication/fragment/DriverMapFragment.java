package vkalashnykov.org.busapplication.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.google.maps.android.SphericalUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;
import vkalashnykov.org.busapplication.api.util.URLUtil;

public class DriverMapFragment extends MapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {

    private LatLng currentPlaceSelection = null;
    private ArrayList<Point> markerPoints = new ArrayList();
    private String currentDriverKey;
    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    GoogleMap mMap;
    private DatabaseReference currentRouteRef;
    int PLACE_PICKER_REQUEST = 1;

    // TODO: Optimization of Route calculatings is done. Algorythm needs to be polished.


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this.getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
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
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 16));
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
        this.mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);

        }
        mMap.setMyLocationEnabled(true);
        getRouteFromDatabase();
        Button selectPointButton = getActivity().findViewById(R.id.addPoint);
        selectPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder placesBuilder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(placesBuilder.build(DriverMapFragment.this.getActivity()),
                            PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Log.e("PlacesAPI", "GooglePlayServicesRepairableException: ", e);
                } catch (GooglePlayServicesNotAvailableException e) {
                    Log.e("PlacesAPI", "GooglePlayServicesRepairableException: ", e);
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                final Marker markerToRemove=marker;
                AlertDialog.Builder removeMarkerDialogBuilder = new AlertDialog.Builder(getActivity());
                removeMarkerDialogBuilder.setMessage(R.string.remove_point_question);
                removeMarkerDialogBuilder.setPositiveButton(R.string.remove,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                double latitude = markerToRemove.getPosition().latitude;
                                double longitude = markerToRemove.getPosition().longitude;
                                markerToRemove.remove();
                                int index = markers.indexOf(markerToRemove);
                                markers.remove(index);
                                markerPoints = new ArrayList<>();
                                for (Marker markerPoint : markers) {
                                    Point point = new Point(markerPoint.getPosition().latitude,
                                            markerPoint.getPosition().longitude);
                                    markerPoints.add(point);
                                }
                                updateRoute();
                            }
                        });
                removeMarkerDialogBuilder.setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                removeMarkerDialogBuilder.show();
                return false;

            }
        });
    }

    private void saveRoute() {
        currentRouteRef.child("route").setValue(markerPoints);
    }

    private void getRouteFromDatabase() {
        currentRouteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route = dataSnapshot.getValue(Route.class);
                markerPoints = route.getRoute();
                for (Point point : markerPoints) {
                    MarkerOptions options = new MarkerOptions();
                    options.position(new LatLng(point.getLatitude(), point.getLongitude()));
                    markers.add(mMap.addMarker(options));
                }

                updateRoute();
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(DriverMapFragment.this.getActivity(),
                        R.string.databaseError, Toast.LENGTH_SHORT).show();
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

        getMapAsync(this);

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
        currentDriverKey = getActivity().getIntent().getStringExtra("USER_KEY");
        ;
        currentRouteRef = FirebaseDatabase.getInstance().getReference().child("routes").child(currentDriverKey);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
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

    public void updateRoute() {
        saveRoute();
        if (polylines != null && !polylines.isEmpty()) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
            polylines.clear();
        }
        if (markerPoints.size() > 1) {
            LatLng origin = new LatLng(
                    markerPoints.get(0).getLatitude(),
                    markerPoints.get(0).getLongitude()
            );
            List<LatLng> waypoints = new ArrayList<>();
            if (markerPoints.size() > 2) {
                for (int i = 1; i < markerPoints.size() - 1; i++) {
                    waypoints.add(new LatLng(
                                    markerPoints.get(i).getLatitude(),
                                    markerPoints.get(i).getLongitude()
                            )
                    );
                }
            }
            LatLng destination = new LatLng(
                    markerPoints.get(markerPoints.size() - 1).getLatitude(),
                    markerPoints.get(markerPoints.size() - 1).getLongitude()
            );
            GoogleDirectionConfiguration.getInstance().setLogEnabled(true);

            GoogleDirection.withServerKey(getString(R.string.api_key))
                    .from(origin)
                    .and(waypoints)
                    .to(destination)
                    .transportMode(TransportMode.DRIVING)
                    .execute(new DirectionCallback() {
                        @Override
                        public void onDirectionSuccess(Direction direction, String rawBody) {

                            Log.d("DirectionsAPI", direction.getStatus());
                            if (direction.getRouteList().size() > 0) {
                                com.akexorcist.googledirection.model.Route route = direction.getRouteList().get(0);
                                int legCount = route.getLegList().size();
                                for (int index = 0; index < legCount; index++) {
                                    Leg leg = route.getLegList().get(index);
                                    List<Step> stepList = leg.getStepList();
                                    ArrayList<PolylineOptions> polylineOptionList = DirectionConverter.createTransitPolyline(
                                            getActivity(), stepList, 5, Color.RED, 3, Color.BLUE);
                                    for (PolylineOptions polylineOption : polylineOptionList) {
                                        polylines.add(mMap.addPolyline(polylineOption));
                                    }
                                }
                                setCameraWithCoordinationBounds(route);
                            } else {
                                Log.e("DirectionsAPI", "Cannot find routes");
                            }

                        }

                        @Override
                        public void onDirectionFailure(Throwable t) {
                            Log.e("DirectionsAPI", "Error on getting result:", t);
                        }
                    });
        }

    }

    private void setCameraWithCoordinationBounds(com.akexorcist.googledirection.model.Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this.getActivity());
                if (place.getPlaceTypes().contains(Place.TYPE_TRANSIT_STATION)) {
                    addPointToRoute(place.getLatLng());
                }
            }
        }

    }

    private void addPointToRoute(LatLng selection) {
        vkalashnykov.org.busapplication.api.domain.Point pointToAdd =
                new vkalashnykov.org.busapplication.api.domain.Point(
                        selection.latitude,
                        selection.longitude
                );
        if (markerPoints == null)
            markerPoints = new ArrayList<>();
        for (Point point : markerPoints) {
            if (point.getLatitude().equals(pointToAdd.getLatitude()) && point.getLongitude().equals(pointToAdd.getLongitude())) {
                return;
            }
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(selection);
        int index=0;
        double minDistance=999999999;
        if (!markerPoints.isEmpty()){
            for (int i=0;i<markerPoints.size();i++){
                LatLng compableLatLng=new LatLng(
                        markerPoints.get(i).getLatitude(),
                        markerPoints.get(i).getLongitude()
                );
                double currentDistance=SphericalUtil.computeDistanceBetween(selection,compableLatLng);
                if (currentDistance<minDistance){
                    minDistance=currentDistance;
                    index=i+1;
                }
            }

        }
        if (index==0){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else if (index==markers.size()){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        markerPoints.add(index,pointToAdd);

        markers.add(index,mMap.addMarker(markerOptions));

        updateRoute();

    }
}
