package Model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PhuKien {

    private long maPhuKien;
    private String tenPhuKien;
    private BigDecimal donGia;
    private boolean trangThai;

    public PhuKien() {
    }

    public PhuKien(
            long maPhuKien,
            String tenPhuKien,
            BigDecimal donGia,
            boolean trangThai
    ) {
        this.maPhuKien = maPhuKien;
        this.tenPhuKien = tenPhuKien;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public long getMaPhuKien() {
        return maPhuKien;
    }

    public void setMaPhuKien(long maPhuKien) {
        this.maPhuKien = maPhuKien;
    }

    public String getTenPhuKien() {
        return tenPhuKien;
    }

    public void setTenPhuKien(String tenPhuKien) {
        this.tenPhuKien = tenPhuKien;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    @Override
    public String toString() {

        NumberFormat nf = NumberFormat.getInstance(
                Locale.forLanguageTag("vi-VN")
        );

        return tenPhuKien + " (+"
                + nf.format(donGia)
                + " đ)";
    }
}