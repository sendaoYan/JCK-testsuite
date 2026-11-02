/*
 * @test
 * @summary java.security.KeyStore.PrivateKeyEntry conformance: constructor and accessors.
 * @run main PrivateKeyEntryTest
 */

import java.security.KeyStore;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public class PrivateKeyEntryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        PrivateKey priv = kp.getPrivate();
        PublicKey pub = kp.getPublic();
        Certificate cert = new Certificate("X.509") {
            public byte[] getEncoded() { return new byte[] { 1, 2, 3 }; }
            public void verify(PublicKey key) { }
            public void verify(PublicKey key, String sigProvider) { }
            public String toString() { return "TestCertificate"; }
            public PublicKey getPublicKey() { return pub; }
        };
        KeyStore.PrivateKeyEntry e = new KeyStore.PrivateKeyEntry(priv, new Certificate[] { cert });
        check(priv.equals(e.getPrivateKey()), "getPrivateKey");
        check(e.getCertificateChain().length == 1, "getCertificateChain length");
        check(e.getCertificateChain()[0] == cert, "getCertificateChain content");
        check(e.getCertificate() == cert, "getCertificate");
        check(e.getAttributes().isEmpty(), "getAttributes empty");
        check(e instanceof KeyStore.Entry, "implements Entry");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
