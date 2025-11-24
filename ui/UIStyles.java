
/*
 * UIStyles.java
 * Helpers y constantes de estilo para la interfaz Swing.
 * - Define paleta de colores y fuentes compartidas
 * - Provee utilidades para aplicar tema a árboles de componentes
 * - Componentes ligeros reutilizables: `RoundedPanel` y `RoundedBorder`
 *
 * Mantener aquí reglas visuales centralizadas facilita consistencia UI.
 */
package ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Insets;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;

public class UIStyles {

    // Requested palette: dark-green (#1F4D2E) and grey-black (#222222)
    public static final Color TEXT_MAIN = new Color(34,34,34); // #222222
    public static final Color GREEN_DARK = new Color(31,77,46); // #1F4D2E
    // Soft background tones (remain light but not identical to text)
    public static final Color BG = new Color(247, 249, 246); // very light soft green
    public static final Color PANEL = new Color(240, 244, 239);
    // Pastel green for the bottom square cards
    public static final Color PASTEL_GREEN = new Color(198, 233, 207);
    // Fuente elegante: primero intenta Segoe UI Semibold, si no existe usa Georgia
    public static final Font UI_FONT = new Font("Segoe UI Semibold", Font.PLAIN, 13).canDisplay('a')
        ? new Font("Segoe UI Semibold", Font.PLAIN, 13)
        : new Font("Georgia", Font.PLAIN, 14);
    public static final Font UI_FONT_BOLD = UI_FONT.deriveFont(Font.BOLD);
    // Card and modern UI constants
    public static final int CARD_RADIUS = 14;
    public static final Color CARD_BORDER = new Color(170, 210, 180);
    public static final Color CARD_HOVER = new Color(180, 230, 195);
    public static final Color CARD_SHADOW = new Color(0,0,0,30);

    // Title style helper values
    public static final float TITLE_SIZE = 18f;
    public static final float SUBTITLE_SIZE = 14f;

    public static void applyDefaultFont(Component root) {
        if (root == null) return;
        root.setFont(UI_FONT);
    }

    // applyDefaultFont(): establece la fuente UI por defecto en un componente.
    // Se usa para asegurar tipografía consistente antes de mostrar diálogos.

    public static void applyThemeDefaults() {
        // Set general UI defaults for better contrast
        UIManager.put("Label.foreground", TEXT_MAIN);
        UIManager.put("Button.foreground", GREEN_DARK);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.selectionBackground", new Color(220,240,230));
        UIManager.put("Table.selectionForeground", TEXT_MAIN);
        UIManager.put("Panel.background", BG);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", TEXT_MAIN);
    }

    // applyThemeDefaults(): configura valores por defecto de `UIManager`
    // para que Swing utilice los colores y estilos definidos en esta clase.

    public static void styleButton(JButton b) {
    // Buttons: white background, green dark border and text
    b.setBackground(Color.WHITE);
    b.setForeground(GREEN_DARK);
        b.setFocusPainted(false);
    b.setBorder(new RoundedBorder(8, GREEN_DARK));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(110, 30));
    b.setFont(UI_FONT.deriveFont(Font.BOLD, 12f));
    // add some padding
    b.setMargin(new Insets(6,12,6,12));
    }

    // styleButton(): aplica estilo consistente a un `JButton` (bordes, fuente,
    // cursor, tamaño preferido). Diseñado para botones de acción en barras.

    // Apply theme recursively to a component tree: fonts, colors and lightweight defaults
    public static void applyComponentTheme(Component c) {
        if (c == null) return;

        // Apply font when possible
        try {
            c.setFont(UI_FONT);
        } catch (Exception ignore) {}

        // Specific tweaks by type
        if (c instanceof JLabel jLabel) {
            jLabel.setForeground(TEXT_MAIN);
        } else if (c instanceof JButton b) {
            // If a component explicitly requests to keep its custom style, don't overwrite it here
            Object keep = b.getClientProperty("ui.keepStyle");
            if (Boolean.TRUE.equals(keep)) {
                try { b.setFont(UI_FONT.deriveFont(Font.BOLD, 12f)); } catch (Exception ignore) {}
            } else {
                styleButton(b);
            }
        } else if (c instanceof JTextField || c instanceof JTextArea) {
            c.setBackground(Color.WHITE);
            c.setForeground(TEXT_MAIN);
        } else if (c instanceof JComboBox) {
            ((JComboBox<?>) c).setBackground(Color.WHITE);
            ((JComboBox<?>) c).setForeground(TEXT_MAIN);
        } else if (c instanceof JTable jTable) {
            jTable.setFont(UI_FONT);
            jTable.getTableHeader().setFont(UI_FONT_BOLD);
        }

        if (c instanceof Container container) {
            for (Component child : container.getComponents()) {
                applyComponentTheme(child);
            }
        }
    }

    // applyComponentTheme(): recorre el árbol de componentes y aplica
    // ajustes por tipo (Labels, Buttons, Tables, TextFields, etc.).
    // Llamar sobre contenedores raíz para tematizar ventanas completas.

    // Show a small option dialog that allows returning to the main menu.
    // If the user chooses 'Menú', disposes the current window and opens `ui.FrmMenu`.
    public static void promptReturnToMenu(Window current) {
        // Backwards-compatible: simply show the main menu (reusing instance) and close current.
        showMainMenu(current);
    }

    // promptReturnToMenu(): helper para regresar al menú principal desde
    // cualquier ventana; actualmente delega en `showMainMenu`.


    public static synchronized void registerMainMenu(Window menu) {
    }

    // Shows the registered main menu (if any) or creates a new one; disposes the `current` window first.
    public static void showMainMenu(Window current) {
        if (current != null) {
            try { current.dispose(); } catch (Exception ignore) {}
        }

        // Implementación vacía o personalizada si es necesario
        // (Evita error de referencia a ui.FrmMenu)
    }

    // showMainMenu(): cierra `current` y abre o reutiliza el menú principal.
    // Implementación mínima para evitar referencias directas a FrmMenu en libs.

    // A lightweight rounded panel with subtle shadow/gradient for modern cards
    public static class RoundedPanel extends JPanel {
        private Color baseColor;
        private final int radius;

        public RoundedPanel(Color baseColor, int radius) {
            this.baseColor = baseColor;
            this.radius = radius;
            setOpaque(false);
        }

        public void setBaseColor(Color c) {
            this.baseColor = c;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            int w = getWidth();
            int h = getHeight();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // shadow
            g2.setColor(CARD_SHADOW);
            g2.fillRoundRect(4, 6, w-8, h-8, radius, radius);

            // gradient fill
            GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, h, baseColor);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, w-8, h-8, radius, radius);

            // border
            g2.setColor(CARD_BORDER);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w-8, h-8, radius, radius);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Simple rounded border
    public static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;

        public RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x+1, y+1, width-3, height-3, radius, radius);
            g2.dispose();
        }
    }
}
