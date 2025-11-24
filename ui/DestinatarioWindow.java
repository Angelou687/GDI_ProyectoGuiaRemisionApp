package ui;

/*
 * DestinatarioWindow.java
 * Ventana para listar y gestionar destinatarios.
 * Comentarios en cada método describen comportamiento y llamadas a DAOs/UI.
 */

import dao.DestinatarioDAO;
import model.Destinatario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DestinatarioWindow extends JDialog {

    private final DestinatarioDAO dao = new DestinatarioDAO();
    private DefaultTableModel model;

    public DestinatarioWindow(Frame owner) {
        // Constructor
        // - Inicializa la ventana modal de destinatarios
        // - Configura tamaño, comportamiento de cierre y aplica estilos
        super(owner, true);
        setTitle("Destinatarios");
        setSize(900, 520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(UIStyles.BG);

        // If user clicks the window close button, return to main menu (reusing instance)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                UIStyles.showMainMenu(DestinatarioWindow.this);
            }
        });

        initUI();
        // apply modern theme/fonts to all child components
        UIStyles.applyComponentTheme(getContentPane());
    }

    private void initUI() {
        // initUI(): crea y organiza componentes Swing
        // - Header y toolbar con botones de acción
        // - Tabla central con listado de destinatarios
        // - Configura listeners para Nuevo/Editar/Eliminar/Refrescar
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        // Header (modern style) + toolbar container
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(6,12,6,12));
        JLabel lblTitle = new JLabel("Destinatarios");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(DestinatarioWindow.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(header);
        // Table (center)
        String[] cols = {"RUC","Nombre","Teléfono","Dirección","Ubigeo","Gmail"};
        model = new DefaultTableModel(cols,0);
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
        table.getTableHeader().setReorderingAllowed(false);

        UIStyles.RoundedPanel leftCard = new UIStyles.RoundedPanel(Color.WHITE, UIStyles.CARD_RADIUS);
        leftCard.setLayout(new BorderLayout(6,6));
        leftCard.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        JLabel lblList = new JLabel("Lista de destinatarios");
        lblList.setFont(UIStyles.UI_FONT_BOLD);
        leftCard.add(lblList, BorderLayout.NORTH);
        JScrollPane tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftCard.add(tableScroll, BorderLayout.CENTER);

        // Column width preferences to avoid truncation
        int[] prefWidths = {120, 200, 110, 320, 80, 200};
        for (int i = 0; i < prefWidths.length && i < table.getColumnModel().getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(prefWidths[i]);
        }
        leftCard.setMinimumSize(new Dimension(480, 200));
        // Let the table expand to the available width
        table.setPreferredScrollableViewportSize(new Dimension(780, 360));

        // Top toolbar with actions (restore original layout behavior)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setOpaque(false);
        JButton btnNuevo = new JButton("Nuevo"); UIStyles.styleButton(btnNuevo);
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        JButton btnRefrescar = new JButton("Actualizar"); UIStyles.styleButton(btnRefrescar);

        // Color the action buttons with the pastel card color and keep dark-green text
        Color actionBg = UIStyles.PASTEL_GREEN;
        btnNuevo.setBackground(actionBg); btnNuevo.setOpaque(true); btnNuevo.setForeground(UIStyles.GREEN_DARK); btnNuevo.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnEditar.setBackground(actionBg); btnEditar.setOpaque(true); btnEditar.setForeground(UIStyles.GREEN_DARK); btnEditar.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnEliminar.setBackground(actionBg); btnEliminar.setOpaque(true); btnEliminar.setForeground(UIStyles.GREEN_DARK); btnEliminar.putClientProperty("ui.keepStyle", Boolean.TRUE);
        btnRefrescar.setBackground(actionBg); btnRefrescar.setOpaque(true); btnRefrescar.setForeground(UIStyles.GREEN_DARK); btnRefrescar.putClientProperty("ui.keepStyle", Boolean.TRUE);

        // Only add action buttons to the top toolbar (no Menú button)
        top.add(btnNuevo); top.add(btnEditar); top.add(btnEliminar); top.add(btnRefrescar);
        northContainer.add(top);
        root.add(northContainer, BorderLayout.NORTH);
        root.add(leftCard, BorderLayout.CENTER);

        add(root);

        // Actions
        // - Refrescar: recarga datos desde DAO
        btnRefrescar.addActionListener(e -> load());
        // - Nuevo: abre modal para crear un destinatario
        btnNuevo.addActionListener(e -> new FrmDestinatario(this, null).setVisible(true));
        // - Editar: obtiene RUC de la fila seleccionada y abre el formulario con el objeto
        btnEditar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila"); return; }
            String ruc = model.getValueAt(r,0).toString();
            Destinatario d = dao.listarTodos().stream().filter(x->x.getRuc().equals(ruc)).findFirst().orElse(null);
            if (d != null) new FrmDestinatario(this, d).setVisible(true);
        });
        // - Eliminar: confirma y llama a DAO.eliminar(ruc); refresca tabla si es exitoso
        btnEliminar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila"); return; }
            String ruc = model.getValueAt(r,0).toString();
            int resp = JOptionPane.showConfirmDialog(this, "Eliminar RUC " + ruc + "?","Confirmar",JOptionPane.YES_NO_OPTION);
            if (resp == JOptionPane.YES_OPTION) {
                if (dao.eliminar(ruc)) { JOptionPane.showMessageDialog(this, "Eliminado"); load(); }
                else JOptionPane.showMessageDialog(this, "Error al eliminar");
            }
        });

        // ...existing code...

        // no inline detail panel: actions open modal forms (FrmDestinatario)

        // initial load
        load();
    }

    public void load() {
        model.setRowCount(0);
        List<Destinatario> lista = dao.listarTodos();
        for (Destinatario d : lista) {
            model.addRow(new Object[]{d.getRuc(), d.getNombre(), d.getNumeroTelefono(), d.getCalleDireccion(), d.getCodigoUbigeo(), d.getGmail()});
        }
    }
}
