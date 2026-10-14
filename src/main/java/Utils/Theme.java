package Utils;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;

public final class Theme {
    public static final Color INK = new Color(43, 42, 39), ACCENT = new Color(214, 86, 59),
            TEAL = ACCENT, BG = new Color(247, 245, 240), MUTED = new Color(119, 117, 108),
            LINE = new Color(226, 223, 216), SOFT = new Color(251, 233, 225);
    private Theme() {}
    public static void install() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.arc", 10); UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10); UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Panel.background", BG);UIManager.put("Component.borderColor",LINE);
        UIManager.put("Table.rowHeight", 42); UIManager.put("Table.selectionBackground", SOFT);
        UIManager.put("Table.selectionForeground", INK); UIManager.put("Table.showVerticalLines", false);
    }
    public static String money(Object value) { return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value) + " đ"; }
    public static JLabel label(String text, int size, Color color) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", size >= 18 ? Font.BOLD : Font.PLAIN, size)); l.setForeground(color); return l;
    }
    public static JButton button(String text, boolean primary) {
        JButton b = new JButton(text); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(primary ? TEAL : Color.WHITE); b.setForeground(primary ? Color.WHITE : INK);
        b.setMargin(new Insets(10,16,10,16)); return b;
    }
    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout) {
            @Override protected void paintComponent(Graphics graphics) {
                Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(getBackground());g.fillRoundRect(0,0,getWidth(),getHeight(),18,18);g.dispose();super.paintComponent(graphics);
            }
        };p.setOpaque(false);p.setBackground(Color.WHITE);p.setBorder(new EmptyBorder(20,22,20,22)); return p;
    }
    public static JPanel heading(String title, String subtitle) {
        JPanel p = new JPanel(new GridLayout(2,1,0,5)); p.setOpaque(false);
        p.add(label(title,25,INK)); p.add(label(subtitle,13,MUTED)); p.setBorder(new EmptyBorder(0,0,18,0)); return p;
    }
    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns,0) { @Override public boolean isCellEditable(int row,int col) {return false;} };
    }
    public static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int col) {
                Component c=super.prepareRenderer(renderer,row,col);
                if(!isRowSelected(row)){c.setBackground(row%2==0?Color.WHITE:new Color(252,250,246));c.setForeground(INK);}
                if(c instanceof JLabel label)label.setBorder(new EmptyBorder(0,10,0,10));return c;
            }
        };t.setRowHeight(44); t.setShowGrid(false); t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI",Font.BOLD,13)); t.getTableHeader().setPreferredSize(new Dimension(0,42)); return t;
    }
    public static void error(Component parent, Exception error) { JOptionPane.showMessageDialog(parent,error.getMessage(),"Kiểm tra thông tin",JOptionPane.WARNING_MESSAGE); }
    public static JPanel transparent(LayoutManager layout){JPanel p=new JPanel(layout);p.setOpaque(false);return p;}
    public static javax.swing.event.DocumentListener onText(Runnable task){return new javax.swing.event.DocumentListener(){public void insertUpdate(javax.swing.event.DocumentEvent e){task.run();}public void removeUpdate(javax.swing.event.DocumentEvent e){task.run();}public void changedUpdate(javax.swing.event.DocumentEvent e){task.run();}};}
}
