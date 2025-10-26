package busfinder;

import busfinder.data.BusStop;
import busfinder.data.database;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class home extends JFrame {

    ImageIcon bus = new ImageIcon("bus icon.png");
    ImageIcon background = new ImageIcon("background.png");
    JPanel toppanel;
    JLabel title, names, fromtext, totext;
    JPanel inputs, inputback, footer, from, from2, fromback, to, to2, toback;
    JButton button;
    JComboBox<BusStop> startCombo;
    JComboBox<BusStop> endCombo;
    database dataManager;

    public home() {
        this.setTitle("MUMBAI BUS ROUTE NAVIGATOR");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setIconImage(bus.getImage());
        this.setLayout(new BorderLayout());
        toppanel = new JPanel();
        toppanel.setLayout(null);
        toppanel.setSize(500, 800);
        toppanel.setOpaque(true);
        toppanel.setBackground(new Color(255, 204, 51));

        title = new JLabel("WHERE TO?");
        title.setBounds(250 - background.getIconWidth() / 2, 140 - background.getIconHeight() / 2, background.getIconWidth(), background.getIconHeight() + 200);
        title.setIcon(background);
        title.setHorizontalTextPosition(JLabel.CENTER);
        title.setVerticalTextPosition(JLabel.TOP);
        title.setIconTextGap(-135);
        title.setFont(new Font("Serif", Font.BOLD, 40));
        title.setForeground(Color.white);
        toppanel.add(title);

        inputs = new JPanel();
        inputs.setBounds(75, 315, 350, 300);
        inputs.setOpaque(true);
        inputs.setBackground(new Color(230, 230, 160));
        inputs.setLayout(null);
        toppanel.add(inputs);

        inputback = new JPanel();
        inputback.setBounds(82, 322, 350, 300);
        inputback.setOpaque(true);
        inputback.setBackground(new Color(190, 190, 130));
        toppanel.add(inputback);

        footer = new JPanel();
        footer.setPreferredSize(new Dimension(500, 60));
        footer.setBackground(new Color(40, 40, 90));
        footer.setOpaque(true);
        footer.setLayout(new BorderLayout());
        footer.setBorder(BorderFactory.createEtchedBorder());

        fromtext = new JLabel("From");
        fromtext.setBounds(30, 48, 80, 80);
        fromtext.setFont(new Font("Times New Roman", Font.BOLD, 22));
        inputs.add(fromtext);
        totext = new JLabel("To");
        totext.setBounds(30, 130, 80, 80);
        totext.setFont(new Font("Times New Roman", Font.BOLD, 22));
        inputs.add(totext);

        names = new JLabel("Developed by: Moiz Arsiwala & Aditya Anchan");
        names.setPreferredSize(new Dimension(300, 40));
        names.setForeground(Color.white);
        names.setFont(new Font("sans serif", Font.ITALIC, 18));
        names.setHorizontalAlignment(JLabel.CENTER);
        footer.add(names, BorderLayout.CENTER);

        from2 = new JPanel(new BorderLayout());
        from2.setBounds(25, 105, 300, 30);
        from2.setOpaque(true);
        from2.setBackground(new Color(255, 2, 55));
        inputs.add(from2);
        from = new JPanel(new BorderLayout());
        from.setBounds(25, 95, 300, 50);
        from.setOpaque(true);
        from.setBackground(new Color(255, 2, 55));
        inputs.add(from);
        fromback = new JPanel();
        fromback.setBounds(30, 100, 300, 50);
        fromback.setOpaque(true);
        fromback.setBackground(new Color(200, 200, 200));
        inputs.add(fromback);

        to2 = new JPanel(new BorderLayout());
        to2.setBounds(25, 187, 300, 30);
        to2.setOpaque(true);
        to2.setBackground(new Color(255, 255, 255));
        inputs.add(to2);
        to = new JPanel(new BorderLayout());
        to.setBounds(25, 178, 300, 50);
        to.setOpaque(true);
        to.setBackground(new Color(255, 255, 255));
        inputs.add(to);
        toback = new JPanel();
        toback.setBounds(30, 182, 300, 50);
        toback.setOpaque(true);
        toback.setBackground(new Color(200, 200, 200));
        inputs.add(toback);

        button = new JButton("Find Route");
        button.setBounds(100, 245, 150, 40);
        button.setFocusable(false);

        dataManager = new database();
        List<BusStop> stops = dataManager.getAllStops();
        List<BusStop> sortedStops = new ArrayList<>(stops);
        for (int i = 0; i < sortedStops.size() - 1; i++) {
            for (int j = 0; j < sortedStops.size() - i - 1; j++) {
                BusStop stop1 = sortedStops.get(j);
                BusStop stop2 = sortedStops.get(j + 1);
                if (stop1.getName().compareTo(stop2.getName()) > 0) {
                    BusStop temp = sortedStops.get(j);
                    sortedStops.set(j, sortedStops.get(j + 1));
                    sortedStops.set(j + 1, temp);
                }
            }
        }
        startCombo = new JComboBox<>(sortedStops.toArray(new BusStop[0]));
        endCombo = new JComboBox<>(sortedStops.toArray(new BusStop[0]));
        startCombo.setPreferredSize(new Dimension(300, 20));
        endCombo.setPreferredSize(new Dimension(300, 20));
        from2.add(startCombo, BorderLayout.CENTER);
        to2.add(endCombo, BorderLayout.CENTER);

        button.addActionListener(e -> {
            BusStop start = (BusStop) startCombo.getSelectedItem();
            BusStop end = (BusStop) endCombo.getSelectedItem();
            if (start.equals(end)) {
                JOptionPane.showMessageDialog(this, "Please Select different start and end stops", "Invalid Selection", JOptionPane.ERROR_MESSAGE);
            }
            if (start != null && end != null && !start.equals(end)) {
                this.setVisible(false);
                new MainApp(this, dataManager, start, end);
            }
        });

        inputs.add(button);
        this.add(footer, BorderLayout.SOUTH);
        this.add(toppanel);
        this.setSize(500, 800);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setResizable(false);
    }

    public static void main(String[] args) {
        new home();
    }
}