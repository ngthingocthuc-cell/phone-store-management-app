package View;

import DAO.DienThoaiDAO;
import Utils.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SanPhamView extends JPanel {
    private final DienThoaiDAO dao=new DienThoaiDAO();
    private final DefaultTableModel model=Theme.model("Mã máy","Thiết bị","Hãng","Bộ nhớ","Giá bán","Tồn","IMEI trong kho","Tình trạng");private final JTable table=Theme.table(model);
    private final JTextField search=new JTextField();private final JComboBox<String> filter=new JComboBox<>(new String[]{"Toàn bộ thiết bị","Đang kinh doanh","Sắp hết (≤ 5)","Đã hết hàng","Ngừng kinh doanh"});
    private final JLabel summary=Theme.label(" ",14,Theme.MUTED),detail=Theme.label("Chọn một máy để xem mô tả.",13,Theme.MUTED);
    private List<DienThoaiDAO.May> all=new ArrayList<>(),visible=new ArrayList<>();
    public SanPhamView(){
        setLayout(new BorderLayout(0,18));setBackground(Theme.BG);setBorder(BorderFactory.createEmptyBorder(22,30,24,30));
        JPanel header=Theme.transparent(new BorderLayout(15,0));header.add(Theme.heading("Kho thiết bị","Danh mục máy và số lượng sẵn sàng bán"));
        JPanel actions=Theme.transparent(new FlowLayout(FlowLayout.RIGHT,8,0));JButton brand=Theme.button("+ Hãng",false),deleteBrand=Theme.button("Xóa hãng",false),add=Theme.button("+ Nhập mẫu máy",true);brand.addActionListener(e->{String value=JOptionPane.showInputDialog(this,"Tên hãng điện thoại:");if(value!=null)try{dao.addBrand(value);reload();}catch(Exception x){Theme.error(this,x);}});deleteBrand.addActionListener(e->deleteBrand());add.addActionListener(e->edit(null));actions.add(brand);actions.add(deleteBrand);actions.add(add);header.add(actions,BorderLayout.EAST);add(header,BorderLayout.NORTH);
        JPanel body=Theme.card(new BorderLayout(0,15));JPanel toolbar=Theme.transparent(new BorderLayout(12,0));search.putClientProperty("JTextField.placeholderText","Tìm theo tên máy, hãng hoặc dung lượng...");search.setPreferredSize(new Dimension(400,42));toolbar.add(search);filter.setPreferredSize(new Dimension(190,42));toolbar.add(filter,BorderLayout.WEST);
        JPanel tools=Theme.transparent(new FlowLayout(FlowLayout.RIGHT,8,0));JButton edit=Theme.button("Chỉnh sửa",false),delete=Theme.button("Xóa thiết bị",false),refresh=Theme.button("Làm mới",false);edit.addActionListener(e->{var p=selected();if(p!=null)edit(p);});delete.addActionListener(e->deleteProduct());refresh.addActionListener(e->reload());tools.add(edit);tools.add(delete);tools.add(refresh);toolbar.add(tools,BorderLayout.EAST);body.add(toolbar,BorderLayout.NORTH);
        int[] widths={65,200,100,85,140,55,280,120};for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);JScrollPane scroll=new JScrollPane(table);scroll.setBorder(BorderFactory.createLineBorder(Theme.LINE));body.add(scroll);
        JPanel foot=Theme.transparent(new GridLayout(2,1,0,8));foot.add(detail);foot.add(summary);body.add(foot,BorderLayout.SOUTH);add(body);
        search.getDocument().addDocumentListener(Theme.onText(this::filter));filter.addActionListener(e->filter());table.getSelectionModel().addListSelectionListener(e->{var p=selected();if(p!=null)detail.setText(p.sp().getTenSP()+" · "+p.sp().getMoTa());});table.addMouseListener(new MouseAdapter(){public void mouseClicked(MouseEvent e){if(e.getClickCount()==2&&selected()!=null)edit(selected());}});reload();
    }
    private DienThoaiDAO.May selected(){int row=table.getSelectedRow();return row>=0&&row<visible.size()?visible.get(row):null;}
    private void reload(){try{all=dao.all();filter();}catch(Exception e){Theme.error(this,e);}}
    private void filter(){table.clearSelection();visible=new ArrayList<>();model.setRowCount(0);String q=TextSearch.normalize(search.getText());int mode=filter.getSelectedIndex(),units=0;BigDecimal value=BigDecimal.ZERO;
        for(var p:all){var s=p.sp();boolean include=switch(mode){case 1->s.isTrangThai();case 2->s.isTrangThai()&&p.tonKho()<=5;case 3->s.isTrangThai()&&p.tonKho()==0;case 4->!s.isTrangThai();default->true;};if(!include||!TextSearch.normalize(s.getTenSP()+" "+p.hang()+" "+s.getDungLuong()+" "+p.imeis()).contains(q))continue;visible.add(p);units+=p.tonKho();value=value.add(s.getDonGia().multiply(BigDecimal.valueOf(p.tonKho())));model.addRow(new Object[]{"M"+String.format("%03d",s.getMaSP()),s.getTenSP(),p.hang(),s.getDungLuong(),Theme.money(s.getDonGia()),p.tonKho(),p.imeis().isBlank()?"—":p.imeis(),!s.isTrangThai()?"Ngừng bán":p.tonKho()==0?"Hết hàng":p.tonKho()<=5?"Cần bổ sung":"Sẵn sàng"});}
        summary.setText(visible.size()+" mẫu máy  /  "+units+" máy trong kho  /  Giá trị theo giá bán: "+Theme.money(value));detail.setText("Nhấp đúp một dòng để chỉnh sửa thông tin máy.");
    }
    private void deleteProduct(){
        var p=selected();
        if(p==null){Theme.error(this,new IllegalArgumentException("Chọn một thiết bị trong kho trước."));return;}
        String name=p.sp().getTenSP()+" / "+p.sp().getDungLuong();
        String warning="Xóa mẫu máy "+name+" khỏi danh mục?";
        if(p.tonKho()>0)warning+="\n\nThao tác này sẽ xóa cả số tồn hiện tại: "+p.tonKho()+" máy.";
        warning+="\nThiết bị đã có trên hóa đơn sẽ không thể xóa.";
        if(JOptionPane.showConfirmDialog(this,warning,"Xác nhận xóa thiết bị",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;
        try{dao.deleteProduct(p.sp().getMaSP());reload();}catch(Exception e){Theme.error(this,e);}
    }
    private void deleteBrand(){
        try{
            var brands=dao.brands();
            if(brands.isEmpty()){Theme.error(this,new IllegalArgumentException("Không có hãng nào để xóa."));return;}
            Object choice=JOptionPane.showInputDialog(this,"Chọn hãng cần xóa. Chỉ xóa được hãng chưa có mẫu máy.","Xóa hãng",JOptionPane.WARNING_MESSAGE,null,brands.toArray(),brands.get(0));
            if(!(choice instanceof DienThoaiDAO.Hang brand))return;
            if(JOptionPane.showConfirmDialog(this,"Xóa hãng \""+brand.ten()+"\"?","Xác nhận xóa hãng",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE)!=JOptionPane.YES_OPTION)return;
            dao.deleteBrand(brand.id());reload();
        }catch(Exception e){Theme.error(this,e);}
    }
    private void edit(DienThoaiDAO.May existing){
        JDialog dialog=new JDialog(SwingUtilities.getWindowAncestor(this),existing==null?"Nhập mẫu máy":"Chỉnh sửa thiết bị",Dialog.ModalityType.APPLICATION_MODAL);dialog.setSize(720,760);dialog.setLocationRelativeTo(this);
        JPanel form=Theme.card(new BorderLayout(0,20));form.add(Theme.heading(existing==null?"Thêm vào kho":"Thông tin thiết bị","Mỗi dung lượng là một dòng quản lý riêng"),BorderLayout.NORTH);
        JPanel fields=Theme.transparent(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();g.fill=GridBagConstraints.HORIZONTAL;g.weightx=1;g.insets=new Insets(7,7,7,7);
        JTextField name=new JTextField(existing==null?"":existing.sp().getTenSP()),capacity=new JTextField(existing==null?"128GB":existing.sp().getDungLuong()),price=new JTextField(existing==null?"":existing.sp().getDonGia().toPlainString()),note=new JTextField(existing==null?"":existing.sp().getMoTa());
        JComboBox<DienThoaiDAO.Hang> brands=new JComboBox<>();try{for(var b:dao.brands())brands.addItem(b);}catch(Exception e){Theme.error(this,e);return;}if(existing!=null)for(int i=0;i<brands.getItemCount();i++)if(brands.getItemAt(i).id()==existing.sp().getMaLoai())brands.setSelectedIndex(i);
        JSpinner stock=new JSpinner(new SpinnerNumberModel(existing==null?0:existing.tonKho(),0,999999,1));JComboBox<String> status=new JComboBox<>(new String[]{"Sẵn sàng kinh doanh","Ngừng kinh doanh"});status.setSelectedIndex(existing!=null&&!existing.sp().isTrangThai()?1:0);JTextArea imeis=new JTextArea(5,30);imeis.setLineWrap(true);imeis.setWrapStyleWord(true);
        field(fields,g,0,0,"Tên thiết bị",name);field(fields,g,1,0,"Hãng sản xuất",brands);field(fields,g,0,2,"Bộ nhớ",capacity);field(fields,g,1,2,"Giá bán (VNĐ)",price);field(fields,g,0,4,existing==null?"Tồn kho chưa gồm IMEI mới":"Tồn kho hiện tại (IMEI mới sẽ cộng thêm)",stock);field(fields,g,1,4,"Màu sắc / bảo hành",note);field(fields,g,0,6,"Tình trạng kinh doanh",status);g.gridx=0;g.gridy=8;g.gridwidth=2;g.weighty=0;fields.add(Theme.label("IMEI bổ sung (15 chữ số, mỗi máy một dòng; số máy sẽ cộng vào tồn kho)",12,Theme.MUTED),g);g.gridy=9;g.weighty=1;g.fill=GridBagConstraints.BOTH;fields.add(new JScrollPane(imeis),g);form.add(fields);
        JPanel buttons=Theme.transparent(new FlowLayout(FlowLayout.RIGHT));JButton cancel=Theme.button("Hủy",false),save=Theme.button("Lưu vào kho",true);cancel.addActionListener(e->dialog.dispose());save.addActionListener(e->{try{stock.commitEdit();dao.save(existing==null?0:existing.sp().getMaSP(),name.getText(),(DienThoaiDAO.Hang)brands.getSelectedItem(),capacity.getText(),new BigDecimal(price.getText().trim()),((Number)stock.getValue()).intValue(),note.getText(),status.getSelectedIndex()==0,imeis.getText());dialog.dispose();reload();}catch(Exception x){Theme.error(dialog,x);}});buttons.add(cancel);buttons.add(save);form.add(buttons,BorderLayout.SOUTH);dialog.setContentPane(form);dialog.setVisible(true);
    }
    private void field(JPanel p,GridBagConstraints g,int x,int row,String label,JComponent input){g.gridx=x;g.gridy=row;p.add(Theme.label(label,12,Theme.MUTED),g);g.gridy=row+1;input.setPreferredSize(new Dimension(250,42));p.add(input,g);}
}
