package com.ugs.drawsync;

import com.ugs.drawsync.gui.GUIBoard;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Multi Board!");
        SwingUtilities.invokeLater(() -> new GUIBoard("DrawSync", 1100, 800));
    }
}