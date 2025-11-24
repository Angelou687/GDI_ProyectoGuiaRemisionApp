package ui;

import dao.RemitenteDAO;
import dao.RemitenteCredDAO;
import dao.UbigeoDAO;
import model.Remitente;
import model.Ubigeo;

import javax.swing.*;
import java.awt.*;

public class FrmRemitente extends JDialog {

    public FrmRemitente(Window owner, String prefillRuc) {
        super(owner, "Registrar remitente", ModalityType.APPLICATION_MODAL);
        setSize(560, 420);
        setLocationRelativeTo(owner);

        JPanel root = new JPanel(new BorderLayout(8,8)); root.setBackground(UIStyles.PANEL); root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JTextField txtRuc = new JTextField(prefillRuc == null ? "" : prefillRuc);
        JTextField txtNombre = new JTextField();
        JTextField txtRazon = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtEmail = new JTextField();
        JTextField txtCalle = new JTextField();
        JComboBox<String> cbUbigeo = new JComboBox<>();
        JPasswordField txtPwd = new JPasswordField();

        int y=0;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("RUC (11):"), gbc); gbc.gridx=1; form.add(txtRuc, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Nombre empresa:"), gbc); gbc.gridx=1; form.add(txtNombre, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Razón social:"), gbc); gbc.gridx=1; form.add(txtRazon, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Teléfono:"), gbc); gbc.gridx=1; form.add(txtTel, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Email:"), gbc); gbc.gridx=1; form.add(txtEmail, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Dirección (calle):"), gbc); gbc.gridx=1; form.add(txtCalle, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Código Ubigeo:"), gbc); gbc.gridx=1; form.add(cbUbigeo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Crear contraseña (opcional):"), gbc); gbc.gridx=1; form.add(txtPwd, gbc); y++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        JButton btnSave = new JButton("Guardar"); UIStyles.styleButton(btnSave);
        JButton btnCancel = new JButton("Cancelar"); UIStyles.styleButton(btnCancel);
        actions.add(btnSave); actions.add(btnCancel);

        btnCancel.addActionListener(e -> dispose());

        // poblar ubigeos desde BD
        UbigeoDAO udao = new UbigeoDAO();
        cbUbigeo.addItem("");
        for (Ubigeo u : udao.listarTodos()) {
            cbUbigeo.addItem(u.getCodigo() + " - " + u.getDepartamento() + "/" + u.getProvincia() + "/" + u.getDistrito());
        }

        btnSave.addActionListener(e -> {
            String ruc = txtRuc.getText().trim();
            String nombre = txtNombre.getText().trim();
            String razon = txtRazon.getText().trim();
            String tel = txtTel.getText().trim();
            String email = txtEmail.getText().trim();
            String calle = txtCalle.getText().trim();
            String ubigeo = "";
            Object sel = cbUbigeo.getSelectedItem();
            if (sel != null) {
                String s = sel.toString();
                ubigeo = s.contains(" - ") ? s.split(" - ")[0].trim() : s.trim();
            }
            String pwd = new String(txtPwd.getPassword());

            if (ruc.isEmpty() || nombre.isEmpty() || razon.isEmpty() || calle.isEmpty() || ubigeo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Campos obligatorios: RUC, Nombre, Razón social, Dirección, Ubigeo");
                return;
            }

            Remitente r = new Remitente(ruc, nombre, razon, tel, email, calle, ubigeo);
            RemitenteDAO dao = new RemitenteDAO();
            boolean ok = dao.insertar(r);
            if (!ok) ok = dao.actualizar(r);
            if (ok) {
                if (!pwd.trim().isEmpty()) {
                    RemitenteCredDAO cdao = new RemitenteCredDAO();
                    cdao.setCredential(ruc, pwd);
                }
                JOptionPane.showMessageDialog(this, "Remitente guardado");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar remitente");
            }
        });

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        getContentPane().add(root);
        UIStyles.applyComponentTheme(getContentPane());
    }
}
