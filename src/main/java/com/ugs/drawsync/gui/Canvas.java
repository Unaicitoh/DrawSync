package com.ugs.drawsync.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class Canvas extends JPanel implements MouseListener, MouseMotionListener {
    public Canvas() {
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2, true));
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(400, 400));
        setFont(new Font("Ink Free", Font.BOLD, 64));
        setForeground(new Color(.8f, .8f, .8f, .5f));
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    private void drawRectangle(Color color, int x, int y, int w, int h) {
        Graphics g = getGraphics();
        g.setColor(color);
        g.fillRect(x - w / 2, y - h / 2, w, h);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("CANVAS", this.getX() + this.getWidth() / 2 - 32 * 4, this.getY() + this.getHeight() / 2 - 32 * 2);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        drawRectangle(Color.BLACK, e.getX(), e.getY(), 50, 50);
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        drawRectangle(Color.BLACK, e.getX(), e.getY(), 50, 50);
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
