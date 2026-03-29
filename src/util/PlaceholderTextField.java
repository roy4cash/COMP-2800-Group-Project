package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * A JTextField subclass that displays gray placeholder text when the field
 * is empty and not focused. Once the user clicks into the field or types,
 * the placeholder disappears and normal black text is shown.
 *
 * Usage:
 *   PlaceholderTextField field = new PlaceholderTextField("e.g. Morning coffee");
 *   panel.add(field);
 */
public class PlaceholderTextField extends JTextField {

    /** The hint text shown when the field is empty and unfocused. */
    private final String placeholder;

    /** Color used to render the placeholder text (light gray). */
    private final Color placeholderColor = new Color(156, 163, 175);

    /**
     * Creates a new PlaceholderTextField.
     *
     * @param placeholder  the hint text displayed when the field is empty and unfocused
     */
    public PlaceholderTextField(String placeholder) {
        this.placeholder = placeholder;

        // Repaint when focus changes so the placeholder appears/disappears instantly
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                repaint();
            }
        });
    }

    /**
     * Overrides paintComponent to draw the placeholder text on top of
     * the standard empty-field background when there is no user input.
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Always let Swing paint the normal field background/border/text first
        super.paintComponent(g);

        // Only draw placeholder when field is empty and not focused
        if (getText().isEmpty() && !isFocusOwner()) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(placeholderColor);
            g2.setFont(getFont());

            // Position text to align with where the user's real text would appear
            Insets ins = getInsets();
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(placeholder, ins.left + 2, y);
            g2.dispose();
        }
    }
}
