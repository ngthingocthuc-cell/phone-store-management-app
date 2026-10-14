package View;
import Utils.Theme;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;

public class VietQRDialog extends JDialog {

    private final String qrUrl;

    private JLabel lblQR;

    private JLabel lblTrangThai;

    public VietQRDialog(
            Window owner,
            long maHD,
            BigDecimal soTien,
            String qrUrl
    ) {

        super(
                owner,
                "Thanh toán VietQR",
                Dialog.ModalityType.APPLICATION_MODAL
        );

        this.qrUrl = qrUrl;

        setDefaultCloseOperation(
                JDialog.DISPOSE_ON_CLOSE
        );

        setSize(460, 620);

        setMinimumSize(
                new Dimension(420, 560)
        );

        setLocationRelativeTo(owner);

        setLayout(new BorderLayout(12, 12));

        getContentPane().setBackground(Color.WHITE);

        add(createHeader(maHD, soTien), BorderLayout.NORTH);

        add(createQRPanel(), BorderLayout.CENTER);

        add(createBottomPanel(), BorderLayout.SOUTH);

        // Tải ảnh nền trong worker để không khóa giao diện.
        taiAnhQR();
    }

    // =====================================================
    // HEADER
    // =====================================================

    private JPanel createHeader(
            long maHD,
            BigDecimal soTien
    ) {

        JPanel panel = new JPanel();

        panel.setBackground(Color.WHITE);

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBorder(
                new EmptyBorder(20, 20, 5, 20)
        );

        JLabel title = new JLabel(
                "QUÉT MÃ ĐỂ THANH TOÁN"
        );

        title.setFont(
                new Font("Segoe UI", Font.BOLD, 20)
        );

        title.setForeground(
                new Color(37, 43, 61)
        );

        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        NumberFormat money =
                NumberFormat.getNumberInstance(
                        Locale.forLanguageTag("vi-VN")
                );

        JLabel amount = new JLabel(
                money.format(soTien) + " VNĐ"
        );

        amount.setFont(
                new Font("Segoe UI", Font.BOLD, 26)
        );

        amount.setForeground(
                Theme.ACCENT
        );

        amount.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel invoice = new JLabel(
                String.format(
                        Locale.ROOT,
                        "Hóa đơn: HD%05d",
                        maHD
                )
        );

        invoice.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        invoice.setForeground(
                new Color(135, 143, 160)
        );

        invoice.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(title);

        panel.add(Box.createVerticalStrut(12));

        panel.add(amount);

        panel.add(Box.createVerticalStrut(6));

        panel.add(invoice);

        return panel;
    }

    // =====================================================
    // KHU VỰC ẢNH QR
    // =====================================================

    private JPanel createQRPanel() {

        JPanel panel = new JPanel(
                new GridBagLayout()
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(0, 20, 0, 20)
        );

        JPanel contents = new JPanel();

        contents.setBackground(Color.WHITE);

        contents.setLayout(
                new BoxLayout(contents, BoxLayout.Y_AXIS)
        );

        lblQR = new JLabel(
                "Đang tải mã QR...",
                SwingConstants.CENTER
        );

        lblQR.setFont(
                new Font("Segoe UI", Font.PLAIN, 13)
        );

        lblQR.setForeground(
                new Color(135, 143, 160)
        );

        lblQR.setPreferredSize(
                new Dimension(340, 340)
        );

        lblQR.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTrangThai = new JLabel(
                "Đang kết nối dịch vụ tạo QR..."
        );

        lblTrangThai.setFont(
                new Font("Segoe UI", Font.PLAIN, 12)
        );

        lblTrangThai.setForeground(
                new Color(135, 143, 160)
        );

        lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);

        contents.add(lblQR);

        contents.add(Box.createVerticalStrut(12));

        contents.add(lblTrangThai);

        panel.add(contents);

        return panel;
    }

    // =====================================================
    // NÚT CUỐI CỬA SỔ
    // =====================================================

    private JPanel createBottomPanel() {

        JPanel panel = new JPanel(
                new GridLayout(2, 1, 8, 8)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                new EmptyBorder(5, 20, 20, 20)
        );

        JButton btnMoTrinhDuyet = new JButton(
                "Mở mã QR trên trình duyệt"
        );

        btnMoTrinhDuyet.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        btnMoTrinhDuyet.addActionListener(e -> {

            try {

                if (Desktop.isDesktopSupported()) {

                    Desktop.getDesktop().browse(
                            URI.create(qrUrl)
                    );

                } else {

                    throw new IllegalStateException(
                            "Máy không hỗ trợ mở trình duyệt tự động."
                    );
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                        this,
                        "Không thể mở trình duyệt:\n"
                        + ex.getMessage(),
                        "Thông báo",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

        JButton btnDong = new JButton("Đóng");

        btnDong.setFont(
                new Font("Segoe UI", Font.BOLD, 13)
        );

        btnDong.addActionListener(
                e -> dispose()
        );

        panel.add(btnMoTrinhDuyet);

        panel.add(btnDong);

        return panel;
    }

    // =====================================================
    // TẢI ẢNH QR
    // =====================================================

    private void taiAnhQR() {

        SwingWorker<BufferedImage, Void> worker =
                new SwingWorker<>() {

            @Override
            protected BufferedImage doInBackground()
                    throws Exception {

                HttpURLConnection connection =
                        (HttpURLConnection)
                                new URL(qrUrl).openConnection();

                connection.setConnectTimeout(10000);

                connection.setReadTimeout(15000);

                connection.setRequestMethod("GET");

                try {

                    int status = connection.getResponseCode();

                    if (status != 200) {

                        throw new IllegalStateException(
                                "Dịch vụ QR trả về HTTP "
                                + status
                        );
                    }

                    BufferedImage image =
                            ImageIO.read(
                                    connection.getInputStream()
                            );

                    if (image == null) {
                        throw new IllegalStateException(
                                "Không đọc được ảnh QR."
                        );
                    }

                    return image;

                } finally {

                    connection.disconnect();
                }
            }

            @Override
            protected void done() {

                try {

                    BufferedImage image = get();

                    Image scaled = image.getScaledInstance(
                            320,
                            320,
                            Image.SCALE_SMOOTH
                    );

                    lblQR.setText("");

                    lblQR.setIcon(
                            new ImageIcon(scaled)
                    );

                    lblTrangThai.setText(
                            "Quét mã QR bằng ứng dụng ngân hàng."
                    );

                    lblTrangThai.setForeground(
                            new Color(16, 145, 100)
                    );

                } catch (Exception e) {

                    lblQR.setIcon(null);

                    lblQR.setText(
                            "<html><div style='text-align:center'>"
                            + "Không tải được mã QR.<br>"
                            + "Thử mở bằng trình duyệt."
                            + "</div></html>"
                    );

                    lblTrangThai.setText(
                            "Kiểm tra kết nối Internet và tài khoản nhận tiền."
                    );

                    lblTrangThai.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }
}