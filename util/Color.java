package util;

/**
 * Color.java
 * Pequeño helper que expone alias a `java.awt.Color` para evitar conflictos
 * de nombres con otras clases `Color` en el proyecto. Mantenerlo simple.
 */
public class Color {
    // Alias útiles; extender si necesitas más colores compartidos.
    public static final java.awt.Color BLACK = java.awt.Color.BLACK;
    public static final java.awt.Color DARK_GRAY = java.awt.Color.DARK_GRAY;
}
