package com.lunagameserve.ggj2015.client.gui;

import com.lunagameserve.ggj2015.client.gui.assets.Asset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by six on 1/24/15.
 */
public class MainPanel extends JPanel {

    public static final int SCREEN_WIDTH = 800;
    public static final int SCREEN_HEIGHT = 600;

    private List<Wire> wires = new ArrayList<>();

    public MainPanel() {
        this.setPreferredSize(new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));

        initWires();
    }

    private void initWires() {
        wires.add(new Wire(Asset.Wire1, Asset.Wire1_cut,
                           new Rectangle2D.Double(180, 0, 64, 150)));
    }

    @Override
    protected void paintComponent(Graphics crapG) {
        super.paintComponent(crapG);
        Graphics2D g = (Graphics2D)crapG;
        Asset.Background.render(g, 0, 0);
        Asset.Bomb.render(g, SCREEN_WIDTH / 4, SCREEN_HEIGHT / 4);
        for (Wire w : wires) {
            w.render(g);
        }
    }

    public void click(int x, int y) {
        wires.stream().filter(w -> w.contains(x, y)).forEach(Wire::cut);
    }
}
