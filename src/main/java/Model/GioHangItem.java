package Model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GioHangItem {

    private SanPham sanPham;
    private int soLuong;

    // Luôn khởi tạo danh sách phuKien, không để null.
    private List<PhuKien> phuKiens = new ArrayList<>();

    public GioHangItem() {
        this.phuKiens = new ArrayList<>();
    }

    public GioHangItem(SanPham sanPham, int soLuong) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.phuKiens = new ArrayList<>();
    }

    public GioHangItem(
            SanPham sanPham,
            int soLuong,
            List<PhuKien> phuKiens
    ) {
        this.sanPham = sanPham;
        this.soLuong = soLuong;

        if (phuKiens == null) {
            this.phuKiens = new ArrayList<>();
        } else {
            this.phuKiens = new ArrayList<>(phuKiens);
        }
    }

    public SanPham getSanPham() {
        return sanPham;
    }

    public void setSanPham(SanPham sanPham) {
        this.sanPham = sanPham;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong < 0) {
            throw new IllegalArgumentException(
                    "Số lượng không được âm."
            );
        }

        this.soLuong = soLuong;
    }

    public List<PhuKien> getPhuKiens() {
        if (phuKiens == null) {
            phuKiens = new ArrayList<>();
        }

        return phuKiens;
    }

    public void setPhuKiens(List<PhuKien> phuKiens) {
        if (phuKiens == null) {
            this.phuKiens = new ArrayList<>();
        } else {
            this.phuKiens = new ArrayList<>(phuKiens);
        }
    }

    // Giá một máy gồm giá sản phẩm và tất cả phuKien.
    public BigDecimal getDonGiaCoPhuKien() {
        BigDecimal donGia = sanPham == null
                ? BigDecimal.ZERO
                : sanPham.getDonGia();

        if (donGia == null) {
            donGia = BigDecimal.ZERO;
        }

        for (PhuKien phuKien : getPhuKiens()) {
            if (phuKien != null && phuKien.getDonGia() != null) {
                donGia = donGia.add(phuKien.getDonGia());
            }
        }

        return donGia;
    }

    // Thành tiền = giá một máy đã gồm phuKien × số lượng.
    public BigDecimal getThanhTien() {
        return getDonGiaCoPhuKien()
                .multiply(BigDecimal.valueOf(soLuong));
    }
    public List<PhuKien> getDanhSachPhuKien() {
    return getPhuKiens();
}
}