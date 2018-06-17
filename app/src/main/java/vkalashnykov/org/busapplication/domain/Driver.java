package vkalashnykov.org.busapplication.domain;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class Driver {
    private String username;
    private String firstName;
    private String lastName;
    private int age;
    private int busSize;
    private ArrayList<Point> route;
    private Point currentPosition;

    public Driver() {
    }

    public Driver(String username, String firstName, String lastName, int age, int busSize) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.busSize = busSize;
        route=new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getBusSize() {
        return busSize;
    }

    public void setBusSize(int busSize) {
        this.busSize = busSize;
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
