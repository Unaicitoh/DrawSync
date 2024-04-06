package com.ugs.drawsync;

import com.ugs.drawsync.gui.GUIBoard;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUIBoard("DrawSync", 1250, 800));
    }
}