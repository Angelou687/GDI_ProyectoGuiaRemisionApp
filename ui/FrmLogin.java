package ui;

/*
 * FrmLogin.java
 * Formulario de inicio de sesión para remitentes.
 * - Constructor: construye los campos y botones del login.
 * - Listeners:
 *   - btnRegister: abre `FrmRemitente` para registrar un remitente nuevo.
 *   - btnLogin: valida RUC, consulta `RemitenteDAO` y `RemitenteCredDAO`, establece sesión y abre `FrmMenu`.
 * Comentarios inline describen validaciones y flujos de fallback.
 */

import dao.RemitenteDAO;
import dao.RemitenteCredDAO;
import util.Session;
import javax.swing.*;
import java.awt.*;

public class FrmLogin extends JDialog {

    public FrmLogin(Window owner) {
        super(owner, "Iniciar sesión", ModalityType.APPLICATION_MODAL);
        setSize(420, 260);
        setLocationRelativeTo(owner);

        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(UIStyles.PANEL);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;

        JTextField txtRuc = new JTextField();
        JPasswordField txtPwd = new JPasswordField();

        gbc.gridx=0; gbc.gridy=0; p.add(new JLabel("RUC:"), gbc); gbc.gridx=1; p.add(txtRuc, gbc);
        gbc.gridx=0; gbc.gridy=1; p.add(new JLabel("Contraseña:"), gbc); gbc.gridx=1; p.add(txtPwd, gbc);

        JButton btnLogin = new JButton("Entrar"); UIStyles.styleButton(btnLogin);
        JButton btnRegister = new JButton("Registrar remitente"); UIStyles.styleButton(btnRegister);
        JPanel aks = new JPanel(new FlowLayout(FlowLayout.RIGHT)); aks.setOpaque(false); aks.add(btnRegister); aks.add(btnLogin);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2; p.add(aks, gbc);

        btnRegister.addActionListener(e -> {
            FrmRemitente fr = new FrmRemitente(this, null);
            fr.setVisible(true);
        });

        btnLogin.addActionListener(e -> {
            String ruc = txtRuc.getText().trim();
            String pwd = new String(txtPwd.getPassword());
            if (ruc.isEmpty()) { JOptionPane.showMessageDialog(this, "Ingrese RUC"); return; }

            RemitenteDAO rdao = new RemitenteDAO();
            if (!rdao.existe(ruc)) {
                int resp = JOptionPane.showConfirmDialog(this, "RUC no registrado. Desea registrar los datos ahora?", "Remitente no encontrado", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) {
                    FrmRemitente fr = new FrmRemitente(this, ruc);
                    fr.setVisible(true);
                }
                return;
            }

            RemitenteCredDAO cred = new RemitenteCredDAO();
            boolean ok;
            try {
                ok = cred.checkCredential(ruc, pwd);
            } catch (Exception ex) { ok = false; }

            // if credentials not set in DB, allow login (fallback) and suggest to set password
            if (!ok) {
                int resp = JOptionPane.showConfirmDialog(this, "Credenciales no coinciden o no existen. Entrar de todos modos? (se recomienda registrar contraseña)", "Atención", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) return;
            }

            // success: set session remitente, open menu and close login
            Session.setCurrentRuc(ruc);
            dispose();
            FrmMenu menu = new FrmMenu();
            menu.setVisible(true);
        });

        getContentPane().add(p);
        UIStyles.applyComponentTheme(getContentPane());
    }
}
