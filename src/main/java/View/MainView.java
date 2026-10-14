package View;

import Utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainView extends JFrame {
    private final JPanel content=Theme.transparent(new BorderLayout());
    private final Map<String,JButton> tabs=new LinkedHashMap<>();
    public MainView(){
        setTitle("NOVA • Cửa hàng điện thoại");setSize(1440,920);setMinimumSize(new Dimension(1160,780));setLocationRelativeTo(null);ExitConfirmation.install(this);
        JPanel root=new JPanel(new BorderLayout());root.setBackground(Theme.BG);
        JPanel masthead=new JPanel(new BorderLayout(20,0));masthead.setBackground(Color.WHITE);masthead.setBorder(new EmptyBorder(17,30,14,30));
        JPanel logo=Theme.transparent(new FlowLayout(FlowLayout.LEFT,12,0));JLabel mark=Theme.label(" N ",24,Color.WHITE);mark.setOpaque(true);mark.setBackground(Theme.ACCENT);mark.setBorder(new EmptyBorder(4,5,4,5));logo.add(mark);logo.add(Theme.label("NOVA",24,Theme.INK));logo.add(Theme.label("/  ĐIỆN THOẠI",12,Theme.MUTED));masthead.add(logo,BorderLayout.WEST);
        JPanel controls=Theme.transparent(new FlowLayout(FlowLayout.RIGHT,12,0));controls.add(Theme.label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),13,Theme.MUTED));
        if(Session.isAdmin()){
            JButton manage=Theme.button("Quản trị",false);JPopupMenu menu=new JPopupMenu();for(String page:new String[]{"Nhân viên","Tài khoản","Thống kê","Cấu hình Gmail"}){JMenuItem item=new JMenuItem(page);item.addActionListener(e->showPage(page));menu.add(item);}manage.addActionListener(e->menu.show(manage,0,manage.getHeight()));controls.add(manage);
        }
        JButton profile=Theme.button(Session.getTaiKhoan().getTenDangNhap()+"",false);JPopupMenu account=new JPopupMenu();JMenuItem password=new JMenuItem("Đổi mật khẩu"),logout=new JMenuItem("Đăng xuất");password.addActionListener(e->showPage("Đổi mật khẩu"));logout.addActionListener(e->logout());account.add(password);account.addSeparator();account.add(logout);profile.addActionListener(e->account.show(profile,0,profile.getHeight()));controls.add(profile);masthead.add(controls,BorderLayout.EAST);
        JPanel navigation=Theme.transparent(new FlowLayout(FlowLayout.LEFT,8,10));navigation.setBorder(new EmptyBorder(0,24,0,24));addTab(navigation,"Tổng quan","01  Bàn điều hành");addTab(navigation,"Bán hàng","02  Lập đơn");if(Session.isAdmin())addTab(navigation,"Điện thoại & kho","03  Kho thiết bị");addTab(navigation,"Hóa đơn","04  Hóa đơn");addTab(navigation,"Bảo hành","05  Sổ IMEI / Bảo hành");
        JPanel header=new JPanel(new BorderLayout());header.setBackground(Theme.BG);header.add(masthead,BorderLayout.NORTH);header.add(navigation,BorderLayout.SOUTH);root.add(header,BorderLayout.NORTH);root.add(content,BorderLayout.CENTER);
        JPanel status=Theme.transparent(new BorderLayout());status.setBorder(new EmptyBorder(7,30,9,30));status.add(Theme.label("NOVA  /  Kho thiết bị · Giao dịch · Chăm sóc sau bán",11,Theme.MUTED));status.add(Theme.label(Session.isAdmin()?"QUẢN TRỊ VIÊN":"NHÂN VIÊN BÁN HÀNG",11,Theme.MUTED),BorderLayout.EAST);root.add(status,BorderLayout.SOUTH);setContentPane(root);showPage("Tổng quan");
    }
    private void addTab(JPanel bar,String page,String caption){JButton tab=Theme.button(caption,false);tab.setBorderPainted(false);tab.addActionListener(e->showPage(page));tabs.put(page,tab);bar.add(tab);}
    private void logout(){if(JOptionPane.showConfirmDialog(this,"Kết thúc phiên làm việc?","Đăng xuất",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){RememberLogin.clear();Session.logout();dispose();new LoginView().setVisible(true);}}
    private void showPage(String name){
        if(java.util.Set.of("Điện thoại & kho","Nhân viên","Tài khoản","Thống kê","Cấu hình Gmail").contains(name)&&!Session.isAdmin())return;
        JPanel page=switch(name){case "Bán hàng"->new BanHangView();case "Điện thoại & kho"->new SanPhamView();case "Hóa đơn"->new HoaDonView();case "Bảo hành"->new BaoHanhView();case "Nhân viên"->new NhanVienView();case "Tài khoản"->new TaiKhoanView();case "Thống kê"->new ThongKeView();case "Cấu hình Gmail"->new GmailAdminView();case "Đổi mật khẩu"->new DoiMatKhauView();default->new DashboardView(this::showPage);};
        for(var entry:tabs.entrySet()){boolean active=entry.getKey().equals(name);entry.getValue().setBackground(active?Theme.INK:Theme.BG);entry.getValue().setForeground(active?Color.WHITE:Theme.MUTED);}
        content.removeAll();content.add(page);content.revalidate();content.repaint();
    }
}