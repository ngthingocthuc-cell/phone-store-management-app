package Model;

public class TaiKhoan {

    private long maTK;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;
    private Long maNV;
    private String email;
    private boolean trangThai;

    public TaiKhoan() {
    }

    public TaiKhoan(
            long maTK,
            String tenDangNhap,
            String matKhau,
            String vaiTro,
            Long maNV,
            String email,
            boolean trangThai
    ) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhau = matKhau;
        this.vaiTro = vaiTro;
        this.maNV = maNV;
        this.email = email;
        this.trangThai = trangThai;
    }

    public long getMaTK() {
        return maTK;
    }

    public void setMaTK(long maTK) {
        this.maTK = maTK;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() {
        return matKhau;
    }

    public void setMatKhau(String matKhau) {
        this.matKhau = matKhau;
    }

    public String getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(String vaiTro) {
        this.vaiTro = vaiTro;
    }

    public Long getMaNV() {
        return maNV;
    }

    public void setMaNV(Long maNV) {
        this.maNV = maNV;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(vaiTro);
    }
}
