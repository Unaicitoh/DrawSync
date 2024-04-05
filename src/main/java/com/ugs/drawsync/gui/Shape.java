package com.ugs.drawsync.gui;

import java.awt.*;

public class Shape extends java.awt.Rectangle {
    private Color color;

    public Shape(Color color, int x, int y, int w, int h) {
        this.color = color;
        this.x = x;
        this.y = y;
        this.width = w;
        this.height = h;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
