package ui;

/*
 * TrasladoWindow.java
 * Ventana de gestión de traslados: lista, crear, editar, eliminar y refrescar.
 * Comentarios por método y por bloques de acción para facilitar mantenimiento.
 */

import dao.TrasladoDAO;
import model.Traslado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.time.format.DateTimeFormatter;

public class TrasladoWindow extends JDialog {
    private final TrasladoDAO dao = new TrasladoDAO();
    private DefaultTableModel model;

    public TrasladoWindow(Frame owner) {
        // Constructor
        // - Inicializa la ventana modal de Traslados
        // - Configura tamaño, título y estilo
        // - Llama a `initUI()` para construir la interfaz
        super(owner, true);
        setTitle("Traslados"); setSize(900,520); setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(UIStyles.BG);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
    }

    private void initUI() {
        // initUI(): Construye todos los componentes Swing de la ventana
        // - Paneles y cabecera
        // - Tabla con modelo `DefaultTableModel`
        // - Botones: Nuevo, Editar, Eliminar, Actualizar
        // - Listeners: acciones asociadas a botones (abrir formulario, eliminar, refrescar)
        // Nota: Las acciones de los botones delegan en `TrasladoDAO` y `FrmTraslado`.
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.setBackground(UIStyles.BG);

        // Header visual igual que DestinatarioWindow
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel("Traslados");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(TrasladoWindow.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(header);

        String[] cols = {"Código traslado","Guía","Placa","Licencia","Inicio","Fin","Estado","Obs"};
        model = new DefaultTableModel(cols,0); JTable table = new JTable(model); table.setRowHeight(26);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setOpaque(false);
        JButton btnNuevo = new JButton("Nuevo"); UIStyles.styleButton(btnNuevo);
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        JButton btnRefrescar = new JButton("Actualizar"); UIStyles.styleButton(btnRefrescar);
        top.add(btnNuevo); top.add(btnEditar); top.add(btnEliminar); top.add(btnRefrescar);
        northContainer.add(top);

        root.add(northContainer, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        add(root);

        btnRefrescar.addActionListener(e->load());
        // Acción: Nuevo traslado -> abre formulario de creación
        btnNuevo.addActionListener(e-> new FrmTraslado(this, null).setVisible(true));
        // Acción: Editar traslado -> obtiene código de la fila seleccionada,
        // busca en la lista retornada por DAO y abre `FrmTraslado` con el objeto
        btnEditar.addActionListener(e->{
            int r = table.getSelectedRow();
            if (r<0){ JOptionPane.showMessageDialog(this,"Seleccione fila"); return; }
            String cod = model.getValueAt(r,0).toString();
            List<Traslado> lista = dao.listarTodos();
            Traslado t = lista.stream().filter(x->x.getCodigoTraslado().equals(cod)).findFirst().orElse(null);
            if (t!=null) new FrmTraslado(this, t).setVisible(true);
        });

        // Acción: Eliminar traslado -> confirma con el usuario y llama a DAO
        btnEliminar.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) {
                JOptionPane.showMessageDialog(this, "Seleccione fila a eliminar");
                return;
            }
            String cod = model.getValueAt(r, 0).toString();
            int opt = JOptionPane.showConfirmDialog(this, "¿Seguro que desea eliminar el traslado '" + cod + "'?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                boolean ok = dao.eliminarTraslado(cod);
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Traslado eliminado");
                    load();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo eliminar. Verifique dependencias o errores.");
                }
            }
        });

        load();

        btnMenu.addActionListener(e -> UIStyles.showMainMenu(TrasladoWindow.this));
    }

    /**
     * load(): carga la lista de traslados desde la base de datos y la muestra
     * en la tabla. Convierte valores de fecha/hora a formato `dd/MM/yyyy`.
     */
    public void load() {
        model.setRowCount(0);
        List<Traslado> lista = dao.listarTodos();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Traslado t: lista) {
            String inicio = "";
            String fin = "";
            if (t.getFechaInicio() != null) inicio = t.getFechaInicio().toLocalDateTime().toLocalDate().format(fmt);
            if (t.getFechaFin() != null) fin = t.getFechaFin().toLocalDateTime().toLocalDate().format(fmt);
            model.addRow(new Object[]{t.getCodigoTraslado(), t.getCodigoGuia(), t.getPlaca(), t.getLicencia(), inicio, fin, t.getEstadoTraslado(), t.getObservaciones()});
        }
    }
}
