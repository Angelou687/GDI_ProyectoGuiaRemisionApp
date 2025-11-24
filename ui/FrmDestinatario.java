package ui;

import dao.DestinatarioDAO;
import model.Destinatario;

import javax.swing.*;
import java.awt.*;

public class FrmDestinatario extends JDialog {

    private final DestinatarioDAO dao = new DestinatarioDAO();
    private final Destinatario editing;

    /*
     * FrmDestinatario:
     * - Constructor: recibe `Destinatario` opcional para edición; configura UI y carga ubigeos.
     * - initUI(): organiza los campos del formulario y botones.
     * - btnOk listener: valida RUC (11 dígitos), campos obligatorios, formato de email, y llama a `dao.insertar` o `dao.actualizar`.
     * - btnCancel: vuelve al menú principal.
     */

    public FrmDestinatario(Window owner, Destinatario d) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = d;
        setTitle(d == null ? "Nuevo destinatario" : "Editar destinatario");
        setSize(680, 540);
        setMinimumSize(new Dimension(600, 480));
        setLocationRelativeTo(owner);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        // Fondo general suave
        getContentPane().setBackground(UIStyles.BG);

        // Panel raíz redondeado
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // Header destacado
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel(editing == null ? "Nuevo destinatario" : "Editar destinatario");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.PASTEL_GREEN);
        header.add(lblTitle, BorderLayout.WEST);

        // Botón menú en la esquina superior derecha
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(FrmDestinatario.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        // Formulario en panel central
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField txtRuc = new JTextField();
        JTextField txtNombre = new JTextField();
        JTextField txtTel = new JTextField();
        JTextField txtDir = new JTextField();
        JComboBox<String> cbUbigeo = new JComboBox<>();
        JTextField txtGmail = new JTextField();

        int y=0;
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=1;
        JLabel labRuc = new JLabel("RUC*:"); labRuc.setFont(UIStyles.UI_FONT_BOLD); form.add(labRuc, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtRuc, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labNom = new JLabel("Nombre*:"); labNom.setFont(UIStyles.UI_FONT_BOLD); form.add(labNom, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtNombre, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labTel = new JLabel("Teléfono:"); form.add(labTel, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtTel, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labDir = new JLabel("Dirección*:"); labDir.setFont(UIStyles.UI_FONT_BOLD); form.add(labDir, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtDir, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labUb = new JLabel("Ubigeo*: "); labUb.setFont(UIStyles.UI_FONT_BOLD); form.add(labUb, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(cbUbigeo, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labGm = new JLabel("Gmail:"); form.add(labGm, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtGmail, gbc);

        // Botones con estilo moderno
        JButton btnOk = new JButton(editing==null?"Crear":"Guardar"); UIStyles.styleButton(btnOk);
        JButton btnCancel = new JButton("Cancelar"); UIStyles.styleButton(btnCancel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
        actions.add(btnOk); actions.add(btnCancel);
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; form.add(actions, gbc);

        // populate ubigeo combo from DB
        cbUbigeo.addItem("");
        for (model.Ubigeo u : new dao.UbigeoDAO().listarTodos()) cbUbigeo.addItem(u.getCodigo());

        // Precargar datos si es edición
        if (editing != null) {
            txtRuc.setText(editing.getRuc()); txtRuc.setEnabled(false);
            txtNombre.setText(editing.getNombre()); txtTel.setText(editing.getNumeroTelefono());
            txtDir.setText(editing.getCalleDireccion()); cbUbigeo.setSelectedItem(editing.getCodigoUbigeo());
            txtGmail.setText(editing.getGmail());
        }

        // Acciones de botones
        btnCancel.addActionListener(e -> UIStyles.showMainMenu(FrmDestinatario.this));
        btnOk.addActionListener(e -> {
            String ruc = txtRuc.getText().trim();
            String nombre = txtNombre.getText().trim();
            String dir = txtDir.getText().trim();
            String ub = cbUbigeo.getSelectedItem() == null ? "" : ((String)cbUbigeo.getSelectedItem()).trim();
            String tel = txtTel.getText().trim();
            String gm = txtGmail.getText().trim();

            if (ruc.isEmpty() || nombre.isEmpty() || dir.isEmpty() || ub.isEmpty()) {
                JOptionPane.showMessageDialog(this, "RUC, Nombre, Dirección y Ubigeo son obligatorios"); return;
            }
            // RUC: exactamente 11 dígitos
            if (!ruc.matches("\\d{11}")) {
                JOptionPane.showMessageDialog(this, "RUC debe tener 11 dígitos numéricos"); return;
            }
            // Ubigeo: exactamente 6 dígitos
            if (!ub.matches("\\d{6}")) { JOptionPane.showMessageDialog(this, "Ubigeo debe tener 6 dígitos"); return; }
            // Email simple RFC-like check
            if (!gm.isEmpty() && !gm.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                JOptionPane.showMessageDialog(this, "Email inválido"); return;
            }

            if (editing == null) {
                Destinatario d = new Destinatario(ruc, nombre, tel, dir, ub, gm);
                if (dao.insertar(d)) { JOptionPane.showMessageDialog(this, "Creado"); dispose(); ((DestinatarioWindow)getOwner()).load(); }
                else JOptionPane.showMessageDialog(this, "Error al crear");
            } else {
                editing.setNombre(nombre); editing.setCalleDireccion(dir); editing.setCodigoUbigeo(ub);
                editing.setNumeroTelefono(tel); editing.setGmail(gm);
                if (dao.actualizar(editing)) { JOptionPane.showMessageDialog(this, "Actualizado"); dispose(); ((DestinatarioWindow)getOwner()).load(); }
                else JOptionPane.showMessageDialog(this, "Error al actualizar");
            }
        });

        // Composición final
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }
}
