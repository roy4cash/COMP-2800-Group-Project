/**
 * File: UITheme.java
 * Purpose: Centralizes colors, fonts, and reusable Swing component styling.
 *
 * This file acts as the project's small design system so visual changes can be
 * made consistently without editing every panel separately.
 */
package util;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;

/**
 * UITheme — central design system for the FAT application.
 *
 * All colours, fonts, and component factory methods live here so that
 * every panel stays visually consistent without duplicating constants.
 *
 * Usage:
 *   JButton btn = UITheme.button("Save", UITheme.PRIMARY);
 *   JPanel  card = UITheme.card("Add Expense");
 */
public final class UITheme {

    private UITheme() {} // utility class — no instances

    // ----------------------------------------------------------------
    // Colour palette
    // ----------------------------------------------------------------

    public static final Color PRIMARY        = new Color(37,  99,  235);
    public static final Color PRIMARY_DARK   = new Color(29,  78,  216);
    public static final Color SUCCESS        = new Color(16,  185, 129);
    public static final Color SUCCESS_DARK   = new Color(5,   150, 105);
    public static final Color DANGER         = new Color(239, 68,  68);
    public static final Color DANGER_DARK    = new Color(220, 38,  38);
    public static final Color WARNING        = new Color(245, 158, 11);
    public static final Color PURPLE         = new Color(139, 92,  246);
    public static final Color SLATE          = new Color(100, 116, 139);

    /** Page background — very light grey */
    public static final Color BG             = new Color(248, 250, 252);
    /** Card / panel background — white */
    public static final Color CARD_BG        = Color.WHITE;
    /** Card title bar background — very light blue */
    public static final Color CARD_TITLE_BG  = new Color(239, 246, 255);
    /** Border colour for cards and fields */
    public static final Color BORDER         = new Color(226, 232, 240);
    /** Muted label text */
    public static final Color LABEL          = new Color(100, 116, 139);
    /** Primary body text */
    public static final Color TEXT           = new Color(30,  41,  59);
    /** Table header background */
    public static final Color HEADER_BG      = new Color(30,  41,  59);
    /** Table alternating row */
    public static final Color ROW_ALT        = new Color(241, 245, 249);
    /** Table selected row */
    public static final Color SEL_BG         = new Color(219, 234, 254);

    // ----------------------------------------------------------------
    // Typography
    // ----------------------------------------------------------------

    public static final Font FONT_PAGE_TITLE = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_PAGE_SUB   = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_CARD_TITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_LABEL      = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font FONT_BODY       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_STAT_VALUE = new Font("Segoe UI", Font.BOLD,  24);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD,  12);
    public static final Font FONT_TABLE_CELL = new Font("Segoe UI", Font.PLAIN, 13);

    // ----------------------------------------------------------------
    // Factory: button with hover effect
    // ----------------------------------------------------------------

    /**
     * Creates a styled button with a smooth hover colour change.
     *
     * @param text  button label
     * @param bg    normal background colour
     */
    public static JButton button(String text, Color bg) {
        Color hover = bg.darker();
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 40));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override public void mouseExited (MouseEvent e) { btn.setBackground(bg);    }
        });
        return btn;
    }

    // ----------------------------------------------------------------
    // Factory: card panel with title bar
    // ----------------------------------------------------------------

    /**
     * Creates a white card panel with a light-blue title bar at the top.
     * The CENTER of the card's BorderLayout is left empty for the caller to fill.
     */
    public static JPanel card(String title) {
        JLabel titleLabel = new JLabel("  " + title);
        titleLabel.setFont(FONT_CARD_TITLE);
        titleLabel.setForeground(TEXT);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(CARD_TITLE_BG);
        titleLabel.setBorder(new EmptyBorder(12, 16, 12, 16));
        titleLabel.setPreferredSize(new Dimension(0, 46));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new LineBorder(BORDER, 1, true));
        card.add(titleLabel, BorderLayout.NORTH);
        return card;
    }

    // ----------------------------------------------------------------
    // Factory: stat card (used in Dashboard, Insights, Investments)
    // ----------------------------------------------------------------

    /**
     * Creates a compact stat card with a coloured left-border accent.
     *
     * @param topLabel    small caps label above the value
     * @param valueLabel  large JLabel — caller should update its text each refresh
     * @param subtext     small help text below the value (pass "" to hide)
     * @param accent      left-border colour
     */
    public static JPanel statCard(String topLabel, JLabel valueLabel,
                                  String subtext, Color accent) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
            BorderFactory.createCompoundBorder(
                new LineBorder(BORDER, 1, false),
                new EmptyBorder(14, 16, 14, 16))));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx   = 0;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Small-caps label at top
        JLabel lbl = new JLabel(topLabel.toUpperCase());
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(LABEL);
        gbc.gridy = 0;
        card.add(lbl, gbc);

        // Large value in the middle
        valueLabel.setFont(FONT_STAT_VALUE);
        valueLabel.setForeground(TEXT);
        gbc.gridy = 1;
        gbc.insets = new Insets(6, 0, 4, 0);
        card.add(valueLabel, gbc);

        // Optional subtext at bottom
        if (subtext != null && !subtext.isEmpty()) {
            JLabel sub = new JLabel(subtext);
            sub.setFont(FONT_SMALL);
            sub.setForeground(LABEL);
            gbc.gridy  = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            card.add(sub, gbc);
        }

        return card;
    }

    // ----------------------------------------------------------------
    // Factory: text field
    // ----------------------------------------------------------------

    /** Creates a consistently styled text field. */
    public static JTextField field() {
        JTextField f = new JTextField();
        applyFieldStyle(f);
        return f;
    }

    /** Applies standard field styling to an existing JTextField (or subclass). */
    public static void applyFieldStyle(JTextField f) {
        f.setFont(FONT_BODY);
        f.setForeground(TEXT);
        f.setBackground(new Color(248, 250, 252));
        f.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        f.setPreferredSize(new Dimension(0, 38));
    }

    // ----------------------------------------------------------------
    // Factory: form label
    // ----------------------------------------------------------------

    /** Creates a muted bold label suitable for form field captions. */
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(LABEL);
        return l;
    }

    // ----------------------------------------------------------------
    // Table helpers
    // ----------------------------------------------------------------

    /**
     * Applies a dark-header, alternating-row style to a JTable.
     * Call this once after constructing the table.
     */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE_CELL);
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(SEL_BG);
        table.setSelectionForeground(TEXT);

        javax.swing.table.JTableHeader header = table.getTableHeader();
        header.setFont(FONT_TABLE_HDR);
        header.setBackground(HEADER_BG);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 40));
        header.setReorderingAllowed(false);
    }

    /**
     * Standard alternating-row renderer.
     * Apply with: table.setDefaultRenderer(Object.class, UITheme.alternatingRowRenderer());
     */
    public static javax.swing.table.DefaultTableCellRenderer alternatingRowRenderer() {
        return new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD_BG : ROW_ALT);
                    setForeground(TEXT);
                } else {
                    setBackground(SEL_BG);
                    setForeground(TEXT);
                }
                return this;
            }
        };
    }

    // ----------------------------------------------------------------
    // Misc helpers
    // ----------------------------------------------------------------

    /** Creates a page-title JLabel. */
    public static JLabel pageTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_PAGE_TITLE);
        l.setForeground(TEXT);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Creates a muted subtitle label for page headers and helper text. */
    public static JLabel pageSubtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_PAGE_SUB);
        l.setForeground(LABEL);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Applies the standard rounded field look to combo boxes. */
    public static void styleComboBox(JComboBox<?> combo) {
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT);
        combo.setBackground(new Color(248, 250, 252));
        combo.setBorder(new LineBorder(BORDER, 1, true));
        combo.setFocusable(false);
    }

    /** Standard scroll pane styling used across the app. */
    public static void styleScrollPane(JScrollPane scroll) {
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getHorizontalScrollBar().setUnitIncrement(16);
        scroll.getVerticalScrollBar().setOpaque(false);
        scroll.getHorizontalScrollBar().setOpaque(false);
        scroll.getVerticalScrollBar().setUI(new AppScrollBarUI());
        scroll.getHorizontalScrollBar().setUI(new AppScrollBarUI());
    }

    /** Rigid vertical spacer (for BoxLayout). */
    public static Component vGap(int px) {
        return Box.createVerticalStrut(px);
    }

    private static final class AppScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void configureScrollBarColors() {
            thumbColor = new Color(191, 219, 254);
            trackColor = BG;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
        }
    }
}
