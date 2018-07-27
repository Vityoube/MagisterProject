package vkalashnykov.org.busapplication.api.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.api.domain.Point;

public class URLUtil {

    public static String downloadUrl(String strUrl) throws IOException {
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
    public static List<String> getDirectionsUrl(ArrayList<Point> markerPoints) {
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

    public static String sendRequest(String uri) throws IOException{
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
}
