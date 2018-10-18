package vkalashnykov.org.busapplication.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.config.GoogleDirectionConfiguration;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;

public class ClientMapFragment extends MapFragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener,
        OnMapReadyCallback {
// TODO: add possibility to add Location to Request from Client if access from Client Request Activity
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private ArrayList<Point> currentSelectedRoute;
    private DatabaseReference currentRouteRef;
    private GoogleMap mMap;
    private final String MAIN_TAG="main";
    private final String REQUEST="request";
    private Marker currentDriverMarker;
    private  IconGenerator mIconGenerator;
    private ImageView mImageView;
    private ArrayList<Marker> markers;
    private  ArrayList<Polyline> polylines;
    private Button sendRequestButton;

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
        if (REQUEST.equals(getTag())){

        }
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
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

        if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1600);
        }
        return super.onCreateView(layoutInflater, viewGroup, bundle);
    }


    // TODO: On Future: change Driver's position marker to more "coloristic" version
    public void updateRoute(ArrayList<Point> selectedRoute) {
        if (markers!=null && !markers.isEmpty()){
            for (Marker marker : markers){
                marker.remove();
            }
        }
        markers=new ArrayList<Marker>();
        for (int i=0;i<selectedRoute.size();i++){
            Point point=selectedRoute.get(i);
            MarkerOptions options=new MarkerOptions();
            options.position(new LatLng(point.getLatitude(),point.getLongitude()));
            if(i==0){
                options.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            } else if (i==selectedRoute.size()-1){
                options.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            } else {
                options.icon(
                        BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            markers.add(mMap.addMarker(options));

        }

        if (polylines != null && !polylines.isEmpty()) {
            for (Polyline polyline : polylines) {
                polyline.remove();
            }
        }
        polylines=new ArrayList<>();
        if (selectedRoute.size() > 1) {
            LatLng origin = new LatLng(
                    selectedRoute.get(0).getLatitude(),
                    selectedRoute.get(0).getLongitude()
            );
            List<LatLng> waypoints = new ArrayList<>();
            if (selectedRoute.size() > 2) {
                for (int i = 1; i < selectedRoute.size() - 1; i++) {
                    waypoints.add(new LatLng(
                                    selectedRoute.get(i).getLatitude(),
                                    selectedRoute.get(i).getLongitude()
                            )
                    );
                }
            }
            LatLng destination = new LatLng(
                    selectedRoute.get(selectedRoute.size() - 1).getLatitude(),
                    selectedRoute.get(selectedRoute.size() - 1).getLongitude()
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
//                                setCameraWithCoordinationBounds(route);
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


    public void updateDriverPosition(Point driverPosition) {
        currentDriverMarker.setPosition(new LatLng(
                driverPosition.getLatitude(),
                driverPosition.getLongitude()
                )
        );
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

    public void createDriverPositionMarker(Point driverPosition) {
        if (currentDriverMarker!=null){
            currentDriverMarker.remove();
        }
        MarkerOptions driverPositionOptions=new MarkerOptions();
        driverPositionOptions.position(new LatLng(driverPosition.getLatitude(),
                driverPosition.getLongitude()));
        driverPositionOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_action_bus));
        currentDriverMarker=mMap.addMarker(driverPositionOptions);
        sendRequestButton=new Button(getActivity());
        View mapFragmentView=getView();
    }
}
