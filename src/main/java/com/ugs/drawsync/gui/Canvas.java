package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {

    private List<Shape> shapes;
    private int oldWidth;
    private int oldHeight;
    private Color color;
    private int stroke = 5;

    public Canvas() {
        shapes = new ArrayList<>();
        color = Color.BLACK;
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 3, true));
        setBackground(Color.WHITE);
        setPreferredSize(getPreferredSize());
        setFont(new Font("Ink Free", Font.BOLD, 64));
        setForeground(new Color(.8f, .8f, .8f, .5f));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                oldWidth = getWidth();
                oldHeight = getHeight();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                drawShape(color, e.getX(), e.getY(), stroke, stroke);
            }

        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                drawShape(color, e.getX(), e.getY(), stroke, stroke);
            }
        });
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(600, 400);
    }

    private void drawShape(Color color, int x, int y, int w, int h) {
        Graphics g = setupGraphics();
        Shape rect = new Shape(color, x, y, w, h);
        shapes.add(rect);
        g.setColor(color);
        g.fillArc(x - w / 2, y - h / 2, w, h, 0, 360);
    }

    private Graphics2D setupGraphics() {
        Graphics g = getGraphics();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return g2d;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawString("CANVAS", this.getX() + this.getWidth() / 2 - 32 * 7, this.getY() + this.getHeight() / 2 - 32 * 2);

        shapes.forEach(r -> {
            int newX = 0;
            int newY = 0;
            if (getWidth() < oldWidth) {
                newX = (int) Math.floor(r.getX() * getWidth() / oldWidth);
            } else {
                newX = (int) Math.ceil(r.getX() * getWidth() / oldWidth);
            }
            if (getHeight() < oldHeight) {
                newY = (int) Math.floor(r.getY() * getHeight() / oldHeight);
            } else {
                newY = (int) Math.ceil(r.getY() * getHeight() / oldHeight);
            }
            r.setLocation(newX, newY);
            g2d.setColor(r.getColor());
            g2d.fill(r);
        });
    }

    public void clear() {
        shapes.clear();
        repaint();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void setStroke(int stroke) {
        this.stroke = stroke;
    }
}
