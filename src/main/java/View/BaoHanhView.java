package View;

import DAO.BaoHanhDAO;
import Utils.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BaoHanhView extends JPanel {
    private final BaoHanhDAO dao=new BaoHanhDAO();private final DefaultTableModel model=Theme.model("IMEI","Thiết bị","Khách hàng","Liên hệ","Hạn bảo hành","Dịch vụ");private final JTable table=Theme.table(model);
    private final JTextField search=new JTextField();private final JComboBox<String> filter=new JComboBox<>(new String[]{"Tất cả máy","Còn bảo hành","Đã hết hạn","Đang sửa","Chờ trả khách"});
    private final JComboBox<String> stage=new JComboBox<>(BaoHanhDAO.STAGES);private final JTextArea note=new JTextArea(3,35);private final JLabel details=Theme.label("Chọn một IMEI để xem nguồn bán và cập nhật dịch vụ.",13,Theme.MUTED),count=Theme.label(" ",13,Theme.MUTED);
    private List<BaoHanhDAO.Phieu> all=new ArrayList<>(),visible=new ArrayList<>();
    private static final DateTimeFormatter DATE=DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public BaoHanhView(){
        setLayout(new BorderLayout(0,18));setBackground(Theme.BG);setBorder(BorderFactory.createEmptyBorder(22,30,24,30));
        JPanel top=Theme.transparent(new BorderLayout());top.add(Theme.heading("Sổ IMEI & bảo hành","Tra cứu máy đã bán, thời hạn và tình trạng sau bán"));JButton register=Theme.button("+ Kích hoạt bảo hành",true);register.addActionListener(e->register());JPanel action=Theme.transparent(new FlowLayout(FlowLayout.RIGHT,0,0));action.add(register);top.add(action,BorderLayout.EAST);add(top,BorderLayout.NORTH);
        JPanel body=Theme.card(new BorderLayout(0,15));JPanel bar=Theme.transparent(new BorderLayout(12,0));search.putClientProperty("JTextField.placeholderText","Tìm IMEI, tên khách, số điện thoại hoặc máy...");search.setPreferredSize(new Dimension(400,42));bar.add(search);filter.setPreferredSize(new Dimension(180,42));bar.add(filter,BorderLayout.WEST);JButton refresh=Theme.button("Làm mới",false);refresh.addActionListener(e->reload());bar.add(refresh,BorderLayout.EAST);body.add(bar,BorderLayout.NORTH);body.add(new JScrollPane(table));body.add(count,BorderLayout.SOUTH);add(body);
        int[] widths={180,240,150,120,180,180};for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        JPanel service=Theme.card(new BorderLayout(18,10));service.add(details,BorderLayout.NORTH);note.setLineWrap(true);note.setWrapStyleWord(true);service.add(new JScrollPane(note));JPanel controls=Theme.transparent(new GridLayout(4,1,0,7));controls.add(Theme.label("TÌNH TRẠNG DỊCH VỤ",11,Theme.MUTED));controls.add(stage);JButton save=Theme.button("Cập nhật xử lý",true);save.addActionListener(e->{var p=selected();if(p==null)return;try{dao.update(p.id(),(String)stage.getSelectedItem(),note.getText());reload();JOptionPane.showMessageDialog(this,"Đã cập nhật tình trạng bảo hành.");}catch(Exception x){Theme.error(this,x);}});controls.add(save);JButton delete=Theme.button("Xóa phiếu bảo hành",false);delete.addActionListener(e->{var p=selected();if(p==null)return;if(JOptionPane.showConfirmDialog(this,"Xóa phiếu bảo hành IMEI "+p.imei()+"? IMEI này có thể được đăng ký lại sau khi xóa.","Xác nhận xóa",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;try{dao.delete(p.id());reload();}catch(Exception x){Theme.error(this,x);}});controls.add(delete);service.add(controls,BorderLayout.EAST);add(service,BorderLayout.SOUTH);
        search.getDocument().addDocumentListener(Theme.onText(this::filter));filter.addActionListener(e->filter());table.getSelectionModel().addListSelectionListener(e->{var p=selected();if(p!=null){details.setText("HD"+p.hoaDon()+" · "+p.khach()+" · Mua "+p.ngayBan().format(DATE)+" · "+(p.conHan()?"Còn bảo hành":"Đã hết hạn"));stage.setSelectedItem(p.trangThai());note.setText(p.ghiChu());}});reload();
    }
    private BaoHanhDAO.Phieu selected(){int row=table.getSelectedRow();return row>=0&&row<visible.size()?visible.get(row):null;}
    private void reload(){try{all=dao.all();filter();}catch(Exception e){Theme.error(this,e);}}
    private void filter(){table.clearSelection();model.setRowCount(0);visible=new ArrayList<>();String query=TextSearch.normalize(search.getText());int mode=filter.getSelectedIndex();for(var p:all){boolean include=switch(mode){case 1->p.conHan();case 2->!p.conHan();case 3->p.trangThai().equals("Đang sửa");case 4->p.trangThai().equals("Chờ trả khách");default->true;};if(!include||!TextSearch.normalize(p.imei()+" "+p.tenMay()+" "+p.khach()+" "+p.sdt()).contains(query))continue;visible.add(p);model.addRow(new Object[]{p.imei(),p.tenMay(),p.khach(),p.sdt(),p.hetHan().format(DATE)+(p.conHan()?" · Còn hạn":" · Hết hạn"),p.trangThai()});}count.setText(visible.size()+" thiết bị  /  Mỗi IMEI gắn với một máy trong hóa đơn đã thanh toán");details.setText("Chọn một IMEI để cập nhật tình trạng dịch vụ.");note.setText("");}
    private void register(){
        List<BaoHanhDAO.DongBan> sold;try{sold=dao.cacMayDaBan();}catch(Exception e){Theme.error(this,e);return;}if(sold.isEmpty()){JOptionPane.showMessageDialog(this,"Chưa có máy trong hóa đơn đã thanh toán. Hãy lập đơn trước.");return;}
        JComboBox<BaoHanhDAO.DongBan> line=new JComboBox<>(sold.toArray(BaoHanhDAO.DongBan[]::new));JComboBox<String> imei=new JComboBox<>();imei.setEditable(true);JTextField customer=new JTextField(),phone=new JTextField();JSpinner months=new JSpinner(new SpinnerNumberModel(12,1,36,1));
        Runnable fillInvoice=()->{var selected=(BaoHanhDAO.DongBan)line.getSelectedItem();if(selected!=null){customer.setText(selected.khach());phone.setText(selected.sdt());imei.removeAllItems();try{for(String value:dao.imeiChuaDangKy(selected.id()))imei.addItem(value);if(imei.getItemCount()==0)imei.getEditor().setItem("");}catch(Exception x){Theme.error(this,x);}}};line.addActionListener(e->fillInvoice.run());fillInvoice.run();
        JPanel fields=Theme.transparent(new GridLayout(0,1,0,7));fields.add(new JLabel("Máy trong hóa đơn đã thanh toán"));fields.add(line);fields.add(new JLabel("IMEI máy (tự lấy từ máy đã nhập kho; máy cũ chưa có IMEI thì nhập tay)"));fields.add(imei);fields.add(new JLabel("Tên khách hàng (tự lấy từ hóa đơn)"));fields.add(customer);fields.add(new JLabel("Số điện thoại (tự lấy nếu hóa đơn có lưu)"));fields.add(phone);fields.add(new JLabel("Thời hạn tính từ ngày mua (tháng)"));fields.add(months);
        while(JOptionPane.showConfirmDialog(this,fields,"Kích hoạt bảo hành theo IMEI",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){try{months.commitEdit();Object serial=imei.getEditor().getItem();dao.register(((BaoHanhDAO.DongBan)line.getSelectedItem()).id(),serial==null?"":serial.toString().trim(),customer.getText().trim(),phone.getText().trim(),((Number)months.getValue()).intValue());reload();return;}catch(Exception e){Theme.error(this,e);}}
    }
}
