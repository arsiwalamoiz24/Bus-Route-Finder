package busfinder;

import busfinder.data.BusStop;
import busfinder.data.database;
import busfinder.gui.mappanel;
import busfinder.gui.routedetailspanel;
import busfinder.helpful.routeresult;
import java.awt.BorderLayout;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class MainApp extends JFrame {

    public home homeFrame;
    JSplitPane splitPane;
    ImageIcon bus = new ImageIcon("assets/bus icon.png");

    public MainApp(home homeFrame, database dataManager, BusStop start, BusStop end) {
        this.homeFrame = homeFrame;

        this.setTitle("MUMBAI BUS ROUTE NAVIGATOR");
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(bus.getImage());

        routedetailspanel detailsPanel = new routedetailspanel(this);
        mappanel mapPanel = new mappanel(dataManager, detailsPanel);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setLeftComponent(new JScrollPane(mapPanel));
        splitPane.setRightComponent(detailsPanel);
        splitPane.setDividerLocation(950);

        this.add(splitPane, BorderLayout.CENTER);
        this.setSize(1400, 870);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        detailsPanel.showLoading();

        javax.swing.SwingWorker<List<routeresult>, Void> worker =
                new javax.swing.SwingWorker<List<routeresult>, Void>() {
            @Override
            protected List<routeresult> doInBackground() throws Exception {
                return mapPanel.findTopThreePaths(start, end);
            }

            @Override
            protected void done() {
                try {
                    List<routeresult> results = get();
                    if (results != null && !results.isEmpty()) {
                        // Show the best route on the map
                        mapPanel.displayRoute(results.get(0));
                        // Show all options in the details panel
                        detailsPanel.showRouteOptions(results);
                    } else {
                        detailsPanel.showRouteOptions(null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    detailsPanel.showRouteOptions(null);
                }
            }
        };
        worker.execute();
    }
}