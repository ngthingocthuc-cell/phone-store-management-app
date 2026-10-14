package DAO;

import Utils.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {

    // =====================================================
    // MODEL NHÂN VIÊN DÙNG CHO MÀN HÌNH
    // =====================================================

    public static class NhanVienItem {

        private long maNV;
        private String hoTen;
        private String gioiTinh;
        private Date ngaySinh;
        private String sdt;
        private String email;
        private String diaChi;
        private Date ngayVaoLam;
        private boolean trangThai;
        private String tenDangNhap;

        public NhanVienItem(
                long maNV,
                String hoTen,
                String gioiTinh,
                Date ngaySinh,
                String sdt,
                String email,
                String diaChi,
                Date ngayVaoLam,
                boolean trangThai,
                String tenDangNhap
        ) {
            this.maNV = maNV;
            this.hoTen = hoTen;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.sdt = sdt;
            this.email = email;
            this.diaChi = diaChi;
            this.ngayVaoLam = ngayVaoLam;
            this.trangThai = trangThai;
            this.tenDangNhap = tenDangNhap;
        }

        public long getMaNV() {
            return maNV;
        }

        public String getHoTen() {
            return hoTen;
        }

        public String getGioiTinh() {
            return gioiTinh;
        }

        public Date getNgaySinh() {
            return ngaySinh;
        }

        public String getSDT() {
            return sdt;
        }

        public String getEmail() {
            return email;
        }

        public String getDiaChi() {
            return diaChi;
        }

        public Date getNgayVaoLam() {
            return ngayVaoLam;
        }

        public boolean isTrangThai() {
            return trangThai;
        }

        public String getTenDangNhap() {
            return tenDangNhap;
        }
    }

    // =====================================================
    // LẤY DANH SÁCH NHÂN VIÊN
    // =====================================================

    public List<NhanVienItem> getAll()
            throws SQLException {

        List<NhanVienItem> list = new ArrayList<>();

        String sql = """
                SELECT
                    n.MaNV,
                    n.HoTen,
                    n.GioiTinh,
                    n.NgaySinh,
                    n.SDT,
                    n.Email,
                    n.DiaChi,
                    n.NgayVaoLam,
                    n.TrangThai,
                    (
                        SELECT t.TenDangNhap
                        FROM TaiKhoan t
                        WHERE t.MaNV = n.MaNV
                        ORDER BY t.MaTK
                        FETCH FIRST 1 ROW ONLY
                    ) AS TenDangNhap
                FROM NhanVien n
                ORDER BY n.MaNV DESC
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                list.add(
                        new NhanVienItem(
                                rs.getLong("MaNV"),
                                rs.getString("HoTen"),
                                rs.getString("GioiTinh"),
                                rs.getDate("NgaySinh"),
                                rs.getString("SDT"),
                                rs.getString("Email"),
                                rs.getString("DiaChi"),
                                rs.getDate("NgayVaoLam"),
                                rs.getBoolean("TrangThai"),
                                rs.getString("TenDangNhap")
                        )
                );
            }
        }

        return list;
    }

    // =====================================================
    // KIỂM TRA TÊN ĐĂNG NHẬP ĐÃ TỒN TẠI CHƯA
    // =====================================================

    private boolean tenDangNhapDaTonTai(
            Connection conn,
            String tenDangNhap,
            Long maTKBoQua
    ) throws SQLException {

        String sql;

        if (maTKBoQua == null) {

            sql = """
                    SELECT COUNT(*)
                    FROM TaiKhoan
                    WHERE LOWER(TenDangNhap) = LOWER(?)
                    """;

        } else {

            sql = """
                    SELECT COUNT(*)
                    FROM TaiKhoan
                    WHERE LOWER(TenDangNhap) = LOWER(?)
                      AND MaTK <> ?
                    """;
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap.trim());

            if (maTKBoQua != null) {
                ps.setLong(2, maTKBoQua);
            }

            try (ResultSet rs = ps.executeQuery()) {

                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // =====================================================
    // LẤY TÀI KHOẢN CỦA NHÂN VIÊN
    // =====================================================

    private Long getMaTaiKhoan(
            Connection conn,
            long maNV
    ) throws SQLException {

        String sql = """
                SELECT MaTK
                FROM TaiKhoan
                WHERE MaNV = ?
                ORDER BY MaTK
                FETCH FIRST 1 ROW ONLY
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, maNV);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return rs.getLong("MaTK");
                }
            }
        }

        return null;
    }

    // =====================================================
    // THÊM NHÂN VIÊN VÀ TÀI KHOẢN
    // MẬT KHẨU BAN ĐẦU: 123456
    // =====================================================

    public void themNhanVien(
            String hoTen,
            String gioiTinh,
            Date ngaySinh,
            String sdt,
            String email,
            String diaChi,
            Date ngayVaoLam,
            String tenDangNhap
    ) throws SQLException {

        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập họ tên."
            );
        }

        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên đăng nhập."
            );
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                if (tenDangNhapDaTonTai(
                        conn,
                        tenDangNhap,
                        null
                )) {
                    throw new IllegalArgumentException(
                            "Tên đăng nhập đã được sử dụng."
                    );
                }

                // Thêm nhân viên
                String sqlNV = """
                        INSERT INTO NhanVien
                            (HoTen, GioiTinh, NgaySinh, SDT,
                             Email, DiaChi, NgayVaoLam, TrangThai)
                        VALUES (?, ?, ?, ?, ?, ?, ?, TRUE)
                        """;

                long maNV;

                try (
                        PreparedStatement ps = conn.prepareStatement(
                                sqlNV,
                                Statement.RETURN_GENERATED_KEYS
                        )
                ) {

                    ps.setString(1, hoTen.trim());
                    ps.setString(2, gioiTinh);

                    if (ngaySinh == null) {
                        ps.setNull(
                                3,
                                java.sql.Types.DATE
                        );
                    } else {
                        ps.setDate(3, ngaySinh);
                    }

                    ps.setString(4, sdt);
                    ps.setString(5, email);
                    ps.setString(6, diaChi);

                    if (ngayVaoLam == null) {
                        ps.setDate(
                                7,
                                new Date(System.currentTimeMillis())
                        );
                    } else {
                        ps.setDate(7, ngayVaoLam);
                    }

                    ps.executeUpdate();

                    try (ResultSet keys = ps.getGeneratedKeys()) {

                        if (!keys.next()) {
                            throw new SQLException(
                                    "Không tạo được mã nhân viên."
                            );
                        }

                        maNV = keys.getLong(1);
                    }
                }

                // Tạo tài khoản cho nhân viên mới
                String sqlTK = """
                        INSERT INTO TaiKhoan
                            (TenDangNhap, MatKhau, VaiTro, MaNV, TrangThai)
                        VALUES (?, ?, 'NHANVIEN', ?, TRUE)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sqlTK)) {

                    ps.setString(1, tenDangNhap.trim());

                    // Mật khẩu mẫu; sẽ cải thiện bảo mật ở bước sau.
                    ps.setString(2, Utils.Passwords.hash("123456"));

                    ps.setLong(3, maNV);

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

    // =====================================================
    // CẬP NHẬT NHÂN VIÊN VÀ TÀI KHOẢN
    // =====================================================

    public void capNhatNhanVien(
            long maNV,
            String hoTen,
            String gioiTinh,
            Date ngaySinh,
            String sdt,
            String email,
            String diaChi,
            Date ngayVaoLam,
            String tenDangNhap,
            boolean trangThai
    ) throws SQLException {

        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập họ tên."
            );
        }

        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên đăng nhập."
            );
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                Long maTK = getMaTaiKhoan(conn, maNV);

                if (tenDangNhapDaTonTai(
                        conn,
                        tenDangNhap,
                        maTK
                )) {
                    throw new IllegalArgumentException(
                            "Tên đăng nhập đã được sử dụng."
                    );
                }

                String sqlNV = """
                        UPDATE NhanVien
                        SET HoTen = ?,
                            GioiTinh = ?,
                            NgaySinh = ?,
                            SDT = ?,
                            Email = ?,
                            DiaChi = ?,
                            NgayVaoLam = ?,
                            TrangThai = ?
                        WHERE MaNV = ?
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {

                    ps.setString(1, hoTen.trim());
                    ps.setString(2, gioiTinh);

                    if (ngaySinh == null) {
                        ps.setNull(
                                3,
                                java.sql.Types.DATE
                        );
                    } else {
                        ps.setDate(3, ngaySinh);
                    }

                    ps.setString(4, sdt);
                    ps.setString(5, email);
                    ps.setString(6, diaChi);

                    if (ngayVaoLam == null) {
                        ps.setDate(
                                7,
                                new Date(System.currentTimeMillis())
                        );
                    } else {
                        ps.setDate(7, ngayVaoLam);
                    }

                    ps.setBoolean(8, trangThai);
                    ps.setLong(9, maNV);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Không tìm thấy nhân viên cần sửa."
                        );
                    }
                }

                if (maTK != null) {

                    String sqlTK = """
                            UPDATE TaiKhoan
                            SET TenDangNhap = ?,
                                TrangThai = ?
                            WHERE MaTK = ?
                            """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlTK)) {

                        ps.setString(1, tenDangNhap.trim());
                        ps.setBoolean(2, trangThai);
                        ps.setLong(3, maTK);

                        ps.executeUpdate();
                    }

                } else {

                    // Nếu trước đó nhân viên chưa có tài khoản
                    String sqlTK = """
                            INSERT INTO TaiKhoan
                                (TenDangNhap, MatKhau, VaiTro,
                                 MaNV, TrangThai)
                            VALUES (?, ?, 'NHANVIEN', ?, ?)
                            """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlTK)) {

                        ps.setString(1, tenDangNhap.trim());
                        ps.setString(2, Utils.Passwords.hash("123456"));
                        ps.setLong(3, maNV);
                        ps.setBoolean(4, trangThai);

                        ps.executeUpdate();
                    }
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
    // NGỪNG LÀM VIỆC VÀ KHÓA TÀI KHOẢN
    // =====================================================

    public void ngungLamViec(long maNV)
            throws SQLException {

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {

                String sqlNV = """
                        UPDATE NhanVien
                        SET TrangThai = FALSE
                        WHERE MaNV = ?
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sqlNV)) {

                    ps.setLong(1, maNV);

                    if (ps.executeUpdate() == 0) {
                        throw new SQLException(
                                "Không tìm thấy nhân viên."
                        );
                    }
                }

                String sqlTK = """
                        UPDATE TaiKhoan
                        SET TrangThai = FALSE
                        WHERE MaNV = ?
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sqlTK)) {

                    ps.setLong(1, maNV);

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
    // =====================================================
// DỌN NHÂN VIÊN KHÔNG CÒN TÀI KHOẢN VÀ CHƯA CÓ HÓA ĐƠN
// CHỈ ADMIN ĐƯỢC PHÉP
// =====================================================

public int xoaNhanVienMoCoiKhongHoaDon()
        throws SQLException {

    if (!Utils.Session.isAdmin()) {
        throw new SQLException(
                "Chỉ Admin mới được dọn dữ liệu nhân viên."
        );
    }

    String sql = """
            DELETE FROM NhanVien
            WHERE NOT EXISTS (
                SELECT 1
                FROM TaiKhoan tk
                WHERE tk.MaNV = NhanVien.MaNV
            )
            AND NOT EXISTS (
                SELECT 1
                FROM HoaDon hd
                WHERE hd.MaNV = NhanVien.MaNV
            )
            """;

    try (
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)
    ) {
        return ps.executeUpdate();
    }
}
}
