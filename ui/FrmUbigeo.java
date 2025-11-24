package ui;

import javax.swing.*;
import java.awt.*;
import model.Ubigeo;

public class FrmUbigeo extends JDialog {
    private final Ubigeo editing;
    public FrmUbigeo(Window owner) {
        this(owner, null);
    }
    public FrmUbigeo(Window owner, Ubigeo editar) {
        // FrmUbigeo:
        // - Constructor: muestra formulario para crear o editar un Ubigeo.
        // - Campos: código, departamento, provincia, distrito.
        // - btnCrear listener: valida campos obligatorios y usa `UbigeoDAO.insertar/actualizar`.
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = editar;
        setTitle(editar == null ? "Nuevo Ubigeo" : "Editar Ubigeo");
        setSize(520, 420);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(UIStyles.BG);

        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        // Header pastel con botón Menú
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel(editar == null ? "Nuevo Ubigeo" : "Editar Ubigeo");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(FrmUbigeo.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        // Formulario en panel central, estilo moderno
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField txtCodigo = new JTextField();
        JTextField txtDepartamento = new JTextField();
        JTextField txtProvincia = new JTextField();
        JTextField txtDistrito = new JTextField();
        if (editing != null) {
            txtCodigo.setText(editing.getCodigo());
            txtDepartamento.setText(editing.getDepartamento());
            txtProvincia.setText(editing.getProvincia());
            txtDistrito.setText(editing.getDistrito());
            txtCodigo.setEnabled(false);
        }

        int y=0;
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=1;
        JLabel labCod = new JLabel("Código*:"); labCod.setFont(UIStyles.UI_FONT_BOLD); labCod.setForeground(UIStyles.GREEN_DARK); form.add(labCod, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtCodigo, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labDep = new JLabel("Departamento*:"); labDep.setFont(UIStyles.UI_FONT_BOLD); labDep.setForeground(UIStyles.GREEN_DARK); form.add(labDep, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtDepartamento, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labProv = new JLabel("Provincia*:"); labProv.setFont(UIStyles.UI_FONT_BOLD); labProv.setForeground(UIStyles.GREEN_DARK); form.add(labProv, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtProvincia, gbc);
        gbc.gridx=0; gbc.gridy=y; JLabel labDist = new JLabel("Distrito*:"); labDist.setFont(UIStyles.UI_FONT_BOLD); labDist.setForeground(UIStyles.GREEN_DARK); form.add(labDist, gbc);
        gbc.gridx=1; gbc.gridy=y++; form.add(txtDistrito, gbc);

        // Botones de acción
        JButton btnCrear = new JButton("Crear"); UIStyles.styleButton(btnCrear);
        JButton btnCancelar = new JButton("Cancelar"); UIStyles.styleButton(btnCancelar);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setOpaque(false);
        acciones.add(btnCrear); acciones.add(btnCancelar);

        btnCancelar.addActionListener(e -> dispose());
        btnCrear.addActionListener(e -> {
            String codigo = txtCodigo.getText().trim();
            String dep = txtDepartamento.getText().trim();
            String prov = txtProvincia.getText().trim();
            String dist = txtDistrito.getText().trim();
            if (codigo.isEmpty() || dep.isEmpty() || prov.isEmpty() || dist.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios");
                return;
            }
            Ubigeo u = new Ubigeo(codigo, dep, prov, dist);
            boolean ok;
            if (editing == null) {
                ok = new dao.UbigeoDAO().insertar(u);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Ubigeo guardado");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Ya existe un ubigeo con ese código");
                }
            } else {
                ok = new dao.UbigeoDAO().actualizar(u);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Ubigeo actualizado");
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al actualizar");
                }
            }
        });

        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(acciones, BorderLayout.SOUTH);
        setContentPane(root);
        UIStyles.applyComponentTheme(getContentPane());
    }
}
