package Utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class ThanhToanConfig {

    private static final String BANK_ID = "MB";
    private static final String ACCOUNT_NO = "0787911669";
    private static final String ACCOUNT_NAME = "NGUYEN THI NGOC THUC";

    private ThanhToanConfig() {
        // Không cho tạo đối tượng từ class cấu hình.
    }

    public static boolean daCauHinh() {
        return BANK_ID != null
                && !BANK_ID.isBlank()
                && ACCOUNT_NO != null
                && ACCOUNT_NO.matches("\\d{1,19}")
                && ACCOUNT_NAME != null
                && !ACCOUNT_NAME.isBlank();
    }

    public static String taoUrl(long maHD, BigDecimal soTien) {
        if (!daCauHinh()) {
            throw new IllegalStateException(
                    "Chưa cấu hình thông tin tài khoản nhận tiền."
            );
        }

        if (maHD <= 0) {
            throw new IllegalArgumentException(
                    "Mã hóa đơn không hợp lệ."
            );
        }

        if (soTien == null || soTien.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Số tiền thanh toán phải lớn hơn 0."
            );
        }

        long amount = soTien
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();

        String noiDung = String.format(
                Locale.ROOT,
                "THANHTOANHD%05d",
                maHD
        );

        String encodedNoiDung = URLEncoder.encode(
                noiDung,
                StandardCharsets.UTF_8
        );

        String encodedTen = URLEncoder.encode(
                ACCOUNT_NAME,
                StandardCharsets.UTF_8
        );

        return "https://img.vietqr.io/image/"
                + BANK_ID
                + "-"
                + ACCOUNT_NO
                + "-compact2.png"
                + "?amount="
                + amount
                + "&addInfo="
                + encodedNoiDung
                + "&accountName="
                + encodedTen;
    }
}