package busfinder.gui;

import busfinder.data.BusStop;
import busfinder.data.database;
import busfinder.data.mapregion;
import busfinder.helpful.CoordinateConverter;
import busfinder.helpful.routeresult;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;   
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class mappanel extends JPanel {

    private final database dataManager;
    private final routedetailspanel routeDetailsPanel;
    private mapregion currentMap;
    private routeresult currentRouteResult;

    public mappanel(database dataManager, routedetailspanel routeDetailsPanel) {
        this.dataManager = dataManager;
        this.routeDetailsPanel = routeDetailsPanel;
        this.currentMap = dataManager.getOverviewMap();
        setBackground(Color.WHITE);
    }

    public routeresult findPathWithTransfers(BusStop start, BusStop end) {
        final double TRANSFER_PENALTY = 10.0;

        Comparator<PathNode> pathNodeComparator = new Comparator<PathNode>() {
            @Override
            public int compare(PathNode node1, PathNode node2) {
                double totalCost1 = node1.cost + heuristic(node1.stop, end);
                double totalCost2 = node2.cost + heuristic(node2.stop, end);
                if (totalCost1 < totalCost2) {
                    return -1;
                } else if (totalCost1 > totalCost2) {
                    return 1;
                } else {
                    return 0;
                }
            }
        };

        PriorityQueue<PathNode> pq = new PriorityQueue<>(pathNodeComparator);
        Map<String, Map<BusStop, PathNode>> visited = new HashMap<>(); //A* search table

        for (String routeName : start.routes) {
            if (!visited.containsKey(routeName)) {
                visited.put(routeName, new HashMap<>());
            }
            PathNode startNode = new PathNode(start, null, routeName, 0, 0.0);
            
            pq.add(startNode);
            visited.get(routeName).put(start, startNode);
        }

        PathNode destinationNode = null;


        while (!pq.isEmpty()) { // basically we check for every single route passing through the current node and the routes taht are passing through the nodes on the the routes passing through evry route
            PathNode currentNode = pq.poll(); //gets me hightest priority element


            if (currentNode.stop.equals(end)) {
                destinationNode = currentNode;
                break;
            }

            List<BusStop> routeStops = dataManager.getRoute(currentNode.route);
            int currentstop = routeStops.indexOf(currentNode.stop);

            int[] adjacentstops = {currentstop - 1, currentstop + 1};
            for (int adjstop : adjacentstops) {
                if (adjstop >= 0 && adjstop < routeStops.size()) {
                    BusStop nextStop = routeStops.get(adjstop);
                    double dist = database.calculateDistance(currentNode.stop.lat, currentNode.stop.lon, nextStop.lat, nextStop.lon);
                    double newCost = currentNode.cost + dist;


                    Map<BusStop, PathNode> routeVisited = visited.get(currentNode.route); // creates map of stop of the current bus route
                    PathNode existing = routeVisited.get(nextStop); //checking if the nextstop was already found
                    if (existing == null || newCost < existing.cost) {
                        PathNode nextnode = new PathNode(nextStop, currentNode, currentNode.route, currentNode.transfers, newCost);
                        routeVisited.put(nextStop, nextnode); //updating the  visited map with a new better nextstop
                        pq.add(nextnode); // adding 
                    }
                }
            }
            //considering transfers 
            for (String newRoute : currentNode.stop.routes) {
                if (!newRoute.equals(currentNode.route)) { //


                    if (!visited.containsKey(newRoute)) {
                        visited.put(newRoute, new HashMap<>());
                    }
                    Map<BusStop, PathNode> newRouteVisited = visited.get(newRoute);
                    PathNode existing = newRouteVisited.get(currentNode.stop);
                    double newCost = currentNode.cost + TRANSFER_PENALTY;

                    if (existing == null || newCost < existing.cost) {
                        PathNode transferNode = new PathNode(currentNode.stop, currentNode, newRoute, currentNode.transfers + 1, newCost);
                        newRouteVisited.put(currentNode.stop, transferNode);
                        pq.add(transferNode);
                    }
                }
            }
        }

        if (destinationNode == null) {
            JOptionPane.showMessageDialog(this, "No route found between " + start.getName() + " and " + end.getName(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        List<List<BusStop>> segments = new ArrayList<>();
        List<String> routeNames = new ArrayList<>();
        PathNode current = destinationNode;
        List<BusStop> currentSegment = new ArrayList<>();
        String currentRoute = destinationNode.route;

        while (current != null) {
            currentSegment.add(current.stop);

            boolean isTransfer = current.previous != null && !current.previous.route.equals(currentRoute);
            if (isTransfer) {
                reverseBusStopList(currentSegment);
                segments.add(new ArrayList<>(currentSegment));
                routeNames.add(currentRoute);

                currentSegment.clear();
                currentSegment.add(current.stop);
                currentRoute = current.previous.route;
            }
            current = current.previous;
        }

        if (!currentSegment.isEmpty()) {
            reverseBusStopList(currentSegment);
            segments.add(new ArrayList<>(currentSegment));
            routeNames.add(currentRoute);
        }

        reverseListOfLists(segments);
        reverseStringList(routeNames);

        double totalDistance = 0;
        for (List<BusStop> seg : segments) {
            for (int i = 0; i < seg.size() - 1; i++) {
                totalDistance += database.calculateDistance(seg.get(i).lat, seg.get(i).lon, seg.get(i + 1).lat, seg.get(i + 1).lon);
            }
        }

        return new routeresult(segments, routeNames, totalDistance, List.of(start, end));
    }

    private double heuristic(BusStop from, BusStop to) {
        return database.calculateDistance(from.lat, from.lon, to.lat, to.lon);
    }

    private static void reverseBusStopList(List<BusStop> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        int size = list.size();
        for (int i = 0; i < size / 2; i++) {
            BusStop temp = list.get(i);
            int swapIndex = size - 1 - i;
            list.set(i, list.get(swapIndex));
            list.set(swapIndex, temp);
        }
    }

    private static void reverseStringList(List<String> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        int size = list.size();
        for (int i = 0; i < size / 2; i++) {
            String temp = list.get(i);
            int swapIndex = size - 1 - i;
            list.set(i, list.get(swapIndex));
            list.set(swapIndex, temp);
        }
    }

    private static void reverseListOfLists(List<List<BusStop>> list) {
        if (list == null || list.size() <= 1) {
            return;
        }
        int size = list.size();
        for (int i = 0; i < size / 2; i++) {
            List<BusStop> temp = list.get(i);
            int swapIndex = size - 1 - i;
            list.set(i, list.get(swapIndex));
            list.set(swapIndex, temp);
        }
    }

    public void displayRoute(routeresult result) {
        currentRouteResult = result;
        List<BusStop> path = result.getFullPath();
        currentMap = dataManager.getOverviewMap();
        for (mapregion region : dataManager.getSortedMapRegions()) {
            boolean allStopsContained = true;
            for (BusStop stop : path) {
                if (!region.contains(stop)) {
                    allStopsContained = false;
                    break;
                }
            }
            if (allStopsContained) {
                currentMap = region;
                break;
            }
        }
        updateAllStopPixelCoordinates();
        revalidate();
        repaint();
    }

    private void updateAllStopPixelCoordinates() {
        if (currentMap == null || currentMap.getImage() == null) {
            System.err.println("Error: currentMap or image is null");
            return;
        }
        CoordinateConverter converter = currentMap.getConverter();
        for (BusStop stop : dataManager.getAllStops()) {
            stop.pixelCoords = converter.convert(stop.lat, stop.lon);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        if (currentMap != null && currentMap.getImage() != null) {
            return new Dimension(currentMap.getImage().getWidth(), currentMap.getImage().getHeight());
        }
        return new Dimension(800, 600);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (currentMap == null || currentMap.getImage() == null) {
            g.setColor(Color.BLACK);
            g.drawString("No map available", 20, 20);
            return;
        }
        int mapX = (getWidth() - currentMap.getImage().getWidth()) / 2;
        int mapY = (getHeight() - currentMap.getImage().getHeight()) / 2;
        g.drawImage(currentMap.getImage(), mapX, mapY, this);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(80, 85, 230));
        for (BusStop stop : dataManager.getAllStops()) {
            if (currentMap.contains(stop)) {
                int x = stop.pixelCoords.x + mapX;
                int y = stop.pixelCoords.y + mapY;
                g2d.fillOval(x, y, 6, 6);
            }
        }

        if (currentRouteResult != null) {
            drawRoute(g2d, mapX, mapY);
        }
    }

    private void drawRoute(Graphics2D g2d, int mapX, int mapY) {
        g2d.setStroke(new BasicStroke(3));
        g2d.setColor(Color.RED);

        int imgWidth = currentMap.getImage().getWidth();
        int imgHeight = currentMap.getImage().getHeight();

        for (List<BusStop> segment : currentRouteResult.segments) {
            for (int i = 0; i < segment.size() - 1; i++) {
                BusStop from = segment.get(i);
                BusStop to = segment.get(i + 1);
                if (currentMap.contains(from) && currentMap.contains(to)) {
                    int x1 = from.pixelCoords.x + mapX;
                    int y1 = from.pixelCoords.y + mapY;
                    int x2 = to.pixelCoords.x + mapX;
                    int y2 = to.pixelCoords.y + mapY;
                    g2d.drawLine(x1, y1, x2, y2);
                }
            }
        }
        List<BusStop> specialStops = new ArrayList<>(currentRouteResult.majorStops);

        for (int i = 0; i < currentRouteResult.segments.size() - 1; i++) {
            List<BusStop> segment = currentRouteResult.segments.get(i);
            if (segment != null && !segment.isEmpty()) {
                BusStop transferStop = segment.get(segment.size() - 1);
                if (!specialStops.contains(transferStop)) {
                    specialStops.add(transferStop);
                }
            }
        }

        for (BusStop stop : specialStops) {
            if (currentMap.contains(stop)) {
                drawStopLabel(g2d, stop, mapX, mapY, new Color(255, 0, 0), 8);
                drawStopLabel(g2d, stop, mapX, mapY, new Color(255, 255, 255), 5);
            }
        }
    }

    private void drawStopLabel(Graphics2D g2d, BusStop stop, int mapX, int mapY, Color color, int size) {
        int x = stop.pixelCoords.x + mapX;
        int y = stop.pixelCoords.y + mapY;

        g2d.setColor(color);
        g2d.fillOval(x - size, y - size, size * 2, size * 2);

        if (size == 8) {
            String stopName = stop.getName();
            g2d.setFont(new Font("SansSerif", Font.BOLD, 12));
            FontMetrics fm = g2d.getFontMetrics();

            int textWidth = fm.stringWidth(stopName);
            int textHeight = fm.getHeight();

            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRect(x + 10, y - textHeight / 2, textWidth + 4, textHeight);

            g2d.setColor(Color.BLACK);
            g2d.drawString(stopName, x + 12, y + textHeight / 2 - fm.getDescent());
        }
    }

    private static class PathNode {

        BusStop stop;
        PathNode previous;
        String route;
        int transfers;
        double cost;

        PathNode(BusStop stop, PathNode previous, String route, int transfers, double cost) {
            this.stop = stop;
            this.previous = previous;
            this.route = route;
            this.transfers = transfers;
            this.cost = cost;
        }
    }
}
