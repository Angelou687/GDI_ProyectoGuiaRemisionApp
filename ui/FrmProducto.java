package ui;

import dao.ProductoDAO;
import model.Producto;

import javax.swing.*;
import java.awt.*;

public class FrmProducto extends JDialog {
    private final ProductoDAO dao = new ProductoDAO();
    private final Producto editing;

    public FrmProducto(Window owner, Producto p) {
        super(owner, Dialog.ModalityType.APPLICATION_MODAL);
        this.editing = p;
        setTitle(p==null?"Nuevo producto":"Editar producto");
        setSize(520,340);
        setLocationRelativeTo(owner);
        initUI();
        UIStyles.applyComponentTheme(getContentPane());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    }

    private void initUI() {
        UIStyles.RoundedPanel root = new UIStyles.RoundedPanel(UIStyles.PANEL, UIStyles.CARD_RADIUS);
        root.setLayout(new BorderLayout(10,10));
        root.setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        UIStyles.RoundedPanel header = new UIStyles.RoundedPanel(UIStyles.PASTEL_GREEN, UIStyles.CARD_RADIUS);
        header.setLayout(new BorderLayout()); header.setPreferredSize(new Dimension(100,44)); header.setBorder(BorderFactory.createEmptyBorder(8,12,8,12));
        JLabel lbl = new JLabel(editing==null?"Nuevo producto":"Editar producto"); lbl.setFont(UIStyles.UI_FONT_BOLD.deriveFont(16f)); lbl.setForeground(UIStyles.GREEN_DARK);
        header.add(lbl, BorderLayout.WEST);
        JButton btnMenu = new JButton("Menú"); UIStyles.styleButton(btnMenu); btnMenu.setFocusable(false); btnMenu.addActionListener(e-> UIStyles.showMainMenu(FrmProducto.this));
        JPanel mp = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,0)); mp.setOpaque(false); mp.add(btnMenu); header.add(mp, BorderLayout.EAST);

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets=new Insets(6,6,6,6); gbc.fill=GridBagConstraints.HORIZONTAL; gbc.weightx=1.0;

        JTextField txtCodigo = new JTextField(); JTextField txtNombre = new JTextField(); JTextField txtPrecio = new JTextField(); JTextField txtUnidad = new JTextField();

        int y=0;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Código producto:"), gbc); gbc.gridx=1; form.add(txtCodigo, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Nombre producto:"), gbc); gbc.gridx=1; form.add(txtNombre, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Precio base:"), gbc); gbc.gridx=1; form.add(txtPrecio, gbc); y++;
        gbc.gridx=0; gbc.gridy=y; form.add(new JLabel("Unidad medida:"), gbc); gbc.gridx=1; form.add(txtUnidad, gbc); y++;

        JButton ok = new JButton(editing==null?"Crear":"Guardar"); UIStyles.styleButton(ok);
        JButton cancel = new JButton("Cancelar"); UIStyles.styleButton(cancel);
        JPanel act = new JPanel(new FlowLayout(FlowLayout.RIGHT)); act.setOpaque(false); act.add(ok); act.add(cancel);
        gbc.gridx=0; gbc.gridy=y; gbc.gridwidth=2; form.add(act, gbc);

        if (editing != null) {
            txtCodigo.setText(editing.getCodigoProducto()); txtCodigo.setEnabled(false);
            txtNombre.setText(editing.getNombreProducto()); txtPrecio.setText(String.valueOf(editing.getPrecioBase())); txtUnidad.setText(editing.getUnidadMedida());
        }

        cancel.addActionListener(e-> UIStyles.showMainMenu(FrmProducto.this));
        ok.addActionListener(e->{
            String cod = txtCodigo.getText().trim(); String nombre = txtNombre.getText().trim(); String pr = txtPrecio.getText().trim(); String unidad = txtUnidad.getText().trim();
            if (cod.isEmpty() || nombre.isEmpty() || unidad.isEmpty()) { JOptionPane.showMessageDialog(this, "Complete los campos obligatorios"); return; }
            double precio;
            try {
                precio = Double.parseDouble(pr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Precio inválido");
                return;
            }
            if (precio < 0) { JOptionPane.showMessageDialog(this, "Precio debe ser >= 0"); return; }

            Producto p = new Producto(cod, nombre, precio, unidad);
            boolean okc;
            if (editing == null) okc = dao.crear(p); else okc = dao.actualizar(p);
            if (okc) { JOptionPane.showMessageDialog(this, editing==null?"Producto creado":"Producto actualizado"); dispose(); } else JOptionPane.showMessageDialog(this, "Error al guardar producto");
        });

        root.add(header, BorderLayout.NORTH); root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }
}
