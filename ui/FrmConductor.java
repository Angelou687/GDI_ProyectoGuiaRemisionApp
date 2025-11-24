package ui;

import dao.ConductorDAO;
import model.Conductor;

import javax.swing.*;
import java.awt.*;

public class FrmConductor extends JDialog {

    private final Conductor editing;

    /*
     * FrmConductor:
     * - Constructor(s): permite crear o editar un `Conductor`.
     * - init (inline in constructor): construye campos (licencia, dni, nombre, telefono, fecha venc.)
     * - btnSave listener: valida campos obligatorios (licencia, nombre, fecha), crea el objeto `Conductor` y llama a DAO insertar/actualizar.
     * - btnCancel: cierra el diálogo.
     */

    public FrmConductor(Window owner) { this(owner, null); }

    public FrmConductor(Window owner, Conductor editar) {
        super(owner, ModalityType.APPLICATION_MODAL);
        this.editing = editar;
        setTitle(editar == null ? "Nuevo conductor" : "Editar conductor");
        setSize(520, 320);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(8,8)); root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12)); root.setBackground(UIStyles.PANEL);
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JTextField txtLic = new JTextField();
        JTextField txtDni = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtTel = new JTextField();
        ui.DatePicker datePicker = new ui.DatePicker();

        int y=0;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Licencia:"), gbc); gbc.gridx=1; form.add(txtLic, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("DNI:"), gbc); gbc.gridx=1; form.add(txtDni, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Nombre:"), gbc); gbc.gridx=1; form.add(txtNombre, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Teléfono:"), gbc); gbc.gridx=1; form.add(txtTel, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Fecha venc. licencia:"), gbc); gbc.gridx=1; form.add(datePicker, gbc); y++;

        if (editar != null) {
            txtLic.setText(editar.getLicencia()); txtLic.setEnabled(false);
            txtDni.setText(editar.getDni()); txtNombre.setText(editar.getNombre()); txtTel.setText(editar.getTelefono());
            if (editar.getFechaVencimiento() != null) datePicker.setDate(editar.getFechaVencimiento());
        }

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton btnSave = new JButton(editar == null ? "Guardar" : "Actualizar"); UIStyles.styleButton(btnSave);
        JButton btnCancel = new JButton("Cancelar"); UIStyles.styleButton(btnCancel);
        actions.add(btnSave); actions.add(btnCancel);

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> {
            String lic = txtLic.getText().trim();
            String dni = txtDni.getText().trim();
            String nombre = txtNombre.getText().trim();
            String tel = txtTel.getText().trim();
            java.sql.Date fecha = datePicker.getDate();
            if (lic.isEmpty() || nombre.isEmpty() || fecha == null) { JOptionPane.showMessageDialog(this, "Licencia, Nombre y Fecha vencimiento son obligatorios"); return; }
            Conductor c = new Conductor(lic, dni, nombre, tel, fecha);
            ConductorDAO dao = new ConductorDAO();
            boolean ok = (editing == null) ? dao.insertar(c) : dao.actualizar(c);
            if (ok) { JOptionPane.showMessageDialog(this, "Conductor guardado"); dispose(); }
            else JOptionPane.showMessageDialog(this, "Error al guardar conductor");
        });

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        getContentPane().add(root);
        UIStyles.applyComponentTheme(getContentPane());
    }
}
