package vkalashnykov.org.busapplication.api.domain;

import java.io.Serializable;
import java.util.ArrayList;

public class Route implements Serializable{
    private String driverName;
    private ArrayList<Point> route;
    private Point currentPosition;

    public Route() {
        route=new ArrayList<>();
    }

    public Route(String driverName, ArrayList<Point> route) {
        this.driverName = driverName;
        this.route = route;
    }

    public Route(String driverName, ArrayList<Point> route, Point currentPosition) {
        this.driverName = driverName;
        this.route = route;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public ArrayList<Point> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Point> route) {
        this.route = route;
    }

    public Point getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Point currentPosition) {
        this.currentPosition = currentPosition;
    }

}
