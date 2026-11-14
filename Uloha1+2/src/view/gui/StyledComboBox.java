package view.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class StyledComboBox extends JComboBox<String> {
    private boolean hovered;

    public StyledComboBox(String[] options, int width) {
        super(options);
        setUI(new BasicComboBoxUI() {
            @Override
            public void installUI(JComponent component) {
                super.installUI(component);
                listBox.setForeground(Color.WHITE);
                listBox.setSelectionForeground(Color.WHITE);
            }

            @Override
            protected JButton createArrowButton() {
                JButton dropDownButton = new JButton(new ChevronIcon());
                dropDownButton.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 8));
                dropDownButton.setContentAreaFilled(false);
                dropDownButton.setFocusPainted(false);
                dropDownButton.setFocusable(false);
                dropDownButton.setOpaque(false);
                dropDownButton.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent event) {
                        hovered = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent event) {
                        hovered = false;
                        repaint();
                    }
                });
                return dropDownButton;
            }

            @Override
            public void paintCurrentValueBackground(Graphics graphics, Rectangle bounds, boolean hasFocus) {
            }
        });
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus
            ) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setForeground(Color.WHITE);
                setBackground(isSelected ? Button.NEUTRAL_HOVER : Button.NEUTRAL);
                setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                setOpaque(index >= 0);
                return this;
            }
        });
        setBackground(Button.NEUTRAL);
        setForeground(Color.WHITE);
        setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 24));
        setPreferredSize(new Dimension(width, 34));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                hovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent event) {
                hovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? Button.NEUTRAL_HOVER : Button.NEUTRAL);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
        g2.dispose();
        super.paintComponent(graphics);
    }

    @Override
    protected void paintBorder(Graphics graphics) {
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color outline = isFocusOwner()
            ? Button.NEUTRAL_HOVER_OUTLINE
            : hovered ? Button.NEUTRAL_HOVER_OUTLINE : Button.NEUTRAL_OUTLINE;
        g2.setColor(outline);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 9, 9);
        g2.dispose();
    }

    private static final class ChevronIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 9;
        }

        @Override
        public int getIconHeight() {
            return 6;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.drawLine(x, y, x + 4, y + 4);
            g2.drawLine(x + 4, y + 4, x + 8, y);
            g2.dispose();
        }
    }
}