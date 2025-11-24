package ui;

/*
 * GuiaWindow.java
 * Ventana para listar, crear, editar y generar PDF de guías.
 * Contiene comentarios por método y por bloques de acción que explican
 * cómo se obtienen datos, validaciones y llamadas a util.GuiaRemisionGenerator.
 */

import dao.GuiaDAO;
import model.CabeceraGuia;
import model.DetalleGuia;
import util.GuiaRemisionGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
// Añadidos para manejo de fecha/hora
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class GuiaWindow extends JDialog {
    private final GuiaDAO dao = new GuiaDAO();
    private DefaultTableModel model;

    public GuiaWindow(Frame owner) {
        // Constructor: inicializa ventana modal, aplica estilos y construye UI
        super(owner, true);
        setTitle("Guías");
        setSize(900,520);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        getContentPane().setBackground(UIStyles.BG);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
    }

    private void initUI() {
        // initUI(): construye encabezado, tabla y botones de acción (Nuevo, Editar, Eliminar, Detalle, Generar PDF)
        // Los listeners delegan en `GuiaDAO`, `DetalleOrdenDAO` y `GuiaRemisionGenerator`.
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(8,8));
        root.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        root.setBackground(UIStyles.BG);

        // Header visual igual que DestinatarioWindow
        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout());
        header.setPreferredSize(new Dimension(100,48));
        header.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitle = new JLabel("Guías");
        lblTitle.setFont(UIStyles.UI_FONT_BOLD.deriveFont(18f));
        lblTitle.setForeground(UIStyles.GREEN_DARK);
        header.add(lblTitle, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú");
        UIStyles.styleButton(btnMenu);
        btnMenu.setFocusable(false);
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(GuiaWindow.this));
        JPanel menuPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        menuPanel.setOpaque(false);
        menuPanel.add(btnMenu);
        header.add(menuPanel, BorderLayout.EAST);

        JPanel northContainer = new JPanel();
        northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
        northContainer.setOpaque(false);
        northContainer.add(header);

        String[] cols = {"Código","Serie","Número","Orden","Remitente","Fecha","Hora","Estado"};
        model = new DefaultTableModel(cols,0);
        JTable table = new JTable(model); table.setRowHeight(26);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setOpaque(false);
        JButton btnNuevo = new JButton("Nuevo"); UIStyles.styleButton(btnNuevo);
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        JButton btnRefrescar = new JButton("Actualizar"); UIStyles.styleButton(btnRefrescar);
        JButton btnDetalle = new JButton("Detalle"); UIStyles.styleButton(btnDetalle);
        JButton btnGenerarPdf = new JButton("Generar PDF"); UIStyles.styleButton(btnGenerarPdf);
        top.add(btnNuevo); top.add(btnEditar); top.add(btnEliminar); top.add(btnRefrescar); top.add(btnDetalle); top.add(btnGenerarPdf);
        northContainer.add(top);
        root.add(northContainer, BorderLayout.NORTH);
        root.add(new JScrollPane(table), BorderLayout.CENTER);
        add(root);

        // Refrescar: recarga la lista de guías
        btnRefrescar.addActionListener(e -> load());
        // Nuevo: abre FrmGuia en modo creación
        btnNuevo.addActionListener(e -> new FrmGuia(this, null).setVisible(true));
        // Editar: abre FrmGuia con la cabecera cargada desde DAO
        btnEditar.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila"); return; }
            String codigo = model.getValueAt(r,0).toString();
            CabeceraGuia g = dao.listarTodas().stream().filter(x->x.getCodigoGuia().equals(codigo)).findFirst().orElse(null);
            if (g!=null) new FrmGuia(this, g).setVisible(true);
        });
        // Eliminar: confirma y llama a dao.eliminar(codigo)
        btnEliminar.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una fila"); return; }
            String codigo = model.getValueAt(r,0).toString();
            int opt = JOptionPane.showConfirmDialog(this, "Confirma eliminar la guía " + codigo + " ?", "Confirmar", JOptionPane.YES_NO_OPTION);
            if (opt != JOptionPane.YES_OPTION) return;
            boolean ok = dao.eliminar(codigo);
            if (ok) { JOptionPane.showMessageDialog(this, "Eliminado"); load(); }
            else { JOptionPane.showMessageDialog(this, "Error al eliminar. Verifique dependencias y logs."); }
        });

        // Mostrar detalle de la orden asociada a la guía seleccionada
        btnDetalle.addActionListener(e -> {
            int r = table.getSelectedRow(); if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una guía"); return; }
            Object ordenVal = model.getValueAt(r, 3); if (ordenVal == null) { JOptionPane.showMessageDialog(this, "La guía seleccionada no tiene orden asociada"); return; }
            String codigoOrden = ordenVal.toString();
            dao.DetalleOrdenDAO ddao = new dao.DetalleOrdenDAO();
            DefaultTableModel dm = ddao.listarModeloTablaPorOrden(codigoOrden);
            JTable t = new JTable(dm);
            t.setFillsViewportHeight(true); t.setRowHeight(24); t.getTableHeader().setFont(UIStyles.UI_FONT_BOLD);
            JScrollPane sp = new JScrollPane(t); sp.setBorder(BorderFactory.createLineBorder(UIStyles.CARD_BORDER));
            JDialog dlg = new JDialog(this, "Detalle de orden " + codigoOrden, true);
            ui.UIStyles.RoundedPanel rp = new ui.UIStyles.RoundedPanel(Color.WHITE, UIStyles.CARD_RADIUS);
            rp.setLayout(new BorderLayout(8,8)); rp.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
            rp.add(sp, BorderLayout.CENTER);
            dlg.add(rp); dlg.setSize(720, 420); dlg.setLocationRelativeTo(this); UIStyles.applyComponentTheme(dlg.getContentPane()); dlg.setVisible(true);
        });

        // Acción: generar PDF usando datos de la guía seleccionada
        btnGenerarPdf.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { JOptionPane.showMessageDialog(this, "Seleccione una guía para generar el PDF"); return; }
            String codigo = model.getValueAt(r,0).toString();
            CabeceraGuia g = dao.listarTodas().stream().filter(x->x.getCodigoGuia().equals(codigo)).findFirst().orElse(null);
            if (g == null) { JOptionPane.showMessageDialog(this, "No se pudo cargar la guía seleccionada"); return; }

            // Determinar fecha/hora de emisión: preferir valores almacenados en la cabecera
            String fechaEmisionStr;
            if (g.getFechaEmision() != null) {
                java.sql.Date d = g.getFechaEmision();
                String fechaPart = d.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                if (g.getHoraEmision() != null) {
                    java.sql.Time ttime = g.getHoraEmision();
                    java.time.LocalTime lt = ttime.toLocalTime();
                    String horaPart = lt.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    fechaEmisionStr = fechaPart + " " + horaPart;
                } else {
                    LocalTime ahora = LocalTime.now();
                    String horaActual = ahora.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                    fechaEmisionStr = fechaPart + " " + horaActual;
                }
            } else {
                // pedir fecha solamente si no existe en DB
                String fechaInput = (String) JOptionPane.showInputDialog(
                        this,
                        "Ingrese Fecha de emisión (dd/MM/yyyy):",
                        "Fecha de emisión",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        ""
                );
                if (fechaInput == null) return; // cancelado
                // Validar formato dd/MM/yyyy
                LocalDate fechaEmitida;
                try {
                    DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    fechaEmitida = LocalDate.parse(fechaInput.trim(), f);
                } catch (DateTimeParseException ex) {
                    JOptionPane.showMessageDialog(this, "Formato de fecha inválido. Use dd/MM/yyyy");
                    return;
                }
                LocalTime ahora = LocalTime.now();
                String horaActual = ahora.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
                fechaEmisionStr = fechaEmitida.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " " + horaActual;
            }

            // Obtener detalles (líneas)
            List<DetalleGuia> detalles = dao.listarDetallePorGuia(codigo);

            // Mapear a util.GuiaRemisionGenerator.DetalleGuia
            List<GuiaRemisionGenerator.DetalleGuia> itemsPdf = new ArrayList<>();
            int contador = 1;
            for (DetalleGuia d : detalles) {
                GuiaRemisionGenerator.DetalleGuia it = new GuiaRemisionGenerator.DetalleGuia(
                        contador++,
                        d.getBienNormalizado(),
                        d.getCodigoBien(),
                        d.getCodigoProductoSunat(),
                        d.getPartidaArancelaria(),
                        d.getCodigoGtin(),
                        d.getDescripcion(),
                        d.getUnidadMedida(),
                        d.getCantidad()
                );
                itemsPdf.add(it);
            }

            // Datos básicos (ajusta según tu esquema; aquí uso placeholders si no existen columnas)
            String empresaNombre = "RED LIPA ECOLOGICA SOCIEDAD ANONIMA CERRADA";
            String rucEmisor = "20604635943";
            String serie = g.getSerie() == null ? "" : g.getSerie();
            String numero = g.getNumero() == null ? "" : g.getNumero();

            // Pedir ruta de guardado
            JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new java.io.File("GUIA_" + serie + "_" + numero + ".pdf"));
            int sel = fc.showSaveDialog(this);
            if (sel != JFileChooser.APPROVE_OPTION) return;
            String path = fc.getSelectedFile().getAbsolutePath();

            try {
                // Recuperar datos adicionales desde la BD cuando estén disponibles
                dao.DestinatarioDAO destDao = new dao.DestinatarioDAO();
                model.Destinatario dest = null;
                if (g.getRucDestinatario() != null && !g.getRucDestinatario().trim().isEmpty()) {
                    dest = destDao.listarTodos().stream().filter(x -> g.getRucDestinatario().equals(x.getRuc())).findFirst().orElse(null);
                }

                dao.TrasladoDAO trasDao = new dao.TrasladoDAO();
                model.Traslado traslado = trasDao.obtenerPorGuia(codigo);

                dao.ConductorDAO condDao = new dao.ConductorDAO();
                String nombreConductor = "";
                String licencia = "";
                String placa = "";
                String autorizacionVehiculo = "";
                if (traslado != null) {
                    placa = traslado.getPlaca() == null ? "" : traslado.getPlaca();
                    licencia = traslado.getLicencia() == null ? "" : traslado.getLicencia();
                    if (licencia != null && !licencia.isEmpty()) {
                        model.Conductor condFound = condDao.buscarPorLicencia(licencia);
                        if (condFound != null) nombreConductor = condFound.getNombre();
                    }
                }

                dao.RemitenteDAO remitDao = new dao.RemitenteDAO();
                String empresaNombreReal = empresaNombre; // default
                String rucEmisorReal = rucEmisor;
                if (g.getRucRemitente() != null && !g.getRucRemitente().trim().isEmpty()) {
                    model.Remitente remitente = remitDao.buscarPorRuc(g.getRucRemitente());
                    if (remitente != null) {
                        empresaNombreReal = remitente.getNombreEmpresa() == null ? empresaNombreReal : remitente.getNombreEmpresa();
                        rucEmisorReal = remitente.getRuc();
                    } else {
                        rucEmisorReal = g.getRucRemitente();
                    }
                }

                String destinatarioNombre = dest == null ? "" : dest.getNombre();
                String destinatarioRuc = dest == null ? (g.getRucDestinatario() == null ? "" : g.getRucDestinatario()) : dest.getRuc();

                String fechaInicioTraslado = "";
                if (traslado != null && traslado.getFechaInicio() != null) {
                    java.time.format.DateTimeFormatter fdt = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
                    fechaInicioTraslado = traslado.getFechaInicio().toLocalDateTime().format(fdt);
                }

                String puntoPartida = g.getDirPartida() == null ? "" : g.getDirPartida();
                String puntoLlegada = g.getDirLlegada() == null ? "" : g.getDirLlegada();
                String motivo = ""; // no field in CabeceraGuia currently

                String pesoTotalStr = "";
                if (g.getPesoTotal() != null) {
                    pesoTotalStr = String.format("%.2f", g.getPesoTotal());
                }

                String unidadPeso = "KGM";
                String modalidad = "";
                String indicadorTransbordo = "";
                String indicadorRetornoEnvases = "";

                // Llamada al generador: pasamos fechaEmisionStr (fecha manual + hora actual)
                util.GuiaRemisionGenerator.createPdf(
                        path,
                        empresaNombreReal,
                        rucEmisorReal,
                        g.getRucRemitente(),
                        serie,
                        numero,
                        fechaEmisionStr,
                        fechaInicioTraslado,
                        puntoPartida,
                        puntoLlegada,
                        motivo,
                        destinatarioNombre,
                        destinatarioRuc,
                        itemsPdf,
                        unidadPeso,
                        pesoTotalStr,
                        modalidad,
                        indicadorTransbordo,
                        indicadorRetornoEnvases,
                        placa,
                        autorizacionVehiculo,
                        nombreConductor,
                        licencia,
                        null
                );
                JOptionPane.showMessageDialog(this, "PDF generado en:\n" + path);
            } catch (Exception ex) {
                // Manejo de errores al generar el PDF
                JOptionPane.showMessageDialog(this, "Error generando PDF: " + ex.getMessage());
            }
        });

        load();

        // Menu button: return to main menu (reuse existing)
        btnMenu.addActionListener(e -> UIStyles.showMainMenu(GuiaWindow.this));
    }

    public void load() {
        // load(): consulta todas las guías vía DAO y rellena la tabla con columnas clave
        model.setRowCount(0);
        List<CabeceraGuia> lista = dao.listarTodas();
        for (CabeceraGuia g: lista) model.addRow(new Object[]{g.getCodigoGuia(), g.getSerie(), g.getNumero(), g.getCodOrden(), g.getRucRemitente(), g.getFechaEmision(), g.getHoraEmision(), g.getEstadoGuia()});
    }
}
