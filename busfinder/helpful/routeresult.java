package busfinder.helpful;

import busfinder.data.BusStop;
import java.util.ArrayList;
import java.util.List;

public class routeresult {

    public final List<List<BusStop>> segments;
    public final List<String> routeNames;
    public final double totalDistance;
    public final List<BusStop> majorStops;

    public routeresult(List<List<BusStop>> segments, List<String> routeNames, double totalDistance, List<BusStop> majorStops) {
        this.segments = new ArrayList<>(segments);
        this.routeNames = new ArrayList<>(routeNames);
        this.totalDistance = totalDistance;
        this.majorStops = new ArrayList<>(majorStops);
    }

    public List<BusStop> getFullPath() {
        List<BusStop> path = new ArrayList<>();
        for (List<BusStop> segment : segments) {
            if (segment == null || segment.isEmpty()) {
                continue;
            }
            if (path.isEmpty()) {
                path.addAll(segment);
            } else {
                if (segment.size() > 1) {
                    path.addAll(segment.subList(1, segment.size())); // was done to avoid duplicating transfer points
                } else if (!path.get(path.size() - 1).equals(segment.get(0))) {
                    path.add(segment.get(0));
                }
            }
        }
        return path;
    }
}
