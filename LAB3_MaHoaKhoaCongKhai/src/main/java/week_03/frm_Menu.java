package week_03;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Cửa sổ chính Lab 3 - mở các form RSA, ECC, RSA+AES, ECDSA, SQL Server và Lịch sử.
 * Lớp chạy chính khi bấm Run Project (F6).
 */
public class frm_Menu extends JFrame {

    private JButton btn_rsa;
    private JButton btn_ecc;
    private JButton btn_rsaaes;
    private JButton btn_signature;
    private JButton btn_sql;
    private JButton btn_history;

    public frm_Menu() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "LAB 3 - Mã hóa khóa công khai", EXIT_ON_CLOSE);
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 12));
        root.setBorder(BorderFactory.createEmptyBorder(18, 20, 20, 20));

        JPanel header = new JPanel(new GridLayout(2, 1, 0, 6));
        header.add(FormHelper.titleLabel("LAB 3 - MÃ HÓA KHÓA CÔNG KHAI", true));
        JLabel subtitle = new JLabel("Chọn một bài để mở form tương ứng (bài 3.1 chạy RSADemo.java bằng Run File):");
        subtitle.setFont(FormHelper.FONT_REGULAR);
        header.add(subtitle);
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        btn_rsa = FormHelper.createButton("3.2 RSA (Swing)", 200, 34);
        btn_ecc = FormHelper.createButton("3.3 ECC", 200, 34);
        btn_rsaaes = FormHelper.createButton("3.4 RSA + AES", 200, 34);
        btn_signature = FormHelper.createButton("3.5 Chữ ký ECDSA", 200, 34);
        btn_sql = FormHelper.createButton("Kết nối SQL Server", 200, 34);
        btn_history = FormHelper.createButton("Xem lịch sử", 200, 34);

        btn_rsa.addActionListener(e -> new frm_RSA().setVisible(true));
        btn_ecc.addActionListener(e -> new frm_ECC().setVisible(true));
        btn_rsaaes.addActionListener(e -> new frm_RSA_AES().setVisible(true));
        btn_signature.addActionListener(e -> new frm_ECC_Signature().setVisible(true));
        btn_sql.addActionListener(e -> new frm_KetNoiSQL().setVisible(true));
        btn_history.addActionListener(e -> new frm_LichSu().setVisible(true));

        grid.add(btn_rsa);
        grid.add(btn_ecc);
        grid.add(btn_rsaaes);
        grid.add(btn_signature);
        grid.add(btn_sql);
        grid.add(btn_history);

        root.add(grid, BorderLayout.CENTER);
        setContentPane(root);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_Menu().setVisible(true));
    }
}
