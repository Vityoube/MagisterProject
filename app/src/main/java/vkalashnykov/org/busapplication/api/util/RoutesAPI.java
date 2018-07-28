package vkalashnykov.org.busapplication.api.util;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import vkalashnykov.org.busapplication.ClientMainActivity;
import vkalashnykov.org.busapplication.MainActivity;
import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Point;
import vkalashnykov.org.busapplication.api.domain.Route;

public class RoutesAPI {
    private Context context;
    private LatLng currentPlaceSelection = null;
    private ArrayList<vkalashnykov.org.busapplication.api.domain.Point> markerPoints = new ArrayList();
    private DatabaseReference routeReference;
    private  String currentDriverKey;
    private static RoutesAPI routesAPI;
    private ArrayList<PolylineOptions> polylines=new ArrayList<>();

    private RoutesAPI() {
    }

    public static synchronized RoutesAPI  getInstance(){
        if (routesAPI==null){
            routesAPI=new RoutesAPI();
        }
        return  routesAPI;
    }

    public void getRoute(String routeKey){
        routeReference= FirebaseDatabase.getInstance().
                getReference().child("routes").child(routeKey);
        routeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Route route=dataSnapshot.getValue(Route.class);
                markerPoints=route.getRoute();

                List<String> urls = getDirectionsUrl(markerPoints);
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
                Toast.makeText(RoutesAPI.this.context,
                        R.string.databaseError,Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void downloadRoute(ArrayList<Point> route){
        List<String> urls=getDirectionsUrl(route);
        if (urls.size() > 1) {
            for (int i = 0; i < urls.size(); i++) {
                String url = urls.get(i);
                DownloadTask downloadTask = new DownloadTask();
                // Start downloading json data from Google Directions API
                downloadTask.execute(url);
            }
        }
    }

    public void addPointToRoute(LatLng latLng){
        String apiKey="AIzaSyAcwyEytYneiCAeth4iXI8iMyatyHUkN5U";
        final String placeUrl="https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+
                latLng.latitude+","+latLng.longitude+"&radius=0&type=bus_station&key="+apiKey;
        currentPlaceSelection=latLng;
        final CallPlacesAPI callPlacesAPI=new CallPlacesAPI();
        try {
            callPlacesAPI.execute(placeUrl).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }




    public class DownloadTask extends AsyncTask<String, Void, String> {

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
                polylines.add(lineOptions);
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
    public List<String> getDirectionsUrl(ArrayList<vkalashnykov.org.busapplication.api.domain.Point> markerPoints) {
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
    private class CallPlacesAPI extends AsyncTask<String,String,String>{



        @Override
        protected String doInBackground(String... strings) {
            String data=null;
            try {
                data=sendRequest(strings[0]);

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
                    }
                    else {
                        markerPoints.add(pointToAdd);
                    }



                    List<String> urls = getDirectionsUrl(markerPoints);
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


    public String sendRequest(String uri) throws IOException{
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        String data=null;
        try {
            URL url=new URL(uri);
            urlConnection= (HttpURLConnection) url.openConnection();
            iStream=urlConnection.getInputStream();
            StringBuffer sb=new StringBuffer();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            String line="";
            while((line=br.readLine())!=null){
                sb.append(line);
            }
            data=sb.toString();
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        Log.d("PLACES_API",data);
        return data;
    }


    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList<Point> getMarkerPoints() {
        return markerPoints;
    }

    public ArrayList<PolylineOptions> getPolylines() {
        return polylines;
    }
}
