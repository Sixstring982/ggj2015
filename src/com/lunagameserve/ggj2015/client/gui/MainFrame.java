package com.lunagameserve.ggj2015.client.gui;

import com.lunagameserve.ggj2015.client.Arguments;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Created by six on 1/24/15.
 */
public class MainFrame extends JFrame {

    private MainPanel panel;

    public MainFrame(Arguments args) {
        SwingUtilities.invokeLater(this::setup);
    }

    private void setup() {
        this.panel = new MainPanel();
        this.add(panel);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        registerListeners(panel);
    }

    private void registerListeners(final MainPanel panel) {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                repaint();
            }
        });

        this.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                panel.click(e.getX(), e.getY());
                repaint();
            }
        });
    }
}
