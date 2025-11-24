package ui;

/*
 * OrdenesWindow.java
 * Ventana modal para listar y gestionar órdenes de pago. Contiene comentarios
 * por método: initUI, listeners y helpers para recargar modelos.
 */

import dao.OrdenDAO;
import model.OrdenDePago;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
// import java.util.List; (unused)

public class OrdenesWindow extends JDialog {
    private final OrdenDAO ordenDAO = new OrdenDAO();

    public OrdenesWindow(Frame owner) {
        // Constructor: inicializa la ventana modal y construye la UI
        super(owner, true);
        setTitle("Ordenes de pago");
        setSize(800, 460);
        setLocationRelativeTo(owner);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        // initUI(): construye header, toolbar, crea la tabla con `ordenDAO.listarModeloTabla()`
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8)); root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout()); header.setPreferredSize(new Dimension(100,48)); header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel("Órdenes"); lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f)); lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false); btnMenu.addActionListener(e -> UIStyles.showMainMenu(OrdenesWindow.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); menuPanel.setOpaque(false); menuPanel.add(btnMenu); header.add(menuPanel, BorderLayout.EAST);

        // Top panel: header + toolbar similar al estilo de VehiculosWindow
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        topPanel.setBackground(UIStyles.PANEL);
        topPanel.add(header, BorderLayout.NORTH);

        JPanel toolbarLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbarLeft.setOpaque(false);
        JButton btnNuevo = new JButton("Nuevo"); UIStyles.styleButton(btnNuevo);
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        JButton btnDetalle = new JButton("Detalle"); UIStyles.styleButton(btnDetalle);
        JButton btnActualizar = new JButton("Actualizar"); UIStyles.styleButton(btnActualizar);
        toolbarLeft.add(btnNuevo); toolbarLeft.add(btnEditar); toolbarLeft.add(btnEliminar); toolbarLeft.add(btnDetalle); toolbarLeft.add(btnActualizar);

        JPanel toolbar = new JPanel(new BorderLayout()); toolbar.setOpaque(false);
        toolbar.add(toolbarLeft, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.CENTER);

        DefaultTableModel model = ordenDAO.listarModeloTabla();
        JTable tabla = new JTable(model);
        // aplicar estilo consistente con otras tablas
        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(28);
        tabla.setSelectionBackground(UIStyles.CARD_HOVER);
        tabla.setSelectionForeground(UIStyles.TEXT_MAIN);
        tabla.getTableHeader().setFont(UIStyles.UI_FONT_BOLD);
        tabla.getTableHeader().setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));

        root.add(topPanel, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        // Nuevo: abre FrmOrden y recarga la lista cuando el modal notifica via callback
        btnNuevo.addActionListener(e -> {
            FrmOrden f = new FrmOrden(this) {
                @Override
                public void onOrdenGuardada(model.OrdenDePago o) {
                    cargarOrdenes(tabla);
                }
            };
            f.setVisible(true);
        });

        // Editar: abre FrmOrden con la orden seleccionada (buscada por código)
        btnEditar.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una orden"); return; }
            String codigo = (String) tabla.getValueAt(r, 0);
            OrdenDePago o = ordenDAO.buscarPorCodigo(codigo);
            if (o == null) { JOptionPane.showMessageDialog(this, "Orden no encontrada"); return; }
            FrmOrden f = new FrmOrden(this, o) {
                @Override
                public void onOrdenGuardada(model.OrdenDePago ord) {
                    cargarOrdenes(tabla);
                }
            };
            f.setVisible(true);
        });

        // Eliminar: confirma y borra la orden seleccionada; recarga tabla si ok
        btnEliminar.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una orden"); return; }
            String codigo = (String) tabla.getValueAt(r, 0);
            int op = JOptionPane.showConfirmDialog(this, "¿Eliminar orden " + codigo + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (op == JOptionPane.YES_OPTION) {
                if (ordenDAO.eliminar(codigo)) { cargarOrdenes(tabla); JOptionPane.showMessageDialog(this, "Orden eliminada"); }
                else JOptionPane.showMessageDialog(this, "No se pudo eliminar la orden", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Detalle: abre un diálogo modal con el modelo de detalle de orden (usa DetalleOrdenDAO)
        btnDetalle.addActionListener(e -> {
            int r = tabla.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una orden"); return; }
            String codigo = (String) tabla.getValueAt(r, 0);
            dao.DetalleOrdenDAO ddao = new dao.DetalleOrdenDAO();
            DefaultTableModel dm = ddao.listarModeloTablaPorOrden(codigo);
            JTable t = new JTable(dm);
            t.setFillsViewportHeight(true); t.setRowHeight(24); t.getTableHeader().setFont(UIStyles.UI_FONT_BOLD);
            JScrollPane sp = new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));
            JDialog dlg = new JDialog(this, "Detalle de orden " + codigo, true);
            ui.UIStyles.RoundedPanel rp = new ui.UIStyles.RoundedPanel(Color.WHITE, UIStyles.CARD_RADIUS);
            rp.setLayout(new BorderLayout(8,8)); rp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            rp.add(sp, BorderLayout.CENTER);
            dlg.add(rp); dlg.setSize(560, 360); dlg.setLocationRelativeTo(this); UIStyles.applyComponentTheme(dlg.getContentPane()); dlg.setVisible(true);
        });

        btnActualizar.addActionListener(e -> cargarOrdenes(tabla));

        add(root);
    }

    private void cargarOrdenes(JTable tabla) {
        // cargarOrdenes(): solicita al DAO el modelo de tabla y lo asigna al JTable
        DefaultTableModel m = ordenDAO.listarModeloTabla();
        tabla.setModel(m);
    }
}
