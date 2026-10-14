package View;

import Controller.LoginController;
import Utils.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginView extends JFrame {
    private final JTextField username=new JTextField();private final JPasswordField password=new JPasswordField();
    private final JCheckBox remember=new JCheckBox("Giữ phiên đăng nhập trên máy này");
    private final JLabel message=Theme.label(" ",12,Theme.ACCENT);private final JButton login=Theme.button("Vào cửa hàng  →",true);
    private final LoginController controller;
    public LoginView(){
        setTitle("NOVA • Bắt đầu phiên làm việc");setSize(900,760);setMinimumSize(new Dimension(780,700));setLocationRelativeTo(null);ExitConfirmation.install(this);
        JPanel root=new JPanel(new BorderLayout()){
            protected void paintComponent(Graphics graphics){super.paintComponent(graphics);Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);g.setColor(new Color(237,229,215));g.fillOval(-170,getHeight()-310,470,470);g.setColor(Theme.SOFT);g.fillOval(getWidth()-190,-150,380,380);g.dispose();}
        };root.setBackground(Theme.BG);root.setBorder(new EmptyBorder(20,0,20,0));
        JPanel center=Theme.transparent(new GridBagLayout());JPanel form=Theme.card(new GridBagLayout());form.setPreferredSize(new Dimension(450,620));GridBagConstraints g=new GridBagConstraints();g.gridx=0;g.weightx=1;g.fill=GridBagConstraints.HORIZONTAL;g.insets=new Insets(7,16,7,16);g.gridy=0;
        JLabel brand=Theme.label("NOVA",32,Theme.ACCENT);form.add(brand,g);g.gridy++;form.add(Theme.label("CỬA HÀNG ĐIỆN THOẠI",11,Theme.MUTED),g);g.gridy++;form.add(Box.createVerticalStrut(8),g);g.gridy++;form.add(Theme.label("Bắt đầu phiên làm việc",25,Theme.INK),g);g.gridy++;form.add(Theme.label("Đơn bán, kho máy và bảo hành cùng một nơi.",13,Theme.MUTED),g);
        g.gridy++;form.add(new JLabel("Tài khoản"),g);g.gridy++;username.setPreferredSize(new Dimension(330,42));username.putClientProperty("JTextField.placeholderText","Tên đăng nhập");form.add(username,g);
        g.gridy++;form.add(new JLabel("Mật khẩu"),g);g.gridy++;password.setPreferredSize(new Dimension(330,42));form.add(password,g);
        JPanel options=Theme.transparent(new BorderLayout());JCheckBox show=new JCheckBox("Hiện mật khẩu");show.setOpaque(false);show.addActionListener(e->password.setEchoChar(show.isSelected()?(char)0:'•'));options.add(show);g.gridy++;form.add(options,g);g.gridy++;remember.setOpaque(false);form.add(remember,g);g.gridy++;form.add(login,g);g.gridy++;form.add(message,g);
        JButton forgot=Theme.button("Khôi phục mật khẩu bằng Gmail",false);forgot.addActionListener(e->new QuenMatKhauView(this).setVisible(true));g.gridy++;form.add(forgot,g);center.add(form);root.add(center,BorderLayout.CENTER);
        JLabel footer=Theme.label("NOVA  /  Thiết bị · Khách hàng · Dịch vụ",12,Theme.MUTED);footer.setHorizontalAlignment(SwingConstants.CENTER);footer.setBorder(new EmptyBorder(14,0,0,0));root.add(footer,BorderLayout.SOUTH);setContentPane(root);getRootPane().setDefaultButton(login);controller=new LoginController(this);
    }
    public boolean tryAutoLogin(){return controller.tryAutoLogin();}public JTextField getTxtUsername(){return username;}public JPasswordField getTxtPassword(){return password;}
    public JCheckBox getChkRememberLogin(){return remember;}public JLabel getLblMessage(){return message;}public JButton getBtnLogin(){return login;}
}