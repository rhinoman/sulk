package sulk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Singleton object for managing signing keys
 */
public class KeyManager {

    private static KeyManager km = null;

    private PrivateKey privateKey = null;

    private PublicKey publicKey = null;

    public static KeyManager getInstance(){
        if(km == null) {
            km = new KeyManager();
        }
        return km;
    }

    /**
     * Private constructor
     */
    private KeyManager(){}

    /**
     * Signs a message using the private key
     * @param plaintext the message to be signed
     * @return the signature in Base64
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public String sign(String plaintext)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{

        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(this.privateKey);
        privateSignature.update(plaintext.getBytes(UTF_8));

        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    /**
     * Verifies a signed message
     * @param plaintext The message to be verified
     * @param signature The signature in Base64
     * @return true if message is valid, false if not
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     */
    public boolean verify(String plaintext, String signature)
        throws NoSuchAlgorithmException, InvalidKeyException, SignatureException{

        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(this.publicKey);
        publicSignature.update(plaintext.getBytes(UTF_8));

        byte[] signatureBytes = Base64.getDecoder().decode(signature);

        return publicSignature.verify(signatureBytes);
    }

    /**
     * Reads Private key from file
     * @param filename the file containing the private key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPrivateKey(String filename)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = kf.generatePrivate(spec);
    }

    /**
     * Reads public key from file
     * @param filename the file containing the public key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPublicKey(String filename)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);
    }




}
