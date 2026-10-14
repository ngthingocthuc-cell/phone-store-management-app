import DAO.*;
import Model.*;
import Utils.*;
import View.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class ProjectCheck {
    static int checks=0;static void check(boolean value,String message){if(!value)throw new AssertionError(message);checks++;}
    interface Action{void run()throws Exception;}
    static void rejected(Action action,String name)throws Exception{boolean failed=false;try{action.run();}catch(Exception e){failed=true;}check(failed,name);}
    static int stock(long id)throws Exception{try(Connection c=DBConnection.getConnection();PreparedStatement s=c.prepareStatement("SELECT TonKho FROM SanPham WHERE MaSP=?")){s.setLong(1,id);try(ResultSet r=s.executeQuery()){r.next();return r.getInt(1);}}}
    static long invoiceCount()throws Exception{try(Connection c=DBConnection.getConnection();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM HoaDon")){r.next();return r.getLong(1);}}
    static void sql(String command)throws Exception{try(Connection c=DBConnection.getConnection();Statement s=c.createStatement()){s.execute(command);}}
    static void render(Component c,String name,int width,int height)throws Exception{
        if(c instanceof JFrame f){f.setSize(width,height);f.validate();c=f.getContentPane();}
        c.addNotify();c.setSize(width,height);layout(c);
        BufferedImage image=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);Graphics2D g=image.createGraphics();g.setColor(Color.WHITE);g.fillRect(0,0,width,height);c.printAll(g);g.dispose();ImageIO.write(image,"png",Path.of("work",name+".png").toFile());if(c instanceof Window w)w.dispose();
    }
    static void layout(Component c){if(c instanceof Container p){p.doLayout();for(Component child:p.getComponents())layout(child);}}
    public static void main(String[] args)throws Exception{
        try{
            DatabaseInitializer.initialize();DatabaseInitializer.initialize();
            var auth=new TaiKhoanDAO();var admin=auth.dangNhap("admin","admin123");check(admin!=null&&admin.isAdmin(),"admin login");check("ngthingocthuc@gmail.com".equals(admin.getEmail()),"recovery email seeded");check(auth.dangNhap("admin","bad")==null,"wrong password rejected");
            var employee=auth.dangNhap("nhanvien01","123456");check(employee!=null&&!employee.isAdmin(),"employee login");Session.setTaiKhoan(admin);
            var catalog=new DienThoaiDAO();check(catalog.all().size()==10,"sample phones idempotent");var phone=catalog.all().get(0).sp();int before=stock(phone.getMaSP());
            var sales=new BanHangDAO();long nv=sales.getAllNhanVien().get(0).getMaNV();var invoices=new HoaDonDAO();
            long id=sales.taoHoaDon(admin,nv,List.of(new GioHangItem(phone,2)),new BigDecimal("50000"),"Tiền mặt","Khách kiểm thử");check(stock(phone.getMaSP())==before-2,"sale deducts stock");
            var found=invoices.timKiemHoaDon(true,null,null,id,(LocalDate)null).get(0);check(found.getThanhTien().equals(phone.getDonGia().multiply(new BigDecimal(2)).subtract(new BigDecimal("50000")).setScale(2)),"invoice totals");check(invoices.getChiTietHoaDon(id).get(0).getSoLuong()==2,"invoice details");
            PDFHoaDon.xuatHoaDon(Path.of("work/check-invoice.pdf").toFile(),found,invoices.getChiTietHoaDon(id));check(Files.size(Path.of("work/check-invoice.pdf"))>1000,"PDF exported");
            long count=invoiceCount();rejected(()->sales.taoHoaDon(admin,nv,List.of(new GioHangItem(phone,99999)),BigDecimal.ZERO,"Tiền mặt",""),"oversell rejected");check(invoiceCount()==count&&stock(phone.getMaSP())==before-2,"failed sale atomic");
            var other=catalog.all().get(1).sp();int otherBefore=stock(other.getMaSP());rejected(()->sales.taoHoaDon(admin,nv,List.of(new GioHangItem(other,1),new GioHangItem(phone,99999)),BigDecimal.ZERO,"Tiền mặt",""),"mixed stock failure");check(stock(other.getMaSP())==otherBefore&&invoiceCount()==count,"all rows rolled back");
            long pending=sales.taoHoaDon(admin,nv,List.of(new GioHangItem(phone,1)),BigDecimal.ZERO,"VietQR","Khách QR");check(invoices.timKiemHoaDon(true,null,null,pending,(LocalDate)null).get(0).getTrangThai().equals("Chờ thanh toán"),"QR awaits confirmation");check(invoices.xacNhanThanhToan(pending),"QR confirmed");check(!invoices.xacNhanThanhToan(pending),"confirmation idempotent");
            String url=ThanhToanConfig.taoUrl(pending,phone.getDonGia());check(url.contains("MB-0787911669")&&url.contains("NGUYEN+THI+NGOC+THUC"),"QR bank account and name");
            invoices.xoaHoaDon(id);invoices.xoaHoaDon(pending);check(stock(phone.getMaSP())==before,"delete returns stock");
            Session.setTaiKhoan(employee);rejected(()->catalog.stop(phone.getMaSP()),"employee cannot manage products");rejected(()->invoices.xoaHoaDon(1),"employee cannot delete invoices");rejected(()->sales.taoHoaDon(employee,nv+100,List.of(new GioHangItem(phone,1)),BigDecimal.ZERO,"Tiền mặt",""),"cannot spoof employee");Session.setTaiKhoan(admin);
            String[] captured={null};var otp=new QuenMatKhauDAO((mail,code)->{check(mail.equals(admin.getEmail()),"OTP recipient");captured[0]=code;});otp.send("admin",admin.getEmail());check(captured[0].matches("\\d{6}"),"OTP six digits");rejected(()->otp.send("admin",admin.getEmail()),"OTP resend cooldown");String wrong=captured[0].equals("000000")?"111111":"000000";rejected(()->otp.reset("admin",admin.getEmail(),wrong,"newPass123"),"OTP wrong code");
            otp.reset("admin",admin.getEmail(),captured[0],"newPass123");check(auth.dangNhap("admin","newPass123")!=null&&auth.dangNhap("admin","admin123")==null,"OTP resets login password");rejected(()->otp.reset("admin",admin.getEmail(),captured[0],"another123"),"OTP single use");
            sql("UPDATE RecoveryOtp SET CreatedAt=DATEADD('SECOND',-61,CURRENT_TIMESTAMP)");otp.send("admin",admin.getEmail());for(int i=0;i<5;i++)rejected(()->otp.reset("admin",admin.getEmail(),captured[0].equals("000000")?"111111":"000000","another123"),"wrong attempt "+i);rejected(()->otp.reset("admin",admin.getEmail(),captured[0],"another123"),"OTP locked after five failures");
            sql("UPDATE RecoveryOtp SET CreatedAt=DATEADD('SECOND',-61,CURRENT_TIMESTAMP)");otp.send("admin",admin.getEmail());sql("UPDATE RecoveryOtp SET ExpiresAt=DATEADD('SECOND',-1,CURRENT_TIMESTAMP)");rejected(()->otp.reset("admin",admin.getEmail(),captured[0],"another123"),"OTP expires");
            sql("UPDATE RecoveryOtp SET CreatedAt=DATEADD('SECOND',-61,CURRENT_TIMESTAMP)");rejected(()->new QuenMatKhauDAO((m,c)->{throw new Exception("simulated SMTP failure");}).send("admin",admin.getEmail()),"SMTP failure handled");
            new DoiMatKhauDAO().doiMatKhau(admin.getMaTK(),"newPass123","admin123");check(auth.dangNhap("admin","admin123")!=null,"change password supports hashing");
            var warranty=new BaoHanhDAO();long warrantySale=sales.taoHoaDon(admin,nv,List.of(new GioHangItem(phone,2)),BigDecimal.ZERO,"Tiền mặt","Khách minh họa");
            var line=warranty.cacMayDaBan().stream().filter(p->p.hoaDon()==warrantySale).findFirst().orElseThrow();
            rejected(()->warranty.register(line.id(),"123","Khách minh họa","0901002222",12),"IMEI requires 15 digits");
            rejected(()->warranty.register(line.id(),"357852146903214","Khách minh họa","phone",12),"warranty phone validated");
            rejected(()->warranty.register(line.id(),"357852146903214","Khách minh họa","0901002222",0),"warranty months validated");
            warranty.register(line.id(),"357852146903214","Khách minh họa","0901002222",12);var registered=warranty.all().get(0);
            check(registered.hoaDon()==warrantySale,"warranty linked to invoice");check(registered.hetHan().equals(line.ngayBan().plusMonths(12)),"warranty calculated from date of sale");
            rejected(()->warranty.register(line.id(),"357852146903214","Khách minh họa","0901002222",12),"IMEI unique");
            warranty.register(line.id(),"357852146903215","Khách thứ hai","0901003333",6);
            rejected(()->warranty.register(line.id(),"357852146903216","Khách thứ ba","0901004444",12),"registration cannot exceed units sold");
            rejected(()->warranty.update(registered.id(),"Đang sửa",""),"service changes require notes");
            warranty.update(registered.id(),"Đang sửa","Khách báo lỗi loa. Đã nhận máy và kiểm tra.");check(warranty.all().stream().filter(p->p.id()==registered.id()).findFirst().orElseThrow().trangThai().equals("Đang sửa"),"service status persisted");
            rejected(()->warranty.update(registered.id(),"unknown","note"),"invalid warranty status rejected");
            rejected(()->invoices.xoaHoaDon(warrantySale),"warranty keeps invoice source");check(stock(phone.getMaSP())==before-2,"rejected invoice delete does not change stock");
            long unpaid=sales.taoHoaDon(admin,nv,List.of(new GioHangItem(other,1)),BigDecimal.ZERO,"VietQR","Khách chờ thanh toán");
            long unpaidLine=invoices.getChiTietHoaDon(unpaid).get(0).getMaCT();rejected(()->warranty.register(unpaidLine,"357852146903217","Khách chờ","0901005555",12),"unpaid invoice cannot activate warranty");check(warranty.cacMayDaBan().stream().noneMatch(p->p.hoaDon()==unpaid),"unpaid sale excluded from picker");invoices.xoaHoaDon(unpaid);
            Session.logout();rejected(()->warranty.all(),"warranty requires login");Session.setTaiKhoan(admin);
            Theme.install();SwingUtilities.invokeAndWait(()->{try{
                LoginView login=new LoginView();render(login,"login-preview",900,760);login.dispose();
                render(new DashboardView(p->{}),"dashboard-preview",1210,770);render(new BanHangView(),"sales-preview",1210,770);render(new SanPhamView(),"catalog-preview",1210,770);
                render(new BaoHanhView(),"warranty-preview",1210,770);
                render(new HoaDonView(),"invoice-preview",1210,770);render(new NhanVienView(),"staff-preview",1210,770);render(new TaiKhoanView(),"accounts-preview",1210,770);render(new ThongKeView(),"stats-preview",1210,770);render(new GmailAdminView(),"gmail-preview",1210,770);
                MainView main=new MainView();render(main,"main-preview",1440,880);main.dispose();
            }catch(Exception e){throw new RuntimeException(e);}});
            System.out.println("PASS: "+checks+" integration checks and 11 UI renders; real SMTP delivery not exercised.");
            System.exit(0);
        }catch(Throwable e){e.printStackTrace();System.exit(1);}
    }
}
