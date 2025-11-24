package ui;

import dao.GuiaDAO;
import dao.DestinatarioDAO;
import dao.OrdenDAO;
import dao.UbigeoDAO;
import model.CabeceraGuia;
import model.Destinatario;
import model.OrdenDePago;
import model.Ubigeo;
import util.Session;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class FrmGuia extends JDialog {
    private final GuiaDAO guiaDAO = new GuiaDAO();
    private final DestinatarioDAO ddao = new DestinatarioDAO();
    private CabeceraGuia editing;

    /*
     * FrmGuia:
     * - Constructor: recibe un `CabeceraGuia` opcional para edición; configura tamaño y llama a `initUI()`.
     * - initUI(): construye el formulario completo (campos de cabecera, combos de órdenes/destinatarios/ubigeo),
     *   precarga valores si `editing != null` y adjunta validaciones y el listener de guardado que llama a `guiaDAO.emitirGuia(...)`.
     * - El listener de btnOk realiza validaciones (campos obligatorios, parseo numérico) y muestra mensajes al usuario.
     */

    public FrmGuia(Window owner, CabeceraGuia g) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = g;
        setTitle(g==null?"Emitir guía":"Editar guía");
        setSize(650,700); // más alto y ancho para mejor visibilidad
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
        JLabel lblTitle = new JLabel(editing == null ? "Nueva guía" : "Editar guía");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        // Botón cerrar en la esquina superior derecha
        JButton btnCerrar = new JButton("Cerrar");
        UIStyles.styleButton(btnCerrar);
        btnCerrar.setFocusable(false);
        btnCerrar.addActionListener(e -> dispose());
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        closePanel.setOpaque(false);
        closePanel.add(btnCerrar);
        header.add(closePanel, BorderLayout.EAST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(6,6,6,6); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx=1.0;

        JTextField txtCodigo = new JTextField();
        JTextField txtSerie  = new JTextField();
        JTextField txtNumero = new JTextField();
        JComboBox<String> cbOrdenes = new JComboBox<>();
        JTextField txtRucRemitente = new JTextField();
        JComboBox<String> cbDestinatario = new JComboBox<>();
        JTextField txtDirPartida = new JTextField();
        JTextField txtDirLlegada = new JTextField();
        JComboBox<String> cbUbigeoOrigen = new JComboBox<>();
        JComboBox<String> cbUbigeoDestino = new JComboBox<>();
        JTextField txtPesoTotal = new JTextField("0.0");
        JTextField txtNumBultos = new JTextField("0");

        // Cambié nombre del botón para evitar conflicto con variable local 'ok' dentro del listener
        JButton btnOk = new JButton(editing==null?"Emitir":"Guardar"); UIStyles.styleButton(btnOk);
        JButton btnCancel  = new JButton("Cancelar"); UIStyles.styleButton(btnCancel);

        int y=0;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Código:"), gbc); gbc.gridx=1; formPanel.add(txtCodigo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Serie:"), gbc); gbc.gridx=1; formPanel.add(txtSerie, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Número:"), gbc); gbc.gridx=1; formPanel.add(txtNumero, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Código orden:"), gbc); gbc.gridx=1; formPanel.add(cbOrdenes, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("RUC remitente:"), gbc); gbc.gridx=1; formPanel.add(txtRucRemitente, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Destinatario:"), gbc); gbc.gridx=1; formPanel.add(cbDestinatario, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Dir. partida:"), gbc); gbc.gridx=1; formPanel.add(txtDirPartida, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Dir. llegada:"), gbc); gbc.gridx=1; formPanel.add(txtDirLlegada, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Ubigeo origen:"), gbc); gbc.gridx=1; formPanel.add(cbUbigeoOrigen, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Ubigeo destino:"), gbc); gbc.gridx=1; formPanel.add(cbUbigeoDestino, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Peso total:"), gbc); gbc.gridx=1; formPanel.add(txtPesoTotal, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; formPanel.add(new JLabel("Número bultos:"), gbc); gbc.gridx=1; formPanel.add(txtNumBultos, gbc); y++;

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false); actions.add(btnOk); actions.add(btnCancel);
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; formPanel.add(actions, gbc);

        // cargar destinatarios en el combo (RUC - NOMBRE)
        List<Destinatario> dests = ddao.listarTodos();
        for (Destinatario d : dests) cbDestinatario.addItem(d.getRuc() + " - " + d.getNombre());

        // cargar órdenes en el combo (código - fecha - ruc)
        OrdenDAO odao = new OrdenDAO();
        cbOrdenes.addItem("");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy");
        for (OrdenDePago o : odao.listarTodos()) {
            String dateStr = "";
            try { if (o.getFecha() != null) dateStr = fmt.format(o.getFecha()); } catch (Exception ignore) {}
            cbOrdenes.addItem(o.getCodigoOrden() + (dateStr.isEmpty() ? "" : " - " + dateStr) + (o.getRucCliente()!=null && !o.getRucCliente().isEmpty() ? " - " + o.getRucCliente() : ""));
        }

        // cargar ubigeos en los combos
        UbigeoDAO udao = new UbigeoDAO();
        cbUbigeoOrigen.addItem(""); cbUbigeoDestino.addItem("");
        for (Ubigeo u : udao.listarTodos()) {
            String label = u.getCodigo() + " - " + u.getDepartamento() + "/" + u.getProvincia() + "/" + u.getDistrito();
            cbUbigeoOrigen.addItem(label);
            cbUbigeoDestino.addItem(label);
        }

        // Si venimos con 'editing' (objeto pasado desde la lista) recargamos la versión completa desde la BD
        if (editing != null) {
            CabeceraGuia full = guiaDAO.obtenerPorCodigo(editing.getCodigoGuia());
            if (full != null) {
                this.editing = full; // sustituir por la versión completa
            }
        }

        // si estamos editando, poblar los campos desde la CabeceraGuia actualizada
        if (editing != null) {
            txtCodigo.setText(editing.getCodigoGuia());
            txtCodigo.setEnabled(false); // no permitir cambiar PK
            txtSerie.setText(editing.getSerie() == null ? "" : editing.getSerie());
            txtNumero.setText(editing.getNumero() == null ? "" : editing.getNumero());
            // seleccionar orden en combo si existe
            if (editing.getCodOrden() != null && !editing.getCodOrden().isEmpty()) {
                for (int i = 0; i < cbOrdenes.getItemCount(); i++) {
                    String it = cbOrdenes.getItemAt(i);
                    if (it != null && it.startsWith(editing.getCodOrden())) { cbOrdenes.setSelectedIndex(i); break; }
                }
            }
            txtRucRemitente.setText(editing.getRucRemitente() == null ? "" : editing.getRucRemitente());
            txtDirPartida.setText(editing.getDirPartida() == null ? "" : editing.getDirPartida());
            txtDirLlegada.setText(editing.getDirLlegada() == null ? "" : editing.getDirLlegada());
            if (editing.getUbigeoOrigen() != null && !editing.getUbigeoOrigen().isEmpty()) {
                for (int i = 0; i < cbUbigeoOrigen.getItemCount(); i++) {
                    String it = cbUbigeoOrigen.getItemAt(i);
                    if (it != null && it.startsWith(editing.getUbigeoOrigen())) { cbUbigeoOrigen.setSelectedIndex(i); break; }
                }
            }
            if (editing.getUbigeoDestino() != null && !editing.getUbigeoDestino().isEmpty()) {
                for (int i = 0; i < cbUbigeoDestino.getItemCount(); i++) {
                    String it = cbUbigeoDestino.getItemAt(i);
                    if (it != null && it.startsWith(editing.getUbigeoDestino())) { cbUbigeoDestino.setSelectedIndex(i); break; }
                }
            }
            txtPesoTotal.setText(editing.getPesoTotal() == null ? "0.0" : String.valueOf(editing.getPesoTotal()));
            txtNumBultos.setText(editing.getNumeroBultos() == null ? "0" : String.valueOf(editing.getNumeroBultos()));
            // seleccionar destinatario en el combo por RUC si existe
            if (editing.getRucDestinatario() != null) {
                for (int i = 0; i < cbDestinatario.getItemCount(); i++) {
                    String item = cbDestinatario.getItemAt(i);
                    if (item != null && item.startsWith(editing.getRucDestinatario())) {
                        cbDestinatario.setSelectedIndex(i);
                        break;
                    }
                }
            }
        }

        // si es nueva guía y hay un remitente logeado, prellenar su RUC
        if (editing == null) {
            String logged = Session.getCurrentRuc();
            if (logged != null && !logged.isEmpty()) {
                txtRucRemitente.setText(logged);
            }
        }

        btnCancel.addActionListener(e-> UIStyles.showMainMenu(FrmGuia.this));
        btnOk.addActionListener(e->{
            // basic validations
            if (txtCodigo.getText().trim().isEmpty() || txtSerie.getText().trim().isEmpty() || txtNumero.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(this,"Complete los campos obligatorios"); return; }
            try {
                String codigo = txtCodigo.getText().trim();
                String serie  = txtSerie.getText().trim();
                String numero = txtNumero.getText().trim();
                String codOrden = "";
                Object selOrd = cbOrdenes.getSelectedItem();
                if (selOrd != null) {
                    String s = selOrd.toString();
                    if (s.contains(" - ")) codOrden = s.split(" - ")[0].trim(); else codOrden = s.trim();
                }
                String rucRemitente = txtRucRemitente.getText().trim(); // ahora tomado del form

                // destinatario: esperamos "RUC - NOMBRE" en el combo; si no, usar el texto completo
                String rucDest = "";
                Object sel = cbDestinatario.getSelectedItem();
                if (sel != null) {
                    String s = sel.toString();
                    if (s.contains(" - ")) rucDest = s.split(" - ")[0].trim();
                    else rucDest = s.trim();
                }
                String dirPartida = txtDirPartida.getText().trim();
                String dirLlegada = txtDirLlegada.getText().trim();
                String ubigeoOrigen = "";
                Object s1 = cbUbigeoOrigen.getSelectedItem();
                if (s1 != null) { String ss = s1.toString(); ubigeoOrigen = ss.contains(" - ") ? ss.split(" - ")[0].trim() : ss.trim(); }
                String ubigeoDestino = "";
                Object s2 = cbUbigeoDestino.getSelectedItem();
                if (s2 != null) { String ss = s2.toString(); ubigeoDestino = ss.contains(" - ") ? ss.split(" - ")[0].trim() : ss.trim(); }
                double pesoTotal = 0.0;
                int numeroBultos = 0;
                try { pesoTotal = Double.parseDouble(txtPesoTotal.getText().trim()); } catch (NumberFormatException ex) { /* deja 0.0 */ }
                try { numeroBultos = Integer.parseInt(txtNumBultos.getText().trim()); } catch (NumberFormatException ex) { /* deja 0 */ }

                // llamada a emitirGuia: pasamos codOrden y rucRemitente desde el formulario
                boolean success = guiaDAO.emitirGuia(
                        codigo, serie, numero, codOrden.isEmpty() ? null : codOrden,
                        rucRemitente.isEmpty() ? null : rucRemitente,
                        rucDest.isEmpty() ? null : rucDest,
                        dirPartida, dirLlegada,
                        ubigeoOrigen, ubigeoDestino,
                        /*motivo*/ "", /*modalidad*/ "",
                        pesoTotal, numeroBultos
                );

                if (success) {
                    JOptionPane.showMessageDialog(this, "Guía emitida correctamente");
                    dispose(); // cerrar ventana al terminar
                } else {
                    JOptionPane.showMessageDialog(this, "Error al emitir la guía. Revisa logs.");
                }
            } catch (HeadlessException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        // Hacer el formulario scrolleable
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        root.add(header, BorderLayout.NORTH);
        root.add(scrollPane, BorderLayout.CENTER);
        setContentPane(root);
    }
}
