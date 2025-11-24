package ui;

/*
 * FrmMenu.java
 * Ventana principal (menu) de la aplicación.
 * Comentarios por método:
 * - setVisible: registra la instancia para reutilizar el menú desde otras ventanas.
 * - initUI: construye la interfaz con tarjetas (cards) que abren las distintas ventanas.
 * - createCard: helper para construir cada tarjeta; maneja icono, texto y evento onClick.
 * - openX methods: métodos utilitarios que lanzan ventanas específicas en el EDT.
 */

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FrmMenu extends JFrame {

    public FrmMenu() {
        setTitle("Guía de Remisión - RED LIPA");
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UIStyles.BG);

        initUI();
    }

    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);
        // register this instance so other dialogs can return to the same menu
        ui.UIStyles.registerMainMenu(this);
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        root.setBackground(UIStyles.BG);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Color.WHITE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        JLabel title = new JLabel("Panel de administración");
        title.setFont(UIStyles.UI_FONT_BOLD.deriveFont(20f));
        title.setForeground(UIStyles.GREEN_DARK);
        JLabel subtitle = new JLabel("Gestiona guías, destinatarios y traslados", SwingConstants.LEFT);
        subtitle.setFont(UIStyles.UI_FONT.deriveFont(Font.ITALIC, 12f));
        subtitle.setForeground(new Color(100,120,100));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);

        // Right-side action buttons: Perfil + Out (logout) + Cerrar
        JPanel panelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        panelBtn.setOpaque(false);
        JButton btnPerfil = new JButton("Perfil"); UIStyles.styleButton(btnPerfil); btnPerfil.setFocusable(false);
        JButton btnOut = new JButton("Out"); UIStyles.styleButton(btnOut); btnOut.setFocusable(false);
        JButton btnPulsar = new JButton("Cerrar"); UIStyles.styleButton(btnPulsar); btnPulsar.setFocusable(false);
        panelBtn.add(btnPerfil); panelBtn.add(btnOut); panelBtn.add(btnPulsar);
        titlePanel.add(panelBtn, BorderLayout.EAST);

        btnPerfil.addActionListener(e -> {
            // open profile: show a combo with available remitente RUCs
            dao.RemitenteDAO rdao = new dao.RemitenteDAO();
            java.util.List<String> rucs = rdao.listarRucs();
            if (rucs.isEmpty()) {
                int resp = JOptionPane.showConfirmDialog(this, "No hay remitentes registrados. Desea crear uno?", "Perfil", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) new ui.FrmRemitente(this, null).setVisible(true);
                return;
            }

            JComboBox<String> cb = new JComboBox<>();
            cb.addItem("");
            for (String r : rucs) cb.addItem(r);
            cb.setSelectedIndex(0);

            int sel = JOptionPane.showConfirmDialog(this, cb, "Seleccione RUC de remitente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (sel != JOptionPane.OK_OPTION) return;
            String chosen = (String) cb.getSelectedItem();
            if (chosen == null || chosen.trim().isEmpty()) return;

            model.Remitente r = rdao.buscarPorRuc(chosen.trim());
            if (r == null) {
                int resp = JOptionPane.showConfirmDialog(this, "Remitente no encontrado. Desea registrar?", "Perfil", JOptionPane.YES_NO_OPTION);
                if (resp == JOptionPane.YES_OPTION) new ui.FrmRemitente(this, chosen.trim()).setVisible(true);
            } else {
                String info = String.format("RUC: %s\nEmpresa: %s\nRazón social: %s\nTel: %s\nEmail: %s\nDirección: %s\nUbigeo: %s",
                        r.getRuc(), r.getNombreEmpresa(), r.getRazonSocial(), r.getTelefono(), r.getEmail(), r.getCalleDireccion(), r.getCodigoUbigeo());
                JOptionPane.showMessageDialog(this, info, "Perfil remitente", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        btnOut.addActionListener(e -> {
            // logout: close menu and show login
            dispose();
            SwingUtilities.invokeLater(() -> new ui.FrmLogin(null).setVisible(true));
        });

        btnPulsar.addActionListener(e -> System.exit(0));

        JPanel cards = new JPanel(new GridLayout(3,3,18,18));
        cards.setOpaque(false);
        cards.add(createCard("Destinatarios", "Gestiona clientes y contactos", "/ui/icons/destinatarios.png", () -> openDestinatarios()));
        cards.add(createCard("Guías", "Crear y administrar guías", "/ui/icons/documento.png", () -> openGuias()));
        cards.add(createCard("Traslados", "Registrar traslados y vehículos", "/ui/icons/traslados.png", () -> openTraslados()));
        cards.add(createCard("Reportes", "Informes y KPIs", "/ui/icons/grafico.png", () -> openReportes()));
        cards.add(createCard("Ubigeo", "Gestiona códigos, departamentos, provincias y distritos", "/ui/icons/ubigeo.png", () -> openUbigeo()));
        cards.add(createCard("Órdenes", "Gestiona órdenes de pago", "/ui/icons/ordenes.png", () -> openOrdenes()));
        cards.add(createCard("Vehículos", "Gestiona placas, marca, modelo, año, color", "/ui/icons/vehiculos.png", () -> openVehiculos()));
        cards.add(createCard("Conductores", "Gestiona conductores y licencias", "/ui/icons/conductores.png", () -> {
            SwingUtilities.invokeLater(() -> new ui.ConductoresWindow(this).setVisible(true));
        }));
        cards.add(createCard("Productos", "Gestiona productos y precios", "/ui/icons/productos.png", () -> openProductos()));

    root.add(titlePanel, BorderLayout.NORTH);
    root.add(cards, BorderLayout.CENTER);

        add(root);
    }

    private void openUbigeo() {
        SwingUtilities.invokeLater(() -> new ui.UbigeoWindow().setVisible(true));
    }

    private void openOrdenes() {
        SwingUtilities.invokeLater(() -> new ui.OrdenesWindow(this).setVisible(true));
    }

    private JPanel createCard(String titleText, String desc, String iconPath, Runnable onClick) {
        UIStyles.RoundedPanel p = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        p.setLayout(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));
        p.setPreferredSize(new Dimension(220,220));

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        // Icono
        JLabel iconLabel = new JLabel();
        if (iconPath != null && !iconPath.isEmpty()) {
            try {
                java.net.URL url = getClass().getResource(iconPath);
                ImageIcon icon = null;
                if (url != null) {
                    icon = new ImageIcon(url);
                } else {
                    // Fallback: intentar cargar desde carpeta src/ui/icons en disco (útil en entorno de desarrollo)
                    try {
                        String fileName = iconPath;
                        if (fileName.startsWith("/")) fileName = fileName.substring(1);
                        java.io.File f = new java.io.File(System.getProperty("user.dir"), fileName.replace('/', java.io.File.separatorChar));
                        if (f.exists()) icon = new ImageIcon(f.getAbsolutePath());
                    } catch (Exception ex) { /* ignore */ }
                }

                if (icon != null) {
                    // Escalar icono un poco más grande para mejor visibilidad en el card
                    Image img = icon.getImage().getScaledInstance(56, 56, Image.SCALE_SMOOTH);
                    iconLabel.setIcon(new ImageIcon(img));
                }
            } catch (Exception ex) { /* ignora si no encuentra el icono */ }
        }
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lab = new JLabel(titleText, SwingConstants.CENTER);
        lab.setAlignmentX(Component.CENTER_ALIGNMENT);
        lab.setFont(UIStyles.UI_FONT_BOLD.deriveFont(16f));
        lab.setForeground(UIStyles.GREEN_DARK);

        JLabel lblDesc = new JLabel(desc, SwingConstants.CENTER);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblDesc.setFont(UIStyles.UI_FONT.deriveFont(12f));
        lblDesc.setForeground(new Color(90,110,90));

        center.add(Box.createVerticalGlue());
        center.add(iconLabel);
        center.add(Box.createRigidArea(new Dimension(0,8)));
        center.add(lab);
        center.add(Box.createRigidArea(new Dimension(0,6)));
        center.add(lblDesc);
        center.add(Box.createVerticalGlue());

        JButton b = new JButton("Abrir");
        UIStyles.styleButton(b);
        b.addActionListener(e -> onClick.run());

        JPanel south = new JPanel(); south.setOpaque(false); south.add(b);

        p.add(center, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);

        // hover effects
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                p.setBaseColor(UIStyles.CARD_HOVER);
                p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                p.setBaseColor(UIStyles.PASTEL_GREEN);
                p.setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });

        return p;
    }

    private void openDestinatarios() {
        SwingUtilities.invokeLater(() -> new ui.DestinatarioWindow(this).setVisible(true));
    }

    private void openGuias() {
        SwingUtilities.invokeLater(() -> new ui.GuiaWindow(this).setVisible(true));
    }

    private void openTraslados() {
        SwingUtilities.invokeLater(() -> new ui.TrasladoWindow(this).setVisible(true));
    }

    private void openReportes() {
        SwingUtilities.invokeLater(() -> new ui.ReportesWindow(this).setVisible(true));
    }

    private void openVehiculos() {
        SwingUtilities.invokeLater(() -> new ui.VehiculosWindow().setVisible(true));
    }

    private void openProductos() {
        SwingUtilities.invokeLater(() -> new ui.ProductosWindow().setVisible(true));
    }
}
