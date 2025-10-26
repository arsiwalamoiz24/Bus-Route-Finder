package busfinder.helpful;

import busfinder.data.referencepoint;
import java.awt.Point;

public class CoordinateConverter {

    private final double lonToXScale, lonToXOffset, latToYScale, latToYOffset;

    public CoordinateConverter(referencepoint p1, referencepoint p2) {
        this.lonToXScale = (double) (p2.x - p1.x) / (p2.lon - p1.lon);
        this.lonToXOffset = p1.x - (this.lonToXScale * p1.lon);
        this.latToYScale = (double) (p2.y - p1.y) / (p2.lat - p1.lat);
        this.latToYOffset = p1.y - (this.latToYScale * p1.lat);
    }

    public Point convert(double lat, double lon) {
        int x = (int) Math.round(lon * lonToXScale + lonToXOffset);
        int y = (int) Math.round(lat * latToYScale + latToYOffset);
        return new Point(x, y);
    }
}