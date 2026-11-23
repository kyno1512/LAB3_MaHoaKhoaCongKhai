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
 * Hệ mã lai RSA + AES: AES-256-GCM mã hóa văn bản dài (nhanh), RSA-OAEP chỉ mã hóa KHÓA AES 32 byte.
 * Bản mã = Base64( khóa AES đã bọc RSA || nonce 12 || dữ liệu AES-GCM ).
 */
public final class RSA_AES_Cipher {

    private static final String RSA = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
    private static final OAEPParameterSpec OAEP = new OAEPParameterSpec(
            "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
    private static final int DEFAULT_WRAPPED = 256; // RSA-2048 -> 256 byte
    private static final int NONCE_LEN = 12;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public void generateKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair p = gen.generateKeyPair();
        publicKey = p.getPublic();
        privateKey = p.getPrivate();
    }

    public boolean hasPublicKey() {
        return publicKey != null;
    }

    public boolean hasPrivateKey() {
        return privateKey != null;
    }

    private int wrappedKeyLength() {
        if (publicKey instanceof RSAPublicKey rsaPub) {
            return rsaPub.getModulus().bitLength() / 8;
        }
        if (privateKey instanceof RSAPrivateKey rsaPri) {
            return rsaPri.getModulus().bitLength() / 8;
        }
        return DEFAULT_WRAPPED;
    }

    public String encrypt(String plaintext) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chưa có khóa: bấm Generate Keys hoặc Load Keys.");
        }
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Bản rõ không được để trống.");
        }
        byte[] aesKey = CryptoSupport.randomBytes(CryptoSupport.AES_KEY_BYTES); // 1) khóa phiên AES ngẫu nhiên
        byte[] nonce = CryptoSupport.randomBytes(NONCE_LEN);
        byte[] data = CryptoSupport.gcmEncrypt(aesKey, nonce, CryptoSupport.utf8(plaintext)); // 2) AES mã hóa dữ liệu

        Cipher rsa = Cipher.getInstance(RSA);
        rsa.init(Cipher.ENCRYPT_MODE, publicKey, OAEP);
        byte[] wrapped = rsa.doFinal(aesKey); // 3) RSA mã hóa khóa AES
        return CryptoSupport.b64(CryptoSupport.concat(wrapped, nonce, data));
    }

    public String decrypt(String ciphertext) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chưa có khóa riêng.");
        }
        int wrappedLen = wrappedKeyLength();
        int minBytes = wrappedLen + NONCE_LEN + CryptoSupport.GCM_TAG_BYTES;
        byte[] all = CryptoSupport.unb64(ciphertext);
        if (all.length < minBytes) {
            throw new IllegalArgumentException("Bản mã RSA-AES quá ngắn hoặc bị hỏng (tối thiểu "
                    + minBytes + " byte, nhận " + all.length + " byte).");
        }
        byte[] wrapped = CryptoSupport.slice(all, 0, wrappedLen);
        byte[] nonce = CryptoSupport.slice(all, wrappedLen, wrappedLen + NONCE_LEN);
        byte[] data = CryptoSupport.slice(all, wrappedLen + NONCE_LEN, all.length);

        Cipher rsa = Cipher.getInstance(RSA);
        rsa.init(Cipher.DECRYPT_MODE, privateKey, OAEP);
        byte[] aesKey = rsa.doFinal(wrapped); // mở khóa AES bằng khóa riêng RSA
        return CryptoSupport.text(CryptoSupport.gcmDecrypt(aesKey, nonce, data));
    }

    /**
     * Lật 1 bit trong phần dữ liệu AES-GCM của bản mã (giữ nguyên định dạng Base64 hợp lệ).
     * Hoạt động chính xác cho cả bản rõ ngắn lẫn văn bản dài > 1.000 ký tự.
     */
    public String tamperCiphertext(String ciphertext) {
        int wrappedLen = wrappedKeyLength();
        int dataStart = wrappedLen + NONCE_LEN;
        int minBytes = dataStart + CryptoSupport.GCM_TAG_BYTES;
        byte[] all = CryptoSupport.unb64(ciphertext);
        if (all.length < minBytes) {
            throw new IllegalArgumentException("Bản mã chưa hợp lệ. Hãy bấm Encrypt trước.");
        }
        int targetByte = dataStart + (all.length - dataStart) / 2;
        all[targetByte] ^= 0x01;
        return CryptoSupport.b64(all);
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
            if (keyBits < 2048) {
                throw new IllegalArgumentException("Khóa RSA phải có kích thước tối thiểu 2048 bit.");
            }
            PrivateKey pri = (privateB64 == null || privateB64.isBlank())
                    ? null
                    : kf.generatePrivate(new PKCS8EncodedKeySpec(CryptoSupport.unb64(privateB64)));
            if (pri != null && !((RSAPublicKey) pub).getModulus().equals(((RSAPrivateKey) pri).getModulus())) {
                throw new IllegalArgumentException("Khóa công khai và khóa riêng trong tệp không khớp nhau.");
            }
            this.publicKey = pub;
            this.privateKey = pri;
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException(
                    "Tệp khóa không phải định dạng RSA hợp lệ (có thể chọn nhầm tệp khóa ECC).", ex);
        }
    }
}
