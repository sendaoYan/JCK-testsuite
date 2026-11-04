/*
 * @test
 * @summary java.security.Signature conformance: white-box structure + black-box behavior.
 * @run main SignatureTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;

public class SignatureTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Signature");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.SignatureSpi"), "extends SignatureSpi");
        checkMethods(c, new String[]{"getInstance", "getProvider", "initVerify", "initSign",
            "sign", "verify", "update", "getAlgorithm", "clone"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] data = "payload bytes for signing".getBytes(StandardCharsets.UTF_8);

        // RSA SHA256withRSA sign/verify roundtrip
        KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
        rsaGen.initialize(2048);
        KeyPair rsa = rsaGen.generateKeyPair();

        Signature signer = Signature.getInstance("SHA256withRSA");
        check("SHA256withRSA".equals(signer.getAlgorithm()), "getAlgorithm");
        check(signer.getProvider() != null, "getProvider non-null");
        signer.initSign(rsa.getPrivate());
        signer.update(data);
        byte[] sig = signer.sign();
        check(sig != null && sig.length > 0, "sign() non-empty");

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(rsa.getPublic());
        verifier.update(data);
        check(verifier.verify(sig), "verify valid signature");

        // byte[] update + verify into provided buffer
        Signature verifier2 = Signature.getInstance("SHA256withRSA");
        verifier2.initVerify(rsa.getPublic());
        verifier2.update(data, 0, data.length);
        check(verifier2.verify(sig), "offset update then verify");

        // sign(byte[],int,int) into provided buffer
        Signature signer2 = Signature.getInstance("SHA256withRSA");
        signer2.initSign(rsa.getPrivate());
        signer2.update(data);
        byte[] sigBuf = new byte[512];
        int len = signer2.sign(sigBuf, 0, sigBuf.length);
        check(len > 0 && len <= sigBuf.length, "sign(byte[],int,int) returns length");
        byte[] sigCopy = Arrays.copyOf(sigBuf, len);
        Signature verifier3 = Signature.getInstance("SHA256withRSA");
        verifier3.initVerify(rsa.getPublic());
        verifier3.update(data);
        check(verifier3.verify(sigCopy), "verify buffer-signed signature");

        // tampered data must fail verification
        Signature verifierBad = Signature.getInstance("SHA256withRSA");
        verifierBad.initVerify(rsa.getPublic());
        verifierBad.update("tampered".getBytes(StandardCharsets.UTF_8));
        check(!verifierBad.verify(sig), "tampered data not verified");

        // update(ByteBuffer)
        Signature signerBB = Signature.getInstance("SHA256withRSA");
        signerBB.initSign(rsa.getPrivate());
        signerBB.update(ByteBuffer.wrap(data));
        byte[] sigBB = signerBB.sign();
        Signature verifierBB = Signature.getInstance("SHA256withRSA");
        verifierBB.initVerify(rsa.getPublic());
        verifierBB.update(data);
        check(verifierBB.verify(sigBB), "ByteBuffer update sign/verify");

        // EC SHA256withECDSA roundtrip
        KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
        ecGen.initialize(new ECGenParameterSpec("secp256r1"));
        KeyPair ec = ecGen.generateKeyPair();
        Signature ecSigner = Signature.getInstance("SHA256withECDSA");
        ecSigner.initSign(ec.getPrivate());
        ecSigner.update(data);
        byte[] ecSig = ecSigner.sign();
        Signature ecVerifier = Signature.getInstance("SHA256withECDSA");
        ecVerifier.initVerify(ec.getPublic());
        ecVerifier.update(data);
        check(ecVerifier.verify(ecSig), "EC sign/verify roundtrip");
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
