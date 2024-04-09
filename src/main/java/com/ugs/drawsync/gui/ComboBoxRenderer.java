package com.ugs.drawsync.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

public class ComboBoxRenderer extends DefaultListCellRenderer {
    private static JButton button;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        CustomArrowUI.drawingType=list.getSelectedIndex();
        label.setHorizontalAlignment(CENTER);
        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            button.setIcon(label.getIcon());
        }
        return label;
    }

    static class CustomArrowUI extends BasicComboBoxUI {
        private Canvas canvas;
        public static int drawingType;

        public CustomArrowUI(Canvas canvas) {
            this.canvas = canvas;
        }

        @Override
        protected JButton createArrowButton() {
            JButton b = GUIBoard.createCanvasButton("src/main/resources/images/penIcon.png");
            button = b;
            b.addActionListener(e -> {
                canvas.setLocation(canvas.getLastPos());
                canvas.setLastMode(canvas.getMode());
                canvas.setMode(Mode.DRAWER);
                canvas.setDrawingType(drawingType);
                }
            );
            return b;
        }
    }
}
