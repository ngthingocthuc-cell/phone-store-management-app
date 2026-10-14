package Utils;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public final class GmailService {
    public static final String DEFAULT_EMAIL="ngthingocthuc@gmail.com";
    private GmailService() {}
    private static Path settings(){return DBConnection.getDatabaseDirectory().resolve("gmail.properties");}
    public static String sender(){
        String env=System.getenv("NOVA_SMTP_USER");if(env!=null&&!env.isBlank())return env.trim();
        Properties p=new Properties();if(Files.exists(settings()))try{String value=Files.readString(settings(),StandardCharsets.UTF_8);if(value.startsWith("\uFEFF"))value=value.substring(1);p.load(new StringReader(value));}catch(IOException e){throw new IllegalStateException("Không đọc được cấu hình Gmail.",e);}return p.getProperty("email",DEFAULT_EMAIL);
    }
    public static boolean configured(){return (System.getenv("NOVA_SMTP_PASSWORD")!=null&&!System.getenv("NOVA_SMTP_PASSWORD").isBlank())||Files.exists(DBConnection.getDatabaseDirectory().resolve("gmail-secret.dpapi"));}
    public static void save(String email,String appPassword) throws Exception {
        if(!email.matches("[A-Za-z0-9._%+-]+@gmail\\.com"))throw new IllegalArgumentException("Nhập địa chỉ Gmail hợp lệ.");
        String secret=appPassword.replaceAll("\\s+", "");if(!secret.matches("[A-Za-z]{16}"))throw new IllegalArgumentException("Mật khẩu ứng dụng Gmail gồm 16 chữ cái.");
        Files.createDirectories(DBConnection.getDatabaseDirectory());String protectedValue=dpapi(secret,true);
        Files.writeString(DBConnection.getDatabaseDirectory().resolve("gmail-secret.dpapi"),protectedValue,StandardCharsets.UTF_8);
        Properties p=new Properties();p.setProperty("email",email.trim());try(Writer w=Files.newBufferedWriter(settings())){p.store(w,"Nova Mobile SMTP sender; password protected by Windows DPAPI");}
    }
    private static String password() throws Exception {
        String env=System.getenv("NOVA_SMTP_PASSWORD");if(env!=null&&!env.isBlank())return env.replaceAll("\\s+", "");
        Path file=DBConnection.getDatabaseDirectory().resolve("gmail-secret.dpapi");if(!Files.exists(file))throw new IllegalStateException("Chưa cấu hình Gmail gửi OTP. Đăng nhập Admin và mở Cấu hình Gmail.");
        String protectedValue=Files.readString(file,StandardCharsets.UTF_8);if(protectedValue.startsWith("\uFEFF"))protectedValue=protectedValue.substring(1);
        return dpapi(protectedValue.trim(),false);
    }
    private static String dpapi(String value,boolean protect) throws Exception {
        if(!System.getProperty("os.name").toLowerCase().contains("windows"))throw new IllegalStateException("Trên máy này hãy dùng NOVA_SMTP_USER và NOVA_SMTP_PASSWORD.");
        String script="Add-Type -AssemblyName System.Security; $v=[Console]::In.ReadLine(); "+(protect?
            "[Convert]::ToBase64String([Security.Cryptography.ProtectedData]::Protect([Text.Encoding]::UTF8.GetBytes($v),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser))":
            "[Text.Encoding]::UTF8.GetString([Security.Cryptography.ProtectedData]::Unprotect([Convert]::FromBase64String($v),$null,[Security.Cryptography.DataProtectionScope]::CurrentUser))");
        Process process=new ProcessBuilder("powershell.exe","-NoProfile","-NonInteractive","-Command",script).start();
        try(Writer w=new OutputStreamWriter(process.getOutputStream(),StandardCharsets.UTF_8)){w.write(value+"\n");}
        if(!process.waitFor(20,TimeUnit.SECONDS)){process.destroyForcibly();throw new IOException("Windows chưa phản hồi khi đọc cấu hình Gmail.");}
        if(process.exitValue()!=0)throw new IOException("Không giải mã được Gmail. Cấu hình lại bằng tài khoản Windows hiện tại.");
        return new String(process.getInputStream().readAllBytes(),StandardCharsets.UTF_8).trim();
    }
    private static jakarta.mail.Session mailSession(String user,String secret) {
        Properties props=new Properties();
        props.setProperty("mail.smtp.host","smtp.gmail.com");props.setProperty("mail.smtp.port","587");props.setProperty("mail.smtp.auth","true");props.setProperty("mail.smtp.starttls.enable","true");props.setProperty("mail.smtp.starttls.required","true");props.setProperty("mail.smtp.ssl.checkserveridentity","true");
        props.setProperty("mail.smtp.connectiontimeout","15000");props.setProperty("mail.smtp.timeout","15000");props.setProperty("mail.smtp.writetimeout","15000");
        return jakarta.mail.Session.getInstance(props,new Authenticator(){@Override protected PasswordAuthentication getPasswordAuthentication(){return new PasswordAuthentication(user,secret);}});
    }
    /** Tests Gmail authentication only; it does not send a message. */
    public static void verifyConnection() throws Exception {
        String user=sender(),secret=password();
        try(Transport transport=mailSession(user,secret).getTransport("smtp")) { transport.connect(user,secret); }
    }
    public static String explainSendFailure(Exception error) {
        for(Throwable current=error;current!=null;current=current.getCause()) {
            if(current instanceof AuthenticationFailedException)
                return "Gmail từ chối đăng nhập. Hãy tạo mật khẩu ứng dụng Gmail mới (16 chữ cái), rồi lưu lại trong Cấu hình Gmail; kiểm tra Gmail gửi đúng tài khoản đã tạo mật khẩu đó và đã bật Xác minh 2 bước.";
            if(current instanceof java.net.UnknownHostException)
                return "Không tìm thấy máy chủ Gmail. Kiểm tra kết nối Internet hoặc DNS rồi thử lại.";
            if(current instanceof java.net.SocketTimeoutException||current instanceof java.net.ConnectException)
                return "Không kết nối được máy chủ Gmail qua cổng SMTP 587. Kiểm tra Internet, tường lửa hoặc mạng đang dùng.";
            if(current instanceof javax.net.ssl.SSLException)
                return "Kết nối bảo mật tới Gmail thất bại. Kiểm tra ngày giờ Windows, mạng hoặc phần mềm bảo mật đang chặn SMTP.";
        }
        return "Gmail chưa gửi được OTP. Vào Cấu hình Gmail và dùng nút Kiểm tra kết nối để xem nguyên nhân.";
    }
    public static void sendOtp(String recipient,String code) throws Exception {
        String user=sender(),secret=password();jakarta.mail.Session session=mailSession(user,secret);
        MimeMessage message=new MimeMessage(session);message.setFrom(new InternetAddress(user,"NOVA MOBILE","UTF-8"));message.setRecipient(Message.RecipientType.TO,new InternetAddress(recipient,true));message.setSubject("NOVA MOBILE - Mã khôi phục mật khẩu","UTF-8");
        message.setText("Mã OTP của bạn: "+code+"\nMã có hiệu lực 5 phút và chỉ dùng một lần.\nKhông chia sẻ mã này với người khác.\nNếu không yêu cầu đổi mật khẩu, hãy bỏ qua email này.","UTF-8");Transport.send(message);
    }
}
