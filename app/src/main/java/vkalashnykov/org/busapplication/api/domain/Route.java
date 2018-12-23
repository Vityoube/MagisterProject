package vkalashnykov.org.busapplication.api.domain;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable{
    private ArrayList<Position> points;

    public Route(ArrayList<Position> points) {
        this.points = new ArrayList<>();
    }

    public ArrayList<Position> getPoints() {
        return points;
    }

    public void setPoints(ArrayList<Position> points) {
        this.points = points;
    }
}
