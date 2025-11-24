package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * Minimal modern-looking DatePicker: text field + button that opens a popup calendar.
 * - Displays date as dd/MM/yyyy
 * - Allows month navigation and picking a day
 * - No external libs required
 */
public class DatePicker extends JPanel {
    private final JTextField txt;
    private final JButton btn;
    private JDialog popup;
    private LocalDate selected;
    private YearMonth viewing;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public DatePicker() {
        this(LocalDate.now());
    }

    public DatePicker(LocalDate initial) {
        setLayout(new BorderLayout(4,0));
        setOpaque(false);
        txt = new JTextField(10);
        txt.setEditable(false);
        txt.setBackground(Color.WHITE);
        btn = new JButton("▾");
        btn.setFocusable(false);
        UIStyles.styleButton(btn);
        add(txt, BorderLayout.CENTER);
        add(btn, BorderLayout.EAST);

        popup = null; // created on demand as an undecorated JDialog so child popups work

        selected = initial;
        viewing = YearMonth.from(selected == null ? LocalDate.now() : selected);
        updateText();

        btn.addActionListener(e -> togglePopup());
        txt.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(MouseEvent e){ togglePopup(); } });
    }

    // togglePopup(): muestra/oculta el panel calendario (popup) asociado
    // al datepicker. Si el popup ya está visible lo oculta, si no lo crea.
    private void togglePopup() {
        if (popup != null && popup.isVisible()) { popup.setVisible(false); return; }
        showDialogAt();
    }

    private JPanel createCalendarPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIStyles.PANEL);
        JPanel head = new JPanel(new BorderLayout(8,0)); head.setOpaque(false);
        // Fast month/year selectors (no prev/next buttons)
        JPanel selectors = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0)); selectors.setOpaque(false);
        String[] monthNames = new java.text.DateFormatSymbols().getMonths();
        JComboBox<String> cbMonth = new JComboBox<>();
        for (int m = 0; m < 12; m++) cbMonth.addItem(monthNames[m]);
        JComboBox<Integer> cbYear = new JComboBox<>();
        int baseYear = LocalDate.now().getYear();
        // allow 5 more years backward per user request
        for (int y = baseYear - 55; y <= baseYear + 10; y++) cbYear.addItem(y);
        selectors.add(cbMonth); selectors.add(cbYear);

        head.add(selectors, BorderLayout.CENTER);
        p.add(head, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0,7,4,4)); grid.setOpaque(false);
        String[] days = {"Do","Lu","Ma","Mi","Ju","Vi","Sa"};
        for (String d : days) { JLabel ld = new JLabel(d, SwingConstants.CENTER); ld.setForeground(UIStyles.GREEN_DARK); grid.add(ld); }

        YearMonth ym = viewing;
        java.time.LocalDate first = ym.atDay(1);
        int start = first.getDayOfWeek().getValue() % 7; // Sunday=0
        int length = ym.lengthOfMonth();
        // fill blanks
        for (int i=0;i<start;i++) grid.add(new JLabel());
        for (int d=1; d<=length; d++) {
            LocalDate day = ym.atDay(d);
            JButton b = new JButton(String.valueOf(d));
            b.setFocusable(false);
            b.setBackground(Color.WHITE);
            if (selected != null && selected.equals(day)) {
                b.setBackground(UIStyles.CARD_HOVER);
            }
            b.addActionListener(ev -> {
                selected = day;
                updateText();
                popup.setVisible(false);
            });
            grid.add(b);
        }

        p.add(grid, BorderLayout.CENTER);

        // Initialize selectors to current viewing
        cbMonth.setSelectedIndex(viewing.getMonthValue() - 1);
        cbYear.setSelectedItem(viewing.getYear());

        // make comboboxes easy to interact with: focusable and respond to mouse wheel
        cbMonth.setFocusable(true);
        cbYear.setFocusable(true);

        cbMonth.addMouseWheelListener(ev -> {
            int rot = ev.getWheelRotation();
            int idx = cbMonth.getSelectedIndex();
            int newIdx = Math.max(0, Math.min(11, idx + rot));
            if (newIdx != idx) {
                cbMonth.setSelectedIndex(newIdx);
                int ysel = cbYear.getItemAt(cbYear.getSelectedIndex());
                viewing = YearMonth.of(ysel, newIdx + 1);
                refreshPopup();
            }
        });

        cbYear.addMouseWheelListener(ev -> {
            int rot = ev.getWheelRotation();
            int idx = cbYear.getSelectedIndex();
            int newIdx = Math.max(0, Math.min(cbYear.getItemCount() - 1, idx + rot));
            if (newIdx != idx) {
                cbYear.setSelectedIndex(newIdx);
                int m = cbMonth.getSelectedIndex() + 1;
                int year = cbYear.getItemAt(newIdx);
                viewing = YearMonth.of(year, m);
                refreshPopup();
            }
        });

        // When user picks month/year, update viewing and refresh calendar
        cbMonth.addActionListener(ev -> {
            int m = cbMonth.getSelectedIndex() + 1;
            int y = (Integer) cbYear.getSelectedItem();
            viewing = YearMonth.of(y, m);
            refreshPopup();
        });
        cbYear.addActionListener(ev -> {
            int m = cbMonth.getSelectedIndex() + 1;
            int y = (Integer) cbYear.getSelectedItem();
            viewing = YearMonth.of(y, m);
            refreshPopup();
        });

        p.setBorder(BorderFactory.createEmptyBorder(8,8,8,8));
        return p;
    }

    private void updateText() {
        if (selected != null) txt.setText(selected.format(fmt)); else txt.setText("");
    }

    // updateText(): actualiza el campo de texto con la fecha seleccionada
    // usando el formato `dd/MM/yyyy`. Si no hay selección, limpia el texto.

    private void refreshPopup() {
        // rebuild dialog content and show dialog at the same position
        if (popup == null) { showDialogAt(); return; }
        popup.getContentPane().removeAll();
        popup.getContentPane().add(createCalendarPanel());
        popup.pack();
        Point loc = getLocationOnScreen();
        popup.setLocation(loc.x, loc.y + getHeight());
        popup.setVisible(true);
    }

    // refreshPopup(): reconstruye el contenido del popup cuando cambia
    // el mes/año en visualización.

    private void showDialogAt() {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) owner = JOptionPane.getRootFrame();
        popup = new JDialog(owner);
        popup.setUndecorated(true);
        popup.setFocusableWindowState(true);
        popup.setAlwaysOnTop(true);
        try { popup.setType(Window.Type.POPUP); } catch (Throwable ignored) {}
        popup.getContentPane().setLayout(new BorderLayout());
        popup.getContentPane().add(createCalendarPanel());
        popup.pack();
        Point loc = getLocationOnScreen();
        popup.setLocation(loc.x, loc.y + getHeight());
        // hide when focus lost/click outside
        popup.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent e) { popup.setVisible(false); }
        });
        popup.setVisible(true);
    }

    // showDialogAt(): crea y posiciona el JDialog no decorado que contiene
    // el calendario; lo hace siempre encima de la ventana padre y lo
    // oculta cuando pierde foco.

    public java.sql.Date getDate() {
        if (selected == null) return null;
        return java.sql.Date.valueOf(selected);
    }

    // getDate(): devuelve la fecha seleccionada como `java.sql.Date` o null.

    public void setDate(java.sql.Date d) {
        if (d == null) { selected = null; updateText(); return; }
        selected = d.toLocalDate(); viewing = YearMonth.from(selected);
        updateText();
    }

    // setDate(): establece la fecha inicial del DatePicker desde un
    // `java.sql.Date` (null para limpiar selección).

    public String getText() { return txt.getText(); }

    // getText(): retorna la representación textual (dd/MM/yyyy) mostrada.
}
