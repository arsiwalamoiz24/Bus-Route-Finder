package busfinder.gui;

import busfinder.MainApp;
import busfinder.data.BusStop;
import busfinder.helpful.routeresult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;

public class routedetailspanel extends JPanel {

    private final JPanel detailsPanel;
    private final DecimalFormat df = new DecimalFormat("#.##");
    private JButton backButton;
    private MainApp mainAppFrame;

    public routedetailspanel(MainApp mainAppFrame) {
        this.mainAppFrame = mainAppFrame;
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(255, 204, 51));

        ImageIcon originalMovingBus = new ImageIcon("animated bus.gif");
        Image originalImage = originalMovingBus.getImage();
        Image scaledImage = originalImage.getScaledInstance(80, 80, Image.SCALE_DEFAULT);
        ImageIcon movingbus = new ImageIcon(scaledImage);

        JLabel title = new JLabel("BUS ROUTE DETAILS");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(Color.DARK_GRAY);
        title.setOpaque(true);
        title.setBackground(new Color(255, 204, 51));
        title.setIcon(movingbus);
        title.setPreferredSize(new Dimension(350, 80));
        title.setHorizontalTextPosition(JLabel.RIGHT);
        title.setVerticalTextPosition(JLabel.CENTER);
        title.setHorizontalAlignment(JLabel.CENTER);
        title.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(new Color(230, 230, 160));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(new Color(40, 40, 90));
        backButton = new JButton("Find another Route");
        backButton.setFont(new Font("Times new Roman", Font.BOLD, 22));
        backButton.setFocusable(false);
        backButton.addActionListener(e -> {
            mainAppFrame.setVisible(false);
            mainAppFrame.homeFrame.setVisible(true);
        });
        buttonPanel.add(backButton);
        this.add(buttonPanel, BorderLayout.SOUTH);

        this.setPreferredSize(new Dimension(350, 900));
    }

    public void updateBusJourneyInfo(routeresult result) {
        detailsPanel.removeAll();
        if (result == null) {
            JLabel noRouteLabel = new JLabel("No route could be found.");
            noRouteLabel.setFont(new Font("Times new Roman", Font.BOLD, 28));
            noRouteLabel.setForeground(Color.RED);
            noRouteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsPanel.add(noRouteLabel);
        } else {
            JLabel totalDistanceLabel = new JLabel("Total Distance: " + df.format(result.totalDistance) + " km");
            totalDistanceLabel.setFont(new Font("Times new Roman", Font.BOLD, 20));
            totalDistanceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsPanel.add(totalDistanceLabel);

            JLabel transfersLabel = new JLabel("Transfers: " + (result.segments.size() - 1));
            transfersLabel.setFont(new Font("Times new Roman", Font.BOLD, 20));
            transfersLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            detailsPanel.add(transfersLabel);

            detailsPanel.add(Box.createRigidArea(new Dimension(0, 15)));

            for (int i = 0; i < result.segments.size(); i++) {
                JLabel legLabel = new JLabel("LEG " + (i + 1) + " | BUS: " + result.routeNames.get(i));
                legLabel.setFont(new Font("Times new Roman", Font.BOLD, 20));
                legLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                detailsPanel.add(legLabel);
                detailsPanel.add(Box.createRigidArea(new Dimension(0, 5)));

                List<BusStop> segment = result.segments.get(i);
                for (int j = 0; j < segment.size(); j++) {
                    BusStop stop = segment.get(j);
                    boolean isTransferStop = (j == segment.size() - 1 && i < result.segments.size() - 1);

                    JLabel stopLabel = new JLabel("- " + stop.getName() + (isTransferStop ? " (TRANSFER)" : ""));
                    stopLabel.setFont(new Font("Times new Roman", Font.PLAIN, 21));
                    if (isTransferStop) {
                        stopLabel.setForeground(Color.red);
                    }
                    stopLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
                    detailsPanel.add(stopLabel);
                }
            }
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }
}