package Utils;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Lưu thông tin ghi nhớ đăng nhập trong vùng Preferences của tài khoản Windows hiện tại.
 * Chỉ sử dụng khi người dùng chủ động tích "Ghi nhớ đăng nhập".
 */
public final class RememberLogin {

    private static final Preferences PREFS =
            Preferences.userRoot().node("NovaMobile/QuanLyDienThoai");

    private static final String KEY_ENABLED = "rememberLoginEnabled";
    private static final String KEY_USERNAME = "rememberLoginUsername";
    private static final String KEY_PASSWORD = "rememberLoginPassword";

    private RememberLogin() {
    }

    public static void save(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.isEmpty()) {
            clear();
            return;
        }

        try {
            PREFS.put(KEY_USERNAME, username.trim());
            PREFS.put(KEY_PASSWORD, password);
            PREFS.putBoolean(KEY_ENABLED, true);
            PREFS.flush();
        } catch (SecurityException | BackingStoreException e) {
            // Không làm hỏng quá trình đăng nhập nếu máy chặn việc lưu Preferences.
            e.printStackTrace();
        }
    }

    public static Credentials load() {
        try {
            if (!PREFS.getBoolean(KEY_ENABLED, false)) {
                return null;
            }

            String username = PREFS.get(KEY_USERNAME, "");
            String password = PREFS.get(KEY_PASSWORD, "");

            if (username.trim().isEmpty() || password.isEmpty()) {
                clear();
                return null;
            }

            return new Credentials(username, password);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void clear() {
        try {
            PREFS.remove(KEY_ENABLED);
            PREFS.remove(KEY_USERNAME);
            PREFS.remove(KEY_PASSWORD);
            PREFS.flush();
        } catch (SecurityException | BackingStoreException e) {
            e.printStackTrace();
        }
    }

    public static final class Credentials {
        private final String username;
        private final String password;

        private Credentials(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
