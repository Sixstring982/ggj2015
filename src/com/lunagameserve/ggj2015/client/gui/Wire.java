package com.lunagameserve.ggj2015.client.gui;

import com.lunagameserve.ggj2015.client.gui.assets.Asset;

import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Created by six on 1/24/15.
 */
public class Wire {
    private Rectangle2D hitRect;
    private final Asset normalAsset;
    private final Asset cutAsset;

    private boolean visible = true;
    private boolean cut = false;

    public Wire(Asset normalAsset, Asset cutAsset, Rectangle2D hitRect) {
        this.hitRect = hitRect;
        this.normalAsset = normalAsset;
        this.cutAsset = cutAsset;
    }

    public void cut() {
        this.cut = true;
    }

    public void reset() {
        this.cut = false;
    }

    public void setVisible(boolean value) {
        this.visible = value;
    }

    public boolean isCut() {
        return cut;
    }

    public boolean contains(int x, int y) {
        return hitRect.contains(x, y);
    }

    public void render(Graphics2D g) {
        if (visible) {
            Asset toRender = normalAsset;
            if (isCut()) {
                toRender = cutAsset;
            }
            toRender.render(g, (int) hitRect.getX(), (int) hitRect.getY());
        }
    }
}
