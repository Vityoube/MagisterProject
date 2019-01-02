package vkalashnykov.org.busapplication.api.domain;


import java.io.Serializable;
import java.text.DecimalFormat;

public class Position implements Serializable{
    private Double latitude;
    private Double longitude;

    public Position() {
    }

    public Position(Double latitude, Double longitude) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

       return this.longitude==position.getLongitude() && this.latitude==position.getLatitude();
    }

    @Override
    public int hashCode() {
        int result = latitude != null ? latitude.hashCode() : 0;
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        DecimalFormat format=new DecimalFormat("#.#####");
        return "[" +
                 format.format(latitude) +
                ", " + format.format(longitude)+
                ']';
    }
}
