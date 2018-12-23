package vkalashnykov.org.busapplication.api.domain;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable{
    private ArrayList<Marker> markers;
    private ArrayList<Polyline> lines;

    public Route(){
        markers=new ArrayList<>();
        lines=new ArrayList<>();
    }

    public ArrayList<Marker> getMarkers() {
        return markers;
    }

    public void setMarkers(ArrayList<Marker> markers) {
        this.markers = markers;
    }

    public ArrayList<Polyline> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Polyline> lines) {
        this.lines = lines;
    }
}
