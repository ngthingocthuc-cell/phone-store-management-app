package DAO;

import Model.SanPham;
import Utils.DBConnection;
import Utils.Session;
import java.sql.*;
import java.math.BigDecimal;
import java.util.*;

public class DienThoaiDAO {
    public record Hang(long id, String ten) { @Override public String toString() { return ten; } }
    public record May(SanPham sp, String hang, int tonKho, String imeis) {}
    public List<Hang> brands() throws SQLException {
        List<Hang> list = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT MaLoai,TenLoai FROM HangDienThoai WHERE TrangThai=TRUE ORDER BY TenLoai")) {
            while(r.next()) list.add(new Hang(r.getLong(1),r.getString(2)));
        } return list;
    }
    public List<May> all() throws SQLException {
        List<May> list = new ArrayList<>();
        try(Connection c=DBConnection.getConnection(); Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT p.*,h.TenLoai FROM SanPham p JOIN HangDienThoai h ON h.MaLoai=p.MaLoai ORDER BY p.TenSP,p.DungLuong")) {
            while(r.next()) {long id=r.getLong("MaSP");StringBuilder imeis=new StringBuilder();try(PreparedStatement serials=c.prepareStatement("SELECT IMEI FROM DienThoaiIMEI WHERE MaSP=? AND MaCT IS NULL ORDER BY MaIMEI")){serials.setLong(1,id);try(ResultSet values=serials.executeQuery()){while(values.next()){if(imeis.length()>0)imeis.append(", ");imeis.append(values.getString(1));}}}list.add(new May(new SanPham(id,r.getString("TenSP"),r.getLong("MaLoai"),r.getString("DungLuong"),r.getBigDecimal("DonGia"),r.getString("MoTa"),r.getBoolean("TrangThai")),r.getString("TenLoai"),r.getInt("TonKho"),imeis.toString()));}
        } return list;
    }
    public void save(long id,String name,Hang brand,String capacity,BigDecimal price,int stock,String note,boolean active,String imeiInput) throws SQLException {
        requireAdmin();
        if(name.isBlank()||name.length()>150||brand==null||capacity.isBlank()||capacity.length()>30||price.signum()<=0||price.compareTo(new BigDecimal("9999999999.99"))>0||stock<0||note.length()>255) throw new IllegalArgumentException("Kiểm tra tên máy, hãng, dung lượng, giá bán và tồn kho. Mô tả tối đa 255 ký tự.");
        List<String> imeis=splitImeis(imeiInput);
        if(imeis.stream().anyMatch(x->!x.matches("\\d{15}"))||new HashSet<>(imeis).size()!=imeis.size())throw new IllegalArgumentException("IMEI phải gồm đúng 15 chữ số và không được trùng nhau.");
        int resultingStock=Math.addExact(stock,imeis.size());
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try{
                long productId=id;
                if(id==0){try(PreparedStatement s=c.prepareStatement("INSERT INTO SanPham (TenSP,MaLoai,DungLuong,DonGia,TonKho,MoTa,TrangThai) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)){bindProduct(s,name,brand,capacity,price,resultingStock,note,active);s.executeUpdate();try(ResultSet keys=s.getGeneratedKeys()){if(!keys.next())throw new SQLException("Không lấy được mã mẫu máy.");productId=keys.getLong(1);}}}
                else{try(PreparedStatement s=c.prepareStatement("UPDATE SanPham SET TenSP=?,MaLoai=?,DungLuong=?,DonGia=?,TonKho=?,MoTa=?,TrangThai=? WHERE MaSP=?")){bindProduct(s,name,brand,capacity,price,resultingStock,note,active);s.setLong(8,id);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Không tìm thấy mẫu máy.");}}
                if(!imeis.isEmpty())try(PreparedStatement s=c.prepareStatement("INSERT INTO DienThoaiIMEI(MaSP,IMEI) VALUES(?,?)")){for(String value:imeis){s.setLong(1,productId);s.setString(2,value);s.addBatch();}s.executeBatch();}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    private List<String> splitImeis(String input){List<String> values=new ArrayList<>();if(input==null)return values;for(String raw:input.split("[\\r\\n,;\\s]+")){String value=raw.trim();if(!value.isEmpty())values.add(value);}return values;}
    private void bindProduct(PreparedStatement s,String name,Hang brand,String capacity,BigDecimal price,int stock,String note,boolean active)throws SQLException{s.setString(1,name.trim());s.setLong(2,brand.id());s.setString(3,capacity.trim());s.setBigDecimal(4,price);s.setInt(5,stock);s.setString(6,note.trim());s.setBoolean(7,active);}
    public int addImeis(long productId,String input) throws SQLException {
        requireAdmin();
        List<String> imeis=new ArrayList<>();
        for(String raw:input.split("[\\r\\n,;\\s]+")){String value=raw.trim();if(!value.isEmpty())imeis.add(value);}
        if(imeis.isEmpty())throw new IllegalArgumentException("Nhập ít nhất một IMEI, mỗi IMEI 15 chữ số.");
        if(imeis.stream().anyMatch(x->!x.matches("\\d{15}"))||new HashSet<>(imeis).size()!=imeis.size())throw new IllegalArgumentException("IMEI phải gồm đúng 15 chữ số và không được trùng nhau.");
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);
            try{
                try(PreparedStatement lock=c.prepareStatement("SELECT MaSP FROM SanPham WHERE MaSP=? FOR UPDATE")){lock.setLong(1,productId);try(ResultSet r=lock.executeQuery()){if(!r.next())throw new IllegalArgumentException("Không tìm thấy mẫu máy.");}}
                try(PreparedStatement insert=c.prepareStatement("INSERT INTO DienThoaiIMEI(MaSP,IMEI) VALUES(?,?)")){
                    for(String value:imeis){insert.setLong(1,productId);insert.setString(2,value);insert.addBatch();}insert.executeBatch();
                }
                try(PreparedStatement update=c.prepareStatement("UPDATE SanPham SET TonKho=TonKho+? WHERE MaSP=?")){update.setInt(1,imeis.size());update.setLong(2,productId);update.executeUpdate();}
                c.commit();return imeis.size();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}finally{c.setAutoCommit(true);}
        }
    }
    public void addBrand(String name) throws SQLException {
        requireAdmin(); if(name==null||name.isBlank()||name.trim().length()>100) throw new IllegalArgumentException("Tên hãng từ 1 đến 100 ký tự.");
        try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO HangDienThoai (TenLoai,TrangThai) VALUES (?,TRUE)")){s.setString(1,name.trim());s.executeUpdate();}
    }
    public void stop(long id) throws SQLException {
        requireAdmin();try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE SanPham SET TrangThai=FALSE WHERE MaSP=?")){s.setLong(1,id);s.executeUpdate();}
    }
    public void setStatus(long id,boolean active) throws SQLException {
        requireAdmin();try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE SanPham SET TrangThai=? WHERE MaSP=?")){s.setBoolean(1,active);s.setLong(2,id);if(s.executeUpdate()!=1)throw new IllegalArgumentException("Không tìm thấy mẫu máy.");}
    }
    public void deleteBrand(long id) throws SQLException {
        requireAdmin();
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement lock=c.prepareStatement("SELECT MaLoai FROM HangDienThoai WHERE MaLoai=? FOR UPDATE")) {
                    lock.setLong(1,id);
                    try(ResultSet r=lock.executeQuery()) {
                        if(!r.next()) throw new IllegalArgumentException("Không tìm thấy hãng cần xóa.");
                    }
                }
                try(PreparedStatement count=c.prepareStatement("SELECT COUNT(*) FROM SanPham WHERE MaLoai=?")) {
                    count.setLong(1,id);
                    try(ResultSet r=count.executeQuery()) {
                        r.next();
                        if(r.getLong(1)>0) throw new IllegalArgumentException("Hãng này còn mẫu máy trong kho. Hãy xóa hoặc chuyển các mẫu máy sang hãng khác trước.");
                    }
                }
                try(PreparedStatement delete=c.prepareStatement("DELETE FROM HangDienThoai WHERE MaLoai=?")) {
                    delete.setLong(1,id);
                    if(delete.executeUpdate()!=1) throw new SQLException("Không xóa được hãng.");
                }
                c.commit();
            } catch(SQLException|RuntimeException e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
    public void deleteProduct(long id) throws SQLException {
        requireAdmin();
        try(Connection c=DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement lock=c.prepareStatement("SELECT MaSP FROM SanPham WHERE MaSP=? FOR UPDATE")) {
                    lock.setLong(1,id);
                    try(ResultSet r=lock.executeQuery()) {
                        if(!r.next()) throw new IllegalArgumentException("Không tìm thấy thiết bị cần xóa.");
                    }
                }
                try(PreparedStatement count=c.prepareStatement("SELECT COUNT(*) FROM HoaDonChiTiet WHERE MaSP=?")) {
                    count.setLong(1,id);
                    try(ResultSet r=count.executeQuery()) {
                        r.next();
                        if(r.getLong(1)>0) throw new IllegalArgumentException("Thiết bị đã xuất hiện trên hóa đơn nên không thể xóa để giữ lịch sử bán hàng. Hãy dùng Ngừng bán.");
                    }
                }
                try(PreparedStatement serials=c.prepareStatement("DELETE FROM DienThoaiIMEI WHERE MaSP=?")){serials.setLong(1,id);serials.executeUpdate();}
                try(PreparedStatement delete=c.prepareStatement("DELETE FROM SanPham WHERE MaSP=?")) {
                    delete.setLong(1,id);
                    if(delete.executeUpdate()!=1) throw new SQLException("Không xóa được thiết bị.");
                }
                c.commit();
            } catch(SQLException|RuntimeException e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
    private void requireAdmin() { if(!Session.isAdmin()) throw new SecurityException("Chỉ quản trị viên được quản lý điện thoại."); }
}
