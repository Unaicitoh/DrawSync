package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Canvas extends JPanel {

    public static final int CANVAS_SIZE = 1500;
    private List<Shape> shapes;
    private Color color;
    private int stroke = 5;
    private int mouseX;
    private int mouseY;
    private boolean doInit;
    private boolean isInit;
    private int drawingType;
    private Mode mode;
    private Mode lastMode;
    private BufferedImage image;
    private boolean canInteract;
    private final Cursor eraserCursor;
    private double zoom = 1f;
    private Point lastPos;
    private Dimension lastSize;
    private boolean isPressed;
    private Point parentMousePos;
    private Thread airbrushThread;
    private boolean isDragging;
    private List<Point> polygonVertices;

    public Canvas() {
        shapes = new ArrayList<>();
        polygonVertices = new ArrayList<>();
        color = Color.BLACK;
        mode = Mode.DRAWER;
        image = new BufferedImage(CANVAS_SIZE, CANVAS_SIZE, BufferedImage.TYPE_INT_ARGB);
        image.setAccelerationPriority(1);
        setDoubleBuffered(true);
        setPreferredSize(new Dimension(CANVAS_SIZE, CANVAS_SIZE));
        eraserCursor = Toolkit.getDefaultToolkit().createCustomCursor(new ImageIcon("src/main/resources/images/eraserIcon.png").getImage(), new Point(10, 30), "EraserCursor");
        setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        setBackground(Color.WHITE);
        addEventListeners();
    }

    private void drawShape(int x, int y, int w, int h) {
        Graphics2D g2d = setupImageGraphics();
        Shape rect = new Shape(color, x, y, w, h);
//        shapes.add(rect);
        float[] rgb = new float[4];
        switch (mode) {
            case DRAWER -> {
                switch (drawingType) {
                    case 0 -> adjustAlphaProp(1, x, y, w, h, rgb, g2d);
                    case 1 -> adjustAlphaProp(.25f, x, y, w, h, rgb, g2d);
                    case 2 -> adjustAlphaProp(.1f, x, y, w, h, rgb, g2d);
                    case 3 -> {
                        color = new Color(color.getColorSpace(), color.getRGBComponents(rgb), 1);
                        g2d.setColor(color);
                        for (int i = -2; i <= 2; i++) {
                            g2d.drawLine(x - stroke + stroke / 2 + i, y + stroke + stroke / 2, x + stroke + stroke / 2 + i, y - stroke + stroke / 2);
                        }
                    }
                    case 4 -> {
                        drawAirbrush(x, y, w, h, rgb);
                    }
                }
            }
            case ERASER -> {
                Composite comp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillArc(x, y, w, h, 0, 360);
                g2d.setComposite(comp);
            }
            case LINER -> {
                color = new Color(color.getColorSpace(), color.getRGBComponents(rgb), 1);
                g2d.setColor(color);
                Point p1 = polygonVertices.get(polygonVertices.size() - 1);
                Point p2 = polygonVertices.get(polygonVertices.size() - 2);
//                w=Math.min(7,w);
                for (int i = -(w / 2); i < w / 2; i++) {
                    g2d.drawLine(p1.x + i, p1.y + i, p2.x + i, p2.y + i);
                }
            }
        }
        repaint();
        g2d.dispose();
    }

    private void adjustAlphaProp(float alpha, int x, int y, int w, int h, float[] rgb, Graphics2D g2d) {
        color = new Color(color.getColorSpace(), color.getRGBComponents(rgb), alpha);
        g2d.setColor(color);
        g2d.fillArc(x, y, w, h, 0, 360);
    }

    private void drawAirbrush(int x, int y, int w, int h, float[] rgb) {
        Random rnd = new Random();
        int density;
        if (w < 25) {
            density = w * h / 5;
        } else if (w < 75) {
            density = w * h / 10;
        } else {
            density = w * h / 15;
        }
        Graphics2D g2 = setupImageGraphics();
        color = new Color(color.getColorSpace(), color.getRGBComponents(rgb), 1);
        g2.setColor(color);
        if (isPressed && !isDragging) {
            airbrushThread = new Thread(() -> {
                while (isPressed && !isDragging) {
                    createAirbrushNoise(x, y, w, h, density, rnd, g2);
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    repaint(x, y, w * stroke, h * stroke);
                }
            });
            airbrushThread.start();
        } else {
            createAirbrushNoise(x, y, w, h, density, rnd, g2);
        }
    }

    private static void createAirbrushNoise(int x, int y, int w, int h, int density, Random rnd, Graphics2D g2) {
        for (int i = 0; i < density; i++) {
            int xOffset = rnd.nextInt(w);
            int yOffset = rnd.nextInt(h);
            g2.fillRect(x + xOffset, y + yOffset, 1, 1);
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        super.paintComponent(g2d);
        if (doInit) {
            Point p;
            Container container = getParent();
            if (!isInit) {
                p = getLocation();
                setLocation(p.x, p.y + container.getSize().height / 2 - getSize().height / 2);
                setLastPos(getLocation());
            }
            Point mousePos = parentMousePos;
            if (isInit && mousePos != null) {
                if (mousePos.x < container.getWidth() / 2 && mousePos.y < container.getHeight() / 2) {
                    setLocation(lastPos.x, lastPos.y);
                } else if (mousePos.x > container.getWidth() / 2 && mousePos.y > container.getHeight() / 2) {
                    setLocation((int) (lastPos.x - (getWidth() - lastSize.getWidth())), (int) (lastPos.y - (getHeight() - lastSize.getHeight())));
                } else if (mousePos.x < container.getWidth() / 2 && mousePos.y > container.getHeight() / 2) {
                    setLocation(lastPos.x, (int) (lastPos.y - (getHeight() - lastSize.getHeight())));
                } else if (mousePos.x > container.getWidth() / 2 && mousePos.y < container.getHeight() / 2) {
                    setLocation((int) (lastPos.x - (getWidth() - lastSize.getWidth())), lastPos.y);
                }
            }
            if (!isInit) {
                isInit = !isInit;
            }
            doInit = !doInit;
        }

        if (image != null) {
            AffineTransform at = AffineTransform.getScaleInstance(zoom, zoom);
            g2d.drawImage(image, at, null);
        }
    }

    private void addEventListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);
                isPressed = true;
                mouseX = e.getX();
                mouseY = e.getY();
                if (e.getButton() == 3) {
                    if (mode == Mode.LINER && !polygonVertices.isEmpty()) {
                        polygonVertices.clear();
                    }
                    lastMode = mode;
                    mode = Mode.MOVING;
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else if (e.getButton() == 1 && canInteract) {
                    int x = (int) (e.getX() / zoom);
                    int y = (int) (e.getY() / zoom);
                    switch (mode) {
                        case DRAWER -> {
                            if (airbrushThread == null || !airbrushThread.isAlive()) {
                                drawShape(x - stroke / 2, y - stroke / 2, stroke, stroke);
                            }
                        }
                        case ERASER -> drawShape(x - stroke / 2, y - stroke / 2, stroke, stroke);
                        case LINER -> {
                            polygonVertices.add(new Point(x, y));
                            if (polygonVertices.size() > 1) {
                                drawShape(0, 0, stroke, stroke);
                            }
                        }
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                super.mousePressed(e);
                isPressed = false;
                isDragging = false;
                if (mode == Mode.MOVING) {
                    setLastPos(getLocation());
                }
                if (e.getButton() == 3) {
                    switch (lastMode) {
                        case DRAWER, LINER -> {
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
                    case DRAWER, LINER -> setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
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
                isDragging = true;
                int x = (int) (e.getX() / zoom);
                int y = (int) (e.getY() / zoom);
                switch (mode) {
                    case DRAWER, ERASER -> {
                        if (canInteract) {
                            drawShape(x - stroke / 2, y - stroke / 2, stroke, stroke);
                        }
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

            @Override
            public void mouseMoved(MouseEvent e) {
                super.mouseMoved(e);
//                int x = (int) (e.getX() / zoom);
//                int y = (int) (e.getY() / zoom);
//                if(mode==Mode.LINER && !polygonVertices.isEmpty()){
//                    drawShape(x,y,stroke,stroke);
//                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                super.componentMoved(e);
            }
        });
    }

    public void clear() {
        Graphics2D g2d = image.createGraphics();
        g2d.setBackground(new Color(0, 0, 0, 0));
        g2d.clearRect(0, 0, image.getWidth(), image.getHeight());
        repaint();
        g2d.dispose();
    }

    private Graphics2D setupImageGraphics() {
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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


    public BufferedImage getImage() {
        return image;
    }

    public double getZoom() {
        return zoom;
    }

    public void setZoom(double zoom) {
        this.zoom = zoom;
    }

    public void setLastPos(Point lastPos) {
        this.lastPos = lastPos;
    }

    public boolean isPressed() {
        return isPressed;
    }


    public void setLastSize(Dimension lastSize) {
        this.lastSize = lastSize;
    }


    public void setParentMousePos(Point parentMousePos) {
        this.parentMousePos = parentMousePos;
    }

    public Point getLastPos() {
        return lastPos;
    }

    public void setDrawingType(int drawingType) {
        this.drawingType = drawingType;
    }

    public List<Point> getPolygonVertices() {
        return polygonVertices;
    }
}
