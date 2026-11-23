# LAB 3 – MÃ HÓA KHÓA CÔNG KHAI

Học phần **An toàn và Bảo mật Thông tin** – Triển khai bằng **Java (Maven, Swing, JDK 17+, Apache NetBeans 24)** kết hợp **Microsoft SQL Server**.

## Thông tin sinh viên
- **Họ và tên sinh viên:** Ngô Thành Hữu
- **Mã số sinh viên (MSSV):** 1150070015
- **Lớp:** 11-TMĐT
- **Link Video Demo YouTube:** https://youtu.be/7l3WS8gFyoM

## Cấu trúc thư mục
- `03_SQL_SERVER/`: Script khởi tạo cơ sở dữ liệu (`01_Tao_CSDL.sql`), tạo tài khoản `lab3_user` (`02_Tao_TaiKhoan_LAB.sql`) và truy vấn thống kê lịch sử (`03_KiemTra_LichSu.sql`).
- `LAB3_MaHoaKhoaCongKhai/`: Project Maven (package `week_03`) gồm:
  - Bài 3.1: `RSADemo` (Console RSA-OAEP SHA-256 2048/3072 bit)
  - Bài 3.2: `frm_RSA` (Swing RSA)
  - Bài 3.3: `frm_ECC` (ECC secp256r1 / ECIES: ECDH + HKDF-SHA256 + AES-256-GCM)
  - Bài 3.4: `frm_RSA_AES` (Hệ mã lai RSA + AES-256-GCM cho văn bản dài)
  - Bài 3.5: `frm_ECC_Signature` (Chữ ký số ECDSA `SHA256withECDSA`)
  - Tích hợp hệ thống: `frm_Menu`, `frm_KetNoiSQL`, `frm_LichSu`
- `BAO_CAO_LAB3_NgoThanhHuu.docx`: Báo cáo thực hành kèm minh chứng kiểm thử.

## Hướng dẫn chạy nhanh
1. Trong **SSMS**, chạy `03_SQL_SERVER/01_Tao_CSDL.sql` và `03_SQL_SERVER/02_Tao_TaiKhoan_LAB.sql` (SQL Server bật TCP/IP cổng `14333` và Mixed Mode).
2. Mở thư mục `LAB3_MaHoaKhoaCongKhai` bằng **Apache NetBeans 24** → **Clean and Build (Shift+F11)**.
3. Chạy Bài 3.1: nhấp phải `RSADemo.java` → **Run File (Shift+F6)**.
4. Chạy chương trình chính: bấm **Run Project (F6)** (`week_03.frm_Menu`) → vào **Kết nối SQL Server** trước khi thực hiện các bài để ghi lịch sử vào `dbo.History`.

## Lưu ý bảo mật
- Mật khẩu SQL Server chỉ nhập trên giao diện khi chạy, không lưu trong mã nguồn.
- `.gitignore` đã cấu hình bỏ qua các tệp khóa (`*_keys.txt`, `*.key`, `*.pem`) và thư mục `target/`.