package Utils;

import Model.TaiKhoan;

public final class Session {

    private static TaiKhoan taiKhoan;

    private Session() {
    }

    public static void setTaiKhoan(TaiKhoan account) {
        taiKhoan = account;
    }

    public static TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    public static boolean isAdmin() {
        return taiKhoan != null && taiKhoan.isAdmin();
    }

    public static boolean isLoggedIn() {
        return taiKhoan != null;
    }

    public static void logout() {
        taiKhoan = null;
    }
}