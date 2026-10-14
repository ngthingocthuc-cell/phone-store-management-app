package View;
import Utils.Theme;

import DAO.DoiMatKhauDAO;
import Model.TaiKhoan;
import Utils.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class DoiMatKhauView extends JPanel {

    private final Color BACKGROUND =
            Theme.BG;

    private final Color PRIMARY =
            Theme.ACCENT;

    private final Color TEXT =
            new Color(37, 43, 61);

    private final Color MUTED =
            new Color(135, 143, 160);

    private final DoiMatKhauDAO dao =
            new DoiMatKhauDAO();

    private JPasswordField txtMatKhauCu;
    private JPasswordField txtMatKhauMoi;
    private JPasswordField txtXacNhan;

    private JCheckBox chkHienMatKhau;

    private JButton btnDoiMatKhau;

    private char echoCu;
    private char echoMoi;
    private char echoXacNhan;

    public DoiMatKhauView() {

        setLayout(new GridBagLayout());

        setBackground(BACKGROUND);

        add(createForm());
    }

    // =====================================================
    // FORM ĐỔI MẬT KHẨU
    // =====================================================

    private JPanel createForm() {

        JPanel card = new JPanel(
                new BorderLayout(0, 20)
        );

        card.setBackground(Color.WHITE);

        card.setPreferredSize(
                new Dimension(540, 520)
        );

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(232, 235, 242)
                        ),
                        new EmptyBorder(
                                28,
                                32,
                                28,
                                32
                        )
                )
        );

        // Tiêu đề
        JPanel header = new JPanel();

        header.setOpaque(false);

        header.setLayout(
                new BoxLayout(
                        header,
                        BoxLayout.Y_AXIS
                )
        );

        JLabel title = new JLabel(
                "Đổi mật khẩu"
        );

        title.setForeground(TEXT);

        title.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        25
                )
        );

        JLabel subtitle = new JLabel(
                "Cập nhật mật khẩu cho tài khoản của bạn."
        );

        subtitle.setForeground(MUTED);

        subtitle.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        13
                )
        );

        JLabel lblUser = new JLabel(
                "Tài khoản: "
                + (
                        Session.getTaiKhoan() == null
                        ? ""
                        : Session.getTaiKhoan().getTenDangNhap()
                )
        );

        lblUser.setForeground(PRIMARY);

        lblUser.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        header.add(title);

        header.add(
                Box.createVerticalStrut(8)
        );

        header.add(subtitle);

        header.add(
                Box.createVerticalStrut(14)
        );

        header.add(lblUser);

        card.add(
                header,
                BorderLayout.NORTH
        );

        // Các trường mật khẩu
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

        gbc.insets = new Insets(
                7,
                0,
                7,
                0
        );

        // Mật khẩu hiện tại
        gbc.gridy = 0;

        form.add(
                createLabel("Mật khẩu hiện tại"),
                gbc
        );

        gbc.gridy = 1;

        txtMatKhauCu = createPasswordField();

        form.add(txtMatKhauCu, gbc);

        // Mật khẩu mới
        gbc.gridy = 2;

        form.add(
                createLabel("Mật khẩu mới"),
                gbc
        );

        gbc.gridy = 3;

        txtMatKhauMoi = createPasswordField();

        form.add(txtMatKhauMoi, gbc);

        // Xác nhận mật khẩu
        gbc.gridy = 4;

        form.add(
                createLabel("Nhập lại mật khẩu mới"),
                gbc
        );

        gbc.gridy = 5;

        txtXacNhan = createPasswordField();

        form.add(txtXacNhan, gbc);

        // Hiện mật khẩu
        gbc.gridy = 6;

        chkHienMatKhau = new JCheckBox(
                "Hiện mật khẩu"
        );

        chkHienMatKhau.setOpaque(false);

        chkHienMatKhau.setForeground(MUTED);

        chkHienMatKhau.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        chkHienMatKhau.addActionListener(
                e -> capNhatHienMatKhau()
        );

        form.add(chkHienMatKhau, gbc);

        // Ghi chú
        gbc.gridy = 7;

        JLabel note = new JLabel(
                "<html>Mật khẩu mới phải có ít nhất 6 ký tự "
                + "và không được để trống.</html>"
        );

        note.setForeground(MUTED);

        note.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        12
                )
        );

        form.add(note, gbc);

        card.add(
                form,
                BorderLayout.CENTER
        );

        // Nút
        JPanel buttons = new JPanel(
                new GridLayout(1, 2, 10, 0)
        );

        buttons.setOpaque(false);

        btnDoiMatKhau = createPrimaryButton(
                "ĐỔI MẬT KHẨU"
        );

        JButton btnLamMoi = createSecondaryButton(
                "Làm mới"
        );

        btnDoiMatKhau.addActionListener(
                e -> xuLyDoiMatKhau()
        );

        btnLamMoi.addActionListener(
                e -> lamMoi()
        );

        buttons.add(btnDoiMatKhau);

        buttons.add(btnLamMoi);

        card.add(
                buttons,
                BorderLayout.SOUTH
        );

        return card;
    }

    // =====================================================
    // TẠO Ô MẬT KHẨU
    // =====================================================

    private JPasswordField createPasswordField() {

        JPasswordField field =
                new JPasswordField();

        field.setFont(
                new Font(
                        "Segoe UI",
                        Font.PLAIN,
                        14
                )
        );

        field.setPreferredSize(
                new Dimension(400, 42)
        );

        field.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(220, 223, 233)
                        ),
                        new EmptyBorder(
                                8,
                                12,
                                8,
                                12
                        )
                )
        );

        return field;
    }

    private JLabel createLabel(String text) {

        JLabel label = new JLabel(text);

        label.setForeground(TEXT);

        label.setFont(
                new Font(
                        "Segoe UI",
                        Font.BOLD,
                        13
                )
        );

        return label;
    }

    // =====================================================
    // HIỆN / ẨN MẬT KHẨU
    // =====================================================

    private void capNhatHienMatKhau() {

        if (chkHienMatKhau.isSelected()) {

            echoCu = txtMatKhauCu.getEchoChar();

            echoMoi = txtMatKhauMoi.getEchoChar();

            echoXacNhan = txtXacNhan.getEchoChar();

            txtMatKhauCu.setEchoChar((char) 0);

            txtMatKhauMoi.setEchoChar((char) 0);

            txtXacNhan.setEchoChar((char) 0);

        } else {

            txtMatKhauCu.setEchoChar(
                    echoCu == 0 ? '•' : echoCu
            );

            txtMatKhauMoi.setEchoChar(
                    echoMoi == 0 ? '•' : echoMoi
            );

            txtXacNhan.setEchoChar(
                    echoXacNhan == 0 ? '•' : echoXacNhan
            );
        }
    }

    // =====================================================
    // XỬ LÝ ĐỔI MẬT KHẨU
    // =====================================================

    private void xuLyDoiMatKhau() {

        TaiKhoan account = Session.getTaiKhoan();

        if (account == null) {

            hienThiLoi(
                    "Không tìm thấy phiên đăng nhập. "
                    + "Vui lòng đăng nhập lại."
            );

            return;
        }

        char[] currentChars =
                txtMatKhauCu.getPassword();

        char[] newChars =
                txtMatKhauMoi.getPassword();

        char[] confirmChars =
                txtXacNhan.getPassword();

        String matKhauCu =
                new String(currentChars);

        String matKhauMoi =
                new String(newChars);

        String xacNhan =
                new String(confirmChars);

        try {

            if (matKhauCu.isEmpty()
                    || matKhauMoi.isEmpty()
                    || xacNhan.isEmpty()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập đầy đủ các trường mật khẩu."
                );
            }

            if (matKhauMoi.length() < 6) {

                throw new IllegalArgumentException(
                        "Mật khẩu mới phải có ít nhất 6 ký tự."
                );
            }

            if (!matKhauMoi.equals(xacNhan)) {

                throw new IllegalArgumentException(
                        "Mật khẩu xác nhận không khớp."
                );
            }

            if (matKhauCu.equals(matKhauMoi)) {

                throw new IllegalArgumentException(
                        "Mật khẩu mới phải khác mật khẩu hiện tại."
                );
            }

            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc muốn đổi mật khẩu?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }

            btnDoiMatKhau.setEnabled(false);

            dao.doiMatKhau(
                    account.getMaTK(),
                    matKhauCu,
                    matKhauMoi
            );

            // Cập nhật trạng thái trong phiên hiện tại
            account.setMatKhau(matKhauMoi);

            JOptionPane.showMessageDialog(
                    this,
                    "Đổi mật khẩu thành công!",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE
            );

            lamMoi();

        } catch (IllegalArgumentException e) {

            hienThiCanhBao(
                    e.getMessage()
            );

        } catch (SQLException e) {

            e.printStackTrace();

            hienThiLoi(
                    "Không thể đổi mật khẩu:\n"
                    + e.getMessage()
            );

        } finally {

            Arrays.fill(currentChars, '\0');

            Arrays.fill(newChars, '\0');

            Arrays.fill(confirmChars, '\0');

            btnDoiMatKhau.setEnabled(true);
        }
    }

    // =====================================================
    // LÀM MỚI
    // =====================================================

    private void lamMoi() {

        txtMatKhauCu.setText("");

        txtMatKhauMoi.setText("");

        txtXacNhan.setText("");

        txtMatKhauCu.requestFocusInWindow();
    }

    // =====================================================
    // NÚT VÀ THÔNG BÁO
    // =====================================================

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

        button.setPreferredSize(
                new Dimension(180, 42)
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

        button.setPreferredSize(
                new Dimension(120, 42)
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
}