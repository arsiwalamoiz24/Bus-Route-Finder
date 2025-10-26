package busfinder;

import busfinder.data.BusStop;
import busfinder.data.database;
import busfinder.gui.mappanel;
import busfinder.gui.routedetailspanel;
import busfinder.helpful.routeresult;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

public class MainApp extends JFrame {

    public home homeFrame;
    JSplitPane splitPane;
    ImageIcon bus = new ImageIcon("bus icon.png");
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

        routeresult result = mapPanel.findPathWithTransfers(start, end);
        if (result != null) {
            mapPanel.displayRoute(result);
            detailsPanel.updateBusJourneyInfo(result);
        } else {
            detailsPanel.updateBusJourneyInfo(null);
        }
    }
}