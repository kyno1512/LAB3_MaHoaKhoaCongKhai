package week_03;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * Tiện ích tạo giao diện Swing thống nhất cho toàn bộ các form Lab 3
 * (tận dụng chung kiểu chữ, kích thước điều khiển và Look & Feel hệ điều hành).
 */
public final class FormHelper {

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private static boolean lafInstalled = false;

    private FormHelper() {
    }

    public static void installSystemLookAndFeel() {
        if (lafInstalled) {
            return;
        }
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Dùng Look & Feel mặc định nếu hệ thống không hỗ trợ
        }
        lafInstalled = true;
    }

    public static JLabel titleLabel(String text, boolean large) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(large ? FONT_TITLE_LARGE : FONT_TITLE);
        return lbl;
    }

    public static JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_REGULAR);
        lbl.setPreferredSize(new Dimension(100, 22));
        return lbl;
    }

    public static JTextArea createTextArea(int rows, boolean editable, boolean mono) {
        JTextArea area = new JTextArea(rows, 50);
        area.setLineWrap(true);
        area.setWrapStyleWord(false);
        area.setEditable(editable);
        area.setFont(mono ? FONT_MONO : FONT_REGULAR);
        return area;
    }

    public static JScrollPane wrapScroll(JTextArea area, int width, int height) {
        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(width, height));
        return sp;
    }

    public static JButton createButton(String text, int width, int height) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_REGULAR);
        btn.setPreferredSize(new Dimension(width, height));
        return btn;
    }

    public static void finalizeFrame(JFrame frame, String title, int closeOperation) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(closeOperation);
        frame.pack();
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
    }
}
