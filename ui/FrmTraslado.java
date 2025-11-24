package ui;

import dao.TrasladoDAO;
import model.Traslado;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.format.DateTimeParseException;

public class FrmTraslado extends JDialog {
    private final TrasladoDAO dao = new TrasladoDAO();
    private final Traslado editing;

    /*
     * FrmTraslado:
     * - Constructor: acepta un `Traslado` opcional para edición.
     * - initUI(): construye campos (código, guía, placa, licencia, fechas, estado, observaciones) y llena combos de placas/licencias/guías.
     * - ok listener: valida campos obligatorios, convierte fechas a `Timestamp`, y llama a `dao.registrarTraslado` o `dao.actualizarTraslado`.
     * - Si `fechaFin` es nula se aplica una estrategia de placeholder para evitar problemas con constraints NOT NULL en algunas DB.
     */

    public FrmTraslado(Window owner, Traslado t) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = t;
        setTitle(t==null?"Nuevo traslado":"Editar traslado");
        setSize(560,520);
        setLocationRelativeTo(owner);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        // Header destacado
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel(editing == null ? "Nuevo traslado" : "Editar traslado");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        // Botón menú en la esquina superior derecha
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(FrmTraslado.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weightx=1.0;

        JTextField txtCod = new JTextField(); JComboBox<String> cbGuia = new JComboBox<>(); JComboBox<String> cbPlaca = new JComboBox<>(); JComboBox<String> cbLic = new JComboBox<>();
        DatePicker dpInicio = new DatePicker(); DatePicker dpFin = new DatePicker();
        // dejar fecha fin vacía por defecto (es opcional cuando se crea un traslado)
        dpFin.setDate(null);
        dpFin.setToolTipText("Fecha fin opcional (dejar en blanco si no aplica)");
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"en tránsito", "confirmado"});
        JTextField txtObs = new JTextField();

        int y=0; gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Código traslado:"), gbc); gbc.gridx=1; formPanel.add(txtCod, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Código guía:"), gbc); gbc.gridx=1; formPanel.add(cbGuia, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Placa:"), gbc); gbc.gridx=1; formPanel.add(cbPlaca, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Licencia:"), gbc); gbc.gridx=1; formPanel.add(cbLic, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Inicio:"), gbc); gbc.gridx=1; formPanel.add(dpInicio, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Fin (opcional):"), gbc); gbc.gridx=1; formPanel.add(dpFin, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Estado:"), gbc); gbc.gridx=1; formPanel.add(cbEstado, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Observaciones:"), gbc); gbc.gridx=1; formPanel.add(txtObs, gbc); y++;

        JButton ok = new JButton(editing==null?"Registrar":"Guardar"); UIStyles.styleButton(ok); JButton cancel = new JButton("Cancelar"); UIStyles.styleButton(cancel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false); actions.add(ok); actions.add(cancel);
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; formPanel.add(actions, gbc);

        // DatePicker handles formatting dd/MM/yyyy

        // populate placas combo
        dao.VehiculoDAO vehDAO = new dao.VehiculoDAO();
        cbPlaca.addItem("");
        for (model.Vehiculo v : vehDAO.listarTodos()) cbPlaca.addItem(v.getPlaca());

        // populate guias combo
        dao.GuiaDAO guiaDAO = new dao.GuiaDAO();
        cbGuia.addItem("");
        for (model.CabeceraGuia g : guiaDAO.listarTodas()) {
            String label = g.getCodigoGuia();
            if (g.getSerie() != null && !g.getSerie().isEmpty()) label += " - " + g.getSerie();
            if (g.getNumero() != null && !g.getNumero().isEmpty()) label += " - " + g.getNumero();
            cbGuia.addItem(label);
        }

        // populate licencias combo
        dao.ConductorDAO conductorDAO = new dao.ConductorDAO();
        cbLic.addItem("");
        for (String l : conductorDAO.listarLicencias()) cbLic.addItem(l);

        if (editing != null) {
            txtCod.setText(editing.getCodigoTraslado()); cbPlaca.setSelectedItem(editing.getPlaca()); cbLic.setSelectedItem(editing.getLicencia());
            // seleccionar guia en combo
            if (editing.getCodigoGuia() != null && !editing.getCodigoGuia().isEmpty()) {
                for (int i = 0; i < cbGuia.getItemCount(); i++) {
                    String it = cbGuia.getItemAt(i);
                    if (it != null && it.startsWith(editing.getCodigoGuia())) { cbGuia.setSelectedIndex(i); break; }
                }
            }
            // mostrar solo fecha en dd/MM/yyyy (si no es null) usando DatePicker
            if (editing.getFechaInicio() != null) {
                dpInicio.setDate(new java.sql.Date(editing.getFechaInicio().getTime()));
            }
            if (editing.getFechaFin() != null) {
                dpFin.setDate(new java.sql.Date(editing.getFechaFin().getTime()));
            }
            cbEstado.setSelectedItem(editing.getEstadoTraslado());
            txtObs.setText(editing.getObservaciones());
        }

        cancel.addActionListener(e-> UIStyles.showMainMenu(FrmTraslado.this));
        ok.addActionListener(e->{
            try {
                String cod = txtCod.getText().trim();
                String guia = "";
                Object selg = cbGuia.getSelectedItem();
                if (selg != null) {
                    String s = selg.toString(); guia = s.contains(" - ") ? s.split(" - ")[0].trim() : s.trim();
                }
                String placa = (String)cbPlaca.getSelectedItem(); if (placa != null) placa = placa.trim(); else placa = ""; String lic = ((String)cbLic.getSelectedItem()); if (lic != null) lic = lic.trim(); else lic = "";
                java.sql.Date iniDate = dpInicio.getDate(); java.sql.Date finDate = dpFin.getDate(); String estado = (String)cbEstado.getSelectedItem(); String obs = txtObs.getText().trim();
                if (cod.isEmpty() || guia.isEmpty() || placa.isEmpty() || lic.isEmpty()) { JOptionPane.showMessageDialog(this,"Campos obligatorios faltantes"); return; }
                if (iniDate == null) { JOptionPane.showMessageDialog(this,"La fecha de inicio es obligatoria"); return; }

                Timestamp tIni = new Timestamp(iniDate.getTime());
                Timestamp tFin;
                if (finDate != null) {
                    tFin = new Timestamp(finDate.getTime());
                    if (tFin.before(tIni)) { JOptionPane.showMessageDialog(this,"Fecha fin debe ser posterior o igual a inicio"); return; }
                } else {
                    // DB has NOT NULL constraint on fecha_fin in some installations.
                    // To allow the UI to show an empty end-date while sending a non-null value,
                    // use the start date as a placeholder. This keeps the value non-null
                    // and avoids violating DB constraints. Later the user can edit the traslado
                    // and set a real fecha_fin.
                    tFin = new Timestamp(tIni.getTime());
                }

                Traslado tt = new Traslado(cod, guia, placa, lic, tIni, tFin, estado, obs);
                if (editing==null) {
                    if (dao.registrarTraslado(tt)) { JOptionPane.showMessageDialog(this,"Registrado"); dispose(); ((TrasladoWindow)getOwner()).load(); }
                    else JOptionPane.showMessageDialog(this,"Error al registrar traslado");
                } else {
                    if (dao.actualizarTraslado(tt)) { JOptionPane.showMessageDialog(this,"Actualizado"); dispose(); ((TrasladoWindow)getOwner()).load(); }
                    else JOptionPane.showMessageDialog(this,"Error al actualizar traslado");
                }
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this,"Formato fecha inválido. Use: dd/MM/yyyy");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this,"Error en datos: " + ex.getMessage());
            }
        });

        root.add(header, BorderLayout.NORTH);
        root.add(formPanel, BorderLayout.CENTER);
        setContentPane(root);
    }
}
