package week_03;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Form cấu hình và kiểm tra kết nối Microsoft SQL Server.
 */
public class frm_KetNoiSQL extends JFrame {

    private JTextField txt_server;
    private JTextField txt_port;
    private JTextField txt_database;
    private JTextField txt_user;
    private JPasswordField txt_password;
    private JButton btn_test;
    private JButton btn_history;
    private JLabel lbl_status;

    public frm_KetNoiSQL() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "Kết nối Microsoft SQL Server", DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        // Tiêu đề
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(FormHelper.titleLabel("KẾT NỐI MICROSOFT SQL SERVER", false), g);

        g.gridwidth = 1;

        txt_server = new JTextField("127.0.0.1");
        txt_server.setFont(FormHelper.FONT_REGULAR);
        txt_server.setPreferredSize(new Dimension(320, 28));
        addRow(root, g, 1, "Server:", txt_server, true);

        txt_port = new JTextField("14333");
        txt_port.setFont(FormHelper.FONT_REGULAR);
        txt_port.setPreferredSize(new Dimension(120, 28));
        addRow(root, g, 2, "Port:", txt_port, false);

        txt_database = new JTextField("LAB3_MaHoaKhoaCongKhai");
        txt_database.setFont(FormHelper.FONT_REGULAR);
        txt_database.setPreferredSize(new Dimension(320, 28));
        addRow(root, g, 3, "Database:", txt_database, true);

        txt_user = new JTextField("lab3_user");
        txt_user.setFont(FormHelper.FONT_REGULAR);
        txt_user.setPreferredSize(new Dimension(320, 28));
        addRow(root, g, 4, "User:", txt_user, true);

        txt_password = new JPasswordField();
        txt_password.setFont(FormHelper.FONT_REGULAR);
        txt_password.setPreferredSize(new Dimension(320, 28));
        addRow(root, g, 5, "Password:", txt_password, true);

        // Nút thao tác
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_test = FormHelper.createButton("Test Connection", 150, 30);
        btn_history = FormHelper.createButton("Xem lịch sử", 130, 30);
        btn_test.addActionListener(e -> btn_testActionPerformed());
        btn_history.addActionListener(e -> btn_historyActionPerformed());
        btnPanel.add(btn_test);
        btnPanel.add(btn_history);

        g.gridx = 1;
        g.gridy = 6;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(btnPanel, g);

        // Trạng thái
        lbl_status = new JLabel("Chưa kết nối.");
        lbl_status.setFont(FormHelper.FONT_REGULAR);
        lbl_status.setPreferredSize(new Dimension(450, 44));
        g.gridx = 0;
        g.gridy = 7;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(lbl_status, g);

        getRootPane().setDefaultButton(btn_test);
        setContentPane(root);
    }

    private void addRow(JPanel root, GridBagConstraints g, int row, String label,
            javax.swing.JComponent field, boolean fillHorizontal) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.0;
        g.fill = GridBagConstraints.NONE;
        root.add(FormHelper.fieldLabel(label), g);

        g.gridx = 1;
        g.weightx = fillHorizontal ? 1.0 : 0.0;
        g.fill = fillHorizontal ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        root.add(field, g);
    }

    private void btn_testActionPerformed() {
        String server = txt_server.getText().trim();
        String portText = txt_port.getText().trim();
        String database = txt_database.getText().trim();
        String user = txt_user.getText().trim();
        char[] password = txt_password.getPassword();

        try {
            if (server.isEmpty() || database.isEmpty() || user.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Server, Database và User không được để trống.",
                        "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length == 0) {
                JOptionPane.showMessageDialog(this, "Hãy nhập mật khẩu SQL Server trước khi kết nối.",
                        "Thiếu mật khẩu", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int port;
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Port phải là số nguyên hợp lệ (1 - 65535).",
                        "Sai định dạng Port", JOptionPane.WARNING_MESSAGE);
                return;
            }
            DbConfig.set(server, port, database, user, password);
            lbl_status.setText("<html>" + DbConfig.test() + "</html>");
        } catch (Exception ex) {
            DbConfig.clear();
            lbl_status.setText("<html>Kết nối thất bại: " + ex.getMessage() + "</html>");
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Không kết nối được SQL Server", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    private void btn_historyActionPerformed() {
        new frm_LichSu().setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_KetNoiSQL().setVisible(true));
    }
}
