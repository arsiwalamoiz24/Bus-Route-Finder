package busfinder.gui;

import busfinder.MainApp;
import busfinder.data.BusStop;
import busfinder.helpful.routeresult;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class routedetailspanel extends JPanel {

    // ── palette ─────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(18,  20,  35);   // deep navy
    private static final Color CARD_BG     = new Color(28,  32,  52);   // card surface
    private static final Color ACCENT      = new Color(255, 193,  38);   // amber
    private static final Color ACCENT2     = new Color(99, 179, 237);   // sky-blue (transfers)
    private static final Color TEXT_PRI    = new Color(240, 240, 255);
    private static final Color TEXT_SEC    = new Color(160, 165, 195);
    private static final Color SEP_LINE    = new Color(45,  50,  80);
    private static final Color BADGE_BUS   = new Color(41, 182, 100);   // green
    private static final Color BADGE_XFER  = new Color(245, 101,  101); // red-orange
    private static final Color BTN_BG      = new Color(99, 179, 237);
    private static final Color BTN_HOVER   = new Color(66, 153, 225);

    private static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  18);
    private static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  14);
    private static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    private static final Font FONT_BADGE   = new Font("Segoe UI", Font.BOLD,  11);

    private static final DecimalFormat DF = new DecimalFormat("#.##");

    private final JPanel contentArea;
    private final MainApp mainAppFrame;

    // ── construction ────────────────────────────────────────────────────────

    public routedetailspanel(MainApp mainAppFrame) {
        this.mainAppFrame = mainAppFrame;
        setLayout(new BorderLayout());
        setBackground(BG);
        setPreferredSize(new Dimension(380, 900));

        // ── header ──
        JPanel header = buildHeader();
        add(header, BorderLayout.NORTH);

        // ── scrollable content ──
        contentArea = new JPanel();
        contentArea.setLayout(new BoxLayout(contentArea, BoxLayout.Y_AXIS));
        contentArea.setBackground(BG);
        contentArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scroll = new JScrollPane(contentArea);
        scroll.setBorder(null);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // ── footer ──
        JPanel footer = buildFooter();
        add(footer, BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(22, 26, 48));
        p.setBorder(new EmptyBorder(14, 16, 14, 16));

        JLabel icon = new JLabel("🚌");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        p.add(icon, BorderLayout.WEST);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel t1 = new JLabel("Route Options");
        t1.setFont(FONT_TITLE);
        t1.setForeground(TEXT_PRI);

        JLabel t2 = new JLabel("Best routes for your journey");
        t2.setFont(FONT_SMALL);
        t2.setForeground(TEXT_SEC);

        titles.add(t1);
        titles.add(t2);
        p.add(titles, BorderLayout.CENTER);

        // divider
        JPanel divider = new JPanel();
        divider.setBackground(SEP_LINE);
        divider.setPreferredSize(new Dimension(0, 1));
        p.add(divider, BorderLayout.SOUTH);

        return p;
    }

    private JPanel buildFooter() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        p.setBackground(new Color(22, 26, 48));
        p.setBorder(new EmptyBorder(4, 12, 10, 12));

        JButton btn = new JButton("← Search Again");
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(BTN_BG);
        btn.setBorder(new EmptyBorder(10, 30, 10, 30));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(BTN_HOVER); }
            @Override public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(BTN_BG); }
        });

        btn.addActionListener(e -> {
            mainAppFrame.setVisible(false);
            mainAppFrame.homeFrame.setVisible(true);
        });
        p.add(btn);
        return p;
    }

    // ── public API ──────────────────────────────────────────────────────────

    public void showLoading() {
        contentArea.removeAll();

        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel spinner = new JLabel("⏳  Calculating routes…");
        spinner.setFont(FONT_HEADING);
        spinner.setForeground(ACCENT);
        spinner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sub = new JLabel("Finding best options for you.");
        sub.setFont(FONT_BODY);
        sub.setForeground(TEXT_SEC);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(spinner);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(sub);

        contentArea.add(card);
        contentArea.revalidate();
        contentArea.repaint();
    }

    /**
     * Renders all route options. Replaces the old updateBusJourneyInfo().
     * Pass null or empty list to show a "not found" state.
     */
    public void showRouteOptions(List<routeresult> results) {
        contentArea.removeAll();

        if (results == null || results.isEmpty()) {
            showNoRoute();
        } else {
            for (int i = 0; i < results.size(); i++) {
                contentArea.add(buildRouteCard(results.get(i), i + 1, results.size()));
                contentArea.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        contentArea.revalidate();
        contentArea.repaint();
    }

    /** Legacy alias so existing callers still compile. */
    public void updateBusJourneyInfo(routeresult result) {
        showRouteOptions(result == null ? null : List.of(result));
    }

    // ── card builders ────────────────────────────────────────────────────────

    private void showNoRoute() {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel icon = new JLabel("🚫");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        icon.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel msg = new JLabel("No route found");
        msg.setFont(FONT_HEADING);
        msg.setForeground(new Color(245, 101, 101));
        msg.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel hint = new JLabel("<html><body style='width:260px;color:#A0A5C3;font-size:12px'>"
                + "These stops may not be connected by any direct or single-transfer bus service. "
                + "Try selecting stops closer together, or check for nearby alternative stops.</body></html>");
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(icon);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(msg);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(hint);
        contentArea.add(card);
    }

    /** Builds one option card (Option 1 / 2 / 3) */
    private JPanel buildRouteCard(routeresult result, int optionNumber, int totalOptions) {
        JPanel card = makeCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // ── card title row ──
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        String optLabel = totalOptions > 1 ? "Option " + optionNumber : "Best Route";
        JLabel optTitle = new JLabel(optLabel);
        optTitle.setFont(FONT_HEADING);
        optTitle.setForeground(optionNumber == 1 ? ACCENT : TEXT_SEC);
        titleRow.add(optTitle, BorderLayout.WEST);

        // summary badges (distance + transfers)
        JPanel badges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badges.setOpaque(false);
        badges.add(makeBadge(DF.format(result.totalDistance) + " km", new Color(60, 70, 100), TEXT_PRI));
        int xfers = result.segments.size() - 1;
        String xferText = xfers == 0 ? "Direct" : xfers + " transfer" + (xfers > 1 ? "s" : "");
        badges.add(makeBadge(xferText, xfers == 0 ? BADGE_BUS : BADGE_XFER, Color.WHITE));
        titleRow.add(badges, BorderLayout.EAST);

        card.add(titleRow);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(makeSeparator());
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // ── segments ──
        for (int i = 0; i < result.segments.size(); i++) {
            List<BusStop> seg = result.segments.get(i);
            String routeName = result.routeNames.get(i);
            boolean isLast = (i == result.segments.size() - 1);

            card.add(buildSegmentBlock(seg, routeName, i + 1, isLast));

            if (!isLast) {
                card.add(Box.createRigidArea(new Dimension(0, 8)));
                card.add(buildTransferRow(seg.get(seg.size() - 1).getName()));
                card.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        return card;
    }

    /** One "Leg" block: route badge + FROM → TO + intermediate stops (collapsed) */
    private JPanel buildSegmentBlock(List<BusStop> seg, String routeName, int legNum, boolean isLast) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // ── leg header: bus number badge + stop count ──
        JPanel legHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        legHeader.setOpaque(false);
        legHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel legBadge = makeBadge("BUS " + routeName, BADGE_BUS, Color.WHITE);
        legHeader.add(legBadge);

        int stops = seg.size();
        JLabel stopCount = new JLabel(stops + " stop" + (stops != 1 ? "s" : ""));
        stopCount.setFont(FONT_SMALL);
        stopCount.setForeground(TEXT_SEC);
        legHeader.add(stopCount);

        p.add(legHeader);
        p.add(Box.createRigidArea(new Dimension(0, 6)));

        // ── stop list: timeline style ──
        BusStop firstStop = seg.get(0);
        BusStop lastStop  = seg.get(seg.size() - 1);

        // Boarding stop
        p.add(makeStopRow(firstStop.getName(), StopType.BOARD));

        // Intermediate stops (only show count, not all names to keep it clean)
        if (seg.size() > 2) {
            int intermediate = seg.size() - 2;
            JLabel midLabel = new JLabel("  │  " + intermediate + " intermediate stop" + (intermediate > 1 ? "s" : ""));
            midLabel.setFont(FONT_SMALL);
            midLabel.setForeground(TEXT_SEC);
            midLabel.setBorder(new EmptyBorder(2, 0, 2, 0));
            midLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(midLabel);
        }

        // Alighting stop
        p.add(makeStopRow(lastStop.getName(), isLast ? StopType.DESTINATION : StopType.TRANSFER));

        return p;
    }

    private enum StopType { BOARD, TRANSFER, DESTINATION }

    private JPanel makeStopRow(String name, StopType type) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        // coloured dot
        Color dotColor = switch (type) {
            case BOARD       -> BADGE_BUS;
            case TRANSFER    -> BADGE_XFER;
            case DESTINATION -> ACCENT;
        };

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("Segoe UI", Font.BOLD, 10));
        dot.setForeground(dotColor);
        row.add(dot);

        // stop name — trim display name if it has a disambiguation suffix like " (Area) #1"
        String display = name;
        int hashIdx = display.lastIndexOf(" #");
        if (hashIdx > 0) display = display.substring(0, hashIdx).trim();
        // also strip trailing parenthetical area suffix for cleaner display
        if (display.endsWith(")")) {
            int parenIdx = display.lastIndexOf(" (");
            if (parenIdx > 0) display = display.substring(0, parenIdx).trim();
        }

        JLabel label = new JLabel(display);
        label.setFont(FONT_BODY);
        label.setForeground(type == StopType.DESTINATION ? ACCENT :
                            type == StopType.TRANSFER    ? new Color(245, 101, 101) : TEXT_PRI);
        row.add(label);

        if (type == StopType.TRANSFER) {
            JLabel tag = makeBadge("TRANSFER", new Color(80, 30, 30), new Color(245, 101, 101));
            row.add(tag);
        } else if (type == StopType.DESTINATION) {
            JLabel tag = makeBadge("DESTINATION", new Color(80, 65, 10), ACCENT);
            row.add(tag);
        }

        return row;
    }

    private Component buildTransferRow(String stopName) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));

        JLabel icon = new JLabel("⇄");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        icon.setForeground(ACCENT2);
        row.add(icon);

        JLabel lbl = new JLabel("Change bus at " + trimStopName(stopName));
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(ACCENT2);
        row.add(lbl);

        return row;
    }

    // ── helper widgets ───────────────────────────────────────────────────────

    private JPanel makeCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(14, 14, 14, 14));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        return card;
    }

    private JLabel makeBadge(String text, Color bg, Color fg) {
        JLabel badge = new JLabel(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        badge.setFont(FONT_BADGE);
        badge.setForeground(fg);
        badge.setOpaque(false);
        badge.setBorder(new EmptyBorder(2, 7, 2, 7));
        return badge;
    }

    private JSeparator makeSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(SEP_LINE);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        return sep;
    }

    private String trimStopName(String name) {
        String s = name;
        int hashIdx = s.lastIndexOf(" #");
        if (hashIdx > 0) s = s.substring(0, hashIdx).trim();
        if (s.endsWith(")")) {
            int pIdx = s.lastIndexOf(" (");
            if (pIdx > 0) s = s.substring(0, pIdx).trim();
        }
        return s;
    }
}