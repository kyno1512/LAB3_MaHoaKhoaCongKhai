package week_03;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.KeyAgreement;

/**
 * Mã hóa ECC kiểu ECIES: ECDH trên đường cong secp256r1 + HKDF-SHA256 + AES-256-GCM.
 * Mỗi lần mã hóa sinh một cặp khóa tạm (ephemeral); bí mật chung ECDH -> khóa AES.
 * Bản mã = Base64( khóa công khai tạm 91 byte || salt 16 || nonce 12 || dữ liệu AES-GCM ).
 */
public final class ECCCipher {

    private static final byte[] INFO = CryptoSupport.utf8("LAB3-ECC-AESGCM");
    private static final int EPH = 91; // độ dài khóa công khai secp256r1 dạng X.509
    private static final int SALT_LEN = 16;
    private static final int NONCE_LEN = 12;
    private static final int MIN_CIPHERTEXT_BYTES = EPH + SALT_LEN + NONCE_LEN + CryptoSupport.GCM_TAG_BYTES;

    private PublicKey publicKey;
    private PrivateKey privateKey;

    private static KeyPair newPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(new ECGenParameterSpec("secp256r1"));
        return gen.generateKeyPair();
    }

    public void generateKeys() throws Exception {
        KeyPair p = newPair();
        publicKey = p.getPublic();
        privateKey = p.getPrivate();
    }

    public boolean hasPublicKey() {
        return publicKey != null;
    }

    public boolean hasPrivateKey() {
        return privateKey != null;
    }

    public String encrypt(String plaintext) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chưa có khóa: bấm Generate Keys hoặc Load Keys.");
        }
        if (plaintext == null || plaintext.isEmpty()) {
            throw new IllegalArgumentException("Bản rõ không được để trống.");
        }
        KeyPair eph = newPair(); // 1) cặp khóa tạm
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(eph.getPrivate());
        ka.doPhase(publicKey, true); // 2) bí mật chung = d_tạm * Q_người_nhận

        byte[] salt = CryptoSupport.randomBytes(SALT_LEN);
        byte[] nonce = CryptoSupport.randomBytes(NONCE_LEN);
        byte[] aesKey = CryptoSupport.hkdf(ka.generateSecret(), salt, INFO); // 3) khóa AES
        byte[] data = CryptoSupport.gcmEncrypt(aesKey, nonce, CryptoSupport.utf8(plaintext));
        return CryptoSupport.b64(CryptoSupport.concat(eph.getPublic().getEncoded(), salt, nonce, data));
    }

    public String decrypt(String ciphertext) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chưa có khóa riêng.");
        }
        byte[] all = CryptoSupport.unb64(ciphertext);
        if (all.length < MIN_CIPHERTEXT_BYTES) {
            throw new IllegalArgumentException("Bản mã ECC quá ngắn hoặc bị hỏng (tối thiểu "
                    + MIN_CIPHERTEXT_BYTES + " byte, nhận " + all.length + " byte).");
        }
        PublicKey eph;
        try {
            eph = KeyFactory.getInstance("EC").generatePublic(
                    new X509EncodedKeySpec(CryptoSupport.slice(all, 0, EPH)));
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException("Phần khóa công khai tạm trong bản mã ECC không hợp lệ.", ex);
        }
        byte[] salt = CryptoSupport.slice(all, EPH, EPH + SALT_LEN);
        byte[] nonce = CryptoSupport.slice(all, EPH + SALT_LEN, EPH + SALT_LEN + NONCE_LEN);
        byte[] data = CryptoSupport.slice(all, EPH + SALT_LEN + NONCE_LEN, all.length);

        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(privateKey);
        ka.doPhase(eph, true); // cùng bí mật chung = d_người_nhận * Q_tạm

        byte[] aesKey = CryptoSupport.hkdf(ka.generateSecret(), salt, INFO);
        return CryptoSupport.text(CryptoSupport.gcmDecrypt(aesKey, nonce, data));
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
            KeyFactory kf = KeyFactory.getInstance("EC");
            PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(CryptoSupport.unb64(publicB64)));
            if (!(pub instanceof ECPublicKey ecPub)
                    || ecPub.getParams().getCurve().getField().getFieldSize() != 256) {
                throw new IllegalArgumentException("Khóa công khai phải thuộc đường cong ECC secp256r1 (256 bit).");
            }
            PrivateKey pri = (privateB64 == null || privateB64.isBlank())
                    ? null
                    : kf.generatePrivate(new PKCS8EncodedKeySpec(CryptoSupport.unb64(privateB64)));
            if (pri != null && (!(pri instanceof ECPrivateKey ecPri)
                    || ecPri.getParams().getCurve().getField().getFieldSize() != 256)) {
                throw new IllegalArgumentException("Khóa riêng phải thuộc đường cong ECC secp256r1 (256 bit).");
            }
            this.publicKey = pub;
            this.privateKey = pri;
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException(
                    "Tệp khóa không phải định dạng ECC (secp256r1) hợp lệ (có thể chọn nhầm tệp khóa RSA).", ex);
        }
    }
}
