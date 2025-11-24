/*
 * BootstrapDialog.java
 * Diálogo Swing que permite ejecutar el bootstrap de la base de datos desde la
 * aplicación. Permite especificar la ruta a `psql`, credenciales admin, nombre
 * de la BD, carpeta/archivo de esquema (`sql` por defecto), y credenciales de
 * la cuenta de aplicación. Muestra un área de log y permite exportarlo.
 */
package ui;

import util.SqlBootstrapper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.awt.Desktop;

public class BootstrapDialog extends JDialog {

    private final JTextField txtPsqlPath = new JTextField();
    private final JTextField txtHost = new JTextField("localhost");
    private final JTextField txtPort = new JTextField("5432");
    private final JTextField txtAdminUser = new JTextField("postgres");
    private final JPasswordField txtAdminPass = new JPasswordField();
    private final JTextField txtDbName = new JTextField("guia_remision");
    private final JTextField txtSchema = new JTextField("sql");
    private final JTextField txtAppUser = new JTextField("app_user");
    private final JPasswordField txtAppPass = new JPasswordField();
    private final JCheckBox chkRunDrop = new JCheckBox("Ejecutar 00_drop_objects.sql antes (borra datos)");
    private final JTextArea logArea = new JTextArea(12, 60);

    private boolean succeeded = false;

    // Constructor: crea el diálogo modal con campos prellenados y botones.
    public BootstrapDialog(Frame owner) {
        super(owner, "Bootstrap DB — Configuración", true);
        initUI();
    }

    // initUI(): construye el formulario de entrada, prefiltra rutas comunes a psql
    // y registra listeners para ejecutar/cancelar/abrir log.
    private void initUI() {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        addRow(p, gbc, y++, "Ruta a psql (opcional)", txtPsqlPath);
        // try to prefill with common Windows psql install locations
        try {
            if (txtPsqlPath.getText() == null || txtPsqlPath.getText().isEmpty()) {
                String[] common = new String[] {
                    "C:\\Program Files\\PostgreSQL\\15\\bin\\psql.exe",
                    "C:\\Program Files\\PostgreSQL\\14\\bin\\psql.exe",
                    "C:\\Program Files\\PostgreSQL\\13\\bin\\psql.exe",
                    "C:\\Program Files\\PostgreSQL\\12\\bin\\psql.exe"
                };
                for (String path: common) {
                    java.io.File f = new java.io.File(path);
                    if (f.exists() && f.isFile()) { txtPsqlPath.setText(path); break; }
                }
            }
        } catch (Throwable t) {
            // ignore prefill errors
        }
        // if still empty, try to find psql in PATH
        try {
            if (txtPsqlPath.getText() == null || txtPsqlPath.getText().isEmpty()) {
                String found = findPsqlInPath();
                if (found != null) txtPsqlPath.setText(found);
            }
        } catch (Throwable ignore) {}
        addRow(p, gbc, y++, "Host", txtHost);
        addRow(p, gbc, y++, "Puerto", txtPort);
        addRow(p, gbc, y++, "Usuario admin", txtAdminUser);
        addRow(p, gbc, y++, "Contraseña admin", txtAdminPass);
        addRow(p, gbc, y++, "Nombre BD destino", txtDbName);
        addRow(p, gbc, y++, "Carpeta o archivo schema (relativo a app)", txtSchema);
        addRow(p, gbc, y++, "Usuario app a crear", txtAppUser);
        addRow(p, gbc, y++, "Contraseña app", txtAppPass);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.weightx = 1.0;
        p.add(chkRunDrop, gbc);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.BOTH;
        logArea.setEditable(false);
        JScrollPane sp = new JScrollPane(logArea);
        p.add(sp, gbc);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnRun = new JButton("Ejecutar bootstrap");
        JButton btnCancel = new JButton("Cancelar");
        JButton btnOpenLog = new JButton("Abrir log");
        btns.add(btnOpenLog);
        btns.add(btnRun);
        btns.add(btnCancel);

        gbc.gridx = 0; gbc.gridy = y++; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(btns, gbc);

            btnRun.addActionListener((java.awt.event.ActionEvent evt) -> {
                // validate psql path before running
                String psql = txtPsqlPath.getText();
                if (psql != null && !psql.isEmpty()) {
                    java.io.File f = new java.io.File(psql);
                    if (!f.exists() || !f.isFile()) {
                        javax.swing.JOptionPane.showMessageDialog(BootstrapDialog.this, "El archivo especificado en 'Ruta a psql' no existe:\n" + psql, "Ruta inválida", javax.swing.JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                onRun(evt);
        });
        btnCancel.addActionListener(e -> { succeeded = false; dispose(); });
        btnOpenLog.addActionListener(e -> openLogFile());

        getContentPane().add(p);
        pack();
        setLocationRelativeTo(getOwner());
    }

    // addRow(): helper para añadir una label + componente en una fila
    private void addRow(JPanel p, GridBagConstraints gbc, int y, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = y; gbc.gridwidth = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JLabel(label+":"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1.0;
        p.add(comp, gbc);
    }

    // onRun(): recoge los valores del formulario, valida y ejecuta el bootstrap
    // en un SwingWorker para no bloquear el EDT. Actualiza `succeeded` y el log.
    private void onRun(ActionEvent ev) {
        // Collect values
        String psql = txtPsqlPath.getText().trim();
        String host = txtHost.getText().trim();
        String port = txtPort.getText().trim();
        String adminUser = txtAdminUser.getText().trim();
        String adminPass = new String(txtAdminPass.getPassword());
        String dbName = txtDbName.getText().trim();
        String schemaPath = txtSchema.getText().trim();
        String appUser = txtAppUser.getText().trim();
        String appPass = new String(txtAppPass.getPassword());
        boolean runDrop = chkRunDrop.isSelected();

        if (dbName.isEmpty() || adminUser.isEmpty() || adminPass.isEmpty() || appUser.isEmpty() || appPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete los datos admin y app (contraseñas obligatorias)");
            return;
        }

        File schemaFileCandidate = new File(schemaPath);
        if (!schemaFileCandidate.exists()) {
            // try relative to current working dir
            schemaFileCandidate = new File(System.getProperty("user.dir"), schemaPath);
        }
        final File schemaFile = schemaFileCandidate;

        logArea.setText("");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void, String> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                appendLog("Iniciando bootstrap...\n");
                try {
                    SqlBootstrapper.Result r = SqlBootstrapper.createDatabaseAndRunSchema(psql, host, port, adminUser, adminPass, dbName, schemaFile, runDrop, appUser, appPass);
                    appendLog(r.output + "\n");
                    if (r.success) {
                        appendLog("Escribiendo db.properties...\n");
                        boolean wrote = SqlBootstrapper.writeDbProperties(host, port, dbName, appUser, appPass);
                        appendLog(wrote ? "db.properties creado.\n" : "No se pudo crear db.properties\n");
                        succeeded = true;
                    } else {
                        succeeded = false;
                    }
                } catch (Exception ex) {
                    appendLog("Excepción: " + ex.getMessage() + "\n");
                    succeeded = false;
                }
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                // Ensure log area is visible and caret at end
                appendLog("\n--- FIN DE LA OPERACIÓN ---\n");
                if (succeeded) {
                    int res = JOptionPane.showOptionDialog(BootstrapDialog.this,
                            "Bootstrap completado correctamente. ¿Abrir log?",
                            "Éxito",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null, new Object[]{"Abrir log","Cerrar"}, "Abrir log");
                    if (res == JOptionPane.YES_OPTION) openLogFile();
                    dispose();
                } else {
                    int res = JOptionPane.showOptionDialog(BootstrapDialog.this,
                            "Bootstrap falló. ¿Ver log para más detalles?",
                            "Error",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.ERROR_MESSAGE,
                            null, new Object[]{"Ver log","Cerrar"}, "Ver log");
                    if (res == JOptionPane.YES_OPTION) openLogFile();
                }
            }
        };
        worker.execute();
    }

    // appendLog(): añade texto al área de logs en el EDT y mantiene el caret al final.
    private void appendLog(String s) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(s);
            try { logArea.setCaretPosition(logArea.getDocument().getLength()); } catch (Exception ignore) {}
        });
    }

    // openLogFile(): exporta el contenido del log a `sql/psql_err_java.txt` y
    // abre el fichero con el editor por defecto si está disponible.
    private void openLogFile() {
        // write logArea content to sql/psql_err_java.txt and open with default editor
        try {
            File dir = new File("sql");
            if (!dir.exists()) dir.mkdirs();
            File f = new File(dir, "psql_err_java.txt");
            try (FileWriter fw = new FileWriter(f, false)) {
                fw.write(logArea.getText());
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(f);
            } else {
                JOptionPane.showMessageDialog(this, "Log guardado en: " + f.getAbsolutePath());
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo abrir el log: " + ex.getMessage());
        }
    }

    // isSucceeded(): indica si la operación terminó correctamente
    public boolean isSucceeded() { return succeeded; }

    // findPsqlInPath(): busca `psql.exe` o `psql` en las rutas de PATH y
    // retorna la primera coincidencia (ruta absoluta) o null si no se encuentra.
    private String findPsqlInPath() {
        String path = System.getenv("PATH");
        if (path == null) return null;
        String[] parts = path.split(File.pathSeparator);
        for (String p : parts) {
            File f = new File(p, "psql.exe");
            if (f.exists() && f.isFile()) return f.getAbsolutePath();
            // also try psql (unix)
            f = new File(p, "psql");
            if (f.exists() && f.isFile()) return f.getAbsolutePath();
        }
        return null;
    }
}
