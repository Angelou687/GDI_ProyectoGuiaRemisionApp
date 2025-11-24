package ui;

import dao.DetalleOrdenDAO;
import dao.OrdenDAO;
import dao.DestinatarioDAO;
import dao.ProductoDAO;
import model.DetalleOrden;
import model.Destinatario;
import model.OrdenDePago;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
// date handled via ui.DatePicker
// no LocalDate/DateTimeFormatter needed; using spinner for dates

public class FrmOrden extends JDialog {
    private final OrdenDePago editing;

    public FrmOrden(Window owner) { this(owner, null); }

    public void onOrdenGuardada(OrdenDePago o) {
        // override in parent
    }

    public FrmOrden(Window owner, OrdenDePago editar) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = editar;
        setTitle(editar == null ? "Nueva orden de pago" : "Editar orden");
        // Increase default dialog size so the details table and form fields fit comfortably
        setSize(900, 700);
        // Provide a reasonable minimum size to avoid collapsing the table
        setMinimumSize(new Dimension(700, 480));
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lbl = new JLabel(editar == null ? "Nueva orden de pago" : "Editar orden");
        lbl.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f)); lbl.setForeground(UIStyles.GREEN_DARK);
        header.add(lbl, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(FrmOrden.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); menuPanel.setOpaque(false); menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(8,8,8,8); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        int y=0;
        JTextField txtCodigo = new JTextField();
        ui.DatePicker datePicker = new ui.DatePicker();
        JComboBox<String> cbRuc = new JComboBox<>();
        JComboBox<String> cbEstado = new JComboBox<>(new String[]{"Pendiente", "Emitida"});

        // load RUCs from destinatarios
        try {
            DestinatarioDAO ddao = new DestinatarioDAO();
            List<Destinatario> list = ddao.listarTodos();
            cbRuc.addItem("");
            for (Destinatario d : list) cbRuc.addItem(d.getRuc());
        } catch (Exception ex) {
            System.out.println("Error cargando destinatarios: " + ex.getMessage());
        }

        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Código orden*:"), gbc); gbc.gridx=1; form.add(txtCodigo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Fecha (DD/MM/YYYY):"), gbc); gbc.gridx=1; form.add(datePicker, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("RUC cliente:"), gbc); gbc.gridx=1; form.add(cbRuc, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Estado:"), gbc); gbc.gridx=1; form.add(cbEstado, gbc); y++;

        if (editar != null) {
            txtCodigo.setText(editar.getCodigoOrden()); txtCodigo.setEnabled(false);
            if (editar.getFecha() != null) {
                datePicker.setDate(editar.getFecha());
            }
            if (editar.getRucCliente() != null) cbRuc.setSelectedItem(editar.getRucCliente());
            if (editar.getEstado() != null) cbEstado.setSelectedItem(editar.getEstado());
        }

        // Details UI: products selector, cantidad, precio and table of detail lines
        ProductoDAO productoDAO = new ProductoDAO();
        DetalleOrdenDAO detalleDAO = new DetalleOrdenDAO();

        JPanel detailsPanel = new JPanel(new BorderLayout(8,8)); detailsPanel.setOpaque(false);

        // top add form for a detail line
        JPanel addLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6)); addLine.setOpaque(false);
        JComboBox<String> cbProducto = new JComboBox<>(); cbProducto.addItem("");
        try {
            for (model.Producto p : productoDAO.listarTodos()) cbProducto.addItem(p.getCodigoProducto() + " - " + p.getNombreProducto());
        } catch (Exception ex) { System.err.println("Error cargando productos: " + ex.getMessage()); }
        JTextField txtCantidad = new JTextField("1", 6);
        JTextField txtPrecio = new JTextField(8);
        JButton btnAgregar = new JButton("Agregar"); UIStyles.styleButton(btnAgregar);
        JButton btnEliminarLinea = new JButton("Eliminar línea"); UIStyles.styleButton(btnEliminarLinea);
        addLine.add(new JLabel("Producto:")); addLine.add(cbProducto);
        addLine.add(new JLabel("Cantidad:")); addLine.add(txtCantidad);
        addLine.add(new JLabel("Precio unit.:")); addLine.add(txtPrecio);
        addLine.add(btnAgregar); addLine.add(btnEliminarLinea);

        // details table
        DefaultTableModel detailsModel = new DefaultTableModel(new String[]{"Código","Nombre","Cantidad","Precio unit.","Subtotal"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tblDetails = new JTable(detailsModel); tblDetails.setFillsViewportHeight(true); tblDetails.setRowHeight(24);
        tblDetails.getTableHeader().setFont(UIStyles.UI_FONT_BOLD);
        JScrollPane spDetails = new JScrollPane(tblDetails); spDetails.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));

        detailsPanel.add(addLine, BorderLayout.NORTH); detailsPanel.add(spDetails, BorderLayout.CENTER);

        // wire product selection to prefill price
        cbProducto.addActionListener(e -> {
            String sel = (String) cbProducto.getSelectedItem();
            if (sel == null || sel.trim().isEmpty()) { txtPrecio.setText(""); return; }
            String code = sel.split(" - ")[0];
            model.Producto prod = productoDAO.listarTodos().stream().filter(p->p.getCodigoProducto().equals(code)).findFirst().orElse(null);
            if (prod != null) txtPrecio.setText(String.format("%.2f", prod.getPrecioBase()));
        });

        btnAgregar.addActionListener(e -> {
            String sel = (String) cbProducto.getSelectedItem(); if (sel == null || sel.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Seleccione un producto"); return; }
            String code = sel.split(" - ")[0]; String name = sel.contains(" - ") ? sel.substring(sel.indexOf(" - ")+3) : "";
            String qstr = txtCantidad.getText().trim(); String pstr = txtPrecio.getText().trim();
            BigDecimal q; BigDecimal pu;
            try { q = new BigDecimal(qstr.replace(',', '.')); if (q.compareTo(BigDecimal.ZERO) < 0) { JOptionPane.showMessageDialog(this, "Cantidad inválida"); return; } }
            catch (HeadlessException ex) { JOptionPane.showMessageDialog(this, "Cantidad inválida"); return; }
            try { pu = new BigDecimal(pstr.replace(',', '.')); if (pu.compareTo(BigDecimal.ZERO) < 0) { JOptionPane.showMessageDialog(this, "Precio inválido"); return; } }
            catch (HeadlessException ex) { JOptionPane.showMessageDialog(this, "Precio inválido"); return; }
            BigDecimal subtotal = q.multiply(pu);
            detailsModel.addRow(new Object[]{code, name, q, pu, subtotal});
        });

        btnEliminarLinea.addActionListener(e -> {
            int r = tblDetails.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila"); return; }
            detailsModel.removeRow(r);
        });

        // place form and details into a center container
        JPanel centerContainer = new JPanel(new BorderLayout(8,8)); centerContainer.setOpaque(false);
        centerContainer.add(form, BorderLayout.NORTH);
        centerContainer.add(detailsPanel, BorderLayout.CENTER);

        JButton btnGuardar = new JButton(editar == null ? "Guardar" : "Actualizar"); UIStyles.styleButton(btnGuardar);
        JButton btnCancelar = new JButton("Cancelar"); UIStyles.styleButton(btnCancelar);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8)); acciones.setOpaque(false); acciones.add(btnGuardar); acciones.add(btnCancelar);
        btnCancelar.addActionListener(e -> dispose());

        btnGuardar.addActionListener(e -> {
            String codigo = txtCodigo.getText().trim();
            if (codigo.isEmpty()) { JOptionPane.showMessageDialog(this, "Código de orden es obligatorio"); return; }
            java.sql.Date fecha = datePicker.getDate();
            String ruc = ""; Object sel = cbRuc.getSelectedItem(); if (sel != null) ruc = sel.toString().trim();
            String estado = "Pendiente"; Object s2 = cbEstado.getSelectedItem(); if (s2 != null) estado = s2.toString().trim();
            OrdenDePago o = new OrdenDePago(codigo, fecha, ruc, estado);
            OrdenDAO dao = new OrdenDAO();
            boolean ok;
            if (editing == null) ok = dao.crear(o); else ok = dao.actualizar(o);
            if (!ok) { JOptionPane.showMessageDialog(this, "Error: no se pudo guardar la orden", "Error", JOptionPane.ERROR_MESSAGE); return; }

            // persist detalles: delete existing and re-insert
            detalleDAO.eliminarPorOrden(codigo);
            boolean allOk = true;
            for (int i = 0; i < detailsModel.getRowCount(); i++) {
                String codProd = detailsModel.getValueAt(i, 0).toString();
                BigDecimal cantidad = new BigDecimal(detailsModel.getValueAt(i, 2).toString());
                BigDecimal precioUnit = new BigDecimal(detailsModel.getValueAt(i, 3).toString());
                DetalleOrden d = new DetalleOrden(codigo, codProd, cantidad, precioUnit);
                if (!detalleDAO.agregarDetalle(d)) { allOk = false; }
            }

            onOrdenGuardada(o);
            JOptionPane.showMessageDialog(this, "Orden guardada" + (allOk ? "" : " (con errores en detalles)"));
            dispose();
        });

        root.add(header, BorderLayout.NORTH);
        // make center area scrollable so long forms/details fit smaller screens
        JScrollPane centerScroll = new JScrollPane(centerContainer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        centerScroll.getViewport().setBackground(UIStyles.PANEL);
        centerScroll.setBorder(null);
        root.add(centerScroll, BorderLayout.CENTER);
        root.add(acciones, BorderLayout.SOUTH);
        setContentPane(root); UIStyles.applyComponentTheme(getContentPane());

        // if editing, load existing details
        if (editing != null) {
            DefaultTableModel dm = (DefaultTableModel) detailsModel;
            DefaultTableModel fromDb = detalleDAO.listarModeloTablaPorOrden(editing.getCodigoOrden());
            dm.setRowCount(0);
            for (int i = 0; i < fromDb.getRowCount(); i++) {
                dm.addRow(new Object[]{fromDb.getValueAt(i,0), fromDb.getValueAt(i,1), fromDb.getValueAt(i,2), fromDb.getValueAt(i,3), fromDb.getValueAt(i,4)});
            }
        }
    }
}
