package busfinder.data;

import java.awt.Point; 
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusStop{

    public final String name;
    public final double lat, lon;
    public Point pixelCoords = new Point(); 
    public final List<String> routes = new ArrayList<>();

    public BusStop(String name, double lat, double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStop stop = (BusStop) o;
        return Objects.equals(name, stop.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}