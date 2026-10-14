package DAO;

import Model.TaiKhoan;
import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    public TaiKhoan dangNhap(
            String tenDangNhap,
            String matKhau
    ) throws SQLException {

        String sql = """
                SELECT
                    MaTK,
                    TenDangNhap,
                    MatKhau,
                    VaiTro,
                    MaNV,
                    Email,
                    TrangThai
                FROM TaiKhoan
                WHERE LOWER(TenDangNhap) = LOWER(?)
                  AND TrangThai = TRUE
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, tenDangNhap.trim());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    return null;
                }
                if (!Utils.Passwords.matches(matKhau,rs.getString("MatKhau"))) return null;

                long maNVValue = rs.getLong("MaNV");

                Long maNV = rs.wasNull()
                        ? null
                        : maNVValue;

                return new TaiKhoan(
                        rs.getLong("MaTK"),
                        rs.getString("TenDangNhap"),
                        rs.getString("MatKhau"),
                        rs.getString("VaiTro"),
                        maNV,
                        rs.getString("Email"),
                        rs.getBoolean("TrangThai")
                );
            }
        }
    }

    /** Cập nhật Gmail nhận OTP cho chính tài khoản Admin đang đăng nhập. */
    public void capNhatEmailAdmin(long maTK, String email) throws SQLException {
        if (!Utils.Session.isAdmin() || Utils.Session.getTaiKhoan().getMaTK()!=maTK) throw new SecurityException("Chỉ cập nhật email của Admin đang đăng nhập.");
        String emailChuanHoa = email == null ? "" : email.trim();
        if (!emailChuanHoa.matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Vui lòng nhập địa chỉ Gmail hợp lệ.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sqlTrung = """
                    SELECT COUNT(*)
                    FROM TaiKhoan t
                    LEFT JOIN NhanVien n ON n.MaNV = t.MaNV
                    WHERE t.TrangThai = TRUE
                      AND t.MaTK <> ?
                      AND LOWER(COALESCE(NULLIF(t.Email, ''), n.Email)) = LOWER(?)
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sqlTrung)) {
                ps.setLong(1, maTK);
                ps.setString(2, emailChuanHoa);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0) {
                        throw new IllegalArgumentException(
                                "Gmail này đang được liên kết với tài khoản khác.");
                    }
                }
            }

            String sqlCapNhat = """
                    UPDATE TaiKhoan
                    SET Email = ?
                    WHERE MaTK = ? AND VaiTro = 'ADMIN'
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sqlCapNhat)) {
                ps.setString(1, emailChuanHoa);
                ps.setLong(2, maTK);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("Không tìm thấy tài khoản Admin để cập nhật Gmail.");
                }
            }
        }
    }
}
