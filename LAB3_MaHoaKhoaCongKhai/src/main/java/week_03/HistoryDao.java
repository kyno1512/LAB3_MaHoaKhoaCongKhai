package week_03;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

/**
 * Ghi / đọc bảng dbo.History (LAB3_MaHoaKhoaCongKhai).
 * Chỉ lưu thuật toán, thao tác, độ dài dữ liệu và kết quả, không lưu khóa hay bản rõ.
 */
public final class HistoryDao {

    private static final Set<String> VALID_OPERATIONS = Set.of("ENCRYPT", "DECRYPT", "SIGN", "VERIFY");

    private HistoryDao() {
    }

    /** operation: ENCRYPT, DECRYPT, SIGN hoặc VERIFY (khớp ràng buộc CHECK trong SQL). */
    public static void insert(String algorithm, String operation, int inputLength, boolean success)
            throws SQLException {
        if (algorithm == null || algorithm.isBlank() || algorithm.length() > 30) {
            throw new IllegalArgumentException("Tên thuật toán không hợp lệ (tối đa 30 ký tự).");
        }
        if (operation == null || !VALID_OPERATIONS.contains(operation)) {
            throw new IllegalArgumentException("Thao tác phải là ENCRYPT, DECRYPT, SIGN hoặc VERIFY.");
        }
        if (inputLength < 0) {
            throw new IllegalArgumentException("Độ dài đầu vào không được âm.");
        }
        String sql = "INSERT INTO dbo.History(Algorithm, Operation, InputLength, Success) VALUES (?, ?, ?, ?)";
        try (Connection con = DbConfig.open(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, algorithm);
            ps.setString(2, operation);
            ps.setInt(3, inputLength);
            ps.setBoolean(4, success);
            ps.executeUpdate();
        }
    }

    /** Ghi lịch sử an toàn: không làm gián đoạn thao tác mã hóa nếu chưa kết nối SQL Server. */
    public static String tryInsert(String algorithm, String operation, int inputLength, boolean success) {
        if (!DbConfig.isReady()) {
            return "(Chưa kết nối SQL Server - chưa lưu lịch sử)";
        }
        try {
            insert(algorithm, operation, Math.max(0, inputLength), success);
            return "(Đã lưu lịch sử SQL Server)";
        } catch (Exception ex) {
            return "(Lỗi lưu lịch sử: " + ex.getMessage() + ")";
        }
    }

    public static DefaultTableModel emptyModel() {
        return new DefaultTableModel(
                new Object[] {"Id", "Thời gian", "Thuật toán", "Thao tác", "Độ dài vào", "Thành công"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static DefaultTableModel loadLatest(int top) throws SQLException {
        if (top <= 0) {
            throw new IllegalArgumentException("Số dòng cần tải phải lớn hơn 0.");
        }
        DefaultTableModel model = emptyModel();
        String sql = "SELECT TOP (?) Id, CreatedAt, Algorithm, Operation, InputLength, Success "
                + "FROM dbo.History ORDER BY Id DESC";
        try (Connection con = DbConfig.open(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, top);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[] {
                        rs.getLong(1),
                        rs.getTimestamp(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getInt(5),
                        rs.getBoolean(6) ? "Có" : "Không"
                    });
                }
            }
        }
        return model;
    }
}
