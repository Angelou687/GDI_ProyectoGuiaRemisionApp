package ui;

/*
 * ReportesWindow.java
 * Ventana para ejecutar reportes predefinidos mediante SP/consultas.
 * Cada bloque de acción y método contiene un comentario que explica su propósito.
 */

import dao.ReporteDAO;
import dao.DetalleOrdenDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ReportesWindow extends JDialog {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public ReportesWindow(Frame owner) {
        // Constructor: crea la ventana modal y configura UI
        super(owner, true);
        setTitle("Reportes");
        setSize(900, 500);
        setLocationRelativeTo(owner);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        // initUI(): construye controles para seleccionar reportes, parámetros y mostrar resultados
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));

        // Header visual pastel y fuente elegante, igual que en FrmDestinatario
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel("Reportes");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);

        // Botón menú en la esquina superior derecha
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(ReportesWindow.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(header);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] opciones = {
            "1) Detalle de una orden (JOIN)",
            "2) Guías por fecha y estado",
            "3) Productos más vendidos (JOIN)",
            "4) Utilización de vehículos (JOIN)",
            "5) Licencias por vencer (JOIN)",
            "6) Guías sin traslado (JOIN)",
            "7) Bultos por cliente 90d (JOIN)",
            "8) KPI diario de guías (JOIN)",
            "9) Clientes sin compras 60d (JOIN)",
            "10) Traslados con vehículo y guía (JOIN)"
        };
        JComboBox<String> cbReporte = new JComboBox<>(opciones);
        JButton btnEjecutar = new JButton("Ejecutar reporte");
        JTextField txtParametro = new JTextField(10);
        // Combo para órdenes (para el reporte 1)
        JComboBox<String> cbOrdenes = new JComboBox<>();
        JLabel lblParam = new JLabel("Parámetro (según reporte):");

        top.add(new JLabel("Reporte:"));
        top.add(cbReporte);
        top.add(lblParam);
        top.add(txtParametro);
        top.add(cbOrdenes);
        cbOrdenes.setVisible(false); // solo visible para reporte 1
        top.add(btnEjecutar);

        // Cargar órdenes desde la base de datos para el combo (se ejecuta en EDT tras iniciar UI)
        SwingUtilities.invokeLater(() -> {
            try (java.sql.Connection cn = db.Conexion.getConnection();
                 java.sql.PreparedStatement ps = cn.prepareStatement("SELECT codigo_orden FROM orden_de_pago ORDER BY fecha DESC");
                 java.sql.ResultSet rs = ps.executeQuery()) {
                cbOrdenes.addItem(""); // opción vacía
                while (rs.next()) {
                    cbOrdenes.addItem(rs.getString("codigo_orden"));
                }
            } catch (java.sql.SQLException ex) {
                System.err.println("No se pudieron cargar órdenes: " + ex.getMessage());
            }
        });

        // Cambiar visibilidad del campo de parámetro según reporte seleccionado
        cbReporte.addActionListener(e -> {
            int sel = cbReporte.getSelectedIndex();
            if (sel == 0) { // detalle de una orden -> mostrar combo de órdenes
                txtParametro.setVisible(false);
                cbOrdenes.setVisible(true);
                lblParam.setText("Orden:");
            } else {
                txtParametro.setVisible(true);
                cbOrdenes.setVisible(false);
                lblParam.setText("Parámetro (según reporte):");
            }
            top.revalidate(); top.repaint();
        });

        // Ajustar visibilidad inicial según opción seleccionada por defecto
        if (cbReporte.getSelectedIndex() == 0) {
            txtParametro.setVisible(false);
            cbOrdenes.setVisible(true);
            lblParam.setText("Orden:");
        }

        // Tabla de resultados (modelo vacío inicialmente)
        DefaultTableModel model = new DefaultTableModel();
        JTable tabla = new JTable(model);
        JScrollPane scroll = new JScrollPane(tabla);

        JPanel topWrapper = new JPanel(new BorderLayout()); topWrapper.setOpaque(false);
        topWrapper.add(top, BorderLayout.WEST);
        northContainer.add(topWrapper);
        root.add(northContainer, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        // Ejecutar reporte según selección: llama a SPs o consultas y asigna el modelo a la tabla
        btnEjecutar.addActionListener(e -> {
            int idx = cbReporte.getSelectedIndex();
            DefaultTableModel tm;
            switch (idx) {
                case 0 -> {
                    // Reporte 1: detalle de orden -> usar DetalleOrdenDAO para obtener modelo
                    String codOrden = (String) cbOrdenes.getSelectedItem();
                    if (codOrden == null || codOrden.trim().isEmpty()) { JOptionPane.showMessageDialog(this, "Seleccione una orden"); return; }
                    DetalleOrdenDAO detDao = new DetalleOrdenDAO();
                    tm = detDao.listarModeloTablaPorOrden(codOrden.trim());
                }
                case 1 -> tm = reporteDAO.ejecutarSP("sp_reporte_guias_por_fecha_estado()");
                case 2 -> tm = reporteDAO.ejecutarSP("sp_reporte_productos_mas_vendidos()");
                case 3 -> tm = reporteDAO.ejecutarSP("sp_reporte_utilizacion_vehiculos()");
                case 4 -> {
                    // Reporte de licencias por vencer: acepta parámetro días (default 90)
                    String dias = txtParametro.getText().trim(); if (dias.isEmpty()) dias = "90";
                    tm = reporteDAO.ejecutarSPConParametro("sp_reporte_licencias_por_vencer", dias);
                }
                case 5 -> tm = reporteDAO.ejecutarSP("sp_reporte_guias_sin_traslado()");
                case 6 -> tm = reporteDAO.ejecutarSP("sp_reporte_bultos_por_cliente_90d()");
                case 7 -> tm = reporteDAO.ejecutarSP("sp_reporte_kpi_guias_diario()");
                case 8 -> tm = reporteDAO.ejecutarConsulta(
                        "SELECT d.ruc, d.nombre " +
                            "FROM destinatario d " +
                            "WHERE d.ruc NOT IN ( " +
                            "  SELECT o.ruc_cliente " +
                            "  FROM orden_de_pago o " +
                            "  WHERE o.fecha >= (current_date - INTERVAL '60 days') " +
                            ") " +
                            "ORDER BY d.nombre"
                    );
                case 9 -> tm = reporteDAO.ejecutarSP("sp_listar_traslados()");
                default -> tm = new DefaultTableModel();
            }
            tabla.setModel(tm);
        });

        // Menu button
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(ReportesWindow.this));

        add(root);
    }
}
