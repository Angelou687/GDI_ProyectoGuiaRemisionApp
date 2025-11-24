package ui;

/*
 * ProductosWindow.java
 * Ventana para listar y gestionar productos. Contiene comentarios por método
 * que describen el propósito de las acciones y de la carga del modelo.
 */

import dao.ProductoDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ProductosWindow extends JFrame {
    private final ProductoDAO dao = new ProductoDAO();

    public ProductosWindow() {
        super("Productos");
        setSize(800, 480);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        // initUI(): construye header, toolbar, tabla y enlaza acciones a botones
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8)); root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setPreferredSize(new Dimension(100,56)); header.setLayout(new BorderLayout()); header.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JLabel lbl = new JLabel("Productos"); lbl.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f)); lbl.setForeground(UIStyles.GREEN_DARK);
        header.add(lbl, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false); btnMenu.addActionListener(e-> UIStyles.showMainMenu(ProductosWindow.this));
        JPanel mp = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); mp.setOpaque(false); mp.add(btnMenu); header.add(mp, BorderLayout.EAST);

        // Table model and table setup (match style used in other windows)
        DefaultTableModel tableModel = new DefaultTableModel(new String[]{"Código", "Nombre", "Precio base", "Unidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane sc = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sc.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));

        // selection background to match other tables
        table.setSelectionBackground(UIStyles.CARD_HOVER);

        JButton btnNew = new JButton("Nuevo"); UIStyles.styleButton(btnNew);
        JButton btnEdit = new JButton("Editar"); UIStyles.styleButton(btnEdit);
        JButton btnDel = new JButton("Eliminar"); UIStyles.styleButton(btnDel);
        JButton btnRefresh = new JButton("Actualizar"); UIStyles.styleButton(btnRefresh);

        // actions toolbar (left-aligned) styled like other windows
        JPanel topToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT)); topToolbar.setOpaque(false);
        Color actionBg = UIStyles.PASTEL_GREEN;
        btnNew.setBackground(actionBg); btnNew.setOpaque(true); btnNew.setForeground(UIStyles.GREEN_DARK); btnNew.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnEdit.setBackground(actionBg); btnEdit.setOpaque(true); btnEdit.setForeground(UIStyles.GREEN_DARK); btnEdit.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnDel.setBackground(actionBg); btnDel.setOpaque(true); btnDel.setForeground(UIStyles.GREEN_DARK); btnDel.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnRefresh.setBackground(actionBg); btnRefresh.setOpaque(true); btnRefresh.setForeground(UIStyles.GREEN_DARK); btnRefresh.putClientProperty("ui.keepStyle", Boolean.TRUE);
        topToolbar.add(btnNew); topToolbar.add(btnEdit); topToolbar.add(btnDel); topToolbar.add(btnRefresh);

        // top area: header (green card) and actions toolbar just below it (use combined panel like ConductoresWindow)
        JPanel topCombined = new JPanel(new BorderLayout()); topCombined.setOpaque(false);
        topCombined.add(header, BorderLayout.NORTH);
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); actionsPanel.setOpaque(false);
        actionsPanel.add(btnNew); actionsPanel.add(btnEdit); actionsPanel.add(btnDel); actionsPanel.add(btnRefresh);
        topCombined.add(actionsPanel, BorderLayout.SOUTH);

        // Card that contains the table (white background like other windows)
        UIStyles.RoundedPanel leftCard = new UIStyles.RoundedPanel(Color.WHITE, UIStyles.CARD_RADIUS);
        leftCard.setLayout(new BorderLayout(6,6)); leftCard.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JLabel lblList = new JLabel("Lista de productos"); lblList.setFont(UIStyles.UI_FONT_BOLD);
        leftCard.add(lblList, BorderLayout.NORTH);
        leftCard.add(sc, BorderLayout.CENTER);

        // Column width preferences
        int[] prefWidths = {120, 320, 120, 100};
        for (int i = 0; i < prefWidths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(prefWidths[i]);
        }
        leftCard.setMinimumSize(new Dimension(480, 200));
        table.setPreferredScrollableViewportSize(new Dimension(760, 360));

        root.add(topCombined, BorderLayout.NORTH);
        root.add(leftCard, BorderLayout.CENTER);

        // Actions (wire buttons)
        // - Nuevo: abre formulario y recarga tabla al cerrar
        btnNew.addActionListener(e -> { FrmProducto f = new FrmProducto(this, null); f.setVisible(true); load(tableModel); });
        // - Editar: abre FrmProducto con el producto seleccionado
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto"); return; }
            String codigo = tableModel.getValueAt(r, 0).toString();
            model.Producto p = null;
            for (model.Producto pp : dao.listarTodos()) if (pp.getCodigoProducto().equals(codigo)) { p = pp; break; }
            if (p == null) { JOptionPane.showMessageDialog(this, "Producto no encontrado"); return; }
            FrmProducto f = new FrmProducto(this, p); f.setVisible(true); load(tableModel);
        });
        // - Eliminar: confirma y llama a dao.eliminar(codigo)
        btnDel.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione un producto"); return; }
            String codigo = tableModel.getValueAt(r, 0).toString();
            int resp = JOptionPane.showConfirmDialog(this, "Eliminar producto " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                if (dao.eliminar(codigo)) { JOptionPane.showMessageDialog(this, "Eliminado"); load(tableModel); }
                else JOptionPane.showMessageDialog(this, "Error al eliminar");
            }
        });
        // - Actualizar: recarga la tabla y limpia selección
        btnRefresh.addActionListener(e -> { load(tableModel); table.clearSelection(); });

        // carga inicial del listado de productos
        load(tableModel);

        // Refrescar al volver de diálogos modales (ventana recupera foco)
        addWindowFocusListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) { load(tableModel); }
        });

        setContentPane(root);
        UIStyles.applyComponentTheme(getContentPane());
    }

    private void load(DefaultTableModel tableModel) {
        // load(): consulta DAO y rellena el modelo con los productos
        tableModel.setRowCount(0);
        java.util.List<model.Producto> lista = dao.listarTodos();
        for (model.Producto p : lista) {
            tableModel.addRow(new Object[]{p.getCodigoProducto(), p.getNombreProducto(), String.format("%.2f", p.getPrecioBase()), p.getUnidadMedida()});
        }
    }
}
