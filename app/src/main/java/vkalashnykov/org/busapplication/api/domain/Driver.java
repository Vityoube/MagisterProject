package vkalashnykov.org.busapplication.api.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Driver {
    private String username;
    private String firstName;
    private String lastName;
    private int age;
    private int busSize;
    private ArrayList<Position> route;
    private Position currentPosition;
    private ArrayList<Route> routes;
    private int trunkCapacity;
    private int salonCapacity;
    private int minSeats;
    private int fullNumberSeats;

    public Driver() {
    }

    public Driver(String username, String firstName, String lastName, int age, int busSize) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.busSize = busSize;
        route=new ArrayList<>();
        routes=new ArrayList<>();
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

    public ArrayList<Position> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Position> route) {
        this.route = route;
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(Position currentPosition) {
        this.currentPosition = currentPosition;
    }

    public ArrayList<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(ArrayList<Route>  routes) {
        this.routes = routes;
    }

    public void addRoute(Route route){
        if (routes==null)
            routes=new ArrayList<>();
        routes.add(route);
    }

    public int getTrunkCapacity() {
        return trunkCapacity;
    }

    public void setTrunkCapacity(int trunkCapacity) {
        this.trunkCapacity = trunkCapacity;
    }

    public int getSalonCapacity() {
        return salonCapacity;
    }

    public void setSalonCapacity(int salonCapacity) {
        this.salonCapacity = salonCapacity;
    }

    public int getMinSeats() {
        return minSeats;
    }

    public void setMinSeats(int minSeats) {
        this.minSeats = minSeats;
    }

    public int getFullNumberSeats() {
        return fullNumberSeats;
    }

    public void setFullNumberSeats(int fullNumberSeats) {
        this.fullNumberSeats = fullNumberSeats;
    }
}
