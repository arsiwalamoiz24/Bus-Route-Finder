package busfinder.data;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class  database{

    private final List<BusStop> allStops = new ArrayList<>();
    private final Map<String, BusStop> stopMap = new HashMap<>();
    private final Map<String, List<BusStop>> routes = new HashMap<>();
    private final List<mapregion> mapRegions = new ArrayList<>();
    private List<mapregion> sortedMapRegions;
    private mapregion overviewMap;

    public database() {
        loadStopsFromCSV("Masterfile_mumbai_bus_stops.csv");
        loadRoutesFromCSV("(TEMPERARY)bus_routes_fully_corrected.csv");
        loadMapRegionsFromCSV("map_regions.csv");
        buildSortedMaps();

        mapregion largestRegionSoFar = null;
        double maxAreaSoFar = -1.0;
        for (mapregion currentRegion : mapRegions) {
            double height = currentRegion.maxLat - currentRegion.minLat;
            double width = currentRegion.maxLon - currentRegion.minLon;
            double currentArea = height * width;
            if (currentArea > maxAreaSoFar) {
                maxAreaSoFar = currentArea;
                largestRegionSoFar = currentRegion;
            }
        }
        overviewMap = largestRegionSoFar;
    }

    private void loadStopsFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                String lonStr = parts[parts.length - 1].trim();
                String latStr = parts[parts.length - 2].trim();
                String name = "";
                for (int i = 0; i < parts.length - 2; i++) {
                    name = name + parts[i];
                    if (i < parts.length - 3) {
                        name = name + ",";
                    }
                }
                name = name.trim();
                if (name.startsWith("\"") && name.endsWith("\"")) {
                    name = name.substring(1, name.length() - 1);
                }
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);
                BusStop stop = new BusStop(name, lat, lon);
                if (name.contains(",")) {
                    System.out.println("Stop: [" + name + "], hashcodee: " + stop.hashCode());
                }
                allStops.add(stop);
                stopMap.put(name.toUpperCase(), stop);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading stops from file: " + e.getMessage());
        }
    }

    private void loadRoutesFromCSV(String filename) {
        Map<String, List<RouteEntry>> tempRoutes = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line = br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                if (parts.length < 3) {
                    continue;
                }
                String busNumber = parts[0].trim();
                int seq;
                try {
                    seq = Integer.parseInt(parts[1].trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid sequence number in route line: " + line);
                    continue;
                }
                String busStopName = "";
                for (int i = 2; i < parts.length; i++) {
                    busStopName = busStopName + parts[i];
                    if (i < parts.length - 1) {
                        busStopName = busStopName + ",";
                    }
                }
                busStopName = busStopName.trim();
                BusStop stop = stopMap.get(busStopName.toUpperCase());
                if (stop != null) {
                    List<RouteEntry> stopsForThisBus = tempRoutes.computeIfAbsent(busNumber, k -> new ArrayList<>());
                    stopsForThisBus.add(new RouteEntry(seq, stop));
                    stop.routes.add(busNumber);
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading routes from file '" + filename + "': " + e.getMessage());
        }

        for (String busNumber : tempRoutes.keySet()) {
            List<RouteEntry> jumbledStops = tempRoutes.get(busNumber);
            jumbledStops.sort(Comparator.comparingInt(entry -> entry.seq));
            List<BusStop> finalRoute = new ArrayList<>();
            for (RouteEntry entry : jumbledStops) {
                finalRoute.add(entry.stop);
            }
            routes.put(busNumber, finalRoute);
        }
    }

    private void loadMapRegionsFromCSV(String filename) {
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 14) {
                    System.out.println("Skipping invalid map region line: " + line);
                    continue;
                }
                try {
                    String name = parts[0].trim();
                    String imageFile = parts[1].trim();
                    double lat1 = Double.parseDouble(parts[2]);
                    double lon1 = Double.parseDouble(parts[3]);
                    int x1 = Integer.parseInt(parts[4]);
                    int y1 = Integer.parseInt(parts[5]);
                    referencepoint refPoint1 = new referencepoint(lat1, lon1, x1, y1);
                    double lat2 = Double.parseDouble(parts[6]);
                    double lon2 = Double.parseDouble(parts[7]);
                    int x2 = Integer.parseInt(parts[8]);
                    int y2 = Integer.parseInt(parts[9]);
                    referencepoint refPoint2 = new referencepoint(lat2, lon2, x2, y2);
                    double minLat = Double.parseDouble(parts[10]);
                    double maxLat = Double.parseDouble(parts[11]);
                    double minLon = Double.parseDouble(parts[12]);
                    double maxLon = Double.parseDouble(parts[13]);
                    BufferedImage image = null;
                    try {
                        File imgFile = new File(imageFile);
                        if (!imgFile.exists()) {
                            System.out.println("Image file not found: " + imageFile);
                            continue;
                        }
                        image = ImageIO.read(imgFile);
                    } catch (IOException e) {
                        System.out.println("Failed to load image: " + imageFile + " - " + e.getMessage());
                        continue;
                    }
                    mapregion region = new mapregion(name, minLat, maxLat, minLon, maxLon, image, refPoint1, refPoint2);
                    mapRegions.add(region);
                } catch (NumberFormatException nfe) {
                    System.out.println("Error parsing number in map region line: " + line + " - " + nfe.getMessage());
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    System.out.println("Error accessing data in map region line (not enough columns?): " + line + " - " + aioobe.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading map regions from file '" + filename + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildSortedMaps() {
        List<mapregion> listToSort = new ArrayList<>(mapRegions);
        int listSize = listToSort.size();
        for (int i = 0; i < listSize - 1; i++) {
            for (int j = 0; j < listSize - i - 1; j++) {
                mapregion region1 = listToSort.get(j);
                mapregion region2 = listToSort.get(j + 1);
                double area1 = (region1.maxLat - region1.minLat) * (region1.maxLon - region1.minLon);
                double area2 = (region2.maxLat - region2.minLat) * (region2.maxLon - region2.minLon);
                if (area1 > area2) {
                    mapregion temp = listToSort.get(j);
                    listToSort.set(j, listToSort.get(j + 1));
                    listToSort.set(j + 1, temp);
                }
            }
        }
        this.sortedMapRegions = listToSort;
    }

    public List<BusStop> getAllStops() {
        return new ArrayList<>(allStops);
    }

    public Map<String, BusStop> getStopMap() {
        return stopMap;
    }

    public List<BusStop> getRoute(String routeName) {
        List<BusStop> originalRoute = routes.get(routeName);
        if (originalRoute != null) {
            return new ArrayList<>(originalRoute);
        } else {
            return null;
        }
    }

    public List<mapregion> getSortedMapRegions() {
        if (sortedMapRegions != null) {
            return new ArrayList<>(sortedMapRegions);
        } else {
            return Collections.emptyList();
        }
    }

    public mapregion getOverviewMap() {
        return overviewMap;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private static class RouteEntry {

        int seq;
        BusStop stop;

        RouteEntry(int seq, BusStop stop) {
            this.seq = seq;
            this.stop = stop;
        }
    }
}
