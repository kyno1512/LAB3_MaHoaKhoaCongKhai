package week_03;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * Bài 3.2 - Giao diện Swing cho mật mã RSA (OAEP - SHA-256).
 */
public class frm_RSA extends JFrame {

    private final RSACipher rsa = new RSACipher();

    private JComboBox<String> cbo_bits;
    private JButton btn_generate;
    private JLabel lbl_keyinfo;
    private JTextArea txt_plaintext;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JLabel lbl_status;

    public frm_RSA() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "RSA Cipher", DISPOSE_ON_CLOSE);
    }

    private void initComponents() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.anchor = GridBagConstraints.WEST;

        // Tiêu đề
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(FormHelper.titleLabel("MẬT MÃ RSA (OAEP - SHA-256)", false), g);

        // Hàng Key size + Generate Keys + thông tin khóa
        g.gridwidth = 1;
        g.gridx = 0;
        g.gridy = 1;
        g.weightx = 0.0;
        g.fill = GridBagConstraints.NONE;
        root.add(FormHelper.fieldLabel("Key size:"), g);

        JPanel keyBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cbo_bits = new JComboBox<>(new String[] {"2048", "3072"});
        cbo_bits.setFont(FormHelper.FONT_REGULAR);
        cbo_bits.setPreferredSize(new Dimension(110, 28));
        cbo_bits.addActionListener(e -> {
            if (rsa.hasKeys()) {
                int selected = Integer.parseInt((String) cbo_bits.getSelectedItem());
                if (selected != rsa.getBits()) {
                    lbl_status.setText("Đang giữ khóa RSA-" + rsa.getBits()
                            + ". Bấm Generate Keys để tạo cặp khóa RSA-" + selected + " mới.");
                }
            }
        });
        btn_generate = FormHelper.createButton("Generate Keys", 140, 30);
        btn_generate.addActionListener(e -> btn_generateActionPerformed());

        lbl_keyinfo = new JLabel("Chưa có khóa.");
        lbl_keyinfo.setFont(FormHelper.FONT_REGULAR);
        lbl_keyinfo.setPreferredSize(new Dimension(340, 24));

        keyBar.add(cbo_bits);
        keyBar.add(btn_generate);
        keyBar.add(lbl_keyinfo);

        g.gridx = 1;
        g.weightx = 1.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(keyBar, g);

        // Plaintext
        g.gridx = 0;
        g.gridy = 2;
        g.weightx = 0.0;
        g.anchor = GridBagConstraints.NORTHWEST;
        g.fill = GridBagConstraints.NONE;
        root.add(FormHelper.fieldLabel("Plaintext:"), g);

        txt_plaintext = FormHelper.createTextArea(3, true, false);
        g.gridx = 1;
        g.weightx = 1.0;
        g.weighty = 0.35;
        g.fill = GridBagConstraints.BOTH;
        root.add(FormHelper.wrapScroll(txt_plaintext, 600, 72), g);

        // Ciphertext
        g.gridx = 0;
        g.gridy = 3;
        g.weightx = 0.0;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.NONE;
        root.add(FormHelper.fieldLabel("Ciphertext:"), g);

        txt_ciphertext = FormHelper.createTextArea(5, true, true);
        g.gridx = 1;
        g.weightx = 1.0;
        g.weighty = 0.65;
        g.fill = GridBagConstraints.BOTH;
        root.add(FormHelper.wrapScroll(txt_ciphertext, 600, 120), g);

        // Nút Encrypt / Decrypt
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_encrypt = FormHelper.createButton("Encrypt", 110, 30);
        btn_decrypt = FormHelper.createButton("Decrypt", 110, 30);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        actionPanel.add(btn_encrypt);
        actionPanel.add(btn_decrypt);

        g.gridx = 1;
        g.gridy = 4;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(actionPanel, g);

        // Trạng thái
        lbl_status = new JLabel("Bấm Generate Keys để tạo cặp khóa.");
        lbl_status.setFont(FormHelper.FONT_REGULAR);
        lbl_status.setPreferredSize(new Dimension(600, 36));
        g.gridx = 0;
        g.gridy = 5;
        g.gridwidth = 2;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(lbl_status, g);

        setContentPane(root);
    }

    private void btn_generateActionPerformed() {
        int bits = Integer.parseInt((String) cbo_bits.getSelectedItem());
        try {
            long start = System.currentTimeMillis();
            rsa.generateKeys(bits);
            long ms = System.currentTimeMillis() - start;
            lbl_keyinfo.setText("RSA-" + bits + ": tạo khóa " + ms + " ms, tối đa "
                    + rsa.maxPlaintextBytes() + " byte");
            lbl_status.setText("Đã tạo cặp khóa. Khóa công khai dùng để mã hóa, khóa riêng để giải mã.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không tạo được khóa: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_encryptActionPerformed() {
        String plaintext = txt_plaintext.getText();
        if (plaintext.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Plaintext trước khi mã hóa.");
            return;
        }
        try {
            String ciphertext = rsa.encrypt(plaintext);
            txt_ciphertext.setText(ciphertext);
            txt_ciphertext.setCaretPosition(0);
            lbl_status.setText("Đã mã hóa RSA (" + ciphertext.length() + " ký tự Base64). "
                    + HistoryDao.tryInsert("RSA", "ENCRYPT", plaintext.length(), true));
        } catch (Exception ex) {
            HistoryDao.tryInsert("RSA", "ENCRYPT", plaintext.length(), false);
            lbl_status.setText("Mã hóa RSA thất bại: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Mã hóa thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_decryptActionPerformed() {
        String ciphertext = txt_ciphertext.getText();
        if (ciphertext.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy nhập Ciphertext trước khi giải mã.");
            return;
        }
        try {
            String plaintext = rsa.decrypt(ciphertext);
            txt_plaintext.setText(plaintext);
            txt_plaintext.setCaretPosition(0);
            lbl_status.setText("Đã giải mã RSA. "
                    + HistoryDao.tryInsert("RSA", "DECRYPT", ciphertext.trim().length(), true));
        } catch (Exception ex) {
            HistoryDao.tryInsert("RSA", "DECRYPT", ciphertext.trim().length(), false);
            lbl_status.setText("Giải mã RSA THẤT BẠI (sai khóa hoặc bản mã bị sửa).");
            JOptionPane.showMessageDialog(this,
                    "Giải mã thất bại: sai khóa riêng hoặc bản mã bị sửa.\n" + ex.getMessage(),
                    "Giải mã thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_RSA().setVisible(true));
    }
}
