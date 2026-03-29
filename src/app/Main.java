package app;

import ui.MainFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Application entry point.
 * Applies the Nimbus look-and-feel with a custom blue colour scheme,
 * then launches the main window on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // Apply modern Nimbus L&F with custom colours before any UI is created
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("nimbusBase",            new Color(37,  99,  235)); // primary blue
            UIManager.put("nimbusBlueGrey",        new Color(100, 116, 139)); // slate
            UIManager.put("control",               new Color(248, 250, 252)); // page background
            UIManager.put("nimbusLightBackground", Color.WHITE);
            UIManager.put("text",                  new Color(30,  41,  59));  // dark text
            UIManager.put("Table.alternateRowColor", new Color(241, 245, 249));
            UIManager.put("defaultFont",           new Font("Segoe UI", Font.PLAIN, 13));
        } catch (Exception e) {
            System.err.println("Could not apply Nimbus L&F: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
