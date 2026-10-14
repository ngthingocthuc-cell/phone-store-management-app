
package View;
import Utils.Theme;

import Utils.DBConnection;
import Utils.TextSearch;
import Utils.Session;
import Model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanView extends JPanel {

    private final Color MAU_NEN = Theme.BG;
    private final Color MAU_CHU = new Color(35, 45, 65);
    private final Color MAU_CHINH = new Color(88, 73, 245);
    private final Color MAU_DO = new Color(205, 55, 65);

    private JTextField txtTimKiem;
    private JTable tblTaiKhoan;
    private DefaultTableModel model;
    private TableRowSorter<DefaultTableModel> sorter;

    private JCheckBox chkHienMatKhau;
    private JLabel lblTrangThai;

    public TaiKhoanView() {
        setLayout(new BorderLayout(12, 12));
        setBackground(MAU_NEN);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        if (!laAdmin()) {
            add(
                    new JLabel(
                            "Bạn không có quyền quản lý tài khoản.",
                            SwingConstants.CENTER
                    ),
                    BorderLayout.CENTER
            );
            return;
        }

        khoiTaoGiaoDien();
        taiDanhSachTaiKhoan();
    }

    // =====================================================
    // PHÂN QUYỀN
    // =====================================================

    private boolean laAdmin() {
        TaiKhoan tk = Session.getTaiKhoan();
        return tk != null && tk.isAdmin();
    }

    private boolean kiemTraAdmin() {
        if (!laAdmin()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Chỉ Admin mới được quản lý tài khoản!",
                    "Không có quyền",
                    JOptionPane.WARNING_MESSAGE
            );
            return false;
        }
        return true;
    }

    // =====================================================
    // GIAO DIỆN
    // =====================================================

    private void khoiTaoGiaoDien() {

        JLabel lblTieuDe = new JLabel("Quản lý tài khoản");
        lblTieuDe.setFont(
                new Font("Segoe UI", Font.BOLD, 25)
        );
        lblTieuDe.setForeground(MAU_CHU);

        JLabel lblMoTa = new JLabel(
                "Xem tài khoản, đổi mật khẩu, khóa và xóa tài khoản."
        );
        lblMoTa.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        lblMoTa.setForeground(Color.GRAY);

        JPanel pnlTieuDe = new JPanel();
        pnlTieuDe.setOpaque(false);
        pnlTieuDe.setLayout(
                new BoxLayout(pnlTieuDe, BoxLayout.Y_AXIS)
        );
        pnlTieuDe.add(lblTieuDe);
        pnlTieuDe.add(Box.createVerticalStrut(5));
        pnlTieuDe.add(lblMoTa);

        // Tìm kiếm
        JPanel pnlTimKiem = new JPanel(
                new BorderLayout(8, 0)
        );
        pnlTimKiem.setOpaque(false);

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );
        txtTimKiem.setToolTipText(
                "Tìm theo mã tài khoản, mã nhân viên, tên đăng nhập hoặc tên nhân viên"
        );

        JButton btnTimKiem = new JButton("Tìm kiếm");
        JButton btnLamMoi = new JButton("Làm mới");

        btnTimKiem.addActionListener(e -> locDanhSach());
        btnLamMoi.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDanhSachTaiKhoan();
        });
        txtTimKiem.addActionListener(e -> locDanhSach());

        JPanel pnlNutTimKiem = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 6, 0)
        );
        pnlNutTimKiem.setOpaque(false);
        pnlNutTimKiem.add(btnTimKiem);
        pnlNutTimKiem.add(btnLamMoi);

        pnlTimKiem.add(txtTimKiem, BorderLayout.CENTER);
        pnlTimKiem.add(pnlNutTimKiem, BorderLayout.EAST);

        JPanel pnlTren = new JPanel(
                new BorderLayout(0, 12)
        );
        pnlTren.setOpaque(false);
        pnlTren.add(pnlTieuDe, BorderLayout.NORTH);
        pnlTren.add(pnlTimKiem, BorderLayout.SOUTH);

        add(pnlTren, BorderLayout.NORTH);

        // Bảng tài khoản
        model = new DefaultTableModel(
                new Object[]{
                        "Mã TK",
                        "Mã NV",
                        "Nhân viên",
                        "Tên đăng nhập",
                        "Mật khẩu",
                        "Vai trò",
                        "Trạng thái"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblTaiKhoan = new JTable(model);
        tblTaiKhoan.setRowHeight(30);
        tblTaiKhoan.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );
        tblTaiKhoan.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        tblTaiKhoan.getTableHeader().setReorderingAllowed(false);
        tblTaiKhoan.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );
        tblTaiKhoan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        sorter = new TableRowSorter<>(model);
        tblTaiKhoan.setRowSorter(sorter);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { locDanhSach(); }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });

        int[] widths = {60, 65, 180, 150, 150, 100, 120};

        for (int i = 0; i < widths.length; i++) {
            tblTaiKhoan.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        // Ẩn mật khẩu mặc định
        tblTaiKhoan.getColumnModel()
                .getColumn(4)
                .setCellRenderer(new DefaultTableCellRenderer() {

                    @Override
                    public Component getTableCellRendererComponent(
                            JTable table,
                            Object value,
                            boolean isSelected,
                            boolean hasFocus,
                            int row,
                            int column
                    ) {
                        String text = value == null
                                ? ""
                                : value.toString();

                        if (chkHienMatKhau != null
                                && !chkHienMatKhau.isSelected()
                                && !text.isEmpty()) {
                            text = "••••••••";
                        }

                        return super.getTableCellRendererComponent(
                                table, text, isSelected,
                                hasFocus, row, column
                        );
                    }
                });

        JScrollPane scroll = new JScrollPane(tblTaiKhoan);
        scroll.setBorder(
                new LineBorder(new Color(220, 225, 235))
        );
        add(scroll, BorderLayout.CENTER);

        // Thanh chức năng
        JPanel pnlChucNang = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 8)
        );
        pnlChucNang.setBackground(Color.WHITE);
        pnlChucNang.setBorder(
                new LineBorder(new Color(225, 230, 240))
        );

        chkHienMatKhau = new JCheckBox("Hiện mật khẩu mặc định");
        chkHienMatKhau.setOpaque(false);
        chkHienMatKhau.setToolTipText("Chỉ mật khẩu mặc định mới xem được; mật khẩu đã đổi không thể khôi phục từ chuỗi băm.");
        chkHienMatKhau.addActionListener(
                e -> tblTaiKhoan.repaint()
        );

        JButton btnDoiMatKhau = new JButton("Đổi mật khẩu");
        JButton btnReset = new JButton("Reset mật khẩu");
        JButton btnKhoaMo = new JButton("Khóa / mở khóa");
        JButton btnXoa = new JButton("Xóa tài khoản");
        JButton btnTaiLai = new JButton("Tải lại");

        btnXoa.setForeground(Color.WHITE);
        btnXoa.setBackground(MAU_DO);
        btnXoa.setFocusPainted(false);
        btnXoa.setBorderPainted(false);

        btnDoiMatKhau.addActionListener(
                e -> doiMatKhauDaChon()
        );
        btnReset.addActionListener(
                e -> resetMatKhauDaChon()
        );
        btnKhoaMo.addActionListener(
                e -> doiTrangThaiDaChon()
        );
        btnXoa.addActionListener(
                e -> xoaTaiKhoanDaChon()
        );
        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            taiDanhSachTaiKhoan();
        });

        pnlChucNang.add(chkHienMatKhau);
        pnlChucNang.add(btnDoiMatKhau);
        pnlChucNang.add(btnReset);
        pnlChucNang.add(btnKhoaMo);
        pnlChucNang.add(btnXoa);
        pnlChucNang.add(btnTaiLai);

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setForeground(Color.GRAY);

        JPanel pnlDuoi = new JPanel(
                new BorderLayout(0, 4)
        );
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(pnlChucNang, BorderLayout.NORTH);
        pnlDuoi.add(lblTrangThai, BorderLayout.SOUTH);

        add(pnlDuoi, BorderLayout.SOUTH);
    }

    // =====================================================
    // TẢI DANH SÁCH TÀI KHOẢN
    // =====================================================

    private void taiDanhSachTaiKhoan() {

        if (!kiemTraAdmin()) {
            return;
        }

        model.setRowCount(0);

        String sql = """
                SELECT
                    tk.MaTK,
                    tk.MaNV,
                    COALESCE(
                        nv.HoTen,
                        '(Không gắn nhân viên)'
                    ) AS HoTen,
                    tk.TenDangNhap,
                    tk.MatKhau,
                    tk.VaiTro,
                    tk.TrangThai
                FROM TaiKhoan tk
                LEFT JOIN NhanVien nv
                    ON tk.MaNV = nv.MaNV
                ORDER BY tk.MaTK
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {

                Object maNVValue = rs.getObject("MaNV");

                String maNV = maNVValue == null
                        ? "-"
                        : maNVValue.toString();

                model.addRow(new Object[]{
                        rs.getLong("MaTK"),
                        maNV,
                        rs.getString("HoTen"),
                        rs.getString("TenDangNhap"),
                        matKhauMacDinhHienThi(
                                rs.getString("VaiTro"),
                                rs.getString("MatKhau")
                        ),
                        rs.getString("VaiTro"),
                        rs.getBoolean("TrangThai")
                                ? "Đang hoạt động"
                                : "Đã khóa"
                });
            }

            locDanhSach();

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không tải được danh sách tài khoản:\n"
                    + e.getMessage()
            );
        }
    }

    private String matKhauMacDinhHienThi(String vaiTro, String storedHash) {
        boolean admin = "ADMIN".equalsIgnoreCase(vaiTro);
        String defaultPassword = admin ? "admin123" : "123456";
        return Utils.Passwords.matches(defaultPassword, storedHash)
                ? defaultPassword
                : "Đã đổi";
    }

    // =====================================================
    // TÌM KIẾM
    // =====================================================

    private void locDanhSach() {

        if (sorter == null || txtTimKiem == null) {
            return;
        }

        String tuKhoa = txtTimKiem.getText().trim();

        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
                @Override
                public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                    // Passwords, role and status are intentionally excluded from search.
                    for (int column = 0; column <= 3; column++) {
                        Object value = entry.getValue(column);
                        if (TextSearch.contains(value == null ? "" : value.toString(), tuKhoa)) {
                            return true;
                        }
                    }
                    return false;
                }
            });
        }

        lblTrangThai.setText(
                "Số tài khoản hiển thị: "
                + tblTaiKhoan.getRowCount()
        );
    }

    // =====================================================
    // ĐỌC DÒNG ĐANG CHỌN
    // =====================================================

    private Long layMaTKDaChon() {

        int row = tblTaiKhoan.getSelectedRow();

        if (row < 0) {
            hienThiCanhBao(
                    "Vui lòng chọn tài khoản trong bảng."
            );
            return null;
        }

        int modelRow = tblTaiKhoan.convertRowIndexToModel(row);

        return Long.parseLong(
                model.getValueAt(modelRow, 0).toString()
        );
    }

    private String layTenDangNhapDaChon() {

        int row = tblTaiKhoan.getSelectedRow();

        if (row < 0) {
            return "";
        }

        int modelRow = tblTaiKhoan.convertRowIndexToModel(row);

        return model.getValueAt(modelRow, 3).toString();
    }

    private String layVaiTroDaChon() {
        int row = tblTaiKhoan.getSelectedRow();
        if (row < 0) {
            return "";
        }
        int modelRow = tblTaiKhoan.convertRowIndexToModel(row);
        return model.getValueAt(modelRow, 5).toString();
    }

    // =====================================================
    // ĐỔI MẬT KHẨU
    // =====================================================

    private void doiMatKhauDaChon() {

        if (!kiemTraAdmin()) {
            return;
        }

        Long maTK = layMaTKDaChon();

        if (maTK == null) {
            return;
        }

        String tenDangNhap = layTenDangNhapDaChon();

        JPasswordField txtMoi = new JPasswordField(20);
        JPasswordField txtXacNhan = new JPasswordField(20);

        JPanel pnl = new JPanel(new GridLayout(0, 1, 5, 5));
        pnl.add(new JLabel("Tài khoản: " + tenDangNhap));
        pnl.add(new JLabel("Mật khẩu mới:"));
        pnl.add(txtMoi);
        pnl.add(new JLabel("Nhập lại mật khẩu mới:"));
        pnl.add(txtXacNhan);

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                pnl,
                "Đổi mật khẩu tài khoản",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (luaChon != JOptionPane.OK_OPTION) {
            return;
        }

        String matKhauMoi = new String(txtMoi.getPassword());
        String xacNhan = new String(txtXacNhan.getPassword());

        if (matKhauMoi.length() < 6) {
            hienThiCanhBao("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        if (!matKhauMoi.equals(xacNhan)) {
            hienThiCanhBao("Mật khẩu xác nhận không khớp.");
            return;
        }

        String sql = """
                UPDATE TaiKhoan
                SET MatKhau = ?
                WHERE MaTK = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, Utils.Passwords.hash(matKhauMoi));
            ps.setLong(2, maTK);

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đổi mật khẩu thành công cho " + tenDangNhap,
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                taiDanhSachTaiKhoan();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không đổi được mật khẩu:\n" + e.getMessage()
            );
        }
    }

    // =====================================================
    // RESET MẬT KHẨU
    // =====================================================

    private void resetMatKhauDaChon() {

        if (!kiemTraAdmin()) {
            return;
        }

        Long maTK = layMaTKDaChon();

        if (maTK == null) {
            return;
        }

        String tenDangNhap = layTenDangNhapDaChon();
        String vaiTro = layVaiTroDaChon();
        String matKhauMacDinh = "ADMIN".equalsIgnoreCase(vaiTro)
                ? "admin123" : "123456";

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Reset mật khẩu tài khoản " + tenDangNhap
                + " về " + matKhauMacDinh + "?",
                "Xác nhận reset mật khẩu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                UPDATE TaiKhoan
                SET MatKhau = ?
                WHERE MaTK = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setString(1, Utils.Passwords.hash(matKhauMacDinh));
            ps.setLong(2, maTK);

            if (ps.executeUpdate() == 1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Đã reset mật khẩu tài khoản "
                        + tenDangNhap + " về " + matKhauMacDinh + ".",
                        "Reset thành công",
                        JOptionPane.INFORMATION_MESSAGE
                );
                taiDanhSachTaiKhoan();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không reset được mật khẩu:\n" + e.getMessage()
            );
        }
    }

    // =====================================================
    // KHÓA / MỞ KHÓA
    // =====================================================

    private void doiTrangThaiDaChon() {

        if (!kiemTraAdmin()) {
            return;
        }

        Long maTK = layMaTKDaChon();

        if (maTK == null) {
            return;
        }

        TaiKhoan current = Session.getTaiKhoan();

        if (current != null
                && maTK.longValue() == current.getMaTK()) {
            hienThiCanhBao(
                    "Không thể khóa tài khoản Admin đang đăng nhập."
            );
            return;
        }

        int row = tblTaiKhoan.convertRowIndexToModel(
                tblTaiKhoan.getSelectedRow()
        );

        boolean dangHoatDong = model.getValueAt(row, 6)
                .toString()
                .equals("Đang hoạt động");

        boolean trangThaiMoi = !dangHoatDong;
        String tenDangNhap = layTenDangNhapDaChon();

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                (trangThaiMoi ? "Mở khóa" : "Khóa")
                + " tài khoản " + tenDangNhap + "?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        );

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        String sql = """
                UPDATE TaiKhoan
                SET TrangThai = ?
                WHERE MaTK = ?
                """;

        try (
                Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        ) {
            ps.setBoolean(1, trangThaiMoi);
            ps.setLong(2, maTK);
            ps.executeUpdate();

            taiDanhSachTaiKhoan();

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không cập nhật được trạng thái tài khoản:\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // XÓA TÀI KHOẢN VÀ NHÂN VIÊN LIÊN KẾT
    // CHỈ ADMIN ĐƯỢC PHÉP
    // =====================================================

    private void xoaTaiKhoanDaChon() {

        if (!kiemTraAdmin()) {
            return;
        }

        Long maTK = layMaTKDaChon();

        if (maTK == null) {
            return;
        }

        TaiKhoan current = Session.getTaiKhoan();

        if (current != null
                && maTK.longValue() == current.getMaTK()) {
            hienThiCanhBao(
                    "Không thể xóa tài khoản đang đăng nhập."
            );
            return;
        }

        String tenDangNhap = layTenDangNhapDaChon();

        int luaChon = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa tài khoản \""
                + tenDangNhap + "\"?\n\n"
                + "Nếu tài khoản gắn với nhân viên và nhân viên đó "
                + "chưa có hóa đơn, hồ sơ nhân viên cũng sẽ bị xóa.\n"
                + "Nếu nhân viên đã lập hóa đơn, thao tác sẽ bị chặn "
                + "để bảo toàn lịch sử.\n\n"
                + "Thao tác này không thể hoàn tác.",
                "Xác nhận xóa tài khoản",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (luaChon != JOptionPane.YES_OPTION) {
            return;
        }

        // Kiểm tra quyền một lần nữa trước khi ghi dữ liệu.
        if (!kiemTraAdmin()) {
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                Long maNV = null;
                String vaiTro;

                // Đọc và khóa tài khoản đang xóa.
                String sqlDoc = """
                        SELECT MaNV, VaiTro
                        FROM TaiKhoan
                        WHERE MaTK = ?
                        FOR UPDATE
                        """;

                try (PreparedStatement ps = conn.prepareStatement(sqlDoc)) {
                    ps.setLong(1, maTK);

                    try (ResultSet rs = ps.executeQuery()) {

                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy tài khoản cần xóa."
                            );
                        }

                        long value = rs.getLong("MaNV");

                        if (!rs.wasNull()) {
                            maNV = value;
                        }

                        vaiTro = rs.getString("VaiTro");
                    }
                }

                boolean laTaiKhoanNhanVien =
                        maNV != null
                        && !"ADMIN".equalsIgnoreCase(vaiTro);

                // Nếu nhân viên có hóa đơn, không xóa hồ sơ nhân viên.
                // Cũng không xóa tài khoản trong trường hợp này để
                // tránh một phần dữ liệu bị mất, một phần vẫn còn.
                if (laTaiKhoanNhanVien) {

                    String sqlDemHoaDon = """
                            SELECT COUNT(*)
                            FROM HoaDon
                            WHERE MaNV = ?
                            """;

                    long soHoaDon;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlDemHoaDon)) {

                        ps.setLong(1, maNV);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            soHoaDon = rs.getLong(1);
                        }
                    }

                    if (soHoaDon > 0) {
                        throw new SQLException(
                                "Nhân viên này đã lập "
                                + soHoaDon
                                + " hóa đơn nên không thể xóa hồ sơ.\n"
                                + "Để bảo toàn lịch sử, hãy dùng chức năng "
                                + "\"Ngừng làm\" ở màn hình Nhân viên."
                        );
                    }

                    // Không xóa hồ sơ nhân viên nếu còn tài khoản
                    // khác đang trỏ tới cùng MaNV.
                    String sqlDemTaiKhoan = """
                            SELECT COUNT(*)
                            FROM TaiKhoan
                            WHERE MaNV = ?
                              AND MaTK <> ?
                            """;

                    long taiKhoanKhac;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlDemTaiKhoan)) {

                        ps.setLong(1, maNV);
                        ps.setLong(2, maTK);

                        try (ResultSet rs = ps.executeQuery()) {
                            rs.next();
                            taiKhoanKhac = rs.getLong(1);
                        }
                    }

                    if (taiKhoanKhac > 0) {
                        throw new SQLException(
                                "Nhân viên này còn tài khoản đăng nhập khác. "
                                + "Hãy xử lý tài khoản đó trước."
                        );
                    }
                }

                // Xóa dữ liệu xác thực phụ thuộc tài khoản.
                xoaTheoMaTK(
                        conn,
                        "DELETE FROM RecoveryOtp WHERE MaTK = ?",
                        maTK
                );

                xoaTheoMaTK(
                        conn,
                        "DELETE FROM LichSuDoiMatKhau WHERE MaTK = ?",
                        maTK
                );

                // Xóa tài khoản trước để giải phóng liên kết MaNV.
                String sqlXoaTaiKhoan = """
                        DELETE FROM TaiKhoan
                        WHERE MaTK = ?
                        """;

                try (PreparedStatement ps =
                             conn.prepareStatement(sqlXoaTaiKhoan)) {

                    ps.setLong(1, maTK);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Không xóa được tài khoản."
                        );
                    }
                }

                // Tài khoản nhân viên không có hóa đơn thì xóa
                // luôn hồ sơ nhân viên được liên kết.
                if (laTaiKhoanNhanVien) {

                    String sqlXoaNhanVien = """
                            DELETE FROM NhanVien
                            WHERE MaNV = ?
                            """;

                    try (PreparedStatement ps =
                                 conn.prepareStatement(sqlXoaNhanVien)) {

                        ps.setLong(1, maNV);

                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "Đã xóa tài khoản nhưng không tìm thấy "
                                    + "hồ sơ nhân viên tương ứng."
                            );
                        }
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

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa tài khoản \"" + tenDangNhap + "\"."
                    + "\nNếu tài khoản gắn với nhân viên không có hóa đơn, "
                    + "hồ sơ nhân viên cũng đã được xóa.",
                    "Xóa thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            taiDanhSachTaiKhoan();

        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Không xóa được tài khoản:\n" + e.getMessage(),
                    "Không thể xóa",
                    JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // =====================================================
    // XÓA DỮ LIỆU PHỤ THUỘC TÀI KHOẢN
    // =====================================================

    private void xoaTheoMaTK(
            Connection conn,
            String sql,
            long maTK
    ) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, maTK);
            ps.executeUpdate();
        }
    }

    // =====================================================
    // HỘP THOẠI
    // =====================================================

    private void hienThiCanhBao(String noiDung) {
        JOptionPane.showMessageDialog(
                this,
                noiDung,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void hienThiLoi(String noiDung) {
        JOptionPane.showMessageDialog(
                this,
                noiDung,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
