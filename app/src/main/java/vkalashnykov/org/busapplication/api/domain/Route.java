package vkalashnykov.org.busapplication.api.domain;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Route implements Serializable{
    private ArrayList<Position> points;
    private String time;
    private String status;
    private List<Distance> distances;
    private List<Request> acceptedRequests;
    private List<Request> requests;
    private  String routeKey;

    public Route() {
        points=new ArrayList<>();
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        distances=new ArrayList<>();
        acceptedRequests=new ArrayList<>();
        requests=new ArrayList<>();
        routeKey= UUID.randomUUID().toString();

    }

    public Route(ArrayList<Position> points, String status) {
        this.points = points;
        time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        this.status=status;
        distances=new ArrayList<>();
        acceptedRequests=new ArrayList<>();
        routeKey= UUID.randomUUID().toString();
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

    public List<Request> getRequests() {
        return requests;
    }

    public void setRequests(List<Request> requests) {
        this.requests = requests;
    }

    public Request addRequest(Request request){
        if (requests==null)
            requests=new ArrayList<>();
        requests.add(request);
        return request;
    }

    public Request removeRequest(Request request){
        if (requests!=null && requests.contains(request))
            requests.remove(request);
        return request;
    }

    public String getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(String routeKey) {
        this.routeKey = routeKey;
    }
}
