package vkalashnykov.org.busapplication.api.domain;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Route implements Serializable{
    private ArrayList<Position> points;
    private String time;

    public Route() {
        points=new ArrayList<>();
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public Route(ArrayList<Position> points) {
        this.points = points;
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public ArrayList<Position> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Position> points) {
        this.points = points;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
