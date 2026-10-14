package DAO;
import Utils.DBConnection;
import Utils.Passwords;
import Utils.GmailService;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.Instant;
import java.util.*;

public class QuenMatKhauDAO {
    @FunctionalInterface public interface MailSender{void send(String email,String code) throws Exception;}
    private final MailSender mail;
    public QuenMatKhauDAO(){this(GmailService::sendOtp);}
    public QuenMatKhauDAO(MailSender sender){mail=sender;}
    private static String digest(String code,String salt){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest((salt+code).getBytes(StandardCharsets.UTF_8)));}catch(NoSuchAlgorithmException e){throw new IllegalStateException(e);}}
    private long account(Connection c,String user,String email,boolean lock) throws SQLException {
        try(PreparedStatement s=c.prepareStatement("SELECT MaTK FROM TaiKhoan WHERE LOWER(TenDangNhap)=LOWER(?) AND TrangThai=TRUE"+(lock?" FOR UPDATE":""))){s.setString(1,user.trim());try(ResultSet r=s.executeQuery()){
            if(!r.next())throw new IllegalArgumentException("Tài khoản và email chưa khớp hoặc tài khoản đã bị khóa.");long id=r.getLong(1);
            try(PreparedStatement e=c.prepareStatement("SELECT COALESCE(NULLIF(t.Email,''),n.Email) FROM TaiKhoan t LEFT JOIN NhanVien n ON n.MaNV=t.MaNV WHERE t.MaTK=?")){e.setLong(1,id);try(ResultSet rr=e.executeQuery()){rr.next();if(!email.trim().equalsIgnoreCase(rr.getString(1)))throw new IllegalArgumentException("Tài khoản và email chưa khớp hoặc tài khoản đã bị khóa.");}}return id;
        }}
    }
    public void send(String user,String email) throws Exception {
        if(user.isBlank()||!email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}"))throw new IllegalArgumentException("Nhập tên đăng nhập và email đã liên kết.");
        String code=String.format(Locale.ROOT,"%06d",new SecureRandom().nextInt(1000000)),salt=UUID.randomUUID().toString();long token;
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                long id=account(c,user,email,true);
                try(PreparedStatement s=c.prepareStatement("SELECT CreatedAt FROM RecoveryOtp WHERE MaTK=? ORDER BY Id DESC FETCH FIRST 1 ROW ONLY")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){if(r.next()&&r.getTimestamp(1).toInstant().plusSeconds(60).isAfter(Instant.now()))throw new IllegalArgumentException("Đợi 60 giây trước khi gửi mã mới.");}}
                try(PreparedStatement s=c.prepareStatement("UPDATE RecoveryOtp SET Used=TRUE WHERE MaTK=?")){s.setLong(1,id);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("INSERT INTO RecoveryOtp (MaTK,CodeHash,Salt,CreatedAt,ExpiresAt,Sent,Used,Attempts) VALUES (?,?,?,?,?,FALSE,FALSE,0)",Statement.RETURN_GENERATED_KEYS)){
                    s.setLong(1,id);s.setString(2,digest(code,salt));s.setString(3,salt);s.setTimestamp(4,Timestamp.from(Instant.now()));s.setTimestamp(5,Timestamp.from(Instant.now().plusSeconds(300)));s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();token=r.getLong(1);}}
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }
        try{mail.send(email.trim(),code);try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("UPDATE RecoveryOtp SET Sent=TRUE WHERE Id=?")){s.setLong(1,token);s.executeUpdate();}}
        catch(Exception e){try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("DELETE FROM RecoveryOtp WHERE Id=?")){s.setLong(1,token);s.executeUpdate();}throw new IllegalStateException(GmailService.explainSendFailure(e),e);}
    }
    public void reset(String user,String email,String code,String newPassword) throws Exception {
        if(!code.matches("\\d{6}"))throw new IllegalArgumentException("OTP gồm đúng 6 chữ số.");
        if(newPassword.length()<6||newPassword.length()>128)throw new IllegalArgumentException("Mật khẩu mới từ 6 đến 128 ký tự.");
        String hash=Passwords.hash(newPassword);
        try(Connection c=DBConnection.getConnection()){
            c.setAutoCommit(false);try{
                long id=account(c,user,email,true),token;
                try(PreparedStatement s=c.prepareStatement("SELECT * FROM RecoveryOtp WHERE MaTK=? AND Sent=TRUE AND Used=FALSE ORDER BY Id DESC FETCH FIRST 1 ROW ONLY FOR UPDATE")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){
                    if(!r.next()||r.getTimestamp("ExpiresAt").toInstant().isBefore(Instant.now())||r.getInt("Attempts")>=5)throw new IllegalArgumentException("OTP hết hạn hoặc đã bị khóa. Gửi mã mới.");token=r.getLong("Id");
                    if(!MessageDigest.isEqual(digest(code,r.getString("Salt")).getBytes(StandardCharsets.UTF_8),r.getString("CodeHash").getBytes(StandardCharsets.UTF_8))){try(PreparedStatement wrong=c.prepareStatement("UPDATE RecoveryOtp SET Attempts=Attempts+1 WHERE Id=?")){wrong.setLong(1,token);wrong.executeUpdate();}c.commit();throw new IllegalArgumentException("OTP không đúng. Tối đa 5 lần thử cho mỗi mã.");}
                }}
                try(PreparedStatement s=c.prepareStatement("UPDATE TaiKhoan SET MatKhau=? WHERE MaTK=?")){s.setString(1,hash);s.setLong(2,id);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("UPDATE RecoveryOtp SET Used=TRUE WHERE MaTK=?")){s.setLong(1,id);s.executeUpdate();}
                try(PreparedStatement s=c.prepareStatement("INSERT INTO LichSuDoiMatKhau (MaTK,PhuongThucXacThuc) VALUES (?,'Gmail OTP')")){s.setLong(1,id);s.executeUpdate();}c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }
    }
}
