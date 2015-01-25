package com.lunagameserve.ggj2015.client.gui.assets;

import com.lunagameserve.ggj2015.bombServer.Log;
import com.lunagameserve.ggj2015.client.gui.MainPanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * Created by six on 1/24/15.
 */
public enum Asset {
    Background() {
        private final int CELL_SIZE = 4;
        private final Color DARK_GRAY = new Color(0xff101010);

        @Override
        public void render(Graphics2D g, int x, int y) {
            for (int xx = 0; xx < MainPanel.SCREEN_WIDTH; xx++) {
                for (int yy = 0; yy < MainPanel.SCREEN_HEIGHT; yy++) {
                    boolean xMod = xx % (CELL_SIZE * 2) > CELL_SIZE;
                    boolean yMod = yy % (CELL_SIZE * 2) > CELL_SIZE;
                    if (xMod ^ yMod) {
                        g.setColor(Color.black);
                    } else {
                        g.setColor(DARK_GRAY);
                    }
                    g.fillRect(x + xx, y + yy, 5, 5);
                }
            }
        }
    },
    Bomb(),
    Wire1(),
    Wire1_cut(),
    Wire2,
    Wire2_cut,
    Wire3,
    Wire3_cut,
    Wire4,
    Wire4_cut,
    Wire5,
    Wire5_cut,
    Wire6,
    Wire6_cut,
    Wire7,
    Wire7_cut,
    Wire8,
    Wire8_cut,
    Wire9,
    Wire9_cut,
    Wire10,
    Wire10_cut;

    public void render(Graphics2D g, int x, int y) {
        g.drawImage(get(), x, y, null);
    }

    private BufferedImage cache = null;

    private BufferedImage get() {
        if (cache == null) {
            try {
                Log.debug("Attempting to load " + assetPath() + "...");
                cache = ImageIO.read(Asset.class.getResourceAsStream(assetPath()));
            } catch (IOException e) {
                System.err.println("Could not read asset " + toString() + ". Quitting.");
                System.exit(4);
            }
        }
        return cache;
    }

    private String assetPath() {
        return toString().toLowerCase() + ".png";
    }
}
