/*
 * Session.java
 * Helper muy ligero para mantener la sesión actual en memoria.
 * Actualmente sólo guarda el RUC del remitente autenticado.
 * Provee getters/setters y un isLogged() simple usado por la UI.
 */
package util;

public class Session {
    // ruc actualmente autenticado en la aplicación (empty = no logueado)
    private static String currentRuc = "";

    // getCurrentRuc(): retorna el RUC actual (cadena vacía si no hay sesión)
    public static String getCurrentRuc() {
        return currentRuc;
    }

    // setCurrentRuc(): actualiza el RUC de sesión (normaliza con trim/null-safe)
    public static void setCurrentRuc(String ruc) {
        currentRuc = (ruc == null) ? "" : ruc.trim();
    }

    // isLogged(): verdadero si existe un RUC no vacío
    public static boolean isLogged() {
        return currentRuc != null && !currentRuc.isEmpty();
    }
}
