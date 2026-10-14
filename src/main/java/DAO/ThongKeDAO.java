package DAO;

import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ThongKeDAO {

    // =====================================================
    // TỔNG QUAN DOANH THU
    // =====================================================

    public static class TongQuan {

        private long soHoaDon;
        private BigDecimal doanhThu;
        private BigDecimal giamGia;

        public TongQuan(
                long soHoaDon,
                BigDecimal doanhThu,
                BigDecimal giamGia
        ) {
            this.soHoaDon = soHoaDon;
            this.doanhThu = doanhThu;
            this.giamGia = giamGia;
        }

        public long getSoHoaDon() {
            return soHoaDon;
        }

        public BigDecimal getDoanhThu() {
            return doanhThu;
        }

        public BigDecimal getGiamGia() {
            return giamGia;
        }
    }

    // =====================================================
    // DOANH THU THEO NGÀY
    // =====================================================

    public static class DoanhThuNgay {

        private LocalDate ngay;
        private BigDecimal doanhThu;
        private long soHoaDon;

        public DoanhThuNgay(
                LocalDate ngay,
                BigDecimal doanhThu,
                long soHoaDon
        ) {
            this.ngay = ngay;
            this.doanhThu = doanhThu;
            this.soHoaDon = soHoaDon;
        }

        public LocalDate getNgay() {
            return ngay;
        }

        public BigDecimal getDoanhThu() {
            return doanhThu;
        }

        public long getSoHoaDon() {
            return soHoaDon;
        }
    }

    // =====================================================
    // SẢN PHẨM BÁN CHẠY
    // =====================================================

    public static class SanPhamBanChay {

        private String tenSanPham;
        private String dungLuong;
        private long soLuong;
        private BigDecimal doanhThu;

        public SanPhamBanChay(
                String tenSanPham,
                String dungLuong,
                long soLuong,
                BigDecimal doanhThu
        ) {
            this.tenSanPham = tenSanPham;
            this.dungLuong = dungLuong;
            this.soLuong = soLuong;
            this.doanhThu = doanhThu;
        }

        public String getTenSanPham() {
            return tenSanPham;
        }

        public String getDungLuong() {
            return dungLuong;
        }

        public long getSoLuong() {
            return soLuong;
        }

        public BigDecimal getDoanhThu() {
            return doanhThu;
        }
    }

    // =====================================================
    // LẤY TỔNG QUAN
    // Chỉ tính hóa đơn đã thanh toán
    // =====================================================

    public TongQuan getTongQuan(
            LocalDate tuNgay,
            LocalDate denNgay
    ) throws SQLException {

        String sql = """
                SELECT
                    COUNT(*) AS SoHoaDon,
                    COALESCE(SUM(ThanhTien), 0) AS DoanhThu,
                    COALESCE(SUM(GiamGia), 0) AS GiamGia
                FROM HoaDon
                WHERE TrangThai = 'Đã thanh toán'
                  AND NgayLap >= ?
                  AND NgayLap < ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            setKhoangNgay(ps, 1, tuNgay, denNgay);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {

                    return new TongQuan(
                            rs.getLong("SoHoaDon"),
                            rs.getBigDecimal("DoanhThu"),
                            rs.getBigDecimal("GiamGia")
                    );
                }
            }
        }

        return new TongQuan(
                0,
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );
    }

    // =====================================================
    // DOANH THU THEO NGÀY
    // =====================================================

    public List<DoanhThuNgay> getDoanhThuTheoNgay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) throws SQLException {

        List<DoanhThuNgay> list = new ArrayList<>();

        String sql = """
                SELECT
                    CAST(NgayLap AS DATE) AS Ngay,
                    COALESCE(SUM(ThanhTien), 0) AS DoanhThu,
                    COUNT(*) AS SoHoaDon
                FROM HoaDon
                WHERE TrangThai = 'Đã thanh toán'
                  AND NgayLap >= ?
                  AND NgayLap < ?
                GROUP BY CAST(NgayLap AS DATE)
                ORDER BY CAST(NgayLap AS DATE)
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            setKhoangNgay(ps, 1, tuNgay, denNgay);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(
                            new DoanhThuNgay(
                                    rs.getDate("Ngay").toLocalDate(),
                                    rs.getBigDecimal("DoanhThu"),
                                    rs.getLong("SoHoaDon")
                            )
                    );
                }
            }
        }

        return list;
    }

    // =====================================================
    // TOP 10 SẢN PHẨM BÁN CHẠY
    // =====================================================

    public List<SanPhamBanChay> getSanPhamBanChay(
            LocalDate tuNgay,
            LocalDate denNgay
    ) throws SQLException {

        List<SanPhamBanChay> list = new ArrayList<>();

        String sql = """
                SELECT
                    ct.TenSanPham,
                    ct.DungLuong,
                    SUM(ct.SoLuong) AS SoLuongBan,
                    COALESCE(SUM(ct.ThanhTien), 0) AS DoanhThu
                FROM HoaDonChiTiet ct
                INNER JOIN HoaDon hd
                    ON ct.MaHD = hd.MaHD
                WHERE hd.TrangThai = 'Đã thanh toán'
                  AND hd.NgayLap >= ?
                  AND hd.NgayLap < ?
                GROUP BY ct.TenSanPham, ct.DungLuong
                ORDER BY SUM(ct.SoLuong) DESC, ct.TenSanPham
                FETCH FIRST 10 ROWS ONLY
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            setKhoangNgay(ps, 1, tuNgay, denNgay);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    list.add(
                            new SanPhamBanChay(
                                    rs.getString("TenSanPham"),
                                    rs.getString("DungLuong"),
                                    rs.getLong("SoLuongBan"),
                                    rs.getBigDecimal("DoanhThu")
                            )
                    );
                }
            }
        }

        return list;
    }

    // =====================================================
    // THIẾT LẬP KHOẢNG NGÀY
    // Ngày kết thúc được tính trọn ngày đó
    // =====================================================

    private void setKhoangNgay(
            PreparedStatement ps,
            int startIndex,
            LocalDate tuNgay,
            LocalDate denNgay
    ) throws SQLException {

        ps.setTimestamp(
                startIndex,
                Timestamp.valueOf(tuNgay.atStartOfDay())
        );

        ps.setTimestamp(
                startIndex + 1,
                Timestamp.valueOf(
                        denNgay.plusDays(1).atStartOfDay()
                )
        );
    }
}
