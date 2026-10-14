
package Main;

import Utils.DatabaseInitializer;
import View.LoginView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    public static void main(String[] args) {

        // Đặt tỉ lệ giao diện Java Swing về mức bình thường.
        // Phải đặt trước khi khởi tạo bất kỳ giao diện Swing nào.
        System.setProperty("sun.java2d.uiScale", "1.0");
        Utils.Theme.install();

        try {
            DatabaseInitializer.initialize();

        } catch (Exception e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    null,
                    "Không thể kết nối hoặc khởi tạo cơ sở dữ liệu.\n"
                    + e.getMessage(),
                    "Lỗi cơ sở dữ liệu",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {

            LoginView loginView = new LoginView();

            // Nếu có thông tin ghi nhớ hợp lệ,
            // tự đăng nhập và mở màn hình chính.
            if (!loginView.tryAutoLogin()) {
                loginView.setVisible(true);
            }
        });
    }
}
