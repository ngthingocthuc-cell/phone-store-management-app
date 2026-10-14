package View;
import Utils.Theme;

import DAO.PhuKienDAO;
import Model.PhuKien;
import Utils.TextSearch;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PhuKienView extends JPanel {

    private final Color BACKGROUND =
            Theme.BG;

    private final Color PRIMARY =
            Theme.ACCENT;

    private final Color TEXT =
            new Color(37, 43, 61);

    private final Color MUTED =
            new Color(135, 143, 160);

    private final PhuKienDAO phuKienDAO =
            new PhuKienDAO();

    private JTextField txtTenPhuKien;
    private JTextField txtDonGia;
    private JTextField txtTimKiem;

    private JCheckBox chkTrangThai;

    private JComboBox<String> cboLocTrangThai;

    private JTable tblPhuKien;

    private DefaultTableModel model;

    private JLabel lblSoLuong;

    private Long maPhuKienDangChon;

    private List<PhuKien> tatCaPhuKien =
            new ArrayList<>();

    private List<PhuKien> danhSachDangHienThi =
            new ArrayList<>();

    private final NumberFormat currency =
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    public PhuKienView() {

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
                "Quản lý phuKien"
        );

        title.setForeground(TEXT);

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 26)
        );

        JLabel subtitle = new JLabel(
                "Quản lý tên phuKien, giá bán và trạng thái kinh doanh."
        );

        subtitle.setForeground(MUTED);

        subtitle.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
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
    // FORM
    // =====================================================

    private JPanel createFormPanel() {

        JPanel panel = createCard();

        panel.setPreferredSize(
                new Dimension(335, 0)
        );

        panel.setLayout(
                new BorderLayout(0, 15)
        );

        panel.add(
                createTitle("Thông tin phuKien"),
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

        gbc.insets = new Insets(6, 0, 5, 0);

        // Tên phuKien
        gbc.gridy = 0;

        form.add(
                createLabel("Tên phuKien"),
                gbc
        );

        gbc.gridy = 1;

        txtTenPhuKien = new JTextField();

        styleInput(txtTenPhuKien);

        form.add(txtTenPhuKien, gbc);

        // Giá bán
        gbc.gridy = 2;

        form.add(
                createLabel("Đơn giá (VNĐ)"),
                gbc
        );

        gbc.gridy = 3;

        txtDonGia = new JTextField("5000");

        styleInput(txtDonGia);

        form.add(txtDonGia, gbc);

        // Trạng thái
        gbc.gridy = 4;

        chkTrangThai = new JCheckBox(
                "Đang kinh doanh"
        );

        chkTrangThai.setSelected(true);

        chkTrangThai.setOpaque(false);

        chkTrangThai.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        form.add(chkTrangThai, gbc);

        panel.add(form, BorderLayout.CENTER);

        // Nút thao tác
        JPanel buttons = new JPanel(
                new GridLayout(2, 2, 8, 8)
        );

        buttons.setOpaque(false);

        JButton btnThem = createPrimaryButton(
                "Thêm mới"
        );

        JButton btnCapNhat = createSecondaryButton(
                "Cập nhật"
        );

        JButton btnNgung = createSecondaryButton(
                "Ngừng bán"
        );

        JButton btnLamMoi = createSecondaryButton(
                "Làm mới"
        );

        btnThem.addActionListener(
                e -> themPhuKien()
        );

        btnCapNhat.addActionListener(
                e -> capNhatPhuKien()
        );

        btnNgung.addActionListener(
                e -> ngungBan()
        );

        btnLamMoi.addActionListener(
                e -> lamMoiForm()
        );

        buttons.add(btnThem);
        buttons.add(btnCapNhat);
        buttons.add(btnNgung);
        buttons.add(btnLamMoi);

        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    // =====================================================
    // BẢNG TOPPING
    // =====================================================

    private JPanel createTablePanel() {

        JPanel panel = createCard();

        panel.setLayout(
                new BorderLayout(0, 12)
        );

        // Thanh tìm kiếm
        JPanel filter = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        8,
                        4
                )
        );

        filter.setOpaque(false);

        filter.add(
                createLabel("Tìm phuKien:")
        );

        txtTimKiem = new JTextField(14);
        txtTimKiem.setToolTipText("Tìm theo mã hoặc tên phuKien; không phân biệt dấu tiếng Việt");

        txtTimKiem.setPreferredSize(
                new Dimension(160, 34)
        );

        filter.add(txtTimKiem);

        filter.add(
                createLabel("Trạng thái:")
        );

        cboLocTrangThai = new JComboBox<>(
                new String[]{
                        "Tất cả",
                        "Đang kinh doanh",
                        "Ngừng kinh doanh"
                }
        );

        cboLocTrangThai.setPreferredSize(
                new Dimension(155, 34)
        );

        filter.add(cboLocTrangThai);

        JButton btnTim = createPrimaryButton("Tìm");

        JButton btnTaiLai = createSecondaryButton(
                "Tải lại"
        );

        filter.add(btnTim);

        filter.add(btnTaiLai);

        // Bảng
        model = new DefaultTableModel(
                new Object[]{
                        "Mã",
                        "Tên phuKien",
                        "Đơn giá",
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

        tblPhuKien = new JTable(model);

        tblPhuKien.setRowHeight(35);

        tblPhuKien.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        tblPhuKien.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblPhuKien.setSelectionBackground(
                Theme.SOFT
        );

        tblPhuKien.setSelectionForeground(TEXT);

        tblPhuKien.setShowVerticalLines(false);

        tblPhuKien.setGridColor(
                new Color(238, 240, 245)
        );

        tblPhuKien.getTableHeader().setFont(
                new Font("Segoe UI", Font.BOLD, 12)
        );

        tblPhuKien.getTableHeader().setBackground(
                new Color(249, 250, 253)
        );

        tblPhuKien.getTableHeader().setPreferredSize(
                new Dimension(0, 36)
        );

        JScrollPane scroll = new JScrollPane(
                tblPhuKien
        );

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(232, 235, 242)
                )
        );

        lblSoLuong = new JLabel("0 phuKien");

        lblSoLuong.setForeground(MUTED);

        lblSoLuong.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        panel.add(filter, BorderLayout.NORTH);

        panel.add(scroll, BorderLayout.CENTER);

        panel.add(lblSoLuong, BorderLayout.SOUTH);

        // Sự kiện
        btnTim.addActionListener(
                e -> locPhuKien()
        );

        txtTimKiem.addActionListener(
                e -> locPhuKien()
        );

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            private void apply() { locPhuKien(); }
            @Override public void insertUpdate(DocumentEvent e) { apply(); }
            @Override public void removeUpdate(DocumentEvent e) { apply(); }
            @Override public void changedUpdate(DocumentEvent e) { apply(); }
        });

        cboLocTrangThai.addActionListener(
                e -> locPhuKien()
        );

        btnTaiLai.addActionListener(e -> {
            txtTimKiem.setText("");
            cboLocTrangThai.setSelectedIndex(0);
            loadData();
        });

        tblPhuKien.getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {
                        chonPhuKien();
                    }
                });

        return panel;
    }

    // =====================================================
    // LOAD DATA
    // =====================================================

    private void loadData() {

        try {

            tatCaPhuKien = phuKienDAO.getAll();

            locPhuKien();

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể tải danh sách phuKien:\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // LỌC TOPPING
    // =====================================================

    private void locPhuKien() {

        if (model == null
                || txtTimKiem == null
                || cboLocTrangThai == null) {
            return;
        }

        String keyword = txtTimKiem.getText();

        String trangThai =
                (String) cboLocTrangThai.getSelectedItem();

        danhSachDangHienThi.clear();

        model.setRowCount(0);

        for (PhuKien phuKien : tatCaPhuKien) {

            boolean matchKeyword =
                    TextSearch.contains(Long.toString(phuKien.getMaPhuKien()), keyword)
                    || TextSearch.contains(phuKien.getTenPhuKien(), keyword);

            if (!matchKeyword) {
                continue;
            }

            if ("Đang kinh doanh".equals(trangThai)
                    && !phuKien.isTrangThai()) {
                continue;
            }

            if ("Ngừng kinh doanh".equals(trangThai)
                    && phuKien.isTrangThai()) {
                continue;
            }

            danhSachDangHienThi.add(phuKien);

            model.addRow(
                    new Object[]{
                            phuKien.getMaPhuKien(),
                            phuKien.getTenPhuKien(),
                            dinhDangTien(phuKien.getDonGia()),
                            phuKien.isTrangThai()
                                    ? "Đang kinh doanh"
                                    : "Ngừng kinh doanh"
                    }
            );
        }

        lblSoLuong.setText(
                danhSachDangHienThi.size()
                + " phuKien"
        );
    }

    // =====================================================
    // CHỌN TOPPING
    // =====================================================

    private void chonPhuKien() {

        int row = tblPhuKien.getSelectedRow();

        if (row < 0
                || row >= danhSachDangHienThi.size()) {
            return;
        }

        PhuKien phuKien =
                danhSachDangHienThi.get(row);

        maPhuKienDangChon =
                phuKien.getMaPhuKien();

        txtTenPhuKien.setText(
                phuKien.getTenPhuKien()
        );

        txtDonGia.setText(
                phuKien.getDonGia().toPlainString()
        );

        chkTrangThai.setSelected(
                phuKien.isTrangThai()
        );
    }

    // =====================================================
    // LẤY DỮ LIỆU FORM
    // =====================================================

    private String layTenPhuKien() {

        String ten = txtTenPhuKien
                .getText()
                .trim();

        if (ten.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên phuKien."
            );
        }

        return ten;
    }

    private BigDecimal layDonGia() {

        String text = txtDonGia
                .getText()
                .trim()
                .replaceAll("[.,\\s]", "");

        if (text.isEmpty()
                || !text.matches("\\d+")) {

            throw new IllegalArgumentException(
                    "Đơn giá phải là số nguyên không âm, "
                    + "ví dụ 5000."
            );
        }

        BigDecimal gia = new BigDecimal(text);

        if (gia.signum() < 0) {
            throw new IllegalArgumentException(
                    "Đơn giá không được âm."
            );
        }

        return gia;
    }

    // =====================================================
    // THÊM TOPPING
    // =====================================================

    private void themPhuKien() {

        try {

            String ten = layTenPhuKien();

            BigDecimal gia = layDonGia();

            phuKienDAO.themPhuKien(
                    ten,
                    gia
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Thêm phuKien thành công!",
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
                    "Không thể thêm phuKien.\n"
                    + "Có thể tên phuKien đã tồn tại.\n\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // CẬP NHẬT TOPPING
    // =====================================================

    private void capNhatPhuKien() {

        if (maPhuKienDangChon == null) {

            hienThiCanhBao(
                    "Vui lòng chọn phuKien cần cập nhật."
            );

            return;
        }

        try {

            String ten = layTenPhuKien();

            BigDecimal gia = layDonGia();

            phuKienDAO.capNhatPhuKien(
                    maPhuKienDangChon,
                    ten,
                    gia,
                    chkTrangThai.isSelected()
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Cập nhật phuKien thành công!",
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
                    "Không thể cập nhật phuKien:\n"
                    + "Kiểm tra xem tên phuKien có bị trùng không.\n\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // NGỪNG BÁN
    // =====================================================

    private void ngungBan() {

        if (maPhuKienDangChon == null) {

            hienThiCanhBao(
                    "Vui lòng chọn phuKien cần ngừng bán."
            );

            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn muốn ngừng kinh doanh phuKien này?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            phuKienDAO.ngungKinhDoanh(
                    maPhuKienDangChon
            );

            JOptionPane.showMessageDialog(
                    this,
                    "Đã ngừng kinh doanh phuKien.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            loadData();

            lamMoiForm();

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể cập nhật trạng thái:\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // LÀM MỚI FORM
    // =====================================================

    private void lamMoiForm() {

        maPhuKienDangChon = null;

        tblPhuKien.clearSelection();

        txtTenPhuKien.setText("");

        txtDonGia.setText("5000");

        chkTrangThai.setSelected(true);

        txtTenPhuKien.requestFocusInWindow();
    }

    // =====================================================
    // THÀNH PHẦN GIAO DIỆN
    // =====================================================

    private JPanel createCard() {

        JPanel panel = new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(15, 15, 15, 15)
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
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        field.setPreferredSize(
                new Dimension(250, 36)
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

    private String dinhDangTien(BigDecimal gia) {

        return currency.format(gia) + " đ";
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
}
