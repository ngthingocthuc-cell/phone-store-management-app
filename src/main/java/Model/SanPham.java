package Model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class SanPham {

    private long maSP;
    private String tenSP;
    private long maLoai;
    private String dungLuong;
    private BigDecimal donGia;
    private String moTa;
    private boolean trangThai;

    public SanPham() {
    }

    public SanPham(
            long maSP,
            String tenSP,
            long maLoai,
            String dungLuong,
            BigDecimal donGia,
            String moTa,
            boolean trangThai
    ) {
        this.maSP = maSP;
        this.tenSP = tenSP;
        this.maLoai = maLoai;
        this.dungLuong = dungLuong;
        this.donGia = donGia;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public long getMaSP() {
        return maSP;
    }

    public void setMaSP(long maSP) {
        this.maSP = maSP;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public long getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(long maLoai) {
        this.maLoai = maLoai;
    }

    public String getDungLuong() {
        return dungLuong;
    }

    public void setDungLuong(String dungLuong) {
        this.dungLuong = dungLuong;
    }

    public BigDecimal getDonGia() {
        return donGia;
    }

    public void setDonGia(BigDecimal donGia) {
        this.donGia = donGia;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
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

        return tenSP
                + " - Dung lượng " + dungLuong
                + " - " + nf.format(donGia)
                + " đ";
    }
}