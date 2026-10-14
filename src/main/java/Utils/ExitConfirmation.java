package Utils;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/** Shared confirmation used when the user closes an application window. */
public final class ExitConfirmation {

    private ExitConfirmation() {
    }

    public static void install(JFrame frame) {
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                Object[] options = {"Thoát", "Ở lại"};
                int choice = JOptionPane.showOptionDialog(
                        frame,
                        "Bạn có chắc muốn thoát ứng dụng không?",
                        "Xác nhận thoát",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[1]
                );
                if (choice == 0) {
                    System.exit(0);
                }
            }
        });
    }
}
