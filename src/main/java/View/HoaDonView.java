
package View;
import Utils.Theme;

import DAO.HoaDonDAO;
import DAO.HoaDonDAO.ChiTietItem;
import DAO.HoaDonDAO.HoaDonItem;
import Model.NhanVien;
import Model.TaiKhoan;
import Utils.PDFHoaDon;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HoaDonView extends JPanel {

    private final Color BACKGROUND = Theme.BG;
    private final Color PRIMARY = Theme.ACCENT;
    private final Color TEXT = new Color(37, 43, 61);
    private final Color MUTED = new Color(135, 143, 160);

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private JTextField txtMaHD;
    private JComboBox<NhanVien> cboNhanVien;

    private JCheckBox chkLocKhoangNgay;
    private JSpinner spnTuNgay;
    private JSpinner spnDenNgay;

    private JTable tblHoaDon;
    private JTable tblChiTiet;

    private DefaultTableModel modelHoaDon;
    private DefaultTableModel modelChiTiet;

    private JLabel lblThongTinHD;
    private JLabel lblTrangThaiHD;
    private JLabel lblSoLuong;

    private JButton btnXuatPDF;
    private JButton btnXacNhanThanhToan;
    private JButton btnXoaHoaDon;

    private List<HoaDonItem> danhSachDangHienThi =
            new ArrayList<>();

    private boolean dangTaiNhanVien = false;

    private final NumberFormat currency =
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    private final SimpleDateFormat dateTimeFormat =
            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public HoaDonView() {
        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(22, 25, 22, 25));

        add(createHeader(), BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(
                new BorderLayout(0, 12)
        );
        mainPanel.setOpaque(false);

        mainPanel.add(createFilterPanel(), BorderLayout.NORTH);
        mainPanel.add(createTables(), BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);

        loadNhanVien();
        addEvents();
        timKiemHoaDon();
    }

    // =====================================================
    // TIÊU ĐỀ
    // =====================================================

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("Quản lý hóa đơn");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Tra cứu, xác nhận thanh toán, xem chi tiết và xuất PDF."
        );
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(6));
        titlePanel.add(subtitle);

        panel.add(titlePanel, BorderLayout.WEST);
        return panel;
    }

    // =====================================================
    // BỘ LỌC
    // =====================================================

    private JPanel createFilterPanel() {
        JPanel card = new JPanel(new BorderLayout(0, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(7, 8, 7, 8)
                )
        );

        // Dòng 1: mã hóa đơn, nhân viên và khoảng ngày.
        JPanel row1 = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 4)
        );
        row1.setOpaque(false);

        row1.add(createLabel("Mã hóa đơn:"));

        txtMaHD = new JTextField(8);
        txtMaHD.setPreferredSize(new Dimension(90, 32));
        txtMaHD.setToolTipText("Nhập 1 hoặc HD00001");
        row1.add(txtMaHD);

        row1.add(createLabel("Nhân viên:"));

        cboNhanVien = new JComboBox<>();
        cboNhanVien.setPreferredSize(new Dimension(170, 32));
        row1.add(cboNhanVien);

        chkLocKhoangNgay = new JCheckBox("Lọc khoảng ngày");
        chkLocKhoangNgay.setOpaque(false);
        row1.add(chkLocKhoangNgay);

        row1.add(createLabel("Từ ngày:"));

        spnTuNgay = taoDateSpinner();
        spnTuNgay.setPreferredSize(new Dimension(115, 32));
        spnTuNgay.setEnabled(false);
        row1.add(spnTuNgay);

        row1.add(createLabel("Đến ngày:"));

        spnDenNgay = taoDateSpinner();
        spnDenNgay.setPreferredSize(new Dimension(115, 32));
        spnDenNgay.setEnabled(false);
        row1.add(spnDenNgay);

        // Dòng 2: các nút thao tác.
        JPanel row2 = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 8, 4)
        );
        row2.setOpaque(false);

        JButton btnTim = createPrimaryButton("Lọc / Tìm kiếm");
        JButton btnLamMoi = createSecondaryButton("Làm mới");
        btnXacNhanThanhToan = createPrimaryButton("Xác nhận đã thanh toán");
        btnXacNhanThanhToan.setEnabled(false);
        btnXacNhanThanhToan.setToolTipText(
                "Chỉ xác nhận sau khi đã kiểm tra tiền vào tài khoản."
        );

        btnXuatPDF = createPrimaryButton("Xuất PDF");

        row2.add(btnTim);
        row2.add(btnLamMoi);
        row2.add(btnXacNhanThanhToan);
        row2.add(btnXuatPDF);

        // Chỉ hiển thị nút xóa cho Admin.
        if (Session.isAdmin()) {
            btnXoaHoaDon = new JButton("Xóa hóa đơn");
            btnXoaHoaDon.setForeground(Color.WHITE);
            btnXoaHoaDon.setBackground(new Color(210, 55, 65));
            btnXoaHoaDon.setFont(
                    new Font("Segoe UI", Font.BOLD, 12)
            );
            btnXoaHoaDon.setFocusPainted(false);
            btnXoaHoaDon.setBorderPainted(false);
            btnXoaHoaDon.setCursor(
                    Cursor.getPredefinedCursor(
                            Cursor.HAND_CURSOR
                    )
            );
            row2.add(btnXoaHoaDon);

            btnXoaHoaDon.addActionListener(
                    e -> xoaHoaDon()
            );
        }

        card.add(row1, BorderLayout.NORTH);
        card.add(row2, BorderLayout.CENTER);

        btnTim.addActionListener(e -> timKiemHoaDon());
        btnLamMoi.addActionListener(e -> lamMoiBoLoc());
        btnXacNhanThanhToan.addActionListener(e -> xacNhanThanhToan());
        btnXuatPDF.addActionListener(e -> xuatPDFHoaDon());

        chkLocKhoangNgay.addActionListener(e -> {
            boolean bat = chkLocKhoangNgay.isSelected();
            spnTuNgay.setEnabled(bat);
            spnDenNgay.setEnabled(bat);
        });

        txtMaHD.addActionListener(e -> timKiemHoaDon());

        return card;
    }

    private JSpinner taoDateSpinner() {
        JSpinner spinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        java.util.Calendar.DAY_OF_MONTH
                )
        );

        spinner.setEditor(
                new JSpinner.DateEditor(spinner, "dd/MM/yyyy")
        );

        return spinner;
    }

    // =====================================================
    // BẢNG HÓA ĐƠN VÀ CHI TIẾT
    // =====================================================

    private JSplitPane createTables() {
        modelHoaDon = new DefaultTableModel(
                new Object[]{
                        "Mã HĐ", "Ngày lập", "Nhân viên",
                        "Khách hàng", "Tổng tiền", "Giảm giá",
                        "Thành tiền", "Phương thức", "Trạng thái"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblHoaDon = new JTable(modelHoaDon);
        styleTable(tblHoaDon);
        tblHoaDon.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {
                90, 155, 145, 145, 115,
                100, 115, 120, 130
        };

        for (int i = 0; i < widths.length; i++) {
            tblHoaDon.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scrollHoaDon = new JScrollPane(tblHoaDon);
        scrollHoaDon.setBorder(
                BorderFactory.createTitledBorder(
                        "Danh sách hóa đơn"
                )
        );

        modelChiTiet = new DefaultTableModel(
                new Object[]{
                        "Tên sản phẩm", "Dung lượng", "Đơn giá điện thoại",
                        "Số lượng", "Phụ kiện", "Thành tiền"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblChiTiet = new JTable(modelChiTiet);
        styleTable(tblChiTiet);

        JScrollPane scrollChiTiet = new JScrollPane(tblChiTiet);
        scrollChiTiet.setBorder(
                BorderFactory.createTitledBorder("Chi tiết hóa đơn")
        );

        JPanel detailInfo = new JPanel(new BorderLayout(10, 0));
        detailInfo.setBackground(Color.WHITE);
        detailInfo.setBorder(new EmptyBorder(8, 12, 8, 12));

        lblThongTinHD = new JLabel(
                "Chọn một hóa đơn để xem chi tiết."
        );
        lblThongTinHD.setForeground(TEXT);
        lblThongTinHD.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        lblTrangThaiHD = new JLabel("");
        lblTrangThaiHD.setForeground(PRIMARY);
        lblTrangThaiHD.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        detailInfo.add(lblThongTinHD, BorderLayout.CENTER);
        detailInfo.add(lblTrangThaiHD, BorderLayout.EAST);

        JPanel detailPanel = new JPanel(new BorderLayout(0, 6));
        detailPanel.setOpaque(false);
        detailPanel.add(detailInfo, BorderLayout.NORTH);
        detailPanel.add(scrollChiTiet, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                scrollHoaDon,
                detailPanel
        );

        split.setResizeWeight(0.55);
        split.setDividerLocation(350);
        split.setDividerSize(8);

        return split;
    }

    // =====================================================
    // NẠP NHÂN VIÊN CHO BỘ LỌC
    // =====================================================

    private void loadNhanVien() {
        dangTaiNhanVien = true;

        try {
            cboNhanVien.removeAllItems();

            TaiKhoan account = Session.getTaiKhoan();
            if (account == null) {
                return;
            }

            List<NhanVien> list = hoaDonDAO.getNhanVienFilter();

            if (account.isAdmin()) {
                cboNhanVien.addItem(null);

                for (NhanVien nv : list) {
                    cboNhanVien.addItem(nv);
                }

                cboNhanVien.setRenderer(
                        new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(
                            JList<?> list,
                            Object value,
                            int index,
                            boolean isSelected,
                            boolean cellHasFocus
                    ) {
                        JLabel label = (JLabel)
                                super.getListCellRendererComponent(
                                        list, value, index,
                                        isSelected, cellHasFocus
                                );

                        label.setText(
                                value == null
                                ? "Tất cả nhân viên"
                                : ((NhanVien) value).getHoTen()
                        );

                        return label;
                    }
                });

                cboNhanVien.setSelectedIndex(0);
                cboNhanVien.setEnabled(true);

            } else {
                Long maNV = account.getMaNV();

                if (maNV == null) {
                    throw new SQLException(
                            "Tài khoản chưa được liên kết với nhân viên."
                    );
                }

                NhanVien found = null;

                for (NhanVien nv : list) {
                    if (nv.getMaNV() == maNV.longValue()) {
                        found = nv;
                        break;
                    }
                }

                if (found != null) {
                    cboNhanVien.addItem(found);
                } else {
                    cboNhanVien.addItem(
                            new NhanVien(
                                    maNV,
                                    account.getTenDangNhap()
                            )
                    );
                }

                cboNhanVien.setEnabled(false);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không thể tải danh sách nhân viên:\n"
                    + e.getMessage()
            );

        } finally {
            dangTaiNhanVien = false;
        }
    }

    private void addEvents() {
        tblHoaDon.getSelectionModel()
                .addListSelectionListener(e -> {
                    if (!e.getValueIsAdjusting()) {
                        hienThiChiTietHoaDon();
                    }
                });

        cboNhanVien.addActionListener(e -> {
            if (!dangTaiNhanVien) {
                timKiemHoaDon();
            }
        });
    }

    // =====================================================
    // TÌM KIẾM / LỌC HÓA ĐƠN
    // =====================================================

    private void timKiemHoaDon() {
        TaiKhoan account = Session.getTaiKhoan();

        if (account == null) {
            hienThiLoi("Phiên đăng nhập không hợp lệ.");
            return;
        }

        Long maHDFilter = null;
        String maHDText = txtMaHD.getText().trim();

        if (!maHDText.isEmpty()) {
            try {
                String numericText = maHDText
                        .toUpperCase(Locale.ROOT)
                        .replaceFirst("^HD", "");

                maHDFilter = Long.valueOf(numericText);

            } catch (NumberFormatException e) {
                hienThiCanhBao(
                        "Mã hóa đơn phải là số, ví dụ 1 hoặc HD00001."
                );
                return;
            }
        }

        Long maNVFilter = null;

        if (account.isAdmin()) {
            NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();

            if (nv != null) {
                maNVFilter = nv.getMaNV();
            }
        }

        LocalDate tuNgay = null;
        LocalDate denNgay = null;

        if (chkLocKhoangNgay.isSelected()) {
            tuNgay = chuyenLocalDate((Date) spnTuNgay.getValue());
            denNgay = chuyenLocalDate((Date) spnDenNgay.getValue());

            if (tuNgay.isAfter(denNgay)) {
                hienThiCanhBao(
                        "Ngày bắt đầu không được sau ngày kết thúc."
                );
                return;
            }
        }

        try {
            List<HoaDonItem> list = hoaDonDAO.timKiemHoaDon(
                    account.isAdmin(),
                    account.getMaNV(),
                    maNVFilter,
                    maHDFilter,
                    tuNgay,
                    denNgay
            );

            hienThiHoaDon(list);
            xoaChiTiet();

        } catch (SQLException | IllegalArgumentException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không thể tải hóa đơn:\n" + e.getMessage()
            );
        }
    }

    private LocalDate chuyenLocalDate(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void hienThiHoaDon(List<HoaDonItem> list) {
        danhSachDangHienThi = new ArrayList<>(list);
        btnXacNhanThanhToan.setEnabled(false);

        modelHoaDon.setRowCount(0);
        modelChiTiet.setRowCount(0);

        for (HoaDonItem hd : list) {
            String maHD = String.format(
                    Locale.ROOT, "HD%05d", hd.getMaHD()
            );

            String ngayLap = hd.getNgayLap() == null
                    ? ""
                    : dateTimeFormat.format(hd.getNgayLap());

            String tenKhachHang =
                    hd.getTenKhachHang() == null
                    || hd.getTenKhachHang().isBlank()
                    ? "Khách lẻ"
                    : hd.getTenKhachHang();

            modelHoaDon.addRow(new Object[]{
                    maHD,
                    ngayLap,
                    hd.getTenNhanVien(),
                    tenKhachHang,
                    dinhDangTien(hd.getTongTien()),
                    dinhDangTien(hd.getGiamGia()),
                    dinhDangTien(hd.getThanhTien()),
                    hd.getPhuongThucTT(),
                    hd.getTrangThai()
            });
        }
    }

    // =====================================================
    // XEM CHI TIẾT HÓA ĐƠN
    // =====================================================

    private void hienThiChiTietHoaDon() {
        int row = tblHoaDon.getSelectedRow();

        if (row < 0 || row >= danhSachDangHienThi.size()) {
            xoaChiTiet();
            return;
        }

        HoaDonItem selected = danhSachDangHienThi.get(row);

        try {
            HoaDonItem hoaDon =
                    layHoaDonDuocPhep(selected.getMaHD());

            if (hoaDon == null) {
                xoaChiTiet();
                hienThiCanhBao(
                        "Bạn không có quyền xem hóa đơn này."
                );
                return;
            }

            btnXacNhanThanhToan.setEnabled(choPhepXacNhanThanhToan(hoaDon));
            hienThiThongTinHoaDon(hoaDon);
            loadChiTiet(hoaDon.getMaHD());

        } catch (SQLException e) {
            btnXacNhanThanhToan.setEnabled(false);
            e.printStackTrace();
            hienThiLoi(
                    "Không thể xem chi tiết hóa đơn:\n"
                    + e.getMessage()
            );
        }
    }

    private HoaDonItem layHoaDonDuocPhep(long maHD)
            throws SQLException {

        TaiKhoan account = Session.getTaiKhoan();

        if (account == null) {
            return null;
        }

        Long maNVFilter = null;

        if (account.isAdmin()) {
            NhanVien nv = (NhanVien) cboNhanVien.getSelectedItem();

            if (nv != null) {
                maNVFilter = nv.getMaNV();
            }
        }

        List<HoaDonItem> list = hoaDonDAO.timKiemHoaDon(
                account.isAdmin(),
                account.getMaNV(),
                maNVFilter,
                maHD,
                (LocalDate) null,
                (LocalDate) null
        );

        return list.isEmpty() ? null : list.get(0);
    }

    private void hienThiThongTinHoaDon(HoaDonItem hoaDon) {
        String maHD = String.format(
                Locale.ROOT, "HD%05d", hoaDon.getMaHD()
        );

        String khachHang =
                hoaDon.getTenKhachHang() == null
                || hoaDon.getTenKhachHang().isBlank()
                ? "Khách lẻ"
                : hoaDon.getTenKhachHang();

        lblThongTinHD.setText(
                "Hóa đơn " + maHD
                + " | Nhân viên: " + hoaDon.getTenNhanVien()
                + " | Khách hàng: " + khachHang
        );

        lblTrangThaiHD.setText(
                "Trạng thái: " + hoaDon.getTrangThai()
        );
    }

    private void loadChiTiet(long maHD) throws SQLException {
        modelChiTiet.setRowCount(0);

        List<ChiTietItem> list =
                hoaDonDAO.getChiTietHoaDon(maHD);

        for (ChiTietItem item : list) {
            modelChiTiet.addRow(new Object[]{
                    item.getTenSanPham(),
                    item.getDungLuong(),
                    dinhDangTien(item.getDonGia()),
                    item.getSoLuong(),
                    item.getTenPhuKien(),
                    dinhDangTien(item.getThanhTien())
            });
        }
    }

    // =====================================================
    // XÓA HÓA ĐƠN - CHỈ ADMIN
    // =====================================================

    private void xoaHoaDon() {
        if (!Session.isAdmin()) {
            hienThiCanhBao(
                    "Chỉ Admin mới được phép xóa hóa đơn."
            );
            return;
        }

        int row = tblHoaDon.getSelectedRow();

        if (row < 0 || row >= danhSachDangHienThi.size()) {
            hienThiCanhBao(
                    "Vui lòng chọn hóa đơn cần xóa."
            );
            return;
        }

        HoaDonItem selected = danhSachDangHienThi.get(row);

        String maHD = String.format(
                Locale.ROOT, "HD%05d", selected.getMaHD()
        );

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn chắc chắn muốn xóa " + maHD + "?\n"
                + "Hóa đơn và chi tiết sẽ bị xóa vĩnh viễn.\n"
                + "Số máy trong hóa đơn sẽ được hoàn về tồn kho.",
                "Xác nhận xóa hóa đơn",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            hoaDonDAO.xoaHoaDon(selected.getMaHD());

            JOptionPane.showMessageDialog(
                    this,
                    "Đã xóa hóa đơn " + maHD + ".",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            timKiemHoaDon();

        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không thể xóa hóa đơn:\n" + e.getMessage()
            );
        }
    }

    private boolean choPhepXacNhanThanhToan(HoaDonItem hoaDon) {
        if (hoaDon == null || !"Chờ thanh toán".equals(hoaDon.getTrangThai())) {
            return false;
        }
        String method = hoaDon.getPhuongThucTT();
        return "Chuyển khoản".equalsIgnoreCase(method)
                || "VietQR".equalsIgnoreCase(method);
    }

    private void xacNhanThanhToan() {
        int row = tblHoaDon.getSelectedRow();
        if (row < 0 || row >= danhSachDangHienThi.size()) {
            hienThiCanhBao("Vui lòng chọn hóa đơn đang chờ thanh toán.");
            return;
        }

        HoaDonItem selected = danhSachDangHienThi.get(row);
        String maHD = String.format(Locale.ROOT, "HD%05d", selected.getMaHD());

        try {
            HoaDonItem hoaDon = layHoaDonDuocPhep(selected.getMaHD());
            if (hoaDon == null) {
                hienThiCanhBao("Bạn không có quyền xác nhận hóa đơn này.");
                return;
            }
            if (!choPhepXacNhanThanhToan(hoaDon)) {
                hienThiCanhBao("Hóa đơn này không còn ở trạng thái chờ thanh toán.");
                timKiemHoaDon();
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn đã kiểm tra và nhận đủ tiền cho " + maHD + " qua "
                    + hoaDon.getPhuongThucTT() + " chưa?\n"
                    + "Chỉ xác nhận khi giao dịch đã vào tài khoản.",
                    "Xác nhận đã nhận tiền",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );
            if (confirm != JOptionPane.YES_OPTION) return;

            if (!hoaDonDAO.xacNhanThanhToan(selected.getMaHD())) {
                hienThiCanhBao(
                        "Không thể cập nhật. Hóa đơn có thể đã được xác nhận ở nơi khác."
                );
                timKiemHoaDon();
                return;
            }

            timKiemHoaDon();
            for (int i = 0; i < danhSachDangHienThi.size(); i++) {
                if (danhSachDangHienThi.get(i).getMaHD() == selected.getMaHD()) {
                    tblHoaDon.setRowSelectionInterval(i, i);
                    break;
                }
            }

            JOptionPane.showMessageDialog(
                    this,
                    maHD + " đã được chuyển sang trạng thái Đã thanh toán.",
                    "Đã xác nhận thanh toán",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (SQLException e) {
            e.printStackTrace();
            hienThiLoi("Không thể xác nhận thanh toán:\n" + e.getMessage());
        }
    }

    // =====================================================
    // XUẤT PDF
    // =====================================================

    private void xuatPDFHoaDon() {
        int row = tblHoaDon.getSelectedRow();

        if (row < 0 || row >= danhSachDangHienThi.size()) {
            hienThiCanhBao(
                    "Vui lòng chọn hóa đơn cần xuất PDF."
            );
            return;
        }

        HoaDonItem selected = danhSachDangHienThi.get(row);

        try {
            HoaDonItem hoaDon =
                    layHoaDonDuocPhep(selected.getMaHD());

            if (hoaDon == null) {
                hienThiCanhBao(
                        "Bạn không có quyền xuất hóa đơn này."
                );
                return;
            }

            List<ChiTietItem> chiTiet =
                    hoaDonDAO.getChiTietHoaDon(hoaDon.getMaHD());

            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Lưu hóa đơn PDF");
            chooser.setSelectedFile(
                    new File(
                            String.format(
                                    Locale.ROOT,
                                    "HoaDon_HD%05d.pdf",
                                    hoaDon.getMaHD()
                            )
                    )
            );

            if (chooser.showSaveDialog(this)
                    != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File file = chooser.getSelectedFile();
            String path = file.getAbsolutePath();

            if (!path.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
                file = new File(path + ".pdf");
            }

            if (file.exists()) {
                int overwrite = JOptionPane.showConfirmDialog(
                        this,
                        "File đã tồn tại. Bạn muốn ghi đè không?",
                        "Xác nhận ghi đè",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (overwrite != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            PDFHoaDon.xuatHoaDon(file, hoaDon, chiTiet);

            JOptionPane.showMessageDialog(
                    this,
                    "Xuất PDF thành công:\n" + file.getAbsolutePath(),
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            int open = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có muốn mở file PDF vừa xuất không?",
                    "Mở file",
                    JOptionPane.YES_NO_OPTION
            );

            if (open == JOptionPane.YES_OPTION
                    && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            hienThiLoi(
                    "Không thể xuất hóa đơn PDF:\n" + e.getMessage()
            );
        }
    }

    // =====================================================
    // LÀM MỚI
    // =====================================================

    private void lamMoiBoLoc() {
        txtMaHD.setText("");
        chkLocKhoangNgay.setSelected(false);

        spnTuNgay.setValue(new Date());
        spnDenNgay.setValue(new Date());

        spnTuNgay.setEnabled(false);
        spnDenNgay.setEnabled(false);

        if (Session.isAdmin() && cboNhanVien.getItemCount() > 0) {
            cboNhanVien.setSelectedIndex(0);
        }

        timKiemHoaDon();
    }

    private void xoaChiTiet() {
        modelChiTiet.setRowCount(0);
        btnXacNhanThanhToan.setEnabled(false);
        lblThongTinHD.setText(
                "Chọn một hóa đơn để xem chi tiết."
        );
        lblTrangThaiHD.setText("");
    }

    // =====================================================
    // GIAO DIỆN BẢNG VÀ NÚT
    // =====================================================

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setSelectionBackground(Theme.SOFT);
        table.setSelectionForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(238, 240, 245));
        table.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        table.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );
        table.getTableHeader().setBackground(
                new Color(249, 250, 253)
        );
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.WHITE);
        button.setBackground(PRIMARY);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(TEXT);
        button.setBackground(new Color(242, 243, 248));
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        return button;
    }

    private String dinhDangTien(java.math.BigDecimal value) {
        return value == null ? "0" : currency.format(value);
    }

    private void hienThiCanhBao(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông báo",
                JOptionPane.WARNING_MESSAGE
        );
    }

    private void hienThiLoi(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Lỗi",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
