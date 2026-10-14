package View;
import Utils.Theme;

import DAO.NhanVienDAO;
import DAO.NhanVienDAO.NhanVienItem;
import Utils.TextSearch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class NhanVienView extends JPanel {

    private final Color BACKGROUND =
            Theme.BG;

    private final Color PRIMARY =
            Theme.ACCENT;

    private final Color TEXT =
            new Color(37, 43, 61);

    private final Color MUTED =
            new Color(135, 143, 160);

    private final NhanVienDAO nhanVienDAO =
            new NhanVienDAO();

    private JTextField txtHoTen;
    private JComboBox<String> cboGioiTinh;
    private JSpinner spnNgaySinh;

    private JTextField txtSDT;
    private JTextField txtEmail;
    private JTextField txtDiaChi;

    private JSpinner spnNgayVaoLam;

    private JTextField txtTenDangNhap;

    private JCheckBox chkTrangThai;

    private JTextField txtTimKiem;

    private JComboBox<String> cboLocTrangThai;

    private JTable tblNhanVien;

    private DefaultTableModel model;

    private JLabel lblSoLuong;

    private Long maNVDangChon = null;

    private List<NhanVienItem> tatCaNhanVien =
            new ArrayList<>();

    private List<NhanVienItem> danhSachDangHienThi =
            new ArrayList<>();

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy");

    public NhanVienView() {

        setLayout(new BorderLayout(0, 16));

        setBackground(BACKGROUND);

        setBorder(
                new EmptyBorder(22, 24, 22, 24)
        );

        add(createHeader(), BorderLayout.NORTH);

        JPanel main = new JPanel(
                new BorderLayout(14, 0)
        );

        main.setOpaque(false);

        main.add(
                createFormPanel(),
                BorderLayout.WEST
        );

        main.add(
                createTablePanel(),
                BorderLayout.CENTER
        );

        add(main, BorderLayout.CENTER);

        loadData();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel createHeader() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        panel.setOpaque(false);

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(
                        titlePanel,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = new JLabel(
                "Quản lý nhân viên"
        );

        title.setForeground(TEXT);

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        26
                )
        );

        JLabel subtitle = new JLabel(
                "Thông tin nhân sự và tài khoản đăng nhập."
        );

        subtitle.setForeground(MUTED);

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        titlePanel.add(title);

        titlePanel.add(
                Box.createVerticalStrut(6)
        );

        titlePanel.add(subtitle);

        panel.add(titlePanel, BorderLayout.WEST);

        return panel;
    }

    // =====================================================
    // FORM NHÂN VIÊN
    // =====================================================

    private JPanel createFormPanel() {

        JPanel panel = createCard();

        panel.setPreferredSize(
                new Dimension(355, 0)
        );

        panel.setLayout(
                new BorderLayout(0, 8)
        );

        panel.add(
                createTitle("Thông tin nhân viên"),
                BorderLayout.NORTH
        );

        JPanel form = new JPanel(
                new GridBagLayout()
        );

        form.setOpaque(false);

        GridBagConstraints gbc =
                new GridBagConstraints();

        gbc.gridx = 0;

        gbc.weightx = 1;

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.anchor = GridBagConstraints.WEST;

        gbc.insets = new Insets(3, 0, 3, 0);

        // Họ tên
        gbc.gridy = 0;

        form.add(createLabel("Họ và tên"), gbc);

        gbc.gridy = 1;

        txtHoTen = new JTextField();

        styleInput(txtHoTen);

        form.add(txtHoTen, gbc);

        // Giới tính
        gbc.gridy = 2;

        form.add(createLabel("Giới tính"), gbc);

        gbc.gridy = 3;

        cboGioiTinh = new JComboBox<>(
                new String[]{
                        "Nam",
                        "Nữ",
                        "Khác"
                }
        );

        form.add(cboGioiTinh, gbc);

        // Ngày sinh
        gbc.gridy = 4;

        form.add(createLabel("Ngày sinh"), gbc);

        gbc.gridy = 5;

        spnNgaySinh = createDateSpinner();

        form.add(spnNgaySinh, gbc);

        // Số điện thoại
        gbc.gridy = 6;

        form.add(createLabel("Số điện thoại"), gbc);

        gbc.gridy = 7;

        txtSDT = new JTextField();

        styleInput(txtSDT);

        form.add(txtSDT, gbc);

        // Email
        gbc.gridy = 8;

        form.add(createLabel("Email"), gbc);

        gbc.gridy = 9;

        txtEmail = new JTextField();

        styleInput(txtEmail);

        form.add(txtEmail, gbc);

        // Địa chỉ
        gbc.gridy = 10;

        form.add(createLabel("Địa chỉ"), gbc);

        gbc.gridy = 11;

        txtDiaChi = new JTextField();

        styleInput(txtDiaChi);

        form.add(txtDiaChi, gbc);

        // Ngày vào làm
        gbc.gridy = 12;

        form.add(createLabel("Ngày vào làm"), gbc);

        gbc.gridy = 13;

        spnNgayVaoLam = createDateSpinner();

        form.add(spnNgayVaoLam, gbc);

        // Tên đăng nhập
        gbc.gridy = 14;

        form.add(createLabel("Tên đăng nhập"), gbc);

        gbc.gridy = 15;

        txtTenDangNhap = new JTextField();

        styleInput(txtTenDangNhap);

        form.add(txtTenDangNhap, gbc);

        // Trạng thái
        gbc.gridy = 16;

        chkTrangThai = new JCheckBox(
                "Đang làm việc"
        );

        chkTrangThai.setSelected(true);

        chkTrangThai.setOpaque(false);

        chkTrangThai.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        form.add(chkTrangThai, gbc);

        JScrollPane scrollForm = new JScrollPane(form);

        scrollForm.setBorder(
                BorderFactory.createEmptyBorder()
        );

        scrollForm.getViewport().setBackground(Color.WHITE);

        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(
                scrollForm,
                BorderLayout.CENTER
        );

        JPanel buttons = new JPanel(
                new GridLayout(2, 2, 7, 7)
        );

        buttons.setOpaque(false);

        JButton btnThem = createPrimaryButton("Thêm mới");

        JButton btnSua = createSecondaryButton("Cập nhật");

        JButton btnNgung = createSecondaryButton("Ngừng làm");

        JButton btnLamMoi = createSecondaryButton("Làm mới");

        btnThem.addActionListener(e -> themNhanVien());

        btnSua.addActionListener(e -> capNhatNhanVien());

        btnNgung.addActionListener(e -> ngungLamViec());

        btnLamMoi.addActionListener(e -> lamMoiForm());

        buttons.add(btnThem);
        buttons.add(btnSua);
        buttons.add(btnNgung);
        buttons.add(btnLamMoi);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    // =====================================================
    // BẢNG NHÂN VIÊN
    // =====================================================

    private JPanel createTablePanel() {

        JPanel panel = createCard();

        panel.setLayout(
                new BorderLayout(0, 12)
        );

        // Bộ lọc
        JPanel filter = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        4
                )
        );

        filter.setOpaque(false);

        filter.add(createLabel("Tìm kiếm:"));

        txtTimKiem = new JTextField(14);
        txtTimKiem.setToolTipText("Tìm theo mã, họ tên, SĐT, email, địa chỉ hoặc tài khoản; không phân biệt dấu");

        txtTimKiem.setPreferredSize(
                new Dimension(155, 34)
        );

        filter.add(txtTimKiem);

        filter.add(createLabel("Trạng thái:"));

        cboLocTrangThai = new JComboBox<>(
                new String[]{
                        "Tất cả",
                        "Đang làm việc",
                        "Đã nghỉ"
                }
        );

        cboLocTrangThai.setPreferredSize(
                new Dimension(140, 34)
        );

        filter.add(cboLocTrangThai);

        JButton btnTim = createPrimaryButton("Tìm");

        JButton btnTaiLai = createSecondaryButton("Tải lại");

        filter.add(btnTim);
        filter.add(btnTaiLai);

        // Table
        model = new DefaultTableModel(
                new Object[]{
                        "Mã NV",
                        "Họ tên",
                        "Giới tính",
                        "Ngày sinh",
                        "Số điện thoại",
                        "Email",
                        "Tên đăng nhập",
                        "Ngày vào làm",
                        "Trạng thái"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(
                    int row,
                    int column
            ) {
                return false;
            }
        };

        tblNhanVien = new JTable(model);

        tblNhanVien.setRowHeight(33);

        tblNhanVien.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblNhanVien.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblNhanVien.setSelectionBackground(
                Theme.SOFT
        );

        tblNhanVien.setSelectionForeground(TEXT);

        tblNhanVien.setShowVerticalLines(false);

        tblNhanVien.setGridColor(
                new Color(238, 240, 245)
        );

        tblNhanVien.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        tblNhanVien.getTableHeader().setBackground(
                new Color(249, 250, 253)
        );

        tblNhanVien.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        tblNhanVien.setAutoResizeMode(
                JTable.AUTO_RESIZE_OFF
        );

        int[] widths = {
                65, 150, 75, 95, 115,
                165, 120, 95, 100
        };

        for (int i = 0; i < widths.length; i++) {

            tblNhanVien.getColumnModel()
                    .getColumn(i)
                    .setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(tblNhanVien);

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(232, 235, 242)
                )
        );

        lblSoLuong = new JLabel("0 nhân viên");

        lblSoLuong.setForeground(MUTED);

        lblSoLuong.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        panel.add(filter, BorderLayout.NORTH);

        panel.add(scroll, BorderLayout.CENTER);

        panel.add(lblSoLuong, BorderLayout.SOUTH);

        btnTim.addActionListener(e -> locNhanVien());

        txtTimKiem.addActionListener(e -> locNhanVien());

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { locNhanVien(); }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });

        cboLocTrangThai.addActionListener(e -> locNhanVien());

        btnTaiLai.addActionListener(
        e -> taiLaiVaDonDepNhanVienMoCoi()
);

        tblNhanVien.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        chonNhanVien();
                    }
                });

        return panel;
    }

    // =====================================================
    // LOAD DATA
    // =====================================================

    private void loadData() {

        try {

            tatCaNhanVien = nhanVienDAO.getAll();

            locNhanVien();

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể tải danh sách nhân viên:\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // LỌC NHÂN VIÊN
    // =====================================================

    private void locNhanVien() {

        if (model == null) {
            return;
        }

        String keyword = txtTimKiem.getText();

        String trangThai =
                (String) cboLocTrangThai.getSelectedItem();

        danhSachDangHienThi.clear();

        model.setRowCount(0);

        for (NhanVienItem nv : tatCaNhanVien) {

            boolean matchKeyword =
                    TextSearch.contains(Long.toString(nv.getMaNV()), keyword)
                    || TextSearch.contains(nv.getHoTen(), keyword)
                    || TextSearch.contains(nv.getSDT(), keyword)
                    || TextSearch.contains(nv.getEmail(), keyword)
                    || TextSearch.contains(nv.getDiaChi(), keyword)
                    || TextSearch.contains(nv.getTenDangNhap(), keyword);

            if (!matchKeyword) {
                continue;
            }

            if ("Đang làm việc".equals(trangThai)
                    && !nv.isTrangThai()) {
                continue;
            }

            if ("Đã nghỉ".equals(trangThai)
                    && nv.isTrangThai()) {
                continue;
            }

            danhSachDangHienThi.add(nv);

            model.addRow(
                    new Object[]{
                            nv.getMaNV(),
                            nv.getHoTen(),
                            nv.getGioiTinh(),
                            dinhDangNgay(nv.getNgaySinh()),
                            nv.getSDT(),
                            nv.getEmail(),
                            nv.getTenDangNhap(),
                            dinhDangNgay(nv.getNgayVaoLam()),
                            nv.isTrangThai()
                                    ? "Đang làm việc"
                                    : "Đã nghỉ"
                    }
            );
        }

        lblSoLuong.setText(
                danhSachDangHienThi.size()
                + " nhân viên"
        );
    }

    // =====================================================
    // CHỌN NHÂN VIÊN
    // =====================================================

    private void chonNhanVien() {

        int row = tblNhanVien.getSelectedRow();

        if (row < 0
                || row >= danhSachDangHienThi.size()) {
            return;
        }

        NhanVienItem nv = danhSachDangHienThi.get(row);

        maNVDangChon = nv.getMaNV();

        txtHoTen.setText(nv.getHoTen());

        cboGioiTinh.setSelectedItem(nv.getGioiTinh());

        setDateSpinner(spnNgaySinh, nv.getNgaySinh());

        txtSDT.setText(
                nv.getSDT() == null ? "" : nv.getSDT()
        );

        txtEmail.setText(
                nv.getEmail() == null ? "" : nv.getEmail()
        );

        txtDiaChi.setText(
                nv.getDiaChi() == null ? "" : nv.getDiaChi()
        );

        setDateSpinner(spnNgayVaoLam, nv.getNgayVaoLam());

        txtTenDangNhap.setText(
                nv.getTenDangNhap() == null
                        ? ""
                        : nv.getTenDangNhap()
        );

        chkTrangThai.setSelected(nv.isTrangThai());
    }

    // =====================================================
    // THÊM NHÂN VIÊN
    // =====================================================

    private void themNhanVien() {

        try {

            ThongTinNhanVien info = layThongTinForm();

            nhanVienDAO.themNhanVien(
                    info.hoTen,
                    info.gioiTinh,
                    info.ngaySinh,
                    info.sdt,
                    info.email,
                    info.diaChi,
                    info.ngayVaoLam,
                    info.tenDangNhap
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm nhân viên và tài khoản thành công!\n"
                    + "Tên đăng nhập: " + info.tenDangNhap + "\n"
                    + "Mật khẩu ban đầu: 123456",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadData();

            lamMoiForm();

        } catch (IllegalArgumentException e) {

            hienThiCanhBao(e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể thêm nhân viên.\n"
                    + "Kiểm tra số điện thoại có bị trùng không.\n\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // CẬP NHẬT NHÂN VIÊN
    // =====================================================

    private void capNhatNhanVien() {

        if (maNVDangChon == null) {

            hienThiCanhBao(
                    "Vui lòng chọn nhân viên cần cập nhật."
            );

            return;
        }

        try {

            ThongTinNhanVien info = layThongTinForm();

            nhanVienDAO.capNhatNhanVien(
                    maNVDangChon,
                    info.hoTen,
                    info.gioiTinh,
                    info.ngaySinh,
                    info.sdt,
                    info.email,
                    info.diaChi,
                    info.ngayVaoLam,
                    info.tenDangNhap,
                    chkTrangThai.isSelected()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật nhân viên thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadData();

            lamMoiForm();

        } catch (IllegalArgumentException e) {

            hienThiCanhBao(e.getMessage());

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể cập nhật nhân viên.\n"
                    + "Có thể số điện thoại đã được dùng.\n\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // NGỪNG LÀM VIỆC
    // =====================================================

    private void ngungLamViec() {

        if (maNVDangChon == null) {

            hienThiCanhBao(
                    "Vui lòng chọn nhân viên cần ngừng làm."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Ngừng làm việc sẽ khóa tài khoản nhân viên.\n"
                + "Lịch sử hóa đơn vẫn được giữ lại.\n\n"
                + "Bạn có chắc chắn muốn tiếp tục?",
                "Xác nhận ngừng làm việc",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            nhanVienDAO.ngungLamViec(maNVDangChon);

            JOptionPane.showMessageDialog(
                    this,
                    "Đã ngừng làm việc và khóa tài khoản.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadData();

            lamMoiForm();

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể ngừng làm việc:\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // ĐỌC DỮ LIỆU FORM
    // =====================================================

    private ThongTinNhanVien layThongTinForm() {

        String hoTen = txtHoTen.getText().trim();

        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập họ tên nhân viên."
            );
        }

        String gioiTinh =
                (String) cboGioiTinh.getSelectedItem();

        String sdt = txtSDT.getText().trim();

        if (!sdt.isEmpty()
                && !sdt.matches("[0-9+(). -]{8,20}")) {

            throw new IllegalArgumentException(
                    "Số điện thoại không hợp lệ."
            );
        }

        String email = txtEmail.getText().trim();

        if (!email.isEmpty()
                && !email.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )) {

            throw new IllegalArgumentException(
                    "Địa chỉ email không hợp lệ."
            );
        }

        String tenDangNhap =
                txtTenDangNhap.getText().trim();

        if (tenDangNhap.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên đăng nhập."
            );
        }

        Date ngaySinh = getDateSpinner(spnNgaySinh);

        Date ngayVaoLam = getDateSpinner(spnNgayVaoLam);

        return new ThongTinNhanVien(
                hoTen,
                gioiTinh,
                ngaySinh,
                sdt.isEmpty() ? null : sdt,
                email.isEmpty() ? null : email,
                txtDiaChi.getText().trim(),
                ngayVaoLam,
                tenDangNhap
        );
    }

    private static class ThongTinNhanVien {

        private final String hoTen;
        private final String gioiTinh;
        private final Date ngaySinh;
        private final String sdt;
        private final String email;
        private final String diaChi;
        private final Date ngayVaoLam;
        private final String tenDangNhap;

        private ThongTinNhanVien(
                String hoTen,
                String gioiTinh,
                Date ngaySinh,
                String sdt,
                String email,
                String diaChi,
                Date ngayVaoLam,
                String tenDangNhap
        ) {

            this.hoTen = hoTen;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.sdt = sdt;
            this.email = email;
            this.diaChi = diaChi;
            this.ngayVaoLam = ngayVaoLam;
            this.tenDangNhap = tenDangNhap;
        }
    }

    // =====================================================
    // NGÀY THÁNG
    // =====================================================

    private JSpinner createDateSpinner() {

        JSpinner spinner = new JSpinner(
                new SpinnerDateModel(
                        new java.util.Date(),
                        null,
                        null,
                        Calendar.DAY_OF_MONTH
                )
        );

        spinner.setEditor(
                new JSpinner.DateEditor(
                        spinner,
                        "dd/MM/yyyy"
                )
        );

        spinner.setPreferredSize(
                new Dimension(250, 32)
        );

        return spinner;
    }

    private Date getDateSpinner(JSpinner spinner) {

        java.util.Date date =
                (java.util.Date) spinner.getValue();

        return new Date(date.getTime());
    }

    private void setDateSpinner(
            JSpinner spinner,
            Date date
    ) {

        if (date != null) {
            spinner.setValue(new java.util.Date(date.getTime()));
        }
    }

    private String dinhDangNgay(Date date) {

        if (date == null) {
            return "";
        }

        return dateFormat.format(date);
    }

    // =====================================================
    // LÀM MỚI FORM
    // =====================================================

    private void lamMoiForm() {

        maNVDangChon = null;

        tblNhanVien.clearSelection();

        txtHoTen.setText("");

        cboGioiTinh.setSelectedIndex(0);

        spnNgaySinh.setValue(new java.util.Date());

        txtSDT.setText("");

        txtEmail.setText("");

        txtDiaChi.setText("");

        spnNgayVaoLam.setValue(new java.util.Date());

        txtTenDangNhap.setText("");

        chkTrangThai.setSelected(true);

        txtHoTen.requestFocusInWindow();
    }

    // =====================================================
    // COMPONENT DÙNG CHUNG
    // =====================================================

    private JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(14, 14, 14, 14)
                )
        );

        return panel;
    }

    private JLabel createTitle(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(TEXT);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 16)
        );

        return label;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(MUTED);

        label.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        return label;
    }

    private void styleInput(JTextField field) {

        field.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        field.setPreferredSize(
                new Dimension(250, 32)
        );
    }

    private JButton createPrimaryButton(String text) {

        JButton button = new JButton(text);

        button.setForeground(Color.WHITE);

        button.setBackground(PRIMARY);

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        button.setFocusPainted(false);

        button.setBorderPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    private JButton createSecondaryButton(String text) {

        JButton button = new JButton(text);

        button.setForeground(TEXT);

        button.setBackground(
                new Color(242, 243, 248)
        );

        button.setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    private void hienThiCanhBao(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "Thông tin chưa hợp lệ",
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
    // =====================================================
// TẢI LẠI VÀ DỌN DỮ LIỆU NHÂN VIÊN MỒ CÔI
// =====================================================

private void taiLaiVaDonDepNhanVienMoCoi() {

    int confirm = JOptionPane.showConfirmDialog(
            this,
            "Tải lại danh sách và dọn dữ liệu nhân viên mồ côi?\n\n"
            + "Hồ sơ không còn tài khoản và chưa có hóa đơn "
            + "sẽ bị xóa khỏi cơ sở dữ liệu.\n"
            + "Nhân viên có hóa đơn sẽ được giữ nguyên "
            + "để bảo toàn lịch sử.\n\n"
            + "Bạn có muốn tiếp tục không?",
            "Xác nhận làm mới dữ liệu",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
    );

    if (confirm != JOptionPane.YES_OPTION) {
        return;
    }

    try {
        int soLuongXoa =
                nhanVienDAO.xoaNhanVienMoCoiKhongHoaDon();

        // Xóa từ khóa lọc và hiển thị lại toàn bộ danh sách.
        txtTimKiem.setText("");
        cboLocTrangThai.setSelectedItem("Tất cả");

        loadData();
        lamMoiForm();

        String thongBao;

        if (soLuongXoa > 0) {
            thongBao = "Đã dọn " + soLuongXoa
                    + " hồ sơ nhân viên không còn tài khoản "
                    + "và chưa có hóa đơn.";
        } else {
            thongBao = "Đã tải lại danh sách.\n"
                    + "Không có hồ sơ mồ côi nào đủ điều kiện xóa.\n"
                    + "Nhân viên có hóa đơn vẫn được giữ lại.";
        }

        JOptionPane.showMessageDialog(
                this,
                thongBao,
                "Làm mới thành công",
                JOptionPane.INFORMATION_MESSAGE
        );

    } catch (SQLException e) {
        e.printStackTrace();

        hienThiLoi(
                "Không thể làm mới dữ liệu nhân viên:\n"
                + e.getMessage()
        );
    }
}
}
