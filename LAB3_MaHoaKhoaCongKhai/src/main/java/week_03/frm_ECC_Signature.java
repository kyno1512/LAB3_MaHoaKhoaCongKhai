package week_03;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Bài 3.5 - Giao diện Swing cho chữ ký số ECDSA (SHA256withECDSA, đường cong secp256r1).
 */
public class frm_ECC_Signature extends JFrame {

    private final ECCSignature ecdsa = new ECCSignature();

    private JTextArea txt_public;
    private JTextArea txt_private;
    private JButton btn_generate;
    private JButton btn_savekeys;
    private JButton btn_loadkeys;
    private JTextArea txt_message;
    private JTextArea txt_signature;
    private JButton btn_sign;
    private JButton btn_verify;
    private JLabel lbl_result;
    private JLabel lbl_status;

    public frm_ECC_Signature() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "ECDSA Signature", DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.NORTHWEST;

        // Tiêu đề
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(FormHelper.titleLabel("CHỮ KÝ SỐ ECDSA (SHA256withECDSA)", false), g);

        g.gridwidth = 1;

        // Public & Private keys
        txt_public = FormHelper.createTextArea(2, false, true);
        addAreaRow(root, g, 1, "Public key:", txt_public, 50, 0.0);

        txt_private = FormHelper.createTextArea(2, false, true);
        addAreaRow(root, g, 2, "Private key:", txt_private, 50, 0.0);

        // Nút quản lý khóa
        JPanel keyButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_generate = FormHelper.createButton("Generate Keys", 140, 30);
        btn_savekeys = FormHelper.createButton("Save Keys", 120, 30);
        btn_loadkeys = FormHelper.createButton("Load Keys", 120, 30);
        btn_generate.addActionListener(e -> btn_generateActionPerformed());
        btn_savekeys.addActionListener(e -> btn_savekeysActionPerformed());
        btn_loadkeys.addActionListener(e -> btn_loadkeysActionPerformed());
        keyButtons.add(btn_generate);
        keyButtons.add(btn_savekeys);
        keyButtons.add(btn_loadkeys);

        g.gridx = 1;
        g.gridy = 3;
        g.weightx = 1.0;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(keyButtons, g);

        // Message & Signature
        txt_message = FormHelper.createTextArea(2, true, false);
        addAreaRow(root, g, 4, "Message:", txt_message, 60, 0.5);

        txt_signature = FormHelper.createTextArea(2, true, true);
        addAreaRow(root, g, 5, "Signature:", txt_signature, 60, 0.5);

        // Nút Sign / Verify + Nhãn VALID/INVALID
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_sign = FormHelper.createButton("Sign", 110, 30);
        btn_verify = FormHelper.createButton("Verify", 110, 30);
        lbl_result = new JLabel("");
        lbl_result.setFont(FormHelper.FONT_TITLE_LARGE);
        lbl_result.setPreferredSize(new Dimension(240, 34));

        btn_sign.addActionListener(e -> btn_signActionPerformed());
        btn_verify.addActionListener(e -> btn_verifyActionPerformed());
        actionPanel.add(btn_sign);
        actionPanel.add(btn_verify);
        actionPanel.add(lbl_result);

        g.gridx = 1;
        g.gridy = 6;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(actionPanel, g);

        // Trạng thái
        lbl_status = new JLabel("Bấm Generate Keys hoặc Load Keys.");
        lbl_status.setFont(FormHelper.FONT_REGULAR);
        lbl_status.setPreferredSize(new Dimension(600, 36));
        g.gridx = 0;
        g.gridy = 7;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(lbl_status, g);

        setContentPane(root);
    }

    private void addAreaRow(JPanel root, GridBagConstraints g, int row, String label,
            JTextArea area, int height, double weighty) {
        g.gridx = 0;
        g.gridy = row;
        g.weightx = 0.0;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.NONE;
        root.add(FormHelper.fieldLabel(label), g);

        g.gridx = 1;
        g.weightx = 1.0;
        g.weighty = weighty;
        g.fill = GridBagConstraints.BOTH;
        root.add(FormHelper.wrapScroll(area, 600, height), g);
    }

    private void showKeys() {
        txt_public.setText(ecdsa.getPublicKeyBase64());
        txt_private.setText(ecdsa.getPrivateKeyBase64());
        txt_public.setCaretPosition(0);
        txt_private.setCaretPosition(0);
    }

    private void btn_generateActionPerformed() {
        try {
            ecdsa.generateKeys();
            showKeys();
            lbl_result.setText("");
            lbl_status.setText("Đã tạo cặp khóa mới. Chữ ký cũ sẽ thành INVALID với khóa mới.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tạo được khóa: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_savekeysActionPerformed() {
        if (txt_public.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Chưa có khóa để lưu.");
            return;
        }
        String content = CryptoSupport.toKeyFile(txt_public.getText(), txt_private.getText());
        String path = FileTextIO.saveString(this, content, "ecdsa_keys.txt");
        if (path != null) {
            lbl_status.setText("Đã lưu khóa vào: " + path + " (giữ bí mật dòng PRIVATE).");
        }
    }

    private void btn_loadkeysActionPerformed() {
        String content = FileTextIO.openString(this);
        if (content == null) {
            return;
        }
        try {
            String[] keys = CryptoSupport.fromKeyFile(content);
            ecdsa.loadKeys(keys[0], keys[1]);
            txt_public.setText(keys[0]);
            txt_private.setText(keys[1]);
            txt_public.setCaretPosition(0);
            txt_private.setCaretPosition(0);
            lbl_result.setText("");
            if (keys[1].isBlank()) {
                lbl_status.setText("Đã nạp khóa công khai từ tệp (không có khóa riêng - chỉ dùng để Verify).");
            } else {
                lbl_status.setText("Đã nạp cặp khóa ECDSA từ tệp.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tệp khóa không hợp lệ: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_signActionPerformed() {
        String message = txt_message.getText();
        if (message.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Message trước khi ký.");
            return;
        }
        try {
            txt_signature.setText(ecdsa.sign(message));
            txt_signature.setCaretPosition(0);
            lbl_result.setText("");
            lbl_status.setText("Đã ký bằng KHÓA RIÊNG. "
                    + HistoryDao.tryInsert("ECDSA", "SIGN", message.length(), true));
        } catch (Exception ex) {
            lbl_result.setText("");
            HistoryDao.tryInsert("ECDSA", "SIGN", message.length(), false);
            lbl_status.setText("Ký số thất bại: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Ký thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_verifyActionPerformed() {
        String message = txt_message.getText();
        String signature = txt_signature.getText();
        if (message.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Message trước khi xác minh.");
            return;
        }
        if (signature.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Signature trước khi xác minh.");
            return;
        }
        try {
            boolean ok = ecdsa.verify(message, signature);
            lbl_result.setText(ok ? "VALID" : "INVALID");
            lbl_result.setForeground(ok ? new Color(0, 128, 0) : Color.RED);
            lbl_status.setText("Đã xác minh bằng KHÓA CÔNG KHAI. "
                    + HistoryDao.tryInsert("ECDSA", "VERIFY", message.length(), ok));
        } catch (Exception ex) {
            lbl_result.setText("INVALID");
            lbl_result.setForeground(Color.RED);
            HistoryDao.tryInsert("ECDSA", "VERIFY", message.length(), false);
            lbl_status.setText("Xác minh thất bại: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Xác minh thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_ECC_Signature().setVisible(true));
    }
}
