package View;
import Utils.Theme;

import DAO.ThongKeDAO;
import DAO.ThongKeDAO.DoanhThuNgay;
import DAO.ThongKeDAO.SanPhamBanChay;
import DAO.ThongKeDAO.TongQuan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ThongKeView extends JPanel {

    private final Color BACKGROUND =
            Theme.BG;

    private final Color PRIMARY =
            Theme.ACCENT;

    private final Color TEXT =
            new Color(37, 43, 61);

    private final Color MUTED =
            new Color(135, 143, 160);

    private final ThongKeDAO dao =
            new ThongKeDAO();

    private JComboBox<String> cboKhoangNgay;

    private JSpinner spnTuNgay;

    private JSpinner spnDenNgay;

    private JLabel lblDoanhThu;
    private JLabel lblSoHoaDon;
    private JLabel lblTrungBinh;
    private JLabel lblGiamGia;
    private JLabel lblKhoangNgay;

    private JTable tblSanPham;

    private DefaultTableModel modelSanPham;

    private RevenueChart bieuDo;

    private final NumberFormat currency =
            NumberFormat.getNumberInstance(
                    Locale.forLanguageTag("vi-VN")
            );

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd/MM/yyyy");

    public ThongKeView() {

        setLayout(new BorderLayout(0, 16));

        setBackground(BACKGROUND);

        setBorder(
                new EmptyBorder(22, 24, 22, 24)
        );

        add(
                createHeader(),
                BorderLayout.NORTH
        );

        JPanel body = new JPanel(
                new BorderLayout(0, 14)
        );

        body.setOpaque(false);

        body.add(
                createFilterPanel(),
                BorderLayout.NORTH
        );

        JPanel content = new JPanel(
                new BorderLayout(0, 14)
        );

        content.setOpaque(false);

        content.add(
                createSummaryCards(),
                BorderLayout.NORTH
        );

        content.add(
                createAnalysisPanel(),
                BorderLayout.CENTER
        );

        body.add(
                content,
                BorderLayout.CENTER
        );

        add(
                body,
                BorderLayout.CENTER
        );

        loadThongKe();
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
                "Thống kê doanh thu"
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
                "Theo dõi doanh thu đã thanh toán và sản phẩm bán chạy."
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

        panel.add(
                titlePanel,
                BorderLayout.WEST
        );

        return panel;
    }

    // =====================================================
    // BỘ LỌC
    // =====================================================

    private JPanel createFilterPanel() {

        JPanel panel = new JPanel(
                new FlowLayout(
                        FlowLayout.LEFT,
                        9,
                        8
                )
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(4, 7, 4, 7)
                )
        );

        panel.add(
                createLabel("Khoảng thời gian:")
        );

        cboKhoangNgay = new JComboBox<>(
                new String[]{
                        "Hôm nay",
                        "7 ngày gần đây",
                        "Tháng này",
                        "Năm nay",
                        "Tùy chọn"
                }
        );

        cboKhoangNgay.setSelectedItem(
                "7 ngày gần đây"
        );

        cboKhoangNgay.setPreferredSize(
                new Dimension(145, 34)
        );

        panel.add(cboKhoangNgay);

        panel.add(
                createLabel("Từ ngày:")
        );

        spnTuNgay = createDateSpinner();

        panel.add(spnTuNgay);

        panel.add(
                createLabel("Đến ngày:")
        );

        spnDenNgay = createDateSpinner();

        panel.add(spnDenNgay);

        JButton btnLoc = createPrimaryButton(
                "Xem thống kê"
        );

        panel.add(btnLoc);

        JButton btnHomNay = createSecondaryButton(
                "Hôm nay"
        );

        panel.add(btnHomNay);

        cboKhoangNgay.addActionListener(e -> {

            capNhatTrangThaiNgay();

            if (!"Tùy chọn".equals(
                    cboKhoangNgay.getSelectedItem()
            )) {
                loadThongKe();
            }
        });

        btnLoc.addActionListener(
                e -> loadThongKe()
        );

        btnHomNay.addActionListener(e -> {

            cboKhoangNgay.setSelectedItem("Hôm nay");

            spnTuNgay.setValue(new Date());

            spnDenNgay.setValue(new Date());

            loadThongKe();
        });

        capNhatTrangThaiNgay();

        return panel;
    }

    private JSpinner createDateSpinner() {

        JSpinner spinner = new JSpinner(
                new SpinnerDateModel(
                        new Date(),
                        null,
                        null,
                        java.util.Calendar.DAY_OF_MONTH
                )
        );

        spinner.setEditor(
                new JSpinner.DateEditor(
                        spinner,
                        "dd/MM/yyyy"
                )
        );

        spinner.setPreferredSize(
                new Dimension(110, 34)
        );

        return spinner;
    }

    private void capNhatTrangThaiNgay() {

        boolean custom = "Tùy chọn".equals(
                cboKhoangNgay.getSelectedItem()
        );

        spnTuNgay.setEnabled(custom);

        spnDenNgay.setEnabled(custom);
    }

    // =====================================================
    // BỐN THẺ THỐNG KÊ
    // =====================================================

    private JPanel createSummaryCards() {

        JPanel panel = new JPanel(
                new GridLayout(1, 4, 12, 0)
        );

        panel.setOpaque(false);

        panel.setPreferredSize(
                new Dimension(0, 105)
        );

        lblDoanhThu = createMetricValue();

        lblSoHoaDon = createMetricValue();

        lblTrungBinh = createMetricValue();

        lblGiamGia = createMetricValue();

        panel.add(
                createMetricCard(
                        "DOANH THU",
                        lblDoanhThu,
                        PRIMARY
                )
        );

        panel.add(
                createMetricCard(
                        "HÓA ĐƠN ĐÃ THANH TOÁN",
                        lblSoHoaDon,
                        new Color(16, 165, 129)
                )
        );

        panel.add(
                createMetricCard(
                        "TRUNG BÌNH / HÓA ĐƠN",
                        lblTrungBinh,
                        new Color(236, 145, 45)
                )
        );

        panel.add(
                createMetricCard(
                        "TỔNG GIẢM GIÁ",
                        lblGiamGia,
                        new Color(226, 82, 117)
                )
        );

        return panel;
    }

    private JPanel createMetricCard(
            String title,
            JLabel valueLabel,
            Color accent
    ) {

        JPanel panel = new JPanel(
                new BorderLayout(0, 10)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(14, 15, 14, 15)
                )
        );

        JLabel lblTitle = new JLabel(title);

        lblTitle.setForeground(MUTED);

        lblTitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        11
                )
        );

        valueLabel.setForeground(accent);

        panel.add(
                lblTitle,
                BorderLayout.NORTH
        );

        panel.add(
                valueLabel,
                BorderLayout.CENTER
        );

        return panel;
    }

    private JLabel createMetricValue() {

        JLabel label = new JLabel("0");

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        21
                )
        );

        return label;
    }

    // =====================================================
    // BIỂU ĐỒ VÀ SẢN PHẨM BÁN CHẠY
    // =====================================================

    private JPanel createAnalysisPanel() {

        JPanel panel = new JPanel(
                new GridLayout(1, 2, 14, 0)
        );

        panel.setOpaque(false);

        panel.add(
                createRevenuePanel()
        );

        panel.add(
                createTopProductsPanel()
        );

        return panel;
    }

    // =====================================================
    // BIỂU ĐỒ DOANH THU
    // =====================================================

    private JPanel createRevenuePanel() {

        JPanel panel = createCard();

        panel.setLayout(
                new BorderLayout(0, 12)
        );

        JPanel header = new JPanel(
                new BorderLayout()
        );

        header.setOpaque(false);

        JLabel title = createSectionTitle(
                "Doanh thu theo ngày"
        );

        lblKhoangNgay = new JLabel("");

        lblKhoangNgay.setForeground(MUTED);

        lblKhoangNgay.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        11
                )
        );

        header.add(title, BorderLayout.NORTH);

        header.add(lblKhoangNgay, BorderLayout.SOUTH);

        bieuDo = new RevenueChart();

        panel.add(
                header,
                BorderLayout.NORTH
        );

        panel.add(
                bieuDo,
                BorderLayout.CENTER
        );

        return panel;
    }

    // =====================================================
    // BẢNG TOP SẢN PHẨM
    // =====================================================

    private JPanel createTopProductsPanel() {

        JPanel panel = createCard();

        panel.setLayout(
                new BorderLayout(0, 12)
        );

        JLabel title = createSectionTitle(
                "Top 10 sản phẩm bán chạy"
        );

        modelSanPham = new DefaultTableModel(
                new Object[]{
                        "Tên sản phẩm",
                        "Dung lượng",
                        "Số lượng",
                        "Doanh thu"
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

        tblSanPham = new JTable(modelSanPham);

        tblSanPham.setRowHeight(31);

        tblSanPham.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        tblSanPham.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        tblSanPham.setShowVerticalLines(false);

        tblSanPham.setGridColor(
                new Color(238, 240, 245)
        );

        tblSanPham.getTableHeader().setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        tblSanPham.getTableHeader().setBackground(
                new Color(249, 250, 253)
        );

        tblSanPham.getTableHeader().setPreferredSize(
                new Dimension(0, 34)
        );

        tblSanPham.getColumnModel()
                .getColumn(0)
                .setPreferredWidth(150);

        tblSanPham.getColumnModel()
                .getColumn(1)
                .setPreferredWidth(45);

        tblSanPham.getColumnModel()
                .getColumn(2)
                .setPreferredWidth(70);

        tblSanPham.getColumnModel()
                .getColumn(3)
                .setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(
                tblSanPham
        );

        scroll.setBorder(
                BorderFactory.createLineBorder(
                        new Color(232, 235, 242)
                )
        );

        panel.add(title, BorderLayout.NORTH);

        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // =====================================================
    // ĐỌC KHOẢNG NGÀY ĐƯỢC CHỌN
    // =====================================================

    private LocalDate[] layKhoangNgay() {

        LocalDate today = LocalDate.now();

        String choice = (String)
                cboKhoangNgay.getSelectedItem();

        LocalDate tuNgay;

        LocalDate denNgay = today;

        switch (choice == null ? "" : choice) {

            case "Hôm nay" -> tuNgay = today;

            case "7 ngày gần đây" ->
                    tuNgay = today.minusDays(6);

            case "Tháng này" ->
                    tuNgay = today.withDayOfMonth(1);

            case "Năm nay" ->
                    tuNgay = today.withDayOfYear(1);

            case "Tùy chọn" -> {

                Date startDate =
                        (Date) spnTuNgay.getValue();

                Date endDate =
                        (Date) spnDenNgay.getValue();

                tuNgay = startDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                denNgay = endDate.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            }

            default -> tuNgay = today.minusDays(6);
        }

        if (tuNgay.isAfter(denNgay)) {

            throw new IllegalArgumentException(
                    "Ngày bắt đầu không được sau ngày kết thúc."
            );
        }

        return new LocalDate[]{
                tuNgay,
                denNgay
        };
    }

    // =====================================================
    // TẢI THỐNG KÊ TỪ H2
    // =====================================================

    private void loadThongKe() {

        LocalDate[] range;

        try {

            range = layKhoangNgay();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    this,
                    e.getMessage(),
                    "Khoảng ngày không hợp lệ",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        LocalDate tuNgay = range[0];

        LocalDate denNgay = range[1];

        try {

            // Tổng quan
            TongQuan tongQuan =
                    dao.getTongQuan(
                            tuNgay,
                            denNgay
                    );

            BigDecimal doanhThu =
                    tongQuan.getDoanhThu();

            BigDecimal giamGia =
                    tongQuan.getGiamGia();

            long soHoaDon =
                    tongQuan.getSoHoaDon();

            lblDoanhThu.setText(
                    dinhDangTien(doanhThu) + " đ"
            );

            lblSoHoaDon.setText(
                    String.valueOf(soHoaDon)
            );

            lblGiamGia.setText(
                    dinhDangTien(giamGia) + " đ"
            );

            BigDecimal trungBinh = BigDecimal.ZERO;

            if (soHoaDon > 0) {

                trungBinh = doanhThu.divide(
                        BigDecimal.valueOf(soHoaDon),
                        2,
                        RoundingMode.HALF_UP
                );
            }

            lblTrungBinh.setText(
                    dinhDangTien(trungBinh) + " đ"
            );

            // Doanh thu từng ngày
            List<DoanhThuNgay> doanhThuNgay =
                    dao.getDoanhThuTheoNgay(
                            tuNgay,
                            denNgay
                    );

            bieuDo.setData(doanhThuNgay);

            // Top sản phẩm
            List<SanPhamBanChay> top =
                    dao.getSanPhamBanChay(
                            tuNgay,
                            denNgay
                    );

            modelSanPham.setRowCount(0);

            for (SanPhamBanChay sp : top) {

                modelSanPham.addRow(
                        new Object[]{
                                sp.getTenSanPham(),
                                sp.getDungLuong(),
                                sp.getSoLuong(),
                                dinhDangTien(sp.getDoanhThu())
                        }
                );
            }

            lblKhoangNgay.setText(
                    dateFormat.format(
                            java.sql.Date.valueOf(tuNgay)
                    )
                    + " - "
                    + dateFormat.format(
                            java.sql.Date.valueOf(denNgay)
                    )
            );

        } catch (SQLException e) {

            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Không thể tải thống kê từ H2:\n"
                    + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // =====================================================
    // BIỂU ĐỒ CỘT TỰ VẼ
    // =====================================================

    private class RevenueChart extends JPanel {

        private List<DoanhThuNgay> data =
                new ArrayList<>();

        public RevenueChart() {

            setOpaque(false);

            setPreferredSize(
                    new Dimension(420, 310)
            );
        }

        public void setData(List<DoanhThuNgay> list) {

            data = list == null
                    ? new ArrayList<>()
                    : new ArrayList<>(list);

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 =
                    (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();

            int height = getHeight();

            int left = 52;

            int right = width - 15;

            int top = 25;

            int bottom = height - 42;

            int chartHeight = bottom - top;

            int chartWidth = right - left;

            // Không có dữ liệu
            if (data.isEmpty()) {

                g2.setColor(MUTED);

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                13
                        )
                );

                String message =
                        "Chưa có doanh thu đã thanh toán.";

                FontMetrics fm = g2.getFontMetrics();

                g2.drawString(
                        message,
                        Math.max(
                                5,
                                (width - fm.stringWidth(message)) / 2
                        ),
                        height / 2
                );

                g2.dispose();

                return;
            }

            BigDecimal max = BigDecimal.ZERO;

            for (DoanhThuNgay item : data) {

                if (item.getDoanhThu().compareTo(max) > 0) {
                    max = item.getDoanhThu();
                }
            }

            if (max.compareTo(BigDecimal.ZERO) <= 0) {
                max = BigDecimal.ONE;
            }

            int count = data.size();

            int step = Math.max(
                    1,
                    chartWidth / count
            );

            int barWidth = Math.min(
                    32,
                    Math.max(8, step - 8)
            );

            // Đường kẻ ngang
            for (int i = 0; i <= 4; i++) {

                int y =
                        top + chartHeight * i / 4;

                g2.setColor(
                        new Color(235, 238, 245)
                );

                g2.drawLine(
                        left,
                        y,
                        right,
                        y
                );

                BigDecimal labelValue =
                        max.multiply(
                                BigDecimal.valueOf(4 - i)
                        ).divide(
                                BigDecimal.valueOf(4),
                                0,
                                RoundingMode.HALF_UP
                        );

                g2.setColor(MUTED);

                g2.setFont(
                        new Font(
                                "Segoe UI",
                                Font.PLAIN,
                                9
                        )
                );

                g2.drawString(
                        dinhDangNgan(labelValue),
                        2,
                        y + 4
                );
            }

            int labelInterval = Math.max(
                    1,
                    (int) Math.ceil(count / 8.0)
            );

            for (int i = 0; i < count; i++) {

                DoanhThuNgay item = data.get(i);

                double ratio = item.getDoanhThu()
                        .divide(
                                max,
                                6,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue();

                int barHeight = (int) (
                        Math.max(0, ratio) * chartHeight
                );

                int x = left
                        + i * step
                        + Math.max(0, (step - barWidth) / 2);

                int y = bottom - barHeight;

                g2.setColor(PRIMARY);

                g2.fillRoundRect(
                        x,
                        y,
                        barWidth,
                        Math.max(1, barHeight),
                        8,
                        8
                );

                if (i % labelInterval == 0
                        || i == count - 1) {

                    String label = String.format(
                            Locale.ROOT,
                            "%02d/%02d",
                            item.getNgay().getDayOfMonth(),
                            item.getNgay().getMonthValue()
                    );

                    g2.setColor(MUTED);

                    g2.setFont(
                            new Font(
                                    "Segoe UI",
                                    Font.PLAIN,
                                    9
                            )
                    );

                    FontMetrics fm = g2.getFontMetrics();

                    int labelWidth =
                            fm.stringWidth(label);

                    int labelX = x
                            + barWidth / 2
                            - labelWidth / 2;

                    g2.drawString(
                            label,
                            labelX,
                            bottom + 18
                    );
                }
            }

            g2.dispose();
        }
    }

    private String dinhDangNgan(BigDecimal gia) {

        if (gia.abs().compareTo(
                new BigDecimal("1000000")
        ) >= 0) {

            return currency.format(
                    gia.divide(
                            new BigDecimal("1000000"),
                            1,
                            RoundingMode.HALF_UP
                    )
            ) + "tr";
        }

        if (gia.abs().compareTo(
                new BigDecimal("1000")
        ) >= 0) {

            return currency.format(
                    gia.divide(
                            new BigDecimal("1000"),
                            0,
                            RoundingMode.HALF_UP
                    )
            ) + "k";
        }

        return currency.format(gia);
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
                        new EmptyBorder(15, 15, 15, 15)
                )
        );

        return panel;
    }

    private JLabel createSectionTitle(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(TEXT);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        16
                )
        );

        return label;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(MUTED);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        return label;
    }

    private JButton createPrimaryButton(String text) {

        JButton button = new JButton(text);

        button.setForeground(Color.WHITE);

        button.setBackground(PRIMARY);

        button.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
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
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        12
                )
        );

        button.setFocusPainted(false);

        button.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR
                )
        );

        return button;
    }

    private String dinhDangTien(BigDecimal value) {

        if (value == null) {
            value = BigDecimal.ZERO;
        }

        return currency.format(value);
    }
}