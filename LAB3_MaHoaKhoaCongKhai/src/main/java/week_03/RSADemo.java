package week_03;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Bài 3.1 - RSA chạy trong cửa sổ Output (console).
 * Chạy: nhấp phải RSADemo.java -> Run File (Shift+F6).
 */
public final class RSADemo {

    public static void main(String[] args) {
        try (Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8)) {
            System.out.println("===== BAI 3.1 - RSA (OAEP, SHA-256) =====");

            int bits = readKeySize(sc);
            if (bits < 0) {
                return;
            }

            RSACipher rsa = new RSACipher();
            long t0 = System.nanoTime();
            rsa.generateKeys(bits);
            double elapsedMs = (System.nanoTime() - t0) / 1e6;

            String pubB64 = rsa.getPublicKeyBase64();
            System.out.printf("Da tao cap khoa RSA-%d trong %.0f ms%n", bits, elapsedMs);
            System.out.println("Khoa cong khai (Base64, " + pubB64.length() + " ky tu): "
                    + pubB64.substring(0, Math.min(40, pubB64.length())) + "...");
            System.out.println("Gioi han ban ro: " + rsa.maxPlaintextBytes() + " byte");

            System.out.print("Nhap ban ro: ");
            if (!sc.hasNextLine()) {
                System.out.println("Khong nhan duoc du lieu ban ro.");
                return;
            }
            String plain = sc.nextLine();
            if (plain.isBlank()) {
                System.out.println("Loi: Ban ro khong duoc de trong hoac chi gom khoang trang.");
                return;
            }

            String cipher = rsa.encrypt(plain);
            System.out.println("Ban ma (Base64, " + cipher.length() + " ky tu): " + cipher);

            String back = rsa.decrypt(cipher);
            System.out.println("Giai ma: " + back);
            System.out.println("Khoi phuc dung: " + plain.equals(back));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            System.out.println("Loi du lieu/thuat toan: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Loi thuc thi RSA: " + ex.getMessage());
        }
    }

    private static int readKeySize(Scanner sc) {
        while (true) {
            System.out.print("Nhap kich thuoc khoa (2048 hoac 3072): ");
            if (!sc.hasNextLine()) {
                return -1;
            }
            String line = sc.nextLine().trim();
            try {
                int bits = Integer.parseInt(line);
                if (bits == 2048 || bits == 3072) {
                    return bits;
                }
                System.out.println("Loi: Chi ho tro kich thuoc khoa 2048 hoac 3072 bit. Vui long nhap lai.");
            } catch (NumberFormatException ex) {
                System.out.println("Loi: Vui long nhap so nguyen (2048 hoac 3072).");
            }
        }
    }
}
