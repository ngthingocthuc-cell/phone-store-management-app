package View;
import DAO.QuenMatKhauDAO;
import Utils.Theme;
import javax.swing.*;
import java.awt.*;

public class QuenMatKhauView extends JDialog {
    private final JTextField user=new JTextField(),email=new JTextField(),otp=new JTextField();
    private final JPasswordField password=new JPasswordField(),confirm=new JPasswordField();
    private final JButton send=Theme.button("Gửi OTP Gmail",false),reset=Theme.button("Xác minh & đặt mật khẩu",true);
    private final JLabel status=Theme.label("OTP có hiệu lực 5 phút. Gửi lại sau 60 giây.",12,Theme.MUTED);
    private final QuenMatKhauDAO dao=new QuenMatKhauDAO();
    @FunctionalInterface private interface Job{void run() throws Exception;}
    public QuenMatKhauView(LoginView owner){
        super(owner,"Khôi phục mật khẩu qua Gmail",true);setSize(520,610);setResizable(false);setLocationRelativeTo(owner);
        JPanel p=Theme.card(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;g.insets=new Insets(5,6,5,6);g.gridy=0;p.add(Theme.label("Lấy lại quyền truy cập",24,Theme.INK),g);
        user.setText(owner.getTxtUsername().getText());field(p,g,"Tên đăng nhập",user);field(p,g,"Email đã liên kết với tài khoản",email);g.gridy++;p.add(send,g);field(p,g,"Mã OTP gồm 6 chữ số",otp);field(p,g,"Mật khẩu mới (ít nhất 6 ký tự)",password);field(p,g,"Nhập lại mật khẩu mới",confirm);g.gridy++;p.add(reset,g);g.gridy++;p.add(status,g);setContentPane(p);
        send.addActionListener(e->{String u=user.getText().trim(),m=email.getText().trim();work(()->dao.send(u,m),()->status.setText("Đã gửi mã. Kiểm tra hộp thư và thư rác."));});
        reset.addActionListener(e->{String u=user.getText().trim(),m=email.getText().trim(),code=otp.getText().trim(),pw=new String(password.getPassword());if(!pw.equals(new String(confirm.getPassword()))){status.setText("Hai mật khẩu chưa khớp.");return;}work(()->dao.reset(u,m,code,pw),()->{JOptionPane.showMessageDialog(this,"Đã đổi mật khẩu. Đăng nhập bằng mật khẩu mới.");owner.getTxtPassword().setText("");dispose();});});
    }
    private void field(JPanel p,GridBagConstraints g,String text,JComponent input){g.gridy++;p.add(Theme.label(text,12,Theme.MUTED),g);g.gridy++;input.setPreferredSize(new Dimension(400,36));p.add(input,g);}
    private void work(Job job,Runnable done){send.setEnabled(false);reset.setEnabled(false);status.setText("Đang xử lý...");new SwingWorker<Void,Void>(){protected Void doInBackground()throws Exception{job.run();return null;}protected void done(){send.setEnabled(true);reset.setEnabled(true);try{get();done.run();}catch(Exception e){Throwable cause=e.getCause()==null?e:e.getCause();status.setText("Chưa thành công. Kiểm tra thông tin.");JOptionPane.showMessageDialog(QuenMatKhauView.this,cause.getMessage(),"Khôi phục mật khẩu",JOptionPane.WARNING_MESSAGE);}}}.execute();}
}
