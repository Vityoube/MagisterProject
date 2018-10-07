package vkalashnykov.org.busapplication.api.domain;

import java.io.Serializable;

public class Point implements Serializable{
    private Double latitude;
    private Double longitude;

    public Point() {
    }

    public Point(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
