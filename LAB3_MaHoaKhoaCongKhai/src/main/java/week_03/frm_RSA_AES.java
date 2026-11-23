package week_03;

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
 * Bài 3.4 - Giao diện Swing cho hệ mã lai RSA + AES-256-GCM (văn bản dài).
 */
public class frm_RSA_AES extends JFrame {

    private final RSA_AES_Cipher hybrid = new RSA_AES_Cipher();

    private JTextArea txt_public;
    private JTextArea txt_private;
    private JButton btn_generate;
    private JButton btn_savekeys;
    private JButton btn_loadkeys;
    private JTextArea txt_plaintext;
    private JTextArea txt_ciphertext;
    private JButton btn_encrypt;
    private JButton btn_decrypt;
    private JButton btn_tamper;
    private JLabel lbl_status;

    public frm_RSA_AES() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "RSA + AES Cipher", DISPOSE_ON_CLOSE);
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
        root.add(FormHelper.titleLabel("HỆ MÃ LAI RSA + AES-GCM (VĂN BẢN DÀI)", false), g);

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

        // Plaintext & Ciphertext (cao hơn cho văn bản > 1.000 ký tự)
        txt_plaintext = FormHelper.createTextArea(4, true, false);
        addAreaRow(root, g, 4, "Plaintext:", txt_plaintext, 96, 0.5);

        txt_ciphertext = FormHelper.createTextArea(4, true, true);
        addAreaRow(root, g, 5, "Ciphertext:", txt_ciphertext, 96, 0.5);

        // Nút Encrypt / Decrypt / Sửa 1 ký tự bản mã
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btn_encrypt = FormHelper.createButton("Encrypt", 110, 30);
        btn_decrypt = FormHelper.createButton("Decrypt", 110, 30);
        btn_tamper = FormHelper.createButton("Sửa 1 ký tự bản mã", 180, 30);
        btn_encrypt.addActionListener(e -> btn_encryptActionPerformed());
        btn_decrypt.addActionListener(e -> btn_decryptActionPerformed());
        btn_tamper.addActionListener(e -> btn_tamperActionPerformed());
        actionButtons.add(btn_encrypt);
        actionButtons.add(btn_decrypt);
        actionButtons.add(btn_tamper);

        g.gridx = 1;
        g.gridy = 6;
        g.weighty = 0.0;
        g.fill = GridBagConstraints.HORIZONTAL;
        root.add(actionButtons, g);

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
        txt_public.setText(hybrid.getPublicKeyBase64());
        txt_private.setText(hybrid.getPrivateKeyBase64());
        txt_public.setCaretPosition(0);
        txt_private.setCaretPosition(0);
    }

    private void btn_generateActionPerformed() {
        try {
            hybrid.generateKeys();
            showKeys();
            lbl_status.setText("Đã tạo cặp khóa mới. Bản mã cũ sẽ không giải mã được với khóa mới.");
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
        String path = FileTextIO.saveString(this, content, "rsa_aes_keys.txt");
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
            hybrid.loadKeys(keys[0], keys[1]);
            txt_public.setText(keys[0]);
            txt_private.setText(keys[1]);
            txt_public.setCaretPosition(0);
            txt_private.setCaretPosition(0);
            if (keys[1].isBlank()) {
                lbl_status.setText("Đã nạp khóa công khai từ tệp (không có khóa riêng - chỉ dùng để mã hóa).");
            } else {
                lbl_status.setText("Đã nạp cặp khóa RSA từ tệp.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Tệp khóa không hợp lệ: " + ex.getMessage(),
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
            String ciphertext = hybrid.encrypt(plaintext);
            txt_ciphertext.setText(ciphertext);
            txt_ciphertext.setCaretPosition(0);
            lbl_status.setText("Đã mã hóa RSA-AES (" + plaintext.length() + " ký tự -> "
                    + ciphertext.length() + " ký tự Base64). "
                    + HistoryDao.tryInsert("RSA-AES", "ENCRYPT", plaintext.length(), true));
        } catch (Exception ex) {
            HistoryDao.tryInsert("RSA-AES", "ENCRYPT", plaintext.length(), false);
            lbl_status.setText("Mã hóa RSA-AES thất bại: " + ex.getMessage());
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
            String plaintext = hybrid.decrypt(ciphertext);
            txt_plaintext.setText(plaintext);
            txt_plaintext.setCaretPosition(0);
            lbl_status.setText("Đã giải mã RSA-AES. "
                    + HistoryDao.tryInsert("RSA-AES", "DECRYPT", ciphertext.trim().length(), true));
        } catch (Exception ex) {
            HistoryDao.tryInsert("RSA-AES", "DECRYPT", ciphertext.trim().length(), false);
            lbl_status.setText("Giải mã RSA-AES THẤT BẠI (sai khóa hoặc bản mã bị sửa).");
            JOptionPane.showMessageDialog(this,
                    "Giải mã thất bại: sai khóa riêng hoặc bản mã bị sửa.\n" + ex,
                    "Giải mã thất bại", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void btn_tamperActionPerformed() {
        String c = txt_ciphertext.getText();
        if (c.isBlank()) {
            JOptionPane.showMessageDialog(this, "Hãy Encrypt trước khi sửa bản mã.");
            return;
        }
        try {
            String tampered = hybrid.tamperCiphertext(c);
            txt_ciphertext.setText(tampered);
            txt_ciphertext.setCaretPosition(0);
            lbl_status.setText("Đã lật 1 bit trong phần dữ liệu AES của bản mã. Bấm Decrypt để xem AES-GCM phát hiện.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Không thể sửa bản mã: " + ex.getMessage(),
                    "Bản mã chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_RSA_AES().setVisible(true));
    }
}
