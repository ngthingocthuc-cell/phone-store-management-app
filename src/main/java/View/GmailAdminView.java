package View;
import DAO.TaiKhoanDAO;
import Utils.*;
import javax.swing.*;
import java.awt.*;

public class GmailAdminView extends JPanel {
    public GmailAdminView(){
        setLayout(new BorderLayout());setBackground(Theme.BG);setBorder(BorderFactory.createEmptyBorder(28,28,28,28));
        add(Theme.heading("Cấu hình Gmail OTP","Thiết lập Gmail gửi mã và email khôi phục cho quản trị viên"),BorderLayout.NORTH);
        JPanel card=Theme.card(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;g.insets=new Insets(8,10,8,10);g.gridy=0;
        JTextField sender=new JTextField(GmailService.sender()),recipient=new JTextField(Session.getTaiKhoan().getEmail()==null?GmailService.DEFAULT_EMAIL:Session.getTaiKhoan().getEmail());JPasswordField secret=new JPasswordField();
        card.add(Theme.label("Gmail gửi mã",14,Theme.INK),g);g.gridy++;card.add(sender,g);g.gridy++;card.add(Theme.label("Mật khẩu ứng dụng Gmail (16 chữ cái)",14,Theme.INK),g);g.gridy++;card.add(secret,g);
        g.gridy++;card.add(Theme.label("Được bảo vệ bằng Windows, chỉ tài khoản Windows này đọc được.",12,Theme.MUTED),g);g.gridy++;JButton save=Theme.button("Lưu cấu hình gửi OTP",true);card.add(save,g);
        g.gridy++;card.add(Theme.label("Email khôi phục của tài khoản Admin hiện tại",14,Theme.INK),g);g.gridy++;card.add(recipient,g);g.gridy++;JButton link=Theme.button("Liên kết email khôi phục",false);card.add(link,g);
        g.gridy++;JLabel status=Theme.label(GmailService.configured()?"Đã có cấu hình gửi Gmail trên máy này":"Chưa cấu hình Gmail gửi OTP",13,Theme.TEAL);card.add(status,g);g.gridy++;JButton verify=Theme.button("Kiểm tra kết nối Gmail",false);card.add(verify,g);g.gridy++;g.weighty=1;card.add(Box.createVerticalGlue(),g);
        JPanel wrapper=new JPanel(new BorderLayout());wrapper.setOpaque(false);wrapper.add(card,BorderLayout.WEST);add(wrapper,BorderLayout.CENTER);
        save.addActionListener(e->{if(!Session.isAdmin())return;String mail=sender.getText().trim(),pw=new String(secret.getPassword());save.setEnabled(false);status.setText("Đang bảo vệ và lưu cấu hình...");new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{GmailService.save(mail,pw);return null;}protected void done(){save.setEnabled(true);try{get();secret.setText("");status.setText("Đã lưu cấu hình gửi Gmail.");}catch(Exception ex){Theme.error(GmailAdminView.this,new Exception(ex.getCause()==null?ex.getMessage():ex.getCause().getMessage()));}}}.execute();});
        verify.addActionListener(e->{if(!Session.isAdmin())return;verify.setEnabled(false);status.setText("Đang kiểm tra đăng nhập SMTP, không gửi thư...");new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{GmailService.verifyConnection();return null;}protected void done(){verify.setEnabled(true);try{get();status.setText("Kết nối Gmail thành công, có thể gửi OTP.");}catch(Exception ex){status.setText("Kết nối Gmail thất bại.");Theme.error(GmailAdminView.this,new Exception(GmailService.explainSendFailure(ex.getCause() instanceof Exception cause?cause:ex)));}}}.execute();});
        link.addActionListener(e->{try{new TaiKhoanDAO().capNhatEmailAdmin(Session.getTaiKhoan().getMaTK(),recipient.getText());Session.getTaiKhoan().setEmail(recipient.getText().trim());status.setText("Đã liên kết email khôi phục Admin.");}catch(Exception ex){Theme.error(this,ex);}});
    }
}
