package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DoiMatKhauDAO {

    public void doiMatKhau(
            long maTK,
            String matKhauCu,
            String matKhauMoi
    ) throws SQLException {
        if (!Utils.Session.isLoggedIn() || Utils.Session.getTaiKhoan().getMaTK()!=maTK) throw new SecurityException("Chỉ đổi mật khẩu của tài khoản đang đăng nhập.");

        if (matKhauCu == null || matKhauCu.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập mật khẩu hiện tại."
            );
        }

        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException(
                    "Mật khẩu mới phải có ít nhất 6 ký tự."
            );
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                // Kiểm tra mật khẩu hiện tại và tài khoản còn hoạt động
                String sqlKiemTra = """
                        SELECT MatKhau
                        FROM TaiKhoan
                        WHERE MaTK = ?
                          AND TrangThai = TRUE
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(sqlKiemTra)
                ) {

                    ps.setLong(1, maTK);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Tài khoản không tồn tại "
                                    + "hoặc đã bị khóa."
                            );
                        }

                        String matKhauTrongDB =
                                rs.getString("MatKhau");

                        if (!Utils.Passwords.matches(matKhauCu,matKhauTrongDB)) {
                            throw new IllegalArgumentException(
                                    "Mật khẩu hiện tại không đúng."
                            );
                        }
                    }
                }

                // Cập nhật mật khẩu mới
                String sqlCapNhat = """
                        UPDATE TaiKhoan
                        SET MatKhau = ?
                        WHERE MaTK = ?
                          AND TrangThai = TRUE
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(sqlCapNhat)
                ) {

                    ps.setString(1, Utils.Passwords.hash(matKhauMoi));
                    ps.setLong(2, maTK);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Không thể cập nhật mật khẩu."
                        );
                    }
                }

                // Ghi lịch sử đổi mật khẩu
                String sqlLichSu = """
                        INSERT INTO LichSuDoiMatKhau
                            (MaTK, ThoiGianDoi, PhuongThucXacThuc)
                        VALUES (?, CURRENT_TIMESTAMP, ?)
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(sqlLichSu)
                ) {

                    ps.setLong(1, maTK);
                    ps.setString(2, "Mật khẩu hiện tại");

                    ps.executeUpdate();
                }

                conn.commit();

            } catch (SQLException | RuntimeException e) {

                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;

            } finally {

                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }
}
