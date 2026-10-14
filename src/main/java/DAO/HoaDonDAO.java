
package DAO;

import Model.NhanVien;
import Utils.DBConnection;
import Utils.Session;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HoaDonDAO {

    // =====================================================
    // MODEL HÓA ĐƠN
    // =====================================================

    public static class HoaDonItem {

        private long maHD;
        private long maNV;
        private String tenNhanVien;
        private Timestamp ngayLap;
        private BigDecimal tongTien;
        private BigDecimal giamGia;
        private BigDecimal thanhTien;
        private String phuongThucTT;
        private String trangThai;
        private String tenKhachHang;

        public HoaDonItem(
                long maHD,
                long maNV,
                String tenNhanVien,
                Timestamp ngayLap,
                BigDecimal tongTien,
                BigDecimal giamGia,
                BigDecimal thanhTien,
                String phuongThucTT,
                String trangThai,
                String tenKhachHang
        ) {
            this.maHD = maHD;
            this.maNV = maNV;
            this.tenNhanVien = tenNhanVien;
            this.ngayLap = ngayLap;
            this.tongTien = tongTien;
            this.giamGia = giamGia;
            this.thanhTien = thanhTien;
            this.phuongThucTT = phuongThucTT;
            this.trangThai = trangThai;
            this.tenKhachHang = tenKhachHang;
        }

        public long getMaHD() {
            return maHD;
        }

        public long getMaNV() {
            return maNV;
        }

        public String getTenNhanVien() {
            return tenNhanVien;
        }

        public Timestamp getNgayLap() {
            return ngayLap;
        }

        public BigDecimal getTongTien() {
            return tongTien;
        }

        public BigDecimal getGiamGia() {
            return giamGia;
        }

        public BigDecimal getThanhTien() {
            return thanhTien;
        }

        public String getPhuongThucTT() {
            return phuongThucTT;
        }

        public String getTrangThai() {
            return trangThai;
        }

        public String getTenKhachHang() {
            return tenKhachHang;
        }
    }

    // =====================================================
    // MODEL CHI TIẾT HÓA ĐƠN
    // =====================================================

    public static class ChiTietItem {

        private long maCT;
        private String tenSanPham;
        private String dungLuong;
        private BigDecimal donGia;
        private int soLuong;
        private String tenPhuKien;
        private BigDecimal thanhTien;

        public ChiTietItem(
                long maCT,
                String tenSanPham,
                String dungLuong,
                BigDecimal donGia,
                int soLuong,
                String tenPhuKien,
                BigDecimal thanhTien
        ) {
            this.maCT = maCT;
            this.tenSanPham = tenSanPham;
            this.dungLuong = dungLuong;
            this.donGia = donGia;
            this.soLuong = soLuong;
            this.tenPhuKien = tenPhuKien;
            this.thanhTien = thanhTien;
        }

        public long getMaCT() {
            return maCT;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public String getDungLuong() {
            return dungLuong;
        }

        public BigDecimal getDonGia() {
            return donGia;
        }

        public int getSoLuong() {
            return soLuong;
        }

        public String getTenPhuKien() {
            return tenPhuKien;
        }

        public BigDecimal getThanhTien() {
            return thanhTien;
        }
    }

    // =====================================================
    // LẤY DANH SÁCH NHÂN VIÊN
    // =====================================================

    public List<NhanVien> getNhanVienFilter()
            throws SQLException {

        List<NhanVien> list = new ArrayList<>();

        String sql = """
                SELECT MaNV, HoTen
                FROM NhanVien
                ORDER BY HoTen
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                list.add(
                        new NhanVien(
                                rs.getLong("MaNV"),
                                rs.getString("HoTen")
                        )
                );
            }
        }

        return list;
    }

    // =====================================================
    // TÌM KIẾM HÓA ĐƠN THEO MÃ, NHÂN VIÊN VÀ KHOẢNG NGÀY
    // Nhân viên chỉ xem hóa đơn của chính mình.
    // Admin có thể xem tất cả hoặc lọc theo nhân viên.
    // =====================================================

    // Giữ hàm cũ để tương thích với các chỗ đang gọi bằng một ngày.
    public List<HoaDonItem> timKiemHoaDon(
            boolean admin,
            Long maNVHienTai,
            Long maNVFilter,
            Long maHDFilter,
            LocalDate ngayFilter
    ) throws SQLException {

        return timKiemHoaDon(
                admin,
                maNVHienTai,
                maNVFilter,
                maHDFilter,
                ngayFilter,
                ngayFilter
        );
    }

    // Hàm mới: tìm từ ngày bắt đầu đến hết ngày kết thúc.
    public List<HoaDonItem> timKiemHoaDon(
            boolean admin,
            Long maNVHienTai,
            Long maNVFilter,
            Long maHDFilter,
            LocalDate tuNgayFilter,
            LocalDate denNgayFilter
    ) throws SQLException {

        List<HoaDonItem> list = new ArrayList<>();
        if (!Session.isLoggedIn()) throw new SQLException("Vui lòng đăng nhập.");
        admin = Session.isAdmin();
        maNVHienTai = Session.getTaiKhoan().getMaNV();

        if ((tuNgayFilter == null) != (denNgayFilter == null)) {
            throw new IllegalArgumentException(
                    "Phải nhập đủ ngày bắt đầu và ngày kết thúc."
            );
        }

        if (tuNgayFilter != null
                && denNgayFilter.isBefore(tuNgayFilter)) {
            throw new IllegalArgumentException(
                    "Ngày kết thúc phải bằng hoặc sau ngày bắt đầu."
            );
        }

        StringBuilder sql = new StringBuilder("""
                SELECT
                    h.MaHD,
                    h.MaNV,
                    n.HoTen AS TenNhanVien,
                    h.NgayLap,
                    h.TongTien,
                    h.GiamGia,
                    h.ThanhTien,
                    h.PhuongThucTT,
                    h.TrangThai,
                    h.TenKhachHang
                FROM HoaDon h
                INNER JOIN NhanVien n
                    ON h.MaNV = n.MaNV
                WHERE 1 = 1
                """);

        List<Object> params = new ArrayList<>();

        // Nhân viên chỉ được xem hóa đơn của chính mình.
        if (!admin) {

            if (maNVHienTai == null) {
                throw new SQLException(
                        "Tài khoản chưa được liên kết với nhân viên."
                );
            }

            sql.append(" AND h.MaNV = ? ");
            params.add(maNVHienTai);

        } else if (maNVFilter != null) {

            sql.append(" AND h.MaNV = ? ");
            params.add(maNVFilter);
        }

        // Lọc mã hóa đơn.
        if (maHDFilter != null) {
            sql.append(" AND h.MaHD = ? ");
            params.add(maHDFilter);
        }

        // Lọc ngày theo khoảng, bao gồm cả ngày kết thúc.
        if (tuNgayFilter != null) {

            sql.append("""
                     AND h.NgayLap >= ?
                     AND h.NgayLap < ?
                    """);

            params.add(
                    Timestamp.valueOf(
                            tuNgayFilter.atStartOfDay()
                    )
            );

            // Mốc đầu ngày kế tiếp giúp không bỏ sót hóa đơn
            // được lập vào buổi tối của ngày kết thúc.
            params.add(
                    Timestamp.valueOf(
                            denNgayFilter.plusDays(1).atStartOfDay()
                    )
            );
        }

        sql.append(
                " ORDER BY h.NgayLap DESC, h.MaHD DESC "
        );

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps =
                        conn.prepareStatement(sql.toString())
        ) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(
                            new HoaDonItem(
                                    rs.getLong("MaHD"),
                                    rs.getLong("MaNV"),
                                    rs.getString("TenNhanVien"),
                                    rs.getTimestamp("NgayLap"),
                                    rs.getBigDecimal("TongTien"),
                                    rs.getBigDecimal("GiamGia"),
                                    rs.getBigDecimal("ThanhTien"),
                                    rs.getString("PhuongThucTT"),
                                    rs.getString("TrangThai"),
                                    rs.getString("TenKhachHang")
                            )
                    );
                }
            }
        }

        return list;
    }

    // =====================================================
    // LẤY CHI TIẾT HÓA ĐƠN
    // =====================================================

    public List<ChiTietItem> getChiTietHoaDon(
            long maHD
    ) throws SQLException {
        kiemTraQuyenHoaDon(maHD);

        List<ChiTietItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    MaCT,
                    TenSanPham,
                    DungLuong,
                    DonGia,
                    SoLuong,
                    ThanhTien
                FROM HoaDonChiTiet
                WHERE MaHD = ?
                ORDER BY MaCT
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setLong(1, maHD);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    long maCT = rs.getLong("MaCT");

                    String phuKien = getPhuKienChiTiet(
                            conn,
                            maCT
                    );

                    list.add(
                            new ChiTietItem(
                                    maCT,
                                    rs.getString("TenSanPham"),
                                    rs.getString("DungLuong"),
                                    rs.getBigDecimal("DonGia"),
                                    rs.getInt("SoLuong"),
                                    phuKien,
                                    rs.getBigDecimal("ThanhTien")
                            )
                    );
                }
            }
        }

        return list;
    }

    // =====================================================
    // LẤY TOPPING CỦA MỘT DÒNG CHI TIẾT
    // =====================================================

    private String getPhuKienChiTiet(
            Connection conn,
            long maCT
    ) throws SQLException {

        String sql = """
                SELECT TenPhuKien, SoLuong
                FROM HoaDonChiTietPhuKien
                WHERE MaCT = ?
                ORDER BY TenPhuKien
                """;

        StringJoiner joiner = new StringJoiner(", ");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, maCT);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    joiner.add(
                            rs.getString("TenPhuKien")
                            + " x"
                            + rs.getInt("SoLuong")
                    );
                }
            }
        }

        String result = joiner.toString();

        return result.isBlank()
                ? "Không phuKien"
                : result;
    }

    // =====================================================
    // XÓA HÓA ĐƠN - CHỈ ADMIN ĐƯỢC PHÉP
    // =====================================================

    public void xoaHoaDon(long maHD) throws SQLException {

        // Kiểm tra quyền ngay trong DAO, không chỉ ẩn nút trên giao diện.
        if (!Session.isAdmin()) {
            throw new SQLException(
                    "Chỉ Admin mới được phép xóa hóa đơn."
            );
        }

        if (maHD <= 0) {
            throw new IllegalArgumentException(
                    "Mã hóa đơn không hợp lệ."
            );
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                try (PreparedStatement warranty=conn.prepareStatement("SELECT COUNT(*) FROM BaoHanh b JOIN HoaDonChiTiet ct ON ct.MaCT=b.MaCT WHERE ct.MaHD=?")) {
                    warranty.setLong(1,maHD);
                    try(ResultSet r=warranty.executeQuery()){r.next();if(r.getInt(1)>0)throw new SQLException("Hóa đơn đã có sổ IMEI bảo hành, cần giữ lại để tra cứu nguồn bán.");}
                }
                // Lock the invoice before returning its reserved/sold stock.
                try (PreparedStatement lock = conn.prepareStatement("SELECT MaHD FROM HoaDon WHERE MaHD=? FOR UPDATE")) {
                    lock.setLong(1, maHD);
                    try (ResultSet rs = lock.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không tìm thấy hóa đơn cần xóa.");
                    }
                }
                try (PreparedStatement lines = conn.prepareStatement("SELECT MaSP,SUM(SoLuong) FROM HoaDonChiTiet WHERE MaHD=? GROUP BY MaSP ORDER BY MaSP")) {
                    lines.setLong(1, maHD);
                    try (ResultSet rs = lines.executeQuery(); PreparedStatement stock = conn.prepareStatement("UPDATE SanPham SET TonKho=TonKho+? WHERE MaSP=?")) {
                        while (rs.next()) {stock.setInt(1,rs.getInt(2));stock.setLong(2,rs.getLong(1));stock.executeUpdate();}
                    }
                }
                // Bước 1: Xóa phuKien thuộc các dòng chi tiết.
                String sqlPhuKien = """
                        DELETE FROM HoaDonChiTietPhuKien
                        WHERE MaCT IN (
                            SELECT MaCT
                            FROM HoaDonChiTiet
                            WHERE MaHD = ?
                        )
                        """;

                try (PreparedStatement ps =
                             conn.prepareStatement(sqlPhuKien)) {
                    ps.setLong(1, maHD);
                    ps.executeUpdate();
                }

                // Bước 2: Xóa chi tiết hóa đơn.
                String sqlChiTiet = """
                        DELETE FROM HoaDonChiTiet
                        WHERE MaHD = ?
                        """;

                try (PreparedStatement ps =
                             conn.prepareStatement(sqlChiTiet)) {
                    ps.setLong(1, maHD);
                    ps.executeUpdate();
                }

                // Bước 3: Xóa hóa đơn chính.
                String sqlHoaDon = """
                        DELETE FROM HoaDon
                        WHERE MaHD = ?
                        """;

                int soDong;

                try (PreparedStatement ps =
                             conn.prepareStatement(sqlHoaDon)) {
                    ps.setLong(1, maHD);
                    soDong = ps.executeUpdate();
                }

                if (soDong == 0) {
                    throw new SQLException(
                            "Không tìm thấy hóa đơn cần xóa."
                    );
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

    // =====================================================
    // XÁC NHẬN THANH TOÁN
    // =====================================================

    public boolean xacNhanThanhToan(long maHD)
            throws SQLException {
        kiemTraQuyenHoaDon(maHD);

        String sql = """
                UPDATE HoaDon
                SET TrangThai = 'Đã thanh toán'
                WHERE MaHD = ?
                  AND TrangThai = 'Chờ thanh toán'
                  AND PhuongThucTT IN ('Chuyển khoản', 'VietQR')
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setLong(1, maHD);

            return ps.executeUpdate() == 1;
        }
    }
    private void kiemTraQuyenHoaDon(long id) throws SQLException {
        if (!Session.isLoggedIn()) throw new SQLException("Vui lòng đăng nhập.");
        if (Session.isAdmin()) return;
        try (Connection c=DBConnection.getConnection(); PreparedStatement s=c.prepareStatement("SELECT MaNV FROM HoaDon WHERE MaHD=?")) {
            s.setLong(1,id);
            try (ResultSet r=s.executeQuery()) {if(!r.next()||!java.util.Objects.equals(Session.getTaiKhoan().getMaNV(),r.getLong(1)))throw new SQLException("Bạn không có quyền truy cập hóa đơn này.");}
        }
    }
}
