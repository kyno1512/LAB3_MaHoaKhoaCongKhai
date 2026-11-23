package week_03;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Mở và lưu tệp văn bản .txt (UTF-8) cho các JFrame Form.
 * Các hàm trả về đường dẫn tệp nếu thành công, trả về null nếu người dùng bấm Cancel hoặc bị lỗi.
 */
public final class FileTextIO {

    private FileTextIO() {
    }

    /** Hiện hộp thoại Open, đọc toàn bộ tệp và đưa nội dung vào ô target. */
    public static String openText(JFrame parent, JTextArea target) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        try {
            String text = CryptoSupport.stripBom(Files.readString(file.toPath(), StandardCharsets.UTF_8)).trim();
            target.setText(text);
            target.setCaretPosition(0);
            return file.getAbsolutePath();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không đọc được tệp: " + ex.getMessage(),
                    "Lỗi mở tệp", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Hiện hộp thoại Save, ghi nội dung ô source ra tệp (tự thêm đuôi .txt nếu thiếu). */
    public static String saveText(JFrame parent, JTextArea source, String defaultName) {
        if (source == null || source.getText().isBlank()) {
            JOptionPane.showMessageDialog(parent, "Chưa có nội dung để lưu.");
            return null;
        }
        return saveString(parent, source.getText(), defaultName);
    }

    /** Hộp thoại Open chung: trả về nội dung tệp (UTF-8, đã bỏ BOM) hoặc null nếu Cancel/lỗi. */
    public static String openString(JFrame parent) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.exists() || !file.isFile()) {
            JOptionPane.showMessageDialog(parent, "Tệp không tồn tại: " + file.getAbsolutePath(),
                    "Lỗi mở tệp", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            return CryptoSupport.stripBom(Files.readString(file.toPath(), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không đọc được tệp: " + ex.getMessage(),
                    "Lỗi mở tệp", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /** Hộp thoại Save chung: ghi chuỗi content ra tệp, hỏi xác nhận nếu tệp đã tồn tại. */
    public static String saveString(JFrame parent, String content, String defaultName) {
        if (content == null || content.isBlank()) {
            JOptionPane.showMessageDialog(parent, "Chưa có nội dung để lưu.");
            return null;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Text file (*.txt)", "txt"));
        chooser.setSelectedFile(new File(defaultName));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File file = chooser.getSelectedFile();
        if (!file.getName().toLowerCase().endsWith(".txt")) {
            file = new File(file.getParentFile(), file.getName() + ".txt");
        }
        if (file.exists()) {
            int confirm = JOptionPane.showConfirmDialog(parent,
                    "Tệp đã tồn tại:\n" + file.getAbsolutePath() + "\nBạn có muốn ghi đè không?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm != JOptionPane.YES_OPTION) {
                return null;
            }
        }
        try {
            Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
            return file.getAbsolutePath();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(parent, "Không lưu được tệp: " + ex.getMessage(),
                    "Lỗi lưu tệp", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}
