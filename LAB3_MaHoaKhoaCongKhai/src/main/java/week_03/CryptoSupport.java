package week_03;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Hàm tiện ích dùng chung cho Lab 3: Base64, UTF-8, số ngẫu nhiên,
 * AES-256-GCM, HKDF-SHA256 và đọc/ghi tệp khóa có kiểm tra lỗi chặt chẽ.
 */
public final class CryptoSupport {

    private static final SecureRandom RANDOM = new SecureRandom();
    public static final int AES_KEY_BYTES = 32;
    public static final int GCM_NONCE_BYTES = 12;
    public static final int GCM_TAG_BYTES = 16;

    private CryptoSupport() {
    }

    public static byte[] randomBytes(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Số byte ngẫu nhiên không hợp lệ: " + n);
        }
        byte[] b = new byte[n];
        RANDOM.nextBytes(b);
        return b;
    }

    public static String b64(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Dữ liệu mã hóa Base64 không được null.");
        }
        return Base64.getEncoder().encodeToString(data);
    }

    /** Base64 -> byte (bỏ qua khoảng trắng, xuống dòng; chặn chuỗi rỗng/lỗi). */
    public static byte[] unb64(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Dữ liệu Base64 không được để trống.");
        }
        String cleaned = stripBom(text).replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Dữ liệu Base64 không được để trống.");
        }
        try {
            return Base64.getDecoder().decode(cleaned);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Dữ liệu không phải Base64 hợp lệ.", ex);
        }
    }

    public static byte[] utf8(String s) {
        if (s == null) {
            throw new IllegalArgumentException("Chuỗi đầu vào không được null.");
        }
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String text(byte[] b) {
        if (b == null) {
            throw new IllegalArgumentException("Mảng byte đầu vào không được null.");
        }
        return new String(b, StandardCharsets.UTF_8);
    }

    /** Loại bỏ ký tự UTF-8 BOM nếu tệp được lưu từ Windows Notepad. */
    public static String stripBom(String s) {
        if (s != null && !s.isEmpty() && s.charAt(0) == '\uFEFF') {
            return s.substring(1);
        }
        return s;
    }

    /** Ghép các mảng byte nối tiếp nhau. */
    public static byte[] concat(byte[]... parts) {
        if (parts == null) {
            throw new IllegalArgumentException("Danh sách mảng byte cần ghép không được null.");
        }
        int total = 0;
        for (byte[] p : parts) {
            if (p != null) {
                total += p.length;
            }
        }
        byte[] out = new byte[total];
        int pos = 0;
        for (byte[] p : parts) {
            if (p != null) {
                System.arraycopy(p, 0, out, pos, p.length);
                pos += p.length;
            }
        }
        return out;
    }

    /** Cắt đoạn [from, to) của mảng, kiểm tra biên chặt chẽ để báo lỗi rõ ràng. */
    public static byte[] slice(byte[] data, int from, int to) {
        if (data == null || from < 0 || to > data.length || from > to) {
            throw new IllegalArgumentException("Bản mã quá ngắn hoặc bị hỏng cấu trúc.");
        }
        return Arrays.copyOfRange(data, from, to);
    }

    /** AES-256-GCM mã hóa (khóa 32 byte, nonce 12 byte). Kết quả kèm tag 16 byte chống sửa. */
    public static byte[] gcmEncrypt(byte[] key, byte[] nonce, byte[] plain) throws Exception {
        validateGcmParams(key, nonce);
        if (plain == null) {
            throw new IllegalArgumentException("Dữ liệu cần mã hóa AES-GCM không được null.");
        }
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        return c.doFinal(plain);
    }

    /** AES-256-GCM giải mã; sai khóa hoặc bản mã bị sửa -> ném AEADBadTagException. */
    public static byte[] gcmDecrypt(byte[] key, byte[] nonce, byte[] data) throws Exception {
        validateGcmParams(key, nonce);
        if (data == null || data.length < GCM_TAG_BYTES) {
            throw new IllegalArgumentException("Phần dữ liệu AES-GCM quá ngắn (thiếu thẻ xác thực 16 byte).");
        }
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
        return c.doFinal(data);
    }

    private static void validateGcmParams(byte[] key, byte[] nonce) {
        if (key == null || key.length != AES_KEY_BYTES) {
            throw new IllegalArgumentException("Khóa AES-256 phải có đúng 32 byte.");
        }
        if (nonce == null || nonce.length != GCM_NONCE_BYTES) {
            throw new IllegalArgumentException("Nonce AES-GCM phải có đúng 12 byte.");
        }
    }

    /** HKDF-SHA256 (RFC 5869): biến bí mật chung ECDH thành khóa AES 32 byte. */
    public static byte[] hkdf(byte[] secret, byte[] salt, byte[] info) throws Exception {
        if (secret == null || secret.length == 0) {
            throw new IllegalArgumentException("Bí mật chung ECDH không hợp lệ.");
        }
        if (salt == null || salt.length == 0) {
            throw new IllegalArgumentException("Salt HKDF không hợp lệ.");
        }
        if (info == null) {
            throw new IllegalArgumentException("Info HKDF không được null.");
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(salt, "HmacSHA256"));
        byte[] prk = mac.doFinal(secret);

        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        mac.update(info);
        mac.update((byte) 1);
        return Arrays.copyOf(mac.doFinal(), AES_KEY_BYTES);
    }

    /** Nội dung tệp khóa: dòng 1 PUBLIC=..., dòng 2 PRIVATE=... (Base64). */
    public static String toKeyFile(String publicB64, String privateB64) {
        if (publicB64 == null || publicB64.isBlank()) {
            throw new IllegalArgumentException("Chưa có khóa công khai để xuất tệp.");
        }
        String pri = (privateB64 == null) ? "" : privateB64.trim();
        return "PUBLIC=" + publicB64.trim() + "\n" + "PRIVATE=" + pri + "\n";
    }

    /**
     * Đọc tệp khóa -> {public, private}; private có thể rỗng nếu tệp chỉ chứa khóa công khai.
     * Hỗ trợ xử lý UTF-8 BOM và kiểm tra hợp lệ Base64 ngay khi đọc.
     */
    public static String[] fromKeyFile(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Tệp khóa đang trống.");
        }
        String cleaned = stripBom(content);
        String pub = null;
        String pri = "";
        for (String rawLine : cleaned.split("\\R")) {
            String line = stripBom(rawLine).trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            if (line.startsWith("PUBLIC=")) {
                pub = line.substring(7).trim();
            } else if (line.startsWith("PRIVATE=")) {
                pri = line.substring(8).trim();
            }
        }
        if (pub == null || pub.isBlank()) {
            throw new IllegalArgumentException("Tệp khóa phải có dòng PUBLIC=... hợp lệ.");
        }
        // Kiểm tra định dạng Base64 ngay để báo lỗi rõ ràng
        unb64(pub);
        if (!pri.isBlank()) {
            unb64(pri);
        }
        return new String[] {pub, pri};
    }
}
