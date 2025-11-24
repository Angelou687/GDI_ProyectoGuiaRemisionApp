
package ui;

/*
 * UbigeoWindow.java
 * Ventana para gestionar códigos UBIGEO (CRUD).
 * Cada método incluye comentarios en español explicando su propósito.
 */

import dao.UbigeoDAO;
import model.Ubigeo;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UbigeoWindow extends JFrame {
    private final UbigeoDAO dao = new UbigeoDAO();
    private final DefaultTableModel model = new DefaultTableModel(new String[]{"Código","Departamento","Provincia","Distrito"}, 0);
    private final JTable tabla = new JTable(model);
    private final JTextField txtCodigo = new JTextField();
    private final JTextField txtDepartamento = new JTextField();
    private final JTextField txtProvincia = new JTextField();
    private final JTextField txtDistrito = new JTextField();

    public UbigeoWindow() {
        // Constructor: configura ventana, encabezado, toolbar y tabla
        setTitle("Ubigeos");
        setSize(800, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        // Panel superior con botones
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        topPanel.setBackground(UIStyles.PANEL);
        UIStyles.RoundedPanel headerPanel = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(100,48));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8,16,8,16));
        JLabel lblTitulo = new JLabel("Ubigeos");
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
        JButton btnEditar = new JButton("Editar"); UIStyles.styleButton(btnEditar);
        JButton btnEliminar = new JButton("Eliminar"); UIStyles.styleButton(btnEliminar);
        JButton btnActualizar = new JButton("Actualizar"); UIStyles.styleButton(btnActualizar);
        toolbarLeft.add(btnNuevo); toolbarLeft.add(btnEditar); toolbarLeft.add(btnEliminar); toolbarLeft.add(btnActualizar);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.add(toolbarLeft, BorderLayout.WEST);
        topPanel.add(toolbar, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);

        // Tabla
        String[] columnas = {"Código", "Departamento", "Provincia", "Distrito"};
        model.setColumnIdentifiers(columnas);
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


        // El formulario se mostrará solo en un JDialog emergente

        // Carga inicial de la tabla
        cargarTabla();

        // Acciones de botones
        // - Nuevo: abre diálogo para crear ubigeo
        btnNuevo.addActionListener(e -> mostrarDialogoUbigeo(null));
        // - Editar: rellena formulario con los datos de la fila seleccionada
        btnEditar.addActionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (fila == -1) {
                JOptionPane.showMessageDialog(this, "Seleccione un ubigeo de la tabla");
                return;
            }
            Ubigeo u = new Ubigeo(
                model.getValueAt(fila,0).toString(),
                model.getValueAt(fila,1).toString(),
                model.getValueAt(fila,2).toString(),
                model.getValueAt(fila,3).toString()
            );
            mostrarDialogoUbigeo(u);
        });
        // - Eliminar: llama a helper eliminarUbigeo() que confirma y borra
        btnEliminar.addActionListener(e -> eliminarUbigeo());
        // - Actualizar: recarga la tabla desde DAO
        btnActualizar.addActionListener(e -> cargarTabla());
        // - Menú: cierra y vuelve al menú principal
        btnMenu.addActionListener(e -> {
            dispose();
            UIStyles.promptReturnToMenu(this);
        });
    }


    private void limpiarForm() {
        // limpiarForm(): resetea los campos del formulario de ubigeo
        txtCodigo.setText("");
        txtDepartamento.setText("");
        txtProvincia.setText("");
        txtDistrito.setText("");
        txtCodigo.setEnabled(true);
    }

    private void guardarUbigeo() {
        // guardarUbigeo(): valida campos y decide entre insertar o actualizar según si el código está habilitado
        String codigo = txtCodigo.getText().trim();
        String dep = txtDepartamento.getText().trim();
        String prov = txtProvincia.getText().trim();
        String dist = txtDistrito.getText().trim();
        if (codigo.isEmpty() || dep.isEmpty() || prov.isEmpty() || dist.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios");
            return;
        }
        Ubigeo u = new Ubigeo(codigo, dep, prov, dist);
        if (txtCodigo.isEnabled()) {
            if (dao.insertar(u)) {
                JOptionPane.showMessageDialog(this, "Ubigeo guardado");
                cargarTabla();
                limpiarForm();
            } else {
                JOptionPane.showMessageDialog(this, "Ya existe un ubigeo con ese código");
            }
        } else {
            if (dao.actualizar(u)) {
                JOptionPane.showMessageDialog(this, "Ubigeo actualizado");
                cargarTabla();
                limpiarForm();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar");
            }
        }
    }

    private void editarUbigeo() {
        // editarUbigeo(): rellena el formulario con la fila seleccionada para edición
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ubigeo de la tabla");
            return;
        }
        txtCodigo.setText(model.getValueAt(fila,0).toString());
        txtDepartamento.setText(model.getValueAt(fila,1).toString());
        txtProvincia.setText(model.getValueAt(fila,2).toString());
        txtDistrito.setText(model.getValueAt(fila,3).toString());
        txtCodigo.setEnabled(false);
    }

    private void cargarTabla() {
        // cargarTabla(): consulta DAO y rellena el modelo de la tabla con todos los ubigeos
        model.setRowCount(0);
        List<Ubigeo> lista = dao.listarTodos();
        for (Ubigeo u : lista) {
            model.addRow(new Object[]{u.getCodigo(), u.getDepartamento(), u.getProvincia(), u.getDistrito()});
        }
    }

    // Muestra un formulario emergente para crear/editar ubigeo
    // Muestra un formulario emergente moderno para crear/editar ubigeo
    private void mostrarDialogoUbigeo(Ubigeo editar) {
        // mostrarDialogoUbigeo(): abre FrmUbigeo (modal) para crear/editar. Al cerrar refresca tabla.
        FrmUbigeo frm = new FrmUbigeo(this, editar);
        frm.setVisible(true);
        cargarTabla(); // refresca la tabla al cerrar
    }

    private void eliminarUbigeo() {
        // eliminarUbigeo(): confirma con el usuario y elimina usando DAO.eliminar(codigo)
        int fila = tabla.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un ubigeo de la tabla");
            return;
        }
        String codigo = model.getValueAt(fila,0).toString();
        if (dao.eliminar(codigo)) {
            JOptionPane.showMessageDialog(this, "Ubigeo eliminado");
            cargarTabla();
            limpiarForm();
        } else {
            JOptionPane.showMessageDialog(this, "Error al eliminar");
        }
    }
}
