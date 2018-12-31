package vkalashnykov.org.busapplication.api.util;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Leg;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.request.DirectionDestinationRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import vkalashnykov.org.busapplication.R;
import vkalashnykov.org.busapplication.api.domain.Position;

public class DistanceUtil {

    public static long calculateDistanceTimeFromDirections(ArrayList<Position> pointsOnRoute, String apiKey){
        final LatLng origin = new LatLng(
                pointsOnRoute.get(0).getLatitude(),
                pointsOnRoute.get(0).getLongitude()
        );
        final LatLng destination = new LatLng(
                pointsOnRoute.get(pointsOnRoute.size() - 1).getLatitude(),
                pointsOnRoute.get(pointsOnRoute.size() - 1).getLongitude()
        );
        List<LatLng> waypoints = new ArrayList<>();
        if (pointsOnRoute.size() > 2) {
            for (int i = 1; i < pointsOnRoute.size() - 1; i++) {
                LatLng waypoint = new LatLng(
                        pointsOnRoute.get(i).getLatitude(),
                        pointsOnRoute.get(i).getLongitude()
                );
                waypoints.add(waypoint);
            }
        }
        final long[] driveTime = {0};
        DirectionDestinationRequest directionRequest = GoogleDirection
                .withServerKey(apiKey)
                .from(origin);
        if (!waypoints.isEmpty())
            directionRequest.and(waypoints);
        directionRequest
                .to(destination)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback(){

                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        if (direction.isOK()){
                            Route route = direction.getRouteList().get(0);

                            for (Leg leg: route.getLegList()){
                                driveTime[0] +=Long.parseLong(leg.getDuration().getValue());
                            }
                        }

                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {

                    }
                });
        return driveTime[0];
    }
}
