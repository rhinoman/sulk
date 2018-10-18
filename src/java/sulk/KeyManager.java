package sulk;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * Parses private key from byte array
     * @param keyBytes the key bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public void readPrivateKey (byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException{

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.privateKey = kf.generatePrivate(spec);
    }

    /**
     * Parses public key from byte array
     * @param keyBytes the key bytes
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public void readPublicKey (byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException{

        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        this.publicKey = kf.generatePublic(spec);
    }

    /**
     * Reads Private key from file
     * @param path the path containing the private key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPrivateKey(Path path)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        readPrivateKey(Files.readAllBytes(path));
    }

    /**
     * Reads public key from file
     * @param path the path containing the public key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPublicKey(Path path)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        readPublicKey(Files.readAllBytes(path));
    }

    /**
     * Reads public key from file
     * @param filename the filename
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPublicKey(String filename)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        readPublicKey(Paths.get(filename));
    }

    /**
     * Reads private key from file
     * @param filename the filename
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPrivateKey(String filename)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        readPrivateKey(Paths.get(filename));

    }

    /**
     * Reads public key from url path
     * @param url the location of the public key
     */
    public void readPublicKey(URL url)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, URISyntaxException {
        readPublicKey(Paths.get(url.toURI()));
    }

    /**
     * Reads private key from url path
     * @param url the location of the private key
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     */
    public void readPrivtaeKey(URL url)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, URISyntaxException {
        readPrivateKey(Paths.get(url.toURI()));
    }

    /**
     * Reads public key from inputstream
     * @param is the input stream
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws URISyntaxException
     */
    public void readPublicKey(InputStream is)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, URISyntaxException {
        readPublicKey(IOUtils.toByteArray(is));
    }

    /**
     * Reads private key from input stream
     * @param is the input stream
     * @throws NoSuchAlgorithmException
     * @throws IOException
     * @throws InvalidKeySpecException
     * @throws URISyntaxException
     */
    public void readPrivateKey(InputStream is)
            throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, URISyntaxException {
        readPrivateKey(IOUtils.toByteArray(is));
    }
}
