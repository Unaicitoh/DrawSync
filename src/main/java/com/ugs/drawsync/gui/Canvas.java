package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
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
    private boolean isInit;
    private Mode mode;
    private Mode lastMode;
    private BufferedImage image;
    private boolean canInteract;
    private final Cursor eraserCursor;
    private double zoom = 1f;
    private Point lastPost;
    private boolean isPressed;

    public Canvas() {
        //TODO ZOOM, BUTTONS
        shapes = new ArrayList<>();
        color = Color.BLACK;
        mode = Mode.DRAWER;
        image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        setDoubleBuffered(true);
        eraserCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("src/main/resources/images/eraserIcon.png").getImage(), new Point(10, 30), "EraserCursor");
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setBackground(Color.WHITE);
        addEventListeners();

    }

    private void drawShape(Color color, int x, int y, int w, int h) {
        Graphics2D g2d = setupImageGraphics();
        Shape rect = new Shape(color, x, y, w, h);
//        shapes.add(rect);
        g2d.setColor(color);
        g2d.fillArc(x, y, w, h, 0, 360);
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        super.paintComponent(g2d);
        System.out.println(zoom);
        if (doInit) {
            Point p;
            if (!isInit) {
                p = getLocation();
                setLocation(p.x, p.y + getParent().getSize().height / 2 - getSize().height / 2);
            }
            if (isInit) {
//              setLocation((int) (getParent().getWidth()/2f+getParent().getX()+getX()-finalX), (int) (getParent().getY()+getY()-finalY));
                setLocation(lastPost.x, lastPost.y);
            }
            if (!isInit) {
                isInit = !isInit;
            }
            doInit = !doInit;
        }
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        if (image != null) {
            AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);
            g2d.drawImage(image, at, null);
        }

    }

    private void addEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                super.mousePressed(e);
                if (e.getButton() == 3) {
                    lastMode = mode;
                    mode = Mode.MOVING;
                    mouseX = e.getX();
                    mouseY = e.getY();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (e.getButton() == 1 && canInteract) {
                    int x = (int) (e.getX() / zoom);
                    int y = (int) (e.getY() / zoom);
                    switch (mode) {
                        case DRAWER -> drawShape(color, x - stroke / 2, y - stroke / 2, stroke, stroke);
                        case ERASER -> drawShape(Color.WHITE, x - stroke / 2, y - stroke / 2, stroke, stroke);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
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
                int x = (int) (e.getX() / zoom);
                int y = (int) (e.getY() / zoom);
                switch (mode) {
                    case DRAWER -> {
                        if (canInteract) drawShape(color, x - stroke / 2, y - stroke / 2, stroke, stroke);
                    }
                    case ERASER -> {
                        if (canInteract)
                            drawShape(Color.WHITE, x - stroke / 2, y - stroke / 2, stroke, stroke);
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
        addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (!isPressed) {
                    if (zoom == 1 && e.getWheelRotation() == 1) {
                        return;
                    } else if (zoom == 10 && e.getWheelRotation() == -1) {
                        return;
                    }
                    lastPost = getLocation();
                    doInit = true;
                    zoom -= e.getWheelRotation() * .05f;
                    zoom = Math.max(1, Math.min(10, zoom));
                    int newWidth = (int) (image.getWidth() * zoom);
                    int newHeight = (int) (image.getHeight() * zoom);
                    setPreferredSize(new Dimension(newWidth, newHeight));
                    revalidate();
                    repaint();
                }
            }
        });
    }

    public void clear() {
        Graphics2D g = image.createGraphics();
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, image.getWidth(),
                image.getHeight());
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, image.getWidth(),
                image.getHeight());
        repaint();
        g.dispose();
    }

    private Graphics2D setupImageGraphics() {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        return g2d;
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
