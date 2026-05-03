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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
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

    // -----------------------------------------------------------------------
    // PUBLIC API — called from MainApp
    // -----------------------------------------------------------------------

    /** Kept for backwards compatibility — returns the single best path. */
    public routeresult findPathWithTransfers(BusStop start, BusStop end) {
        List<routeresult> top = findTopThreePaths(start, end);
        return top.isEmpty() ? null : top.get(0);
    }

    /**
     * Returns up to 3 distinct route options, ranked by total cost.
     * Each option uses a different combination of bus routes so the user
     * has genuinely different alternatives to choose from.
     */
    public List<routeresult> findTopThreePaths(BusStop start, BusStop end) {
        List<routeresult> results = new ArrayList<>();
        Set<String> usedKeys = new HashSet<>();

        // Escalating transfer penalties push the search towards alternative routes
        double[] penalties = {10.0, 25.0, 50.0};

        for (double penalty : penalties) {
            routeresult r = runSearch(start, end, penalty, usedKeys);
            if (r != null) {
                String key = String.join("|", r.routeNames);
                if (!usedKeys.contains(key)) {
                    results.add(r);
                    usedKeys.add(key);
                }
            }
            if (results.size() >= 3) break;
        }

        return results;
    }

    // -----------------------------------------------------------------------
    // CORE A* SEARCH
    // -----------------------------------------------------------------------

    /**
     * Direction-correct A* search.
     *
     * KEY FIX: buses travel in only one direction along a route.  A passenger
     * at stop index 5 cannot use that route to reach stop index 2 — they would
     * need a different (reverse) service.  We enforce this by:
     *   1. Only seeding start routes where end comes AFTER start in the sequence.
     *   2. Only advancing FORWARD (+1) along a route, never backwards.
     *   3. Only allowing a transfer onto a new route when the destination (or
     *      the end stop) is AHEAD of the transfer point on that new route.
     */
    private routeresult runSearch(BusStop start, BusStop end, double transferPenalty, Set<String> avoidKeys) {
        Comparator<PathNode> comp = (n1, n2) ->
            Double.compare(n1.cost + heuristic(n1.stop, end),
                           n2.cost + heuristic(n2.stop, end));

        PriorityQueue<PathNode> pq = new PriorityQueue<>(comp);
        Map<String, Map<BusStop, PathNode>> visited = new HashMap<>();

        // Seed: only board routes at start where the bus will pass through end
        for (String routeName : start.routes) {
            List<BusStop> routeStops = dataManager.getRoute(routeName);
            if (routeStops == null) continue;
            int startIdx = routeStops.indexOf(start);
            int endIdx   = routeStops.indexOf(end);
            if (startIdx < 0) continue;
            // Skip if end is BEFORE or AT start in this route's direction
            if (endIdx >= 0 && endIdx <= startIdx) continue;

            PathNode node = new PathNode(start, null, routeName, 0, 0.0);
            visited.computeIfAbsent(routeName, k -> new HashMap<>()).put(start, node);
            pq.add(node);
        }

        PathNode destinationNode = null;

        while (!pq.isEmpty()) {
            PathNode cur = pq.poll();

            if (cur.stop.equals(end)) {
                destinationNode = cur;
                break;
            }

            List<BusStop> routeStops = dataManager.getRoute(cur.route);
            if (routeStops == null) continue;
            int idx = routeStops.indexOf(cur.stop);

            // Move FORWARD only — buses are unidirectional
            int nextIdx = idx + 1;
            if (nextIdx < routeStops.size()) {
                BusStop next = routeStops.get(nextIdx);
                double nc = cur.cost + database.calculateDistance(
                        cur.stop.lat, cur.stop.lon, next.lat, next.lon);
                Map<BusStop, PathNode> rv = visited.computeIfAbsent(cur.route, k -> new HashMap<>());
                PathNode ex = rv.get(next);
                if (ex == null || nc < ex.cost) {
                    PathNode nn = new PathNode(next, cur, cur.route, cur.transfers, nc);
                    rv.put(next, nn);
                    pq.add(nn);
                }
            }

            // Consider transfers to other routes at this stop
            for (String newRoute : cur.stop.routes) {
                if (newRoute.equals(cur.route)) continue;
                List<BusStop> nr = dataManager.getRoute(newRoute);
                if (nr == null) continue;
                int boardIdx = nr.indexOf(cur.stop);
                int destIdx  = nr.indexOf(end);
                if (boardIdx < 0) continue;
                // Only transfer if this route goes FORWARD toward the destination
                if (destIdx >= 0 && destIdx <= boardIdx) continue;

                // Penalise routes that were already used in previously found paths
                double extra = avoidKeys.stream().anyMatch(k -> k.contains(newRoute))
                        ? transferPenalty * 2
                        : transferPenalty;

                double nc = cur.cost + extra;
                Map<BusStop, PathNode> rv = visited.computeIfAbsent(newRoute, k -> new HashMap<>());
                PathNode ex = rv.get(cur.stop);
                if (ex == null || nc < ex.cost) {
                    PathNode tn = new PathNode(cur.stop, cur, newRoute, cur.transfers + 1, nc);
                    rv.put(cur.stop, tn);
                    pq.add(tn);
                }
            }
        }

        return destinationNode == null ? null : buildResult(destinationNode);
    }

    // -----------------------------------------------------------------------
    // PATH RECONSTRUCTION
    // -----------------------------------------------------------------------

    private routeresult buildResult(PathNode destinationNode) {
        List<List<BusStop>> segments = new ArrayList<>();
        List<String> routeNames = new ArrayList<>();
        PathNode current = destinationNode;
        List<BusStop> currentSegment = new ArrayList<>();
        String currentRoute = destinationNode.route;

        while (current != null) {
            currentSegment.add(current.stop);
            boolean isTransfer = current.previous != null
                    && !current.previous.route.equals(currentRoute);
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
                totalDistance += database.calculateDistance(
                        seg.get(i).lat, seg.get(i).lon,
                        seg.get(i + 1).lat, seg.get(i + 1).lon);
            }
        }

        BusStop first = segments.get(0).get(0);
        BusStop last  = segments.get(segments.size() - 1).get(segments.get(segments.size() - 1).size() - 1);
        return new routeresult(segments, routeNames, totalDistance, List.of(first, last));
    }

    // -----------------------------------------------------------------------
    // HEURISTIC
    // -----------------------------------------------------------------------

    private double heuristic(BusStop from, BusStop to) {
        return database.calculateDistance(from.lat, from.lon, to.lat, to.lon);
    }

    // -----------------------------------------------------------------------
    // DISPLAY
    // -----------------------------------------------------------------------

    public void displayRoute(routeresult result) {
        currentRouteResult = result;
        List<BusStop> path = result.getFullPath();
        currentMap = dataManager.getOverviewMap();
        for (mapregion region : dataManager.getSortedMapRegions()) {
            boolean allContained = true;
            for (BusStop stop : path) {
                if (!region.contains(stop)) { allContained = false; break; }
            }
            if (allContained) { currentMap = region; break; }
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
        int mapX = (getWidth()  - currentMap.getImage().getWidth())  / 2;
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

        for (List<BusStop> segment : currentRouteResult.segments) {
            for (int i = 0; i < segment.size() - 1; i++) {
                BusStop from = segment.get(i);
                BusStop to   = segment.get(i + 1);
                if (currentMap.contains(from) && currentMap.contains(to)) {
                    g2d.drawLine(from.pixelCoords.x + mapX, from.pixelCoords.y + mapY,
                                 to.pixelCoords.x   + mapX, to.pixelCoords.y   + mapY);
                }
            }
        }

        List<BusStop> specialStops = new ArrayList<>(currentRouteResult.majorStops);
        for (int i = 0; i < currentRouteResult.segments.size() - 1; i++) {
            List<BusStop> seg = currentRouteResult.segments.get(i);
            if (seg != null && !seg.isEmpty()) {
                BusStop transfer = seg.get(seg.size() - 1);
                if (!specialStops.contains(transfer)) specialStops.add(transfer);
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
            int textWidth  = fm.stringWidth(stopName);
            int textHeight = fm.getHeight();
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillRect(x + 10, y - textHeight / 2, textWidth + 4, textHeight);
            g2d.setColor(Color.BLACK);
            g2d.drawString(stopName, x + 12, y + textHeight / 2 - fm.getDescent());
        }
    }

    // -----------------------------------------------------------------------
    // UTILITIES
    // -----------------------------------------------------------------------

    private static void reverseBusStopList(List<BusStop> list) {
        for (int i = 0, j = list.size() - 1; i < j; i++, j--) {
            BusStop tmp = list.get(i); list.set(i, list.get(j)); list.set(j, tmp);
        }
    }

    private static void reverseStringList(List<String> list) {
        for (int i = 0, j = list.size() - 1; i < j; i++, j--) {
            String tmp = list.get(i); list.set(i, list.get(j)); list.set(j, tmp);
        }
    }

    private static void reverseListOfLists(List<List<BusStop>> list) {
        for (int i = 0, j = list.size() - 1; i < j; i++, j--) {
            List<BusStop> tmp = list.get(i); list.set(i, list.get(j)); list.set(j, tmp);
        }
    }

    // -----------------------------------------------------------------------
    // PATH NODE
    // -----------------------------------------------------------------------

    private static class PathNode {
        BusStop stop;
        PathNode previous;
        String route;
        int transfers;
        double cost;

        PathNode(BusStop stop, PathNode previous, String route, int transfers, double cost) {
            this.stop      = stop;
            this.previous  = previous;
            this.route     = route;
            this.transfers = transfers;
            this.cost      = cost;
        }
    }
}
