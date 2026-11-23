package week_03;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * RSA với đệm OAEP (SHA-256). Mã hóa bằng KHÓA CÔNG KHAI, giải mã bằng KHÓA RIÊNG.
 * RSA chỉ mã hóa được thông điệp ngắn: tối đa 190 byte (khóa 2048 bit) hoặc 318 byte (3072 bit).
 */
public final class RSACipher {

    private static final String TRANSFORMATION = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private int bits;

    /** Tạo cặp khóa mới 2048 hoặc 3072 bit. */
    public void generateKeys(int keyBits) throws Exception {
        if (keyBits != 2048 && keyBits != 3072) {
            throw new IllegalArgumentException("Chỉ chọn kích thước khóa 2048 hoặc 3072 bit.");
        }
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(keyBits);
        KeyPair pair = gen.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();
        bits = keyBits;
    }

    public boolean hasKeys() {
        return publicKey != null && privateKey != null;
    }

    public boolean hasPublicKey() {
        return publicKey != null;
    }

    public boolean hasPrivateKey() {
        return privateKey != null;
    }

    public int getBits() {
        return bits;
    }

    /** Số byte tối đa mã hóa được với OAEP-SHA256: k - 2*32 - 2. */
    public int maxPlaintextBytes() {
        return bits <= 0 ? 0 : (bits / 8 - 66);
    }

    public String encrypt(String plaintext) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chưa có khóa: bấm Generate Keys trước.");
        }
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Bản rõ không được để trống.");
        }
        byte[] data = CryptoSupport.utf8(plaintext);
        if (data.length > maxPlaintextBytes()) {
            throw new IllegalArgumentException("Bản rõ (" + plaintext.length() + " ký tự, "
                    + data.length + " byte UTF-8) vượt giới hạn " + maxPlaintextBytes()
                    + " byte của RSA-" + bits + ". Văn bản dài hãy dùng RSA + AES (bài 3.4).");
        }
        Cipher c = Cipher.getInstance(TRANSFORMATION);
        c.init(Cipher.ENCRYPT_MODE, publicKey, OAEP);
        return CryptoSupport.b64(c.doFinal(data));
    }

    public String decrypt(String ciphertext) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chưa có khóa riêng.");
        }
        byte[] raw = CryptoSupport.unb64(ciphertext);
        int expectedBytes = bits / 8;
        if (expectedBytes > 0 && raw.length != expectedBytes) {
            throw new IllegalArgumentException("Độ dài bản mã không khớp khóa RSA-" + bits
                    + " (cần " + expectedBytes + " byte, nhận " + raw.length + " byte).");
        }
        Cipher c = Cipher.getInstance(TRANSFORMATION);
        c.init(Cipher.DECRYPT_MODE, privateKey, OAEP);
        return CryptoSupport.text(c.doFinal(raw));
    }

    public String getPublicKeyBase64() {
        if (publicKey == null) {
            throw new IllegalStateException("Chưa có khóa công khai.");
        }
        return CryptoSupport.b64(publicKey.getEncoded());
    }

    public String getPrivateKeyBase64() {
        if (privateKey == null) {
            return "";
        }
        return CryptoSupport.b64(privateKey.getEncoded());
    }

    public void loadKeys(String publicB64, String privateB64) throws Exception {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(CryptoSupport.unb64(publicB64)));
            int keyBits = ((RSAPublicKey) pub).getModulus().bitLength();
            if (keyBits != 2048 && keyBits != 3072) {
                throw new IllegalArgumentException("Chỉ hỗ trợ khóa RSA 2048 hoặc 3072 bit (tệp có " + keyBits + " bit).");
            }
            PrivateKey pri = (privateB64 == null || privateB64.isBlank())
                    ? null
                    : kf.generatePrivate(new PKCS8EncodedKeySpec(CryptoSupport.unb64(privateB64)));
            if (pri != null && !((RSAPublicKey) pub).getModulus().equals(((RSAPrivateKey) pri).getModulus())) {
                throw new IllegalArgumentException("Khóa công khai và khóa riêng trong tệp không thuộc cùng một cặp khóa RSA.");
            }
            this.publicKey = pub;
            this.privateKey = pri;
            this.bits = keyBits;
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException("Tệp khóa không phải định dạng RSA hợp lệ.", ex);
        }
    }
}
