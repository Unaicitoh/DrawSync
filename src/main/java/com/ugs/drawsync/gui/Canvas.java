package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Canvas extends JPanel {

    public static final int CANVAS_SIZE = 1000;
    private List<Shape> shapes;
    private Color color;
    private int stroke = 5;
    private int mouseX;
    private int mouseY;
    private boolean doInit;
    private Mode mode;
    private Mode lastMode;
    private BufferedImage image;
    private boolean canInteract;
    private Cursor eraserCursor;

    public Canvas() {
        //TODO ZOOM, BUTTONS
        shapes = new ArrayList<>();
        color = Color.BLACK;
        mode = Mode.DRAWER;
        doInit = true;
        image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        setDoubleBuffered(true);
        eraserCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("src/main/resources/images/eraserIcon.png").getImage(), new Point(10, 30), "EraserCursor");
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setBackground(Color.WHITE);
        addEventListeners();

    }


    private void drawShape(Color color, int x, int y, int w, int h) {
        Graphics2D g = setupGraphics();
        Shape rect = new Shape(color, x, y, w, h);
//        shapes.add(rect);
        g.setColor(color);
        g.fillArc(x, y, w, h, 0, 360);
        repaint(x, y, w, h);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (doInit) {
            Point p = getLocation();
            setLocation(p.x, p.y + getParent().getSize().height / 2 - getSize().height / 2);
            doInit = !doInit;
        }
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (image != null) {
            g.drawImage(image, 0, 0, null);
        }
//        shapes.forEach(r -> {
//            g2d.setColor(r.getColor());
//            g2d.fillRect(r.x,r.y,r.width,r.height);
//        });
    }

    private void addEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == 3) {
                    lastMode = mode;
                    mode = Mode.MOVING;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (e.getButton() == 1 && canInteract) {
                    switch (mode) {
                        case DRAWER -> drawShape(color, e.getX() - stroke / 2, e.getY() - stroke / 2, stroke, stroke);
                        case ERASER -> drawShape(Color.WHITE, e.getX() - stroke / 2, e.getY() - stroke / 2, stroke, stroke);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mousePressed(e);
                if (e.getButton() == 3) {
                    switch (lastMode) {
                        case DRAWER -> {
                            setMode(lastMode);
                            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                        }
                        case ERASER -> {
                            setMode(lastMode);
                            setCursor(eraserCursor);
                        }
                    }
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mousePressed(e);
                canInteract = true;
                switch (mode) {
                    case DRAWER -> setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                    case ERASER -> setCursor(eraserCursor);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                super.mousePressed(e);
                canInteract = false;
                if (mode != Mode.MOVING) {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                switch (mode) {
                    case DRAWER -> {
                        if (canInteract) drawShape(color, e.getX() - stroke / 2, e.getY() - stroke / 2, stroke, stroke);
                    }
                    case ERASER -> {
                        if (canInteract)
                            drawShape(Color.WHITE, e.getX() - stroke / 2, e.getY() - stroke / 2, stroke, stroke);
                    }
                    case MOVING -> {
                        int diffX = e.getX() - mouseX;
                        int diffY = e.getY() - mouseY;
                        mouseX = e.getX() - diffX;
                        mouseY = e.getY() - diffY;
                        Point p = getLocation();
                        setLocation(p.x + diffX, p.y + diffY);
                    }
                }
            }
        });
    }

    public void clear() {
        image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        //shapes.clear();
        repaint();
    }

    private Graphics2D setupGraphics() {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return g2d;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(CANVAS_SIZE, CANVAS_SIZE);
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

    public int getMouseX() {
        return mouseX;
    }

    public void setMouseX(int mouseX) {
        this.mouseX = mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public void setMouseY(int mouseY) {
        this.mouseY = mouseY;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() {
        return mode;
    }

    public Mode getLastMode() {
        return lastMode;
    }

    public void setLastMode(Mode lastMode) {
        this.lastMode = lastMode;
    }

    public void doInit(boolean doInit) {
        this.doInit = doInit;
    }

    public boolean doInit() {
        return doInit;
    }
}
