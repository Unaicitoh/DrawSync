package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

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

    public Canvas() {
        shapes = new ArrayList<>();
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

    private void drawShape(Color color, int x, int y, int w, int h) {
        Graphics2D g2d = setupImageGraphics();
        Shape rect = new Shape(color, x, y, w, h);
//        shapes.add(rect);
        switch (mode) {
            case DRAWER -> {
                switch (drawingType) {
                    case 0 -> color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 1);
                    case 1 -> color = new Color(color.getRed(), color.getGreen(), color.getBlue(), .2f);
                }
                g2d.setColor(color);
                g2d.fillArc(x, y, w, h, 0, 360);
            }
            case ERASER -> {
                Composite comp = g2d.getComposite();
                g2d.setComposite(AlphaComposite.Clear);
                g2d.fillArc(x, y, w, h, 0, 360);
                g2d.setComposite(comp);
            }
        }
        repaint();
        g2d.dispose();
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
                        case ERASER -> drawShape(null, x - stroke / 2, y - stroke / 2, stroke, stroke);
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                super.mousePressed(e);
                if (mode == Mode.MOVING) {
                    setLastPos(getLocation());
                }
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
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
}
