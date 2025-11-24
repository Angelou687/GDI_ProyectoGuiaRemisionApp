/*
 * BootstrapRunner.java
 * Pequeño lanzador que abre el diálogo de bootstrap en el hilo de la UI.
 * Se usa para ejecutar la herramienta de bootstrap desde la línea de comandos
 * o atajos (por ejemplo `run-bootstrap.cmd`). No realiza lógica adicional.
 */
package util;

import javax.swing.SwingUtilities;

public class BootstrapRunner {
    // main(): inicializa la UI y lanza `ui.BootstrapDialog` en el EDT.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ui.BootstrapDialog dlg = new ui.BootstrapDialog(null);
            dlg.setVisible(true);
            System.exit(dlg.isSucceeded() ? 0 : 1);
        });
    }
}
