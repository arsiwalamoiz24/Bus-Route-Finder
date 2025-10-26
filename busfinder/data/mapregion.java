package busfinder.data;

import busfinder.helpful.CoordinateConverter;
import java.awt.image.BufferedImage; 

public class mapregion {
    public final String name;
    public final double minLat, maxLat, minLon, maxLon;
    public final BufferedImage image;
    public final referencepoint refPoint1, refPoint2;

    public mapregion(String name, double minLat, double maxLat, double minLon, double maxLon, BufferedImage image, referencepoint refPoint1, referencepoint refPoint2) {
        this.name = name;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.image = image;
        this.refPoint1 = refPoint1;
        this.refPoint2 = refPoint2;
    }

    public boolean contains(BusStop stop) {
        return minLat <= stop.lat && stop.lat <= maxLat && minLon <= stop.lon && stop.lon <= maxLon;
    }

    public BufferedImage getImage() {
        return image;
    }

    public CoordinateConverter getConverter() {
        return new CoordinateConverter(refPoint1, refPoint2);
    }
}