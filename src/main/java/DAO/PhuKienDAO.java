package DAO;

import Model.PhuKien;
import Utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PhuKienDAO {

    // Lấy tất cả phuKien, gồm cả phuKien đã ngừng bán.
    public List<PhuKien> getAll() throws SQLException {

        List<PhuKien> list = new ArrayList<>();

        String sql = """
                SELECT MaPhuKien, TenPhuKien, DonGia, TrangThai
                FROM PhuKien
                ORDER BY TenPhuKien
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {

            while (rs.next()) {

                list.add(
                        new PhuKien(
                                rs.getLong("MaPhuKien"),
                                rs.getString("TenPhuKien"),
                                rs.getBigDecimal("DonGia"),
                                rs.getBoolean("TrangThai")
                        )
                );
            }
        }

        return list;
    }

    // Thêm phuKien mới.
    public void themPhuKien(
            String tenPhuKien,
            BigDecimal donGia
    ) throws SQLException {

        String sql = """
                INSERT INTO PhuKien
                    (TenPhuKien, DonGia, TrangThai)
                VALUES (?, ?, TRUE)
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, tenPhuKien.trim());
            ps.setBigDecimal(2, donGia);

            ps.executeUpdate();
        }
    }

    // Cập nhật tên, giá và trạng thái.
    public void capNhatPhuKien(
            long maPhuKien,
            String tenPhuKien,
            BigDecimal donGia,
            boolean trangThai
    ) throws SQLException {

        String sql = """
                UPDATE PhuKien
                SET TenPhuKien = ?,
                    DonGia = ?,
                    TrangThai = ?
                WHERE MaPhuKien = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setString(1, tenPhuKien.trim());
            ps.setBigDecimal(2, donGia);
            ps.setBoolean(3, trangThai);
            ps.setLong(4, maPhuKien);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "Không tìm thấy phuKien cần cập nhật."
                );
            }
        }
    }

    // Ngừng bán nhưng không xóa dữ liệu.
    public void ngungKinhDoanh(long maPhuKien)
            throws SQLException {

        String sql = """
                UPDATE PhuKien
                SET TrangThai = FALSE
                WHERE MaPhuKien = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {

            ps.setLong(1, maPhuKien);

            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "Không tìm thấy phuKien cần ngừng bán."
                );
            }
        }
    }
}