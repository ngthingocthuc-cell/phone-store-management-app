package Controller;

import DAO.TaiKhoanDAO;
import Model.TaiKhoan;
import Utils.RememberLogin;
import Utils.Session;
import View.LoginView;
import View.MainView;

import javax.swing.*;
import java.sql.SQLException;

public class LoginController {

    private final LoginView view;
    private final TaiKhoanDAO taiKhoanDAO;

    public LoginController(LoginView view) {
        this.view = view;
        this.taiKhoanDAO = new TaiKhoanDAO();

        view.getBtnLogin().addActionListener(e -> dangNhap());
        view.getTxtPassword().addActionListener(e -> dangNhap());
    }

    // ============================================
    // TỰ ĐĂNG NHẬP KHI MỞ ỨNG DỤNG
    // ============================================
    public boolean tryAutoLogin() {
        RememberLogin.Credentials credentials = RememberLogin.load();

        if (credentials == null) {
            return false;
        }

        try {
            TaiKhoan tk = taiKhoanDAO.dangNhap(
                    credentials.getUsername(),
                    credentials.getPassword()
            );

            if (tk == null) {
                // Tài khoản bị khóa hoặc mật khẩu đã thay đổi.
                RememberLogin.clear();

                view.getTxtUsername().setText(
                        credentials.getUsername()
                );

                view.getTxtPassword().setText("");
                view.getChkRememberLogin().setSelected(false);

                view.getLblMessage().setText(
                        "Thông tin ghi nhớ không còn hợp lệ. Vui lòng đăng nhập lại."
                );

                return false;
            }

            moManHinhChinh(tk);
            return true;

        } catch (SQLException e) {
            e.printStackTrace();

            view.getLblMessage().setText(
                    "Không thể kiểm tra tài khoản. Vui lòng đăng nhập lại."
            );

            return false;
        }
    }

    // ============================================
    // ĐĂNG NHẬP THỦ CÔNG
    // ============================================
    private void dangNhap() {
        String username = view.getTxtUsername().getText().trim();
        String password = new String(
                view.getTxtPassword().getPassword()
        );

        if (username.isEmpty() || password.isEmpty()) {
            view.getLblMessage().setText(
                    "Vui lòng nhập đầy đủ tài khoản và mật khẩu!"
            );
            return;
        }

        try {
            TaiKhoan tk = taiKhoanDAO.dangNhap(
                    username,
                    password
            );

            if (tk == null) {
                view.getLblMessage().setText(
                        "Sai tài khoản hoặc mật khẩu!"
                );
                return;
            }

            // Được tích: lưu thông tin để tự đăng nhập lần sau.
            if (view.getChkRememberLogin().isSelected()) {
                RememberLogin.save(username, password);
            } else {
                RememberLogin.clear();
            }

            moManHinhChinh(tk);

        } catch (SQLException e) {
            e.printStackTrace();

            view.getLblMessage().setText(
                    "Lỗi truy cập cơ sở dữ liệu: " + e.getMessage()
            );
        }
    }

    // ============================================
    // MỞ MÀN HÌNH CHÍNH
    // ============================================
    private void moManHinhChinh(TaiKhoan tk) {
        Session.setTaiKhoan(tk);

        SwingUtilities.invokeLater(() -> {
            MainView mainView = new MainView();
            mainView.setVisible(true);
            view.dispose();
        });
    }
}