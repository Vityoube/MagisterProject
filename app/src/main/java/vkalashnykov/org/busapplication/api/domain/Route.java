package vkalashnykov.org.busapplication.api.domain;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class Route implements Serializable{
    private ArrayList<Position> points;
    private String time;
    private String status;
    private List<Distance> distances;
    private List<Request> acceptedRequests;

    public Route() {
        points=new ArrayList<>();
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        distances=new ArrayList<>();
        acceptedRequests=new ArrayList<>();

    }

    public Route(ArrayList<Position> points, String status) {
        this.points = points;
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.status=status;
        distances=new ArrayList<>();
        acceptedRequests=new ArrayList<>();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return this.points.equals(route.getPoints()) &&
                this.time.equals(route.getTime()) && this.status.equals(route.getStatus());
    }

    public Position addPoint(Position point, int position){
        if (points==null)
            points=new ArrayList<>();
        if (position<points.size())
            points.add(position,point);
        return point;
    }

    public List<Request> getAcceptedRequests() {
        return acceptedRequests;
    }

    public void setAcceptedRequests(List<Request> acceptedRequests) {
        this.acceptedRequests = acceptedRequests;
    }

    public Request addAcceptedRequest(Request request){
        if (acceptedRequests==null)
            acceptedRequests=new ArrayList<>();
        acceptedRequests.add(request);
        return request;
    }

    public Request removeAcceptedRequest(Request request){
        if (acceptedRequests!=null && acceptedRequests.contains(request))
            acceptedRequests.remove(request);
        return request;
    }
}
