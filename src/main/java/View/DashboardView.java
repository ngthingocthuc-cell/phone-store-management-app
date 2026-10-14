package View;

import DAO.DienThoaiDAO;
import DAO.BaoHanhDAO;
import Utils.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class DashboardView extends JPanel {
    public DashboardView(Consumer<String> navigate){
        setLayout(new BorderLayout(0,18));setBackground(Theme.BG);setBorder(BorderFactory.createEmptyBorder(22,30,22,30));
        JPanel intro=Theme.transparent(new BorderLayout());intro.add(Theme.heading("Nhịp cửa hàng","Theo dõi hoạt động bán máy và công việc cần xử lý hôm nay"));JButton sell=Theme.button("+ Lập đơn bán máy",true);sell.addActionListener(e->navigate.accept("Bán hàng"));JPanel call=Theme.transparent(new FlowLayout(FlowLayout.RIGHT,0,0));call.add(sell);intro.add(call,BorderLayout.EAST);add(intro,BorderLayout.NORTH);
        JPanel left=Theme.transparent(new BorderLayout(0,18)),right=Theme.transparent(new BorderLayout(0,18));right.setPreferredSize(new Dimension(390,0));
        BigDecimal revenue=BigDecimal.ZERO;long invoices=0;double[] week=new double[7];var recent=Theme.model("Hóa đơn","Khách hàng","Giá trị","Thanh toán");
        String scope=Session.isAdmin()?"":" AND MaNV="+(Session.getTaiKhoan().getMaNV()==null?-1:Session.getTaiKhoan().getMaNV());
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){
            try(ResultSet r=s.executeQuery("SELECT COUNT(*),COALESCE(SUM(ThanhTien),0) FROM HoaDon WHERE TrangThai='Đã thanh toán' AND CAST(NgayLap AS DATE)=CURRENT_DATE"+scope)){r.next();invoices=r.getLong(1);revenue=r.getBigDecimal(2);}
            try(PreparedStatement q=c.prepareStatement("SELECT CAST(NgayLap AS DATE),SUM(ThanhTien) FROM HoaDon WHERE TrangThai='Đã thanh toán' AND NgayLap>=?"+scope+" GROUP BY CAST(NgayLap AS DATE)")){q.setTimestamp(1,Timestamp.valueOf(LocalDate.now().minusDays(6).atStartOfDay()));try(ResultSet r=q.executeQuery()){while(r.next()){int index=(int)java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now().minusDays(6),r.getDate(1).toLocalDate());if(index>=0&&index<7)week[index]=r.getDouble(2);}}}
            try(ResultSet r=s.executeQuery("SELECT MaHD,TenKhachHang,ThanhTien,TrangThai FROM HoaDon WHERE 1=1"+scope+" ORDER BY MaHD DESC FETCH FIRST 6 ROWS ONLY")){while(r.next())recent.addRow(new Object[]{"HD"+String.format("%05d",r.getLong(1)),r.getString(2)==null||r.getString(2).isBlank()?"Khách tại cửa hàng":r.getString(2),Theme.money(r.getBigDecimal(3)),r.getString(4)});}
        }catch(Exception e){Theme.error(this,e);}
        JPanel earnings=Theme.card(new BorderLayout(0,16));earnings.setPreferredSize(new Dimension(0,305));JPanel figures=Theme.transparent(new BorderLayout());JPanel amount=Theme.transparent(new GridLayout(3,1,0,5));amount.add(Theme.label("DOANH THU HÔM NAY",11,Theme.MUTED));amount.add(Theme.label(Theme.money(revenue),34,Theme.INK));amount.add(Theme.label(Session.isAdmin()?"Hóa đơn đã thanh toán của toàn cửa hàng":"Hóa đơn đã thanh toán do bạn phụ trách",12,Theme.MUTED));figures.add(amount);
        JPanel orders=Theme.transparent(new GridLayout(3,1,0,5));orders.setBorder(BorderFactory.createEmptyBorder(0,25,0,10));orders.add(Theme.label("ĐƠN HOÀN TẤT",11,Theme.MUTED));orders.add(Theme.label(String.valueOf(invoices),34,Theme.ACCENT));orders.add(Theme.label("Trong ngày hôm nay",12,Theme.MUTED));figures.add(orders,BorderLayout.EAST);earnings.add(figures,BorderLayout.NORTH);earnings.add(new WeekChart(week));left.add(earnings,BorderLayout.NORTH);
        JPanel transactions=Theme.card(new BorderLayout(0,14));JPanel recentHeader=Theme.transparent(new BorderLayout());recentHeader.add(Theme.label("Giao dịch gần đây",20,Theme.INK));JButton bills=Theme.button("Xem hóa đơn →",false);bills.addActionListener(e->navigate.accept("Hóa đơn"));recentHeader.add(bills,BorderLayout.EAST);transactions.add(recentHeader,BorderLayout.NORTH);transactions.add(new JScrollPane(Theme.table(recent)));transactions.add(Theme.label(recent.getRowCount()==0?"Chưa có giao dịch. Bắt đầu bằng một đơn bán máy.":"Chuyển khoản chờ xác nhận được theo dõi riêng trong Hóa đơn.",12,Theme.MUTED),BorderLayout.SOUTH);left.add(transactions);
        Map<String,Integer> stocks=new LinkedHashMap<>();int units=0;var low=Theme.model("Thiết bị cần bổ sung","Tồn");
        try{for(var p:new DienThoaiDAO().all())if(p.sp().isTrangThai()){stocks.merge(p.hang(),p.tonKho(),Integer::sum);units+=p.tonKho();if(p.tonKho()<=5)low.addRow(new Object[]{p.sp().getTenSP()+" / "+p.sp().getDungLuong(),p.tonKho()});}}catch(Exception e){Theme.error(this,e);}
        JPanel stock=Theme.card(new BorderLayout(0,13));stock.add(compactHeading("Máy sẵn sàng bán",units+" máy · Phân bổ theo hãng"),BorderLayout.NORTH);JPanel bars=Theme.transparent(new GridLayout(0,2,14,10));int max=stocks.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        for(var item:stocks.entrySet()){JPanel row=Theme.transparent(new BorderLayout(5,0));JLabel brand=Theme.label(item.getKey(),12,Theme.INK);brand.setPreferredSize(new Dimension(62,22));row.add(brand,BorderLayout.WEST);JProgressBar bar=new JProgressBar(0,Math.max(1,max));bar.setValue(item.getValue());bar.setForeground(Theme.ACCENT);bar.setBackground(Theme.SOFT);bar.setBorderPainted(false);row.add(bar);JLabel number=Theme.label(String.valueOf(item.getValue()),12,Theme.MUTED);number.setPreferredSize(new Dimension(23,22));row.add(number,BorderLayout.EAST);bars.add(row);}stock.add(bars);
        JButton inventory=Theme.button(Session.isAdmin()?"Mở kho thiết bị →":"Lập đơn từ kho →",false);inventory.addActionListener(e->navigate.accept(Session.isAdmin()?"Điện thoại & kho":"Bán hàng"));stock.add(inventory,BorderLayout.SOUTH);right.add(stock);
        JPanel tasks=Theme.card(new BorderLayout(0,10));tasks.setPreferredSize(new Dimension(0,265));tasks.add(compactHeading("Cần chú ý",low.getRowCount()+" mẫu còn tối đa 5 máy"),BorderLayout.NORTH);JTable lowTable=Theme.table(low);lowTable.getColumnModel().getColumn(1).setMaxWidth(50);tasks.add(new JScrollPane(lowTable));
        int repairing=0;try{for(var p:new BaoHanhDAO().all())if(p.trangThai().equals("Đang sửa")||p.trangThai().equals("Chờ trả khách"))repairing++;}catch(Exception e){Theme.error(this,e);}JButton warranty=Theme.button(repairing+" máy cần xử lý bảo hành  →",false);warranty.addActionListener(e->navigate.accept("Bảo hành"));tasks.add(warranty,BorderLayout.SOUTH);right.add(tasks,BorderLayout.SOUTH);
        JPanel body=Theme.transparent(new BorderLayout(20,0));body.add(left);body.add(right,BorderLayout.EAST);add(body);
    }
    private static JPanel compactHeading(String title,String subtitle){JPanel p=Theme.transparent(new GridLayout(2,1,0,5));p.add(Theme.label(title,20,Theme.INK));p.add(Theme.label(subtitle,12,Theme.MUTED));return p;}
    private static class WeekChart extends JComponent {
        private final double[] values;WeekChart(double[] values){this.values=values;setPreferredSize(new Dimension(450,130));}
        protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);int l=12,r=getWidth()-35,top=18,bottom=getHeight()-28;double max=java.util.Arrays.stream(values).max().orElse(0);g.setFont(new Font("Segoe UI",Font.PLAIN,11));g.setColor(Theme.LINE);for(int i=0;i<3;i++){int y=top+(bottom-top)*i/2;g.drawLine(l,y,r,y);}Path2D line=new Path2D.Double();for(int i=0;i<7;i++){int x=l+(r-l)*i/6,y=max==0?bottom:bottom-(int)(values[i]/max*(bottom-top));if(i==0)line.moveTo(x,y);else line.lineTo(x,y);g.setColor(Theme.MUTED);String day=LocalDate.now().minusDays(6-i).format(DateTimeFormatter.ofPattern("dd/MM"));g.drawString(day,x-12,getHeight()-7);}g.setColor(Theme.ACCENT);g.setStroke(new BasicStroke(2.5f));g.draw(line);if(max==0){g.setColor(Theme.MUTED);g.drawString("Chưa có doanh thu đã thanh toán trong 7 ngày",l+10,top+18);}else for(int i=0;i<7;i++){int x=l+(r-l)*i/6,y=bottom-(int)(values[i]/max*(bottom-top));g.fillOval(x-3,y-3,6,6);}g.dispose();}
    }
}
