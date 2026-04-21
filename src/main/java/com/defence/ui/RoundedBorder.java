package com.defence.ui;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * RoundedBorder — Custom rounded-corner border for JButtons.
 * Used throughout the dark military theme.
 */
public class RoundedBorder extends AbstractBorder {

    private final int    radius;
    private final Color  borderColor;

    public RoundedBorder(int radius) {
        this.radius      = radius;
        this.borderColor = new Color(0x4A, 0x8C, 0x4A); // hover green
    }

    public RoundedBorder(int radius, Color color) {
        this.radius      = radius;
        this.borderColor = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y,
                            int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(borderColor);
        g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component c) {
        int pad = radius / 2 + 2;
        return new Insets(pad, pad, pad, pad);
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        int pad = radius / 2 + 2;
        insets.left = insets.right = insets.top = insets.bottom = pad;
        return insets;
    }
}
