package DAO;

import Utils.DBConnection;
import Utils.Session;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Sổ IMEI gắn với từng máy đã bán và thời hạn bảo hành. */
public class BaoHanhDAO {
    public static final String[] STAGES={"Đang sử dụng","Đang sửa","Chờ trả khách","Đã trả khách"};
    public record DongBan(long id,long hoaDon,String ten,int soLuong,LocalDate ngayBan,String khach,String sdt) {
        @Override public String toString(){return "HD"+hoaDon+" · "+ten+" · "+soLuong+" máy";}
    }
    public record Phieu(long id,long hoaDon,String imei,String tenMay,String khach,String sdt,
                        LocalDate ngayBan,LocalDate hetHan,String trangThai,String ghiChu) {
        public boolean conHan(){return !hetHan.isBefore(LocalDate.now());}
    }
    private void loggedIn(){if(!Session.isLoggedIn())throw new SecurityException("Vui lòng đăng nhập.");}
    public List<DongBan> cacMayDaBan() throws SQLException {
        loggedIn();List<DongBan> list=new ArrayList<>();
        String sql="SELECT ct.MaCT,h.MaHD,ct.TenSanPham,ct.DungLuong,ct.SoLuong,CAST(h.NgayLap AS DATE) NgayBan,COALESCE(NULLIF(h.TenKhachHang,''),'Khách lẻ') TenKhachHang,COALESCE(h.SDTKhachHang,'') SDTKhachHang FROM HoaDonChiTiet ct JOIN HoaDon h ON h.MaHD=ct.MaHD WHERE h.TrangThai='Đã thanh toán'";
        if(!Session.isAdmin())sql+=" AND h.MaNV=?";
        sql+=" ORDER BY h.MaHD DESC,ct.MaCT";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){
            if(!Session.isAdmin()){Long nv=Session.getTaiKhoan().getMaNV();if(nv==null)return list;s.setLong(1,nv);}
            try(ResultSet r=s.executeQuery()){while(r.next())list.add(new DongBan(r.getLong(1),r.getLong(2),r.getString(3)+" / "+r.getString(4),r.getInt(5),r.getDate(6).toLocalDate(),r.getString(7),r.getString(8)));}
        }return list;
    }
    public List<String> imeiChuaDangKy(long line) throws SQLException {
        loggedIn();List<String> list=new ArrayList<>();
        String sql="SELECT i.IMEI FROM DienThoaiIMEI i JOIN HoaDonChiTiet ct ON ct.MaCT=i.MaCT JOIN HoaDon h ON h.MaHD=ct.MaHD LEFT JOIN BaoHanh b ON b.IMEI=i.IMEI WHERE i.MaCT=? AND h.TrangThai='Đã thanh toán' AND b.MaBH IS NULL ORDER BY i.MaIMEI";
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement(sql)){s.setLong(1,line);try(ResultSet r=s.executeQuery()){while(r.next())list.add(r.getString(1));}}return list;
    }
    public List<Phieu> all() throws SQLException {
        loggedIn();List<Phieu> list=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT b.*,ct.MaHD,ct.TenSanPham,ct.DungLuong FROM BaoHanh b JOIN HoaDonChiTiet ct ON ct.MaCT=b.MaCT ORDER BY b.MaBH DESC")){
            while(r.next())list.add(new Phieu(r.getLong("MaBH"),r.getLong("MaHD"),r.getString("IMEI"),r.getString("TenSanPham")+" / "+r.getString("DungLuong"),r.getString("TenKhach"),r.getString("SDT"),r.getDate("NgayBan").toLocalDate(),r.getDate("HetHan").toLocalDate(),r.getString("TrangThai"),r.getString("GhiChu")));
        }return list;
    }
    public void register(long line,String imei,String customer,String phone,int months) throws SQLException {
        loggedIn();
        if(imei==null||!imei.matches("\\d{15}"))throw new IllegalArgumentException("IMEI phải gồm 15 chữ số.");
        if(customer==null||customer.isBlank()||customer.length()>150||(phone!=null&&!phone.isBlank()&&!phone.matches("\\+?\\d{9,15}"))||months<1||months>36)throw new IllegalArgumentException("Kiểm tra tên khách, số điện thoại (nếu có) và thời hạn từ 1 đến 36 tháng.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                // Lock the sale line so concurrent registration cannot exceed quantity sold.
                LocalDate bought;int quantity;
                try(PreparedStatement s=c.prepareStatement("SELECT MaHD,SoLuong FROM HoaDonChiTiet WHERE MaCT=? FOR UPDATE")){
                    s.setLong(1,line);try(ResultSet r=s.executeQuery()){
                        if(!r.next())throw new IllegalArgumentException("Không tìm thấy máy trong hóa đơn.");quantity=r.getInt("SoLuong");long invoice=r.getLong("MaHD");
                        try(PreparedStatement h=c.prepareStatement("SELECT MaNV,TrangThai,CAST(NgayLap AS DATE) NgayBan FROM HoaDon WHERE MaHD=?")){
                            h.setLong(1,invoice);try(ResultSet sale=h.executeQuery()){
                                if(!sale.next()||!"Đã thanh toán".equals(sale.getString("TrangThai")))throw new IllegalArgumentException("Chỉ đăng ký bảo hành cho hóa đơn đã thanh toán.");
                                if(!Session.isAdmin()&&!java.util.Objects.equals(Session.getTaiKhoan().getMaNV(),sale.getLong("MaNV")))throw new SecurityException("Chỉ đăng ký cho hóa đơn do bạn phụ trách.");
                                bought=sale.getDate("NgayBan").toLocalDate();
                            }
                        }
                    }
                }
                try(PreparedStatement s=c.prepareStatement("SELECT COUNT(*) FROM BaoHanh WHERE MaCT=?")){s.setLong(1,line);try(ResultSet r=s.executeQuery()){r.next();if(r.getInt(1)>=quantity)throw new IllegalArgumentException("Đã đăng ký đủ IMEI cho số máy trong dòng hóa đơn.");}}
                try(PreparedStatement s=c.prepareStatement("SELECT 1 FROM BaoHanh WHERE IMEI=?")){s.setString(1,imei);try(ResultSet r=s.executeQuery()){if(r.next())throw new IllegalArgumentException("IMEI đã có trong sổ bảo hành.");}}
                try(PreparedStatement s=c.prepareStatement("SELECT MaCT FROM DienThoaiIMEI WHERE IMEI=?")){s.setString(1,imei);try(ResultSet r=s.executeQuery()){if(r.next()&&(r.getObject(1)==null||r.getLong(1)!=line))throw new IllegalArgumentException("IMEI này không thuộc máy trong hóa đơn đã chọn.");}}
                try(PreparedStatement s=c.prepareStatement("INSERT INTO BaoHanh (MaCT,IMEI,TenKhach,SDT,NgayBan,HetHan,TrangThai,GhiChu) VALUES (?,?,?,?,?,?,?,'')")){
                    s.setLong(1,line);s.setString(2,imei);s.setString(3,customer.trim());s.setString(4,phone==null?"":phone.trim());s.setDate(5,Date.valueOf(bought));s.setDate(6,Date.valueOf(bought.plusMonths(months)));s.setString(7,STAGES[0]);s.executeUpdate();
                }c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    public void update(long id,String stage,String note) throws SQLException {
        loggedIn();if(!Set.of(STAGES).contains(stage)||note==null||note.length()>500)throw new IllegalArgumentException("Trạng thái không hợp lệ hoặc ghi chú dài quá 500 ký tự.");
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE BaoHanh SET TrangThai=?,GhiChu=? WHERE MaBH=?")){s.setString(1,stage);s.setString(2,note.trim());s.setLong(3,id);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Phiếu bảo hành không còn tồn tại.");}
    }
    public void delete(long id) throws SQLException {
        loggedIn();
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM BaoHanh WHERE MaBH=?")){
            s.setLong(1,id);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Phiếu bảo hành không còn tồn tại.");
        }
    }
}
