package ui;

/*
 * VehiculosWindow.java
 * Ventana (JFrame) para gestionar vehículos: listar, crear, editar y eliminar.
 * Cada método tiene un comentario breve que indica su propósito.
 */

import model.Vehiculo;
import dao.VehiculoDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class VehiculosWindow extends JFrame {
    private final DefaultTableModel model = new DefaultTableModel(
        new String[]{"Placa", "N° MTC", "Tipo", "Marca", "Modelo", "Carga máx."}, 0);
    private final JTable tabla = new JTable(model);
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();

    public void agregarVehiculo(Vehiculo v) {
        // agregarVehiculo(): añade una fila al modelo con los datos del vehículo
        model.addRow(new Object[]{
            v.getPlaca(),
            v.getNumeroMtc(),
            v.getTipoVehiculo(),
            v.getMarca(),
            v.getModelo(),
            v.getCargaMax()
        });
    }

    private void cargarVehiculos() {
        // cargarVehiculos(): consulta DAO y rellena la tabla
        model.setRowCount(0);
        for (Vehiculo v : vehiculoDAO.listarTodos()) {
            agregarVehiculo(v);
        }
    }

    public VehiculosWindow() {
        // Constructor: configura la ventana, construye la UI y cargas iniciales
        setTitle("Vehículos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        topPanel.setBackground(UIStyles.PANEL);
        UIStyles.RoundedPanel headerPanel = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(100,48));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitulo = new JLabel("Vehículos");
        lblTitulo.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitulo.setForeground(UIStyles.GREEN_DARK);
        headerPanel.add(lblTitulo, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> { dispose(); UIStyles.promptReturnToMenu(this); });
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); rightPanel.setOpaque(false);
        rightPanel.add(btnMenu);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        topPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel toolbarLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        toolbarLeft.setOpaque(false);
            JButton btnNuevo = new JButton("Nuevo"); UIStyles.styleButton(btnNuevo);
            // Nuevo: abre formulario modal; override callback para recargar lista al guardar
            btnNuevo.addActionListener(e -> {
                    FrmVehiculo frm = new FrmVehiculo(this) {
                            @Override
                            public void onVehiculoGuardado(Vehiculo v) {
                                cargarVehiculos();
                            }
                        };
                    frm.setVisible(true);
                });
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        // Editar: obtiene la placa de la fila seleccionada, busca el vehículo y abre FrmVehiculo para editar
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla");
                return;
            }
            String placa = (String) model.getValueAt(fila, 0);
            Vehiculo v = vehiculoDAO.buscarPorPlaca(placa);
            if (v == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el vehículo en la base de datos");
                return;
            }
            FrmVehiculo frm = new FrmVehiculo(this, v) {
                @Override
                public void onVehiculoGuardado(Vehiculo vw) {
                    cargarVehiculos();
                }
            };
            frm.setVisible(true);
        });
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        // Eliminar: confirma y llama a DAO.eliminar(placa). Si OK recarga tabla
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un vehículo de la tabla");
                return;
            }
            String placa = (String) model.getValueAt(fila, 0);
            int resp = JOptionPane.showConfirmDialog(this,
                    "¿Seguro que desea eliminar el vehículo con placa " + placa + "?",
                    "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                boolean ok = vehiculoDAO.eliminar(placa);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Vehículo eliminado");
                    cargarVehiculos();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al eliminar vehículo", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        JButton btnActualizar = new JButton("Actualizar"); UIStyles.styleButton(btnActualizar);
        // Actualizar: recarga lista desde DAO
        btnActualizar.addActionListener(e -> cargarVehiculos());
        toolbarLeft.add(btnNuevo); toolbarLeft.add(btnEditar); toolbarLeft.add(btnEliminar); toolbarLeft.add(btnActualizar);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(toolbarLeft, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        tabla.setFillsViewportHeight(true);
        tabla.setRowHeight(28);
        tabla.setSelectionBackground(UIStyles.CARD_HOVER);
        tabla.setSelectionForeground(UIStyles.TEXT_MAIN);
        tabla.getTableHeader().setFont(UIStyles.UI_FONT_BOLD);
        tabla.getTableHeader().setBackground(Color.WHITE);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));
        add(scroll, BorderLayout.CENTER);
        // carga inicial de vehículos al abrir la ventana
        cargarVehiculos();
    }
}
