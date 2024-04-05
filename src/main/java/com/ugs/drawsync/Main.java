package com.ugs.drawsync;

import com.ugs.drawsync.gui.GUIBoard;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIBoard("DrawSync", 1200, 900));
    }
}