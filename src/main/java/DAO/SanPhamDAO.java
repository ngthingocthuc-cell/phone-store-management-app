package DAO;

import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SanPhamDAO {

    // =====================================================
    // MODEL LOẠI ĐỒ UỐNG
    // =====================================================

    public static class HangDienThoaiItem {

        private long maLoai;
        private String tenLoai;

        public HangDienThoaiItem(long maLoai, String tenLoai) {
            this.maLoai = maLoai;
            this.tenLoai = tenLoai;
        }

        public long getMaLoai() {
            return maLoai;
        }

        public String getTenLoai() {
            return tenLoai;
        }

        @Override
        public String toString() {
            return tenLoai;
        }
    }

    // =====================================================
    // MODEL DÒNG SẢN PHẨM
    // =====================================================

    public static class SanPhamItem {

        private long maSP;
        private String tenSP;
        private long maLoai;
        private String tenLoai;
        private String dungLuong;
        private BigDecimal donGia;
        private String moTa;
        private boolean trangThai;

        public SanPhamItem(
                long maSP,
                String tenSP,
                long maLoai,
                String tenLoai,
                String dungLuong,
                BigDecimal donGia,
                String moTa,
                boolean trangThai
        ) {
            this.maSP = maSP;
            this.tenSP = tenSP;
            this.maLoai = maLoai;
            this.tenLoai = tenLoai;
            this.dungLuong = dungLuong;
            this.donGia = donGia;
            this.moTa = moTa;
            this.trangThai = trangThai;
        }

        public long getMaSP() {
            return maSP;
        }

        public String getTenSP() {
            return tenSP;
        }

        public long getMaLoai() {
            return maLoai;
        }

        public String getTenLoai() {
            return tenLoai;
        }

        public String getDungLuong() {
            return dungLuong;
        }

        public BigDecimal getDonGia() {
            return donGia;
        }

        public String getMoTa() {
            return moTa;
        }

        public boolean isTrangThai() {
            return trangThai;
        }
    }

    // =====================================================
    // LẤY DANH SÁCH LOẠI ĐỒ UỐNG
    // =====================================================

    public List<HangDienThoaiItem> getAllHangDienThoai()
            throws SQLException {

        List<HangDienThoaiItem> list = new ArrayList<>();

        String sql = """
                SELECT MaLoai, TenLoai
                FROM HangDienThoai
                WHERE TrangThai = TRUE
                ORDER BY TenLoai
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(
                        new HangDienThoaiItem(
                                rs.getLong("MaLoai"),
                                rs.getString("TenLoai")
                        )
                );
            }
        }

        return list;
    }

    public HangDienThoaiItem themHangDienThoai(String tenLoai) throws SQLException {
        String name = tenLoai == null ? "" : tenLoai.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Nhập tên loại điện thoại.");
        if (name.length() > 100) throw new IllegalArgumentException("Tên loại tối đa 100 ký tự.");

        try (Connection conn = DBConnection.getConnection()) {
            Long existingId = null;
            String existingName = null;
            boolean existingActive = false;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT MaLoai, TenLoai, TrangThai FROM HangDienThoai WHERE LOWER(TenLoai)=LOWER(?)")) {
                ps.setString(1, name);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingId = rs.getLong("MaLoai");
                        existingName = rs.getString("TenLoai");
                        existingActive = rs.getBoolean("TrangThai");
                    }
                }
            }

            if (existingId != null) {
                if (existingActive) throw new IllegalArgumentException("Loại điện thoại này đã tồn tại.");
                try (PreparedStatement update = conn.prepareStatement(
                        "UPDATE HangDienThoai SET TrangThai=TRUE WHERE MaLoai=?")) {
                    update.setLong(1, existingId);
                    update.executeUpdate();
                }
                return new HangDienThoaiItem(existingId, existingName);
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO HangDienThoai (TenLoai, MoTa, TrangThai) VALUES (?, NULL, TRUE)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, name);
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("Không lấy được mã loại điện thoại mới.");
                    return new HangDienThoaiItem(keys.getLong(1), name);
                }
            }
        }
    }

    public List<String> getAllDungLuong() throws SQLException {
        List<String> sizes = new ArrayList<>();
        String sql = "SELECT TenDungLuong FROM DungLuong ORDER BY CASE WHEN UPPER(TenDungLuong)='M' THEN 0 WHEN UPPER(TenDungLuong)='L' THEN 1 ELSE 2 END, TenDungLuong";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) sizes.add(rs.getString("TenDungLuong"));
        }
        return sizes;
    }

    public String themDungLuong(String dungLuong) throws SQLException {
        String size = dungLuong == null ? "" : dungLuong.trim();
        if (size.isEmpty()) throw new IllegalArgumentException("Nhập tên dung lượng.");
        if (size.length() > 30) throw new IllegalArgumentException("Tên dung lượng tối đa 30 ký tự.");
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement check = conn.prepareStatement(
                     "SELECT 1 FROM DungLuong WHERE LOWER(TenDungLuong)=LOWER(?)")) {
            check.setString(1, size);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) throw new IllegalArgumentException("Dung lượng này đã tồn tại.");
            }
            try (PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO DungLuong (TenDungLuong) VALUES (?)")) {
                insert.setString(1, size);
                insert.executeUpdate();
            }
        }
        return size;
    }

    public void xoaHangDienThoai(long maLoai) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM SanPham WHERE MaLoai=?")) {
                check.setLong(1, maLoai);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0) {
                        throw new IllegalArgumentException(
                                "Không thể xóa loại này vì đang được ít nhất một sản phẩm sử dụng."
                        );
                    }
                }
            }

            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM HangDienThoai WHERE MaLoai=?")) {
                delete.setLong(1, maLoai);
                if (delete.executeUpdate() == 0) {
                    throw new SQLException("Không tìm thấy loại điện thoại cần xóa.");
                }
            }
        }
    }

    public void xoaDungLuong(String dungLuong) throws SQLException {
        String size = dungLuong == null ? "" : dungLuong.trim();
        if (size.isEmpty()) throw new IllegalArgumentException("Hãy chọn dung lượng cần xóa.");

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT COUNT(*) FROM SanPham WHERE UPPER(DungLuong)=UPPER(?)")) {
                check.setString(1, size);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next() && rs.getLong(1) > 0) {
                        throw new IllegalArgumentException(
                                "Không thể xóa dung lượng này vì đang được ít nhất một sản phẩm sử dụng."
                        );
                    }
                }
            }

            try (PreparedStatement delete = conn.prepareStatement(
                    "DELETE FROM DungLuong WHERE UPPER(TenDungLuong)=UPPER(?)")) {
                delete.setString(1, size);
                if (delete.executeUpdate() == 0) {
                    throw new SQLException("Không tìm thấy dung lượng cần xóa.");
                }
            }
        }
    }

    // =====================================================
    // LẤY TẤT CẢ SẢN PHẨM
    // =====================================================

    public List<SanPhamItem> getAllSanPham()
            throws SQLException {

        List<SanPhamItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    s.MaSP,
                    s.TenSP,
                    s.MaLoai,
                    l.TenLoai,
                    s.DungLuong,
                    s.DonGia,
                    s.MoTa,
                    s.TrangThai
                FROM SanPham s
                INNER JOIN HangDienThoai l
                    ON s.MaLoai = l.MaLoai
                ORDER BY s.TenSP, s.DungLuong
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                list.add(
                        new SanPhamItem(
                                rs.getLong("MaSP"),
                                rs.getString("TenSP"),
                                rs.getLong("MaLoai"),
                                rs.getString("TenLoai"),
                                rs.getString("DungLuong"),
                                rs.getBigDecimal("DonGia"),
                                rs.getString("MoTa"),
                                rs.getBoolean("TrangThai")
                        )
                );
            }
        }

        return list;
    }

    // =====================================================
    // THÊM SẢN PHẨM
    // =====================================================

    public void themSanPham(
            String tenSP,
            long maLoai,
            String dungLuong,
            BigDecimal donGia,
            String moTa
    ) throws SQLException {

        String sql = """
                INSERT INTO SanPham
                    (TenSP, MaLoai, DungLuong, DonGia, MoTa, TrangThai)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, tenSP.trim());
            ps.setLong(2, maLoai);
            ps.setString(3, dungLuong);
            ps.setBigDecimal(4, donGia);
            ps.setString(5, moTa);

            ps.executeUpdate();
        }
    }

    // =====================================================
    // CẬP NHẬT SẢN PHẨM
    // =====================================================

    public void capNhatSanPham(
            long maSP,
            String tenSP,
            long maLoai,
            String dungLuong,
            BigDecimal donGia,
            String moTa,
            boolean trangThai
    ) throws SQLException {

        String sql = """
                UPDATE SanPham
                SET TenSP = ?,
                    MaLoai = ?,
                    DungLuong = ?,
                    DonGia = ?,
                    MoTa = ?,
                    TrangThai = ?
                WHERE MaSP = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, tenSP.trim());
            ps.setLong(2, maLoai);
            ps.setString(3, dungLuong);
            ps.setBigDecimal(4, donGia);
            ps.setString(5, moTa);
            ps.setBoolean(6, trangThai);
            ps.setLong(7, maSP);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new SQLException(
                        "Không tìm thấy sản phẩm cần cập nhật."
                );
            }
        }
    }

    // =====================================================
    // NGỪNG KINH DOANH
    // Không xóa cứng để giữ lịch sử hóa đơn
    // =====================================================

    public void ngungKinhDoanh(long maSP)
            throws SQLException {

        String sql = """
                UPDATE SanPham
                SET TrangThai = FALSE
                WHERE MaSP = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setLong(1, maSP);
            ps.executeUpdate();
        }
    }
}
