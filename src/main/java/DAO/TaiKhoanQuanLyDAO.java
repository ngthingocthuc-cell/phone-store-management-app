package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanQuanLyDAO {

    public static class TaiKhoanItem {

        private long maTK;
        private String tenDangNhap;
        private String vaiTro;
        private Long maNV;
        private String tenNhanVien;
        private boolean trangThai;
        private Timestamp ngayTao;

        public TaiKhoanItem(
                long maTK,
                String tenDangNhap,
                String vaiTro,
                Long maNV,
                String tenNhanVien,
                boolean trangThai,
                Timestamp ngayTao
        ) {
            this.maTK = maTK;
            this.tenDangNhap = tenDangNhap;
            this.vaiTro = vaiTro;
            this.maNV = maNV;
            this.tenNhanVien = tenNhanVien;
            this.trangThai = trangThai;
            this.ngayTao = ngayTao;
        }

        public long getMaTK() {
            return maTK;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }

        public String getVaiTro() {
            return vaiTro;
        }

        public Long getMaNV() {
            return maNV;
        }

        public String getTenNhanVien() {
            return tenNhanVien;
        }

        public boolean isTrangThai() {
            return trangThai;
        }

        public Timestamp getNgayTao() {
            return ngayTao;
        }
    }

    // =====================================================
    // LẤY DANH SÁCH TÀI KHOẢN
    // =====================================================

    public List<TaiKhoanItem> getAll()
            throws SQLException {

        List<TaiKhoanItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    t.MaTK,
                    t.TenDangNhap,
                    t.VaiTro,
                    t.MaNV,
                    n.HoTen AS TenNhanVien,
                    t.TrangThai,
                    t.NgayTao
                FROM TaiKhoan t
                LEFT JOIN NhanVien n
                    ON t.MaNV = n.MaNV
                ORDER BY t.MaTK DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                long maNVValue = rs.getLong("MaNV");

                Long maNV = rs.wasNull()
                        ? null
                        : maNVValue;

                list.add(
                        new TaiKhoanItem(
                                rs.getLong("MaTK"),
                                rs.getString("TenDangNhap"),
                                rs.getString("VaiTro"),
                                maNV,
                                rs.getString("TenNhanVien"),
                                rs.getBoolean("TrangThai"),
                                rs.getTimestamp("NgayTao")
                        )
                );
            }
        }

        return list;
    }

    // =====================================================
    // KHÓA / MỞ KHÓA TÀI KHOẢN
    // =====================================================

    public void capNhatTrangThai(
            long maTK,
            boolean trangThai
    ) throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            // Không cho mở lại tài khoản của nhân viên đã nghỉ.
            if (trangThai) {

                String checkSql = """
                        SELECT n.TrangThai
                        FROM TaiKhoan t
                        INNER JOIN NhanVien n
                            ON t.MaNV = n.MaNV
                        WHERE t.MaTK = ?
                        """;

                try (
                        PreparedStatement ps =
                                conn.prepareStatement(checkSql)
                ) {

                    ps.setLong(1, maTK);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (rs.next()
                                && !rs.getBoolean("TrangThai")) {

                            throw new SQLException(
                                    "Nhân viên đã nghỉ làm. "
                                    + "Không thể mở lại tài khoản."
                            );
                        }
                    }
                }
            }

            String sql = """
                    UPDATE TaiKhoan
                    SET TrangThai = ?
                    WHERE MaTK = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setBoolean(1, trangThai);
                ps.setLong(2, maTK);

                if (ps.executeUpdate() == 0) {

                    throw new SQLException(
                            "Không tìm thấy tài khoản."
                    );
                }
            }
        }
    }

    // =====================================================
    // ĐẶT LẠI MẬT KHẨU
    // =====================================================

    public void datLaiMatKhau(long maTK)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {
            String vaiTro;
            try (PreparedStatement lookup = conn.prepareStatement(
                    "SELECT VaiTro FROM TaiKhoan WHERE MaTK = ?")) {
                lookup.setLong(1, maTK);
                try (ResultSet rs = lookup.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "Không tìm thấy tài khoản cần đặt lại mật khẩu.");
                    }
                    vaiTro = rs.getString("VaiTro");
                }
            }
            String defaultPassword = "ADMIN".equalsIgnoreCase(vaiTro)
                    ? "admin123" : "123456";
            try (PreparedStatement update = conn.prepareStatement(
                    "UPDATE TaiKhoan SET MatKhau = ? WHERE MaTK = ?")) {
                update.setString(1, Utils.Passwords.hash(defaultPassword));
                update.setLong(2, maTK);
                if (update.executeUpdate() != 1) {
                    throw new SQLException(
                            "Không tìm thấy tài khoản cần đặt lại mật khẩu.");
                }
            }
        }
    }
}
