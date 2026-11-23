package week_03;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Chữ ký số ECDSA (SHA256withECDSA, đường cong secp256r1):
 * KÝ bằng khóa riêng, XÁC MINH bằng khóa công khai.
 */
public final class ECCSignature {

    private PublicKey publicKey;
    private PrivateKey privateKey;

    public void generateKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("EC");
        gen.initialize(new ECGenParameterSpec("secp256r1"));
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

    public String sign(String message) throws Exception {
        if (privateKey == null) {
            throw new IllegalStateException("Chưa có khóa riêng: bấm Generate Keys hoặc Load Keys.");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Thông điệp cần ký không được để trống.");
        }
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initSign(privateKey);
        s.update(CryptoSupport.utf8(message));
        return CryptoSupport.b64(s.sign());
    }

    /**
     * true = VALID: chữ ký đúng với thông điệp và khóa công khai.
     * Chữ ký sai định dạng Base64 hoặc sai cấu trúc DER đều trả về false (INVALID).
     */
    public boolean verify(String message, String signatureB64) throws Exception {
        if (publicKey == null) {
            throw new IllegalStateException("Chưa có khóa công khai.");
        }
        if (message == null || message.isEmpty()) {
            throw new IllegalArgumentException("Thông điệp cần xác minh không được để trống.");
        }
        if (signatureB64 == null || signatureB64.isBlank()) {
            throw new IllegalArgumentException("Chữ ký cần xác minh không được để trống.");
        }
        byte[] sigBytes;
        try {
            sigBytes = CryptoSupport.unb64(signatureB64);
        } catch (IllegalArgumentException ex) {
            return false; // chuỗi Base64 bị sửa hỏng cũng là chữ ký INVALID
        }
        Signature s = Signature.getInstance("SHA256withECDSA");
        s.initVerify(publicKey);
        s.update(CryptoSupport.utf8(message));
        try {
            return s.verify(sigBytes);
        } catch (SignatureException ex) {
            return false; // chữ ký sai định dạng DER cũng là INVALID
        }
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
                throw new IllegalArgumentException("Khóa công khai phải thuộc đường cong secp256r1 (256 bit).");
            }
            PrivateKey pri = (privateB64 == null || privateB64.isBlank())
                    ? null
                    : kf.generatePrivate(new PKCS8EncodedKeySpec(CryptoSupport.unb64(privateB64)));
            if (pri != null && (!(pri instanceof ECPrivateKey ecPri)
                    || ecPri.getParams().getCurve().getField().getFieldSize() != 256)) {
                throw new IllegalArgumentException("Khóa riêng phải thuộc đường cong secp256r1 (256 bit).");
            }
            this.publicKey = pub;
            this.privateKey = pri;
        } catch (InvalidKeySpecException ex) {
            throw new IllegalArgumentException(
                    "Tệp khóa không phải định dạng ECDSA (secp256r1) hợp lệ (có thể chọn nhầm tệp khóa RSA).", ex);
        }
    }
}
