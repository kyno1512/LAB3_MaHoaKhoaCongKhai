package week_03;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Form hiển thị 50 bản ghi lịch sử mới nhất từ bảng dbo.History (SQL Server).
 */
public class frm_LichSu extends JFrame {

    private JTable tbl_history;
    private JButton btn_refresh;
    private JLabel lbl_status;

    public frm_LichSu() {
        FormHelper.installSystemLookAndFeel();
        initComponents();
        FormHelper.finalizeFrame(this, "Lịch sử thao tác (SQL Server)", DISPOSE_ON_CLOSE);
        tbl_history.setModel(HistoryDao.emptyModel());
        loadData();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JLabel title = FormHelper.titleLabel("LỊCH SỬ THAO TÁC - BẢNG DBO.HISTORY", false);
        root.add(title, BorderLayout.NORTH);

        tbl_history = new JTable();
        tbl_history.setFont(FormHelper.FONT_REGULAR);
        tbl_history.setRowHeight(22);
        tbl_history.getTableHeader().setFont(FormHelper.FONT_REGULAR);
        JScrollPane scroll = new JScrollPane(tbl_history);
        scroll.setPreferredSize(new Dimension(760, 280));
        root.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btn_refresh = FormHelper.createButton("Tải lại", 110, 30);
        btn_refresh.addActionListener(e -> btn_refreshActionPerformed());
        btnWrap.add(btn_refresh);

        lbl_status = new JLabel("Đang tải...");
        lbl_status.setFont(FormHelper.FONT_REGULAR);
        lbl_status.setPreferredSize(new Dimension(640, 26));

        bottom.add(btnWrap, BorderLayout.WEST);
        bottom.add(lbl_status, BorderLayout.CENTER);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    /** Đọc 50 dòng mới nhất từ SQL Server và hiện lên JTable; reset bảng nếu lỗi. */
    private void loadData() {
        try {
            tbl_history.setModel(HistoryDao.loadLatest(50));
            lbl_status.setText("Đã tải " + tbl_history.getRowCount() + " dòng mới nhất từ dbo.History.");
        } catch (Exception ex) {
            tbl_history.setModel(HistoryDao.emptyModel());
            lbl_status.setText("Không đọc được lịch sử: " + ex.getMessage());
        }
    }

    private void btn_refreshActionPerformed() {
        loadData();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> new frm_LichSu().setVisible(true));
    }
}
