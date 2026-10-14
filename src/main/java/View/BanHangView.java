package View;

import DAO.*;
import Model.*;
import Utils.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BanHangView extends JPanel {
    private final DienThoaiDAO catalog=new DienThoaiDAO();private final BanHangDAO sales=new BanHangDAO();
    private final List<GioHangItem> cart=new ArrayList<>();private List<DienThoaiDAO.May> phones=new ArrayList<>(),visible=new ArrayList<>();
    private final JTextField search=new JTextField(),customer=new JTextField(),customerPhone=new JTextField(),discount=new JTextField("0");
    private final JComboBox<String> brand=new JComboBox<>(),payment=new JComboBox<>(new String[]{"Tiền mặt","VietQR","Chuyển khoản"});private final JComboBox<NhanVien> staff=new JComboBox<>();
    private final DefaultTableModel model=Theme.model("Thiết bị","SL","Thành tiền"),catalogModel=Theme.model("Thiết bị","Bộ nhớ","Giá bán","Còn");
    private final JTable table=Theme.table(model),devices=Theme.table(catalogModel);
    private final JLabel total=Theme.label("0 đ",30,Theme.ACCENT),sub=Theme.label("0 đ",13,Theme.INK),device=Theme.label("Chọn một máy trong danh sách",20,Theme.INK),specs=Theme.label(" ",13,Theme.MUTED);
    private final JButton add=Theme.button("+ Thêm máy vào đơn",true);
    public BanHangView(){
        setLayout(new BorderLayout(0,18));setBackground(Theme.BG);setBorder(BorderFactory.createEmptyBorder(22,30,22,30));add(Theme.heading("Bán máy tại quầy","01 Chọn máy  /  02 Kiểm tra đơn  /  03 Thanh toán và kích hoạt bảo hành"),BorderLayout.NORTH);
        JPanel receipt=Theme.card(new BorderLayout(0,8));receipt.setBorder(BorderFactory.createEmptyBorder(12,18,12,18));receipt.setPreferredSize(new Dimension(450,0));JPanel title=Theme.transparent(new GridLayout(2,1,0,4));title.add(Theme.label("ĐƠN BÁN HIỆN TẠI",11,Theme.ACCENT));title.add(Theme.label("Phiếu tính tiền",23,Theme.INK));receipt.add(title,BorderLayout.NORTH);
        table.getColumnModel().getColumn(0).setPreferredWidth(220);table.getColumnModel().getColumn(1).setMaxWidth(40);table.getColumnModel().getColumn(2).setPreferredWidth(135);JScrollPane cartScroll=new JScrollPane(table);cartScroll.setMinimumSize(new Dimension(0,130));receipt.add(cartScroll,BorderLayout.CENTER);
        JPanel footer=Theme.transparent(new BorderLayout(0,7));JPanel actions=Theme.transparent(new GridLayout(1,3,6,0));JButton quantity=Theme.button("Số lượng",false),remove=Theme.button("Bỏ máy",false),clear=Theme.button("Đơn mới",false);for(JButton button:new JButton[]{quantity,remove,clear})button.setMargin(new Insets(5,8,5,8));quantity.addActionListener(e->editQuantity());remove.addActionListener(e->{int row=table.getSelectedRow();if(row>=0){cart.remove(row);renderCart();}});clear.addActionListener(e->{if(cart.isEmpty()||JOptionPane.showConfirmDialog(this,"Bỏ toàn bộ máy trong đơn hiện tại?","Đơn mới",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION){cart.clear();renderCart();}});actions.add(quantity);actions.add(remove);actions.add(clear);footer.add(actions,BorderLayout.NORTH);
        JPanel fields=Theme.transparent(new GridLayout(0,2,8,4));fields.add(Theme.label("Khách hàng",12,Theme.MUTED));fields.add(customer);fields.add(Theme.label("Số điện thoại",12,Theme.MUTED));fields.add(customerPhone);fields.add(Theme.label("Nhân viên lập đơn",12,Theme.MUTED));fields.add(staff);fields.add(Theme.label("Giảm giá (VNĐ)",12,Theme.MUTED));fields.add(discount);fields.add(Theme.label("Phương thức",12,Theme.MUTED));fields.add(payment);fields.add(Theme.label("Tổng trước giảm",12,Theme.MUTED));fields.add(sub);footer.add(fields);
        JPanel finish=Theme.transparent(new BorderLayout(0,9));finish.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.LINE));JPanel value=Theme.transparent(new BorderLayout());value.setBorder(BorderFactory.createEmptyBorder(12,0,4,0));value.add(Theme.label("CẦN THU",11,Theme.MUTED));value.add(total,BorderLayout.EAST);finish.add(value,BorderLayout.NORTH);JButton checkout=Theme.button("Hoàn tất đơn bán  →",true);checkout.addActionListener(e->checkout());finish.add(checkout);JLabel bank=Theme.label("MB 0787911669 · NGUYEN THI NGOC THUC",10,Theme.MUTED);bank.setHorizontalAlignment(SwingConstants.CENTER);finish.add(bank,BorderLayout.SOUTH);footer.add(finish,BorderLayout.SOUTH);receipt.add(footer,BorderLayout.SOUTH);
        JPanel selection=Theme.card(new BorderLayout(0,18));JPanel searchBar=Theme.transparent(new BorderLayout(10,0));search.putClientProperty("JTextField.placeholderText","Tên máy hoặc dung lượng...");search.setPreferredSize(new Dimension(200,42));searchBar.add(search);brand.setPreferredSize(new Dimension(145,42));searchBar.add(brand,BorderLayout.WEST);JButton refresh=Theme.button("Kho máy",false);refresh.addActionListener(e->load());searchBar.add(refresh,BorderLayout.EAST);selection.add(searchBar,BorderLayout.NORTH);
        devices.getColumnModel().getColumn(0).setPreferredWidth(240);devices.getColumnModel().getColumn(1).setPreferredWidth(100);devices.getColumnModel().getColumn(2).setPreferredWidth(150);devices.getColumnModel().getColumn(3).setMaxWidth(65);selection.add(new JScrollPane(devices));
        JPanel selected=Theme.transparent(new BorderLayout(16,0));selected.setBorder(BorderFactory.createMatteBorder(1,0,0,0,Theme.LINE));JPanel words=Theme.transparent(new GridLayout(3,1,0,7));words.setBorder(BorderFactory.createEmptyBorder(16,0,0,0));words.add(Theme.label("THIẾT BỊ ĐANG CHỌN",11,Theme.ACCENT));words.add(device);words.add(specs);selected.add(words);add.setEnabled(false);add.addActionListener(e->{int row=devices.getSelectedRow();if(row>=0)add(visible.get(row));});JPanel addArea=Theme.transparent(new GridBagLayout());addArea.add(add);selected.add(addArea,BorderLayout.EAST);selection.add(selected,BorderLayout.SOUTH);
        JPanel workspace=Theme.transparent(new BorderLayout(20,0));workspace.add(receipt,BorderLayout.WEST);workspace.add(selection);add(workspace);
        search.getDocument().addDocumentListener(Theme.onText(this::renderProducts));discount.getDocument().addDocumentListener(Theme.onText(this::updateTotal));brand.addActionListener(e->renderProducts());devices.getSelectionModel().addListSelectionListener(e->showDevice());devices.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2&&devices.getSelectedRow()>=0)add(visible.get(devices.getSelectedRow()));}});
        try{for(var person:sales.getAllNhanVien())if(Session.isAdmin()||java.util.Objects.equals(Session.getTaiKhoan().getMaNV(),person.getMaNV()))staff.addItem(person);}catch(Exception e){Theme.error(this,e);}staff.setEnabled(Session.isAdmin());load();
    }
    private void load(){try{phones=catalog.all();Object old=brand.getSelectedItem();brand.removeAllItems();brand.addItem("Tất cả hãng");for(var b:catalog.brands())brand.addItem(b.ten());brand.setSelectedItem(old==null?"Tất cả hãng":old);renderProducts();}catch(Exception e){Theme.error(this,e);}}
    private void renderProducts(){devices.clearSelection();visible=new ArrayList<>();catalogModel.setRowCount(0);String q=TextSearch.normalize(search.getText());Object selectedBrand=brand.getSelectedItem();for(var p:phones){var s=p.sp();if(!s.isTrangThai()||!TextSearch.normalize(s.getTenSP()+" "+s.getDungLuong()+" "+p.hang()).contains(q)||(selectedBrand!=null&&!selectedBrand.equals("Tất cả hãng")&&!selectedBrand.equals(p.hang())))continue;visible.add(p);catalogModel.addRow(new Object[]{s.getTenSP(),s.getDungLuong(),Theme.money(s.getDonGia()),p.tonKho()});}showDevice();}
    private void showDevice(){int row=devices.getSelectedRow();if(row<0||row>=visible.size()){device.setText("Chọn một máy trong danh sách");specs.setText("Nhấp đúp để thêm nhanh vào đơn.");add.setEnabled(false);return;}var p=visible.get(row);device.setText(p.sp().getTenSP()+" / "+p.sp().getDungLuong());specs.setText(p.hang()+" · "+Theme.money(p.sp().getDonGia())+" · Còn "+p.tonKho()+" máy");add.setEnabled(p.tonKho()>0);}
    private void add(DienThoaiDAO.May p){if(p.tonKho()==0){JOptionPane.showMessageDialog(this,"Máy đã hết hàng.");return;}for(var line:cart)if(line.getSanPham().getMaSP()==p.sp().getMaSP()){if(line.getSoLuong()>=p.tonKho()){JOptionPane.showMessageDialog(this,"Đã chọn hết số máy còn trong kho.");return;}line.setSoLuong(line.getSoLuong()+1);renderCart();return;}cart.add(new GioHangItem(p.sp(),1));renderCart();}
    private void editQuantity(){int row=table.getSelectedRow();if(row<0)return;String value=JOptionPane.showInputDialog(this,"Số lượng mới:",cart.get(row).getSoLuong());if(value==null)return;try{int n=Integer.parseInt(value);var line=cart.get(row);int available=phones.stream().filter(p->p.sp().getMaSP()==line.getSanPham().getMaSP()).findFirst().orElseThrow().tonKho();if(n<1||n>available)throw new IllegalArgumentException("Số lượng từ 1 đến "+available);line.setSoLuong(n);renderCart();}catch(Exception e){Theme.error(this,e);}}
    private BigDecimal subtotal(){BigDecimal value=BigDecimal.ZERO;for(var i:cart)value=value.add(i.getThanhTien());return value;}
    private BigDecimal getDiscount(){BigDecimal d=new BigDecimal(discount.getText().trim());if(d.signum()<0||d.compareTo(subtotal())>0||d.scale()>2)throw new IllegalArgumentException("Giảm giá không hợp lệ.");return d;}
    private void renderCart(){model.setRowCount(0);for(var i:cart)model.addRow(new Object[]{i.getSanPham().getTenSP()+" / "+i.getSanPham().getDungLuong(),i.getSoLuong(),Theme.money(i.getThanhTien())});updateTotal();}
    private void updateTotal(){sub.setText(Theme.money(subtotal()));try{total.setText(Theme.money(subtotal().subtract(getDiscount())));}catch(Exception e){total.setText("Kiểm tra giảm giá");}}
    private void checkout(){
        long id;BigDecimal amount;String method=(String)payment.getSelectedItem();
        try{if(cart.isEmpty())throw new IllegalArgumentException("Chọn điện thoại trước khi thanh toán.");var person=(NhanVien)staff.getSelectedItem();if(person==null)throw new IllegalArgumentException("Chưa có nhân viên phụ trách.");BigDecimal d=getDiscount();amount=subtotal().subtract(d);if(!method.equals("Tiền mặt")&&amount.signum()<=0)throw new IllegalArgumentException("Thanh toán QR cần số tiền lớn hơn 0.");
            if(JOptionPane.showConfirmDialog(this,"Lưu đơn hàng "+Theme.money(amount)+" bằng "+method+"?","Xác nhận hóa đơn",JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;
            id=sales.taoHoaDon(Session.getTaiKhoan(),person.getMaNV(),cart,d,method,customer.getText(),customerPhone.getText());
        }catch(Exception e){Theme.error(this,e);return;}
        // Clear only after commit: QR download failure cannot create the same invoice twice.
        cart.clear();discount.setText("0");customer.setText("");customerPhone.setText("");renderCart();load();
        if(method.equals("Tiền mặt")){JOptionPane.showMessageDialog(this,"Đã lưu đơn bán #"+id+". Xem PDF tại Hóa đơn; đăng ký IMEI tại Sổ IMEI / Bảo hành.");return;}
        new VietQRDialog(SwingUtilities.getWindowAncestor(this),id,amount,ThanhToanConfig.taoUrl(id,amount)).setVisible(true);
        if(JOptionPane.showConfirmDialog(this,"Đã kiểm tra tài khoản MB và nhận đủ "+Theme.money(amount)+" cho hóa đơn #"+id+"?\nChọn Không để giữ trạng thái chờ thanh toán.","Xác nhận tiền đã về",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION)
            try{new HoaDonDAO().xacNhanThanhToan(id);}catch(Exception e){Theme.error(this,e);}
    }
}
