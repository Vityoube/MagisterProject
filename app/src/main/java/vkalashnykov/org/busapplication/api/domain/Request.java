package vkalashnykov.org.busapplication.api.domain;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Request {
    private String createDate;
    private String status;
    private  Position from;
    private Position to;
    private int seatsNumber;
    private int trunk;
    private int salonTrunk;
    private String clientKey;
    private String driverKey;
    private String routeKey;


    public Request() {
    }

    public Request(Position from, Position to, String status,
                   int seatsNumber, int trunk, int salonTrunk, String clientKey,
                   String driverKey, String routeKey) {
        this.from=from;
        this.to=to;
        createDate=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.status=status;
        this.seatsNumber=seatsNumber;
        this.trunk=trunk;
        this.salonTrunk=salonTrunk;
        this.clientKey=clientKey;
        this.driverKey=driverKey;
        this.routeKey=routeKey;
    }


    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String  status) {
        this.status = status;
    }

    public Position getFrom() {
        return from;
    }

    public void setFrom(Position from) {
        this.from = from;
    }

    public Position getTo() {
        return to;
    }

    public void setTo(Position to) {
        this.to = to;
    }

    public int getSeatsNumber() {
        return seatsNumber;
    }

    public void setSeatsNumber(int seatsNumber) {
        this.seatsNumber = seatsNumber;
    }

    public int getTrunk() {
        return trunk;
    }

    public void setTrunk(int trunk) {
        this.trunk = trunk;
    }

    public int getSalonTrunk() {
        return salonTrunk;
    }

    public void setSalonTrunk(int salonTrunk) {
        this.salonTrunk = salonTrunk;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getDriverKey() {
        return driverKey;
    }

    public void setDriverKey(String driverKey) {
        this.driverKey = driverKey;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }
}
