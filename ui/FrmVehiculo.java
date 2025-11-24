package ui;

import model.Vehiculo;
import dao.VehiculoDAO;
import javax.swing.*;
import java.awt.*;

public class FrmVehiculo extends JDialog {
    private final Vehiculo editing;
    public FrmVehiculo(Window owner) {
        this(owner, null);
    }
    public void onVehiculoGuardado(Vehiculo v) {
        // Este método será sobreescrito por VehiculosWindow
    }
    public FrmVehiculo(Window owner, Vehiculo editar) {
        // FrmVehiculo:
        // - Constructor: inicializa el formulario para crear/editar un vehículo.
        // - onVehiculoGuardado: callback que la ventana padre puede sobreescribir para recargar listados.
        // - En el listener de guardar: valida placa y carga, crea `Vehiculo` y llama a `VehiculoDAO.insertar/actualizar`.
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = editar;
        setTitle(editar == null ? "Nuevo vehículo" : "Editar vehículo");
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
        JLabel lblTitle = new JLabel(editar == null ? "Nuevo vehículo" : "Editar vehículo");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(FrmVehiculo.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        // Formulario moderno
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        int y=0;
        JTextField txtPlaca = new JTextField();
        JTextField txtNumeroMtc = new JTextField();
        JTextField txtTipo = new JTextField();
        JTextField txtMarca = new JTextField();
        JTextField txtModelo = new JTextField();
        JTextField txtCarga = new JTextField();
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Placa*:"), gbc); gbc.gridx=1; form.add(txtPlaca, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("N° MTC:"), gbc); gbc.gridx=1; form.add(txtNumeroMtc, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Tipo vehículo:"), gbc); gbc.gridx=1; form.add(txtTipo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Marca:"), gbc); gbc.gridx=1; form.add(txtMarca, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Modelo:"), gbc); gbc.gridx=1; form.add(txtModelo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Carga máxima (kg):"), gbc); gbc.gridx=1; form.add(txtCarga, gbc); y++;
        if (editar != null) {
            txtPlaca.setText(editing.getPlaca());
            txtNumeroMtc.setText(editing.getNumeroMtc());
            txtTipo.setText(editing.getTipoVehiculo());
            txtMarca.setText(editing.getMarca());
            txtModelo.setText(editing.getModelo());
            txtCarga.setText(String.valueOf(editing.getCargaMax()));
            txtPlaca.setEnabled(false);
        }
        JButton btnGuardar = new JButton(editar == null ? "Guardar" : "Actualizar"); UIStyles.styleButton(btnGuardar);
        JButton btnCancelar = new JButton("Cancelar"); UIStyles.styleButton(btnCancelar);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        acciones.setOpaque(false);
        acciones.add(btnGuardar); acciones.add(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());
        btnGuardar.addActionListener(e -> {
            String placa = txtPlaca.getText().trim();
            String numeroMtc = txtNumeroMtc.getText().trim();
            String tipo = txtTipo.getText().trim();
            String marca = txtMarca.getText().trim();
            String modelo = txtModelo.getText().trim();
            String cargaS = txtCarga.getText().trim();
            if (placa.isEmpty()) {
                JOptionPane.showMessageDialog(this, "La placa es obligatoria");
                return;
            }
            double carga = 0.0;
            if (!cargaS.isEmpty()) {
                try {
                    carga = Double.parseDouble(cargaS);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Carga máxima debe ser un número", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            Vehiculo v = new Vehiculo(placa, numeroMtc, tipo, marca, modelo, carga);
            VehiculoDAO dao = new VehiculoDAO();
            boolean ok;
            if (editing == null) {
                ok = dao.insertar(v);
            } else {
                ok = dao.actualizar(v);
            }
            if (ok) {
                onVehiculoGuardado(v);
                JOptionPane.showMessageDialog(this, "Vehículo guardado");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Error: la placa ya existe o hubo un problema", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        root.add(header, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(acciones, BorderLayout.SOUTH);
        setContentPane(root);
        UIStyles.applyComponentTheme(getContentPane());
    }
}
