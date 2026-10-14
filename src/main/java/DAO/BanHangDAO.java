package DAO;
import Model.*;
import Utils.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class BanHangDAO {
    public List<NhanVien> getAllNhanVien() throws SQLException {
        List<NhanVien> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT MaNV,HoTen FROM NhanVien WHERE TrangThai=TRUE ORDER BY HoTen")){while(r.next())list.add(new NhanVien(r.getLong(1),r.getString(2)));}return list;
    }
    public long taoHoaDon(TaiKhoan account,long employee,List<GioHangItem> cart,BigDecimal discount,String method,String customer,String customerPhone) throws SQLException {
        if(account==null||Session.getTaiKhoan()==null||account.getMaTK()!=Session.getTaiKhoan().getMaTK())throw new SecurityException("Vui lòng đăng nhập.");
        if(!account.isAdmin()&&!Objects.equals(account.getMaNV(),employee))throw new SecurityException("Chỉ được lập hóa đơn cho nhân viên của tài khoản đang đăng nhập.");
        if(cart==null||cart.isEmpty())throw new IllegalArgumentException("Giỏ hàng đang trống.");
        if(!Set.of("Tiền mặt","VietQR","Chuyển khoản").contains(method))throw new IllegalArgumentException("Phương thức không hợp lệ.");
        if(customer!=null&&customer.length()>150)throw new IllegalArgumentException("Tên khách tối đa 150 ký tự.");
        if(customerPhone!=null&&!customerPhone.isBlank()&&!customerPhone.trim().matches("\\+?\\d{9,15}"))throw new IllegalArgumentException("Số điện thoại khách phải gồm 9 đến 15 chữ số.");
        BigDecimal total=BigDecimal.ZERO;
        Map<Long,Integer> quantities=new TreeMap<>();
        for(var item:cart){if(item.getSanPham()==null||item.getSoLuong()<=0||!item.getPhuKiens().isEmpty())throw new IllegalArgumentException("Dòng hàng không hợp lệ.");
            quantities.merge(item.getSanPham().getMaSP(),item.getSoLuong(),Math::addExact);total=total.add(item.getThanhTien());}
        if(discount==null||discount.signum()<0||discount.compareTo(total)>0||discount.scale()>2)throw new IllegalArgumentException("Giảm giá phải từ 0 đến tổng tiền, tối đa 2 số thập phân.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement s=c.prepareStatement("SELECT 1 FROM NhanVien WHERE MaNV=? AND TrangThai=TRUE")){s.setLong(1,employee);try(ResultSet r=s.executeQuery()){if(!r.next())throw new IllegalArgumentException("Nhân viên không còn hoạt động.");}}
                // Lock stock in stable product order. All changes and the invoice commit together.
                for(var entry:quantities.entrySet()){
                    try(PreparedStatement s=c.prepareStatement("SELECT TenSP,DungLuong,DonGia,TonKho,TrangThai FROM SanPham WHERE MaSP=? FOR UPDATE")){
                        s.setLong(1,entry.getKey());try(ResultSet r=s.executeQuery()){
                            if(!r.next()||!r.getBoolean("TrangThai"))throw new IllegalArgumentException("Máy đã ngừng kinh doanh. Làm mới danh sách.");
                            if(r.getInt("TonKho")<entry.getValue())throw new IllegalArgumentException(r.getString("TenSP")+": tồn kho không đủ (còn "+r.getInt("TonKho")+").");
                            for(var item:cart)if(item.getSanPham().getMaSP()==entry.getKey()){
                                if(r.getBigDecimal("DonGia").compareTo(item.getSanPham().getDonGia())!=0)throw new IllegalArgumentException("Giá máy đã thay đổi. Xóa máy khỏi giỏ rồi thêm lại.");
                                item.getSanPham().setTenSP(r.getString("TenSP"));item.getSanPham().setDungLuong(r.getString("DungLuong"));
                            }
                        }
                    }
                    try(PreparedStatement s=c.prepareStatement("UPDATE SanPham SET TonKho=TonKho-? WHERE MaSP=? AND TonKho>=?")){s.setInt(1,entry.getValue());s.setLong(2,entry.getKey());s.setInt(3,entry.getValue());if(s.executeUpdate()!=1)throw new SQLException("Không thể cập nhật tồn kho.");}
                }
                long id;
                try(PreparedStatement s=c.prepareStatement("INSERT INTO HoaDon (MaNV,TongTien,GiamGia,ThanhTien,PhuongThucTT,TrangThai,TenKhachHang,SDTKhachHang) VALUES (?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    s.setLong(1,employee);s.setBigDecimal(2,total);s.setBigDecimal(3,discount);s.setBigDecimal(4,total.subtract(discount));s.setString(5,method);s.setString(6,method.equals("Tiền mặt")?"Đã thanh toán":"Chờ thanh toán");s.setString(7,customer==null?"":customer.trim());s.setString(8,customerPhone==null?"":customerPhone.trim());s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){if(!r.next())throw new SQLException("Không lấy được mã hóa đơn.");id=r.getLong(1);}}
                try(PreparedStatement s=c.prepareStatement("INSERT INTO HoaDonChiTiet (MaHD,MaSP,TenSanPham,DungLuong,DonGia,SoLuong,ThanhTien) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){
                    for(var item:cart){s.setLong(1,id);s.setLong(2,item.getSanPham().getMaSP());s.setString(3,item.getSanPham().getTenSP());s.setString(4,item.getSanPham().getDungLuong());s.setBigDecimal(5,item.getSanPham().getDonGia());s.setInt(6,item.getSoLuong());s.setBigDecimal(7,item.getThanhTien());s.executeUpdate();try(ResultSet line=s.getGeneratedKeys()){if(line.next()){long lineId=line.getLong(1);try(PreparedStatement serials=c.prepareStatement("SELECT MaIMEI FROM DienThoaiIMEI WHERE MaSP=? AND MaCT IS NULL ORDER BY MaIMEI FETCH FIRST "+item.getSoLuong()+" ROWS ONLY FOR UPDATE")){serials.setLong(1,item.getSanPham().getMaSP());try(ResultSet found=serials.executeQuery();PreparedStatement assign=c.prepareStatement("UPDATE DienThoaiIMEI SET MaCT=? WHERE MaIMEI=? AND MaCT IS NULL")){while(found.next()){assign.setLong(1,lineId);assign.setLong(2,found.getLong(1));assign.addBatch();}assign.executeBatch();}}}}}}
                c.commit();return id;
            }catch(SQLException|RuntimeException e){try{c.rollback();}catch(SQLException rollback){e.addSuppressed(rollback);}throw e;}
        }
    }
}
