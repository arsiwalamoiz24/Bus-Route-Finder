package busfinder.helpful;

import busfinder.data.BusStop;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class SearchableComboBox extends JComboBox<BusStop> {
    private final List<BusStop> items;

    public SearchableComboBox(List<BusStop> items) {
        super(items.toArray(new BusStop[0]));
        this.items = items;
        this.setEditable(true);

        JTextField textField = (JTextField) this.getEditor().getEditorComponent();
        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                SwingUtilities.invokeLater(() -> {
                    int key = e.getKeyCode();
                    if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_UP || key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_LEFT) {
                        return;
                    }

                    String text = textField.getText();
                    String lowerText = text.toLowerCase();
                    List<BusStop> filtered = new ArrayList<>();
                    for (BusStop stop : items) {
                        if (stop.getName().toLowerCase().contains(lowerText)) {
                            filtered.add(stop);
                        }
                    }

                    if (filtered.isEmpty()) {
                        hidePopup();
                    } else {
                        setModel(new DefaultComboBoxModel<>(filtered.toArray(new BusStop[0])));
                        setSelectedItem(text); // Keep what user typed
                        textField.setText(text);
                        showPopup();
                    }
                });
            }
        });
    }

    @Override
    public Object getSelectedItem() {
        Object item = super.getSelectedItem();
        if (item instanceof BusStop) {
            return item;
        }
        if (item instanceof String) {
            String text = (String) item;
            for (BusStop stop : items) {
                if (stop.getName().equalsIgnoreCase(text)) {
                    return stop;
                }
            }
            // Fallback: if they just typed something but didn't pick perfectly, return first match
            String lowerText = text.toLowerCase();
            for (BusStop stop : items) {
                if (stop.getName().toLowerCase().contains(lowerText)) {
                    return stop;
                }
            }
        }
        return null;
    }
}
