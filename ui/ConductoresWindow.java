package ui;

/*
 * ConductoresWindow.java
 * Ventana modal para gestionar conductores: listar, crear, editar, eliminar.
 * Comentarios por método y por bloques de UI y acciones.
 */

import dao.ConductorDAO;
import model.Conductor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.util.List;

public class ConductoresWindow extends JDialog {

    private final ConductorDAO dao = new ConductorDAO();

    public ConductoresWindow(Window owner) {
        // Constructor
        // - Crea la ventana modal con tamaño y posición
        // - Construye la UI y aplica estilos
        super(owner, "Conductores", ModalityType.APPLICATION_MODAL);
        setSize(760, 420);
        setLocationRelativeTo(owner);

        ui.UIStyles.RoundedPanel root = new ui.UIStyles.RoundedPanel(ui.UIStyles.BG, ui.UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        ui.UIStyles.RoundedPanel header = new ui.UIStyles.RoundedPanel(ui.UIStyles.PASTEL_GREEN, ui.UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout()); header.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JLabel lbl = new JLabel("Conductores"); lbl.setFont(ui.UIStyles.UI_FONT_BOLD.deriveFont(16f)); lbl.setForeground(ui.UIStyles.GREEN_DARK);
        header.add(lbl, BorderLayout.WEST);

        // Botón Menú en el encabezado que cierra la ventana
        JButton btnMenu = new JButton("Menú"); ui.UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> dispose());
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Licencia","DNI","Nombre","Tel","Venc. Licencia"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable tabla = new JTable(model); tabla.setRowHeight(26);
        tabla.setSelectionBackground(ui.UIStyles.CARD_HOVER);
        JScrollPane scroll = new JScrollPane(tabla); scroll.setBorder(BorderFactory.createLineBorder(ui.UIStyles.CARD_BORDER));

        // Panel combinado: encabezado arriba y botones CRUD inmediatamente debajo
        JPanel topCombined = new JPanel(new BorderLayout()); topCombined.setOpaque(false);
        topCombined.add(header, BorderLayout.NORTH);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT,8,8)); actionsPanel.setOpaque(false);
        JButton btnNuevo = new JButton("Nuevo"); ui.UIStyles.styleButton(btnNuevo);
        JButton btnEditar = new JButton("Editar"); ui.UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); ui.UIStyles.styleButton(btnEliminar);
        JButton btnActualizar = new JButton("Actualizar"); ui.UIStyles.styleButton(btnActualizar);
        actionsPanel.add(btnNuevo); actionsPanel.add(btnEditar); actionsPanel.add(btnEliminar); actionsPanel.add(btnActualizar);

        topCombined.add(actionsPanel, BorderLayout.SOUTH);

        root.add(topCombined, BorderLayout.NORTH);
        root.add(scroll, BorderLayout.CENTER);

        // Carga inicial de datos
        cargar(model);

        // Listeners de botones
        // - Actualizar: recarga la tabla
        btnActualizar.addActionListener(e -> cargar(model));

        // - Nuevo: abre formulario modal para crear conductor
        btnNuevo.addActionListener(e -> new FrmConductor(this).setVisible(true));

        // - Editar: abre formulario con conductor seleccionado (busca por licencia)
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Seleccione un conductor"); return; }
            String lic = model.getValueAt(fila,0).toString();
            Conductor c = dao.buscarPorLicencia(lic);
            if (c != null) new FrmConductor(this, c).setVisible(true);
        });

        // - Eliminar: confirma y llama a DAO.eliminar(licencia); refresca tabla si es exitoso
        btnEliminar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila < 0) { JOptionPane.showMessageDialog(this, "Seleccione un conductor"); return; }
            String lic = model.getValueAt(fila,0).toString();
            int resp = JOptionPane.showConfirmDialog(this, "Eliminar licencia " + lic + " ?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                if (dao.eliminar(lic)) { JOptionPane.showMessageDialog(this, "Eliminado"); cargar(model);} else JOptionPane.showMessageDialog(this, "Error al eliminar");
            }
        });

        // - Refresco automático cuando la ventana recupera foco (por cierre de modales hijos)
        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(java.awt.event.WindowEvent e) { cargar(model); }
        });

        setContentPane(root);
        UIStyles.applyComponentTheme(getContentPane());
    }

    private void cargar(DefaultTableModel model) {
        // cargar(): rellena el modelo de la tabla con datos de DAO
        model.setRowCount(0);
        List<Conductor> lista = dao.listarTodos();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Conductor c : lista) {
            String fecha = c.getFechaVencimiento() == null ? "" : c.getFechaVencimiento().toLocalDate().format(fmt);
            model.addRow(new Object[]{c.getLicencia(), c.getDni(), c.getNombre(), c.getTelefono(), fecha});
        }
    }
}
