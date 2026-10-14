package Model;

public class NhanVien {

    private long maNV;
    private String hoTen;

    public NhanVien() {
    }

    public NhanVien(long maNV, String hoTen) {
        this.maNV = maNV;
        this.hoTen = hoTen;
    }

    public long getMaNV() {
        return maNV;
    }

    public void setMaNV(long maNV) {
        this.maNV = maNV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        this.hoTen = hoTen;
    }

    @Override
    public String toString() {
        return hoTen;
    }
}