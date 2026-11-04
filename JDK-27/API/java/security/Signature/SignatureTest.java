/*
 * @test
 * @summary java.security.Signature conformance: full contract coverage.
 *          getInstance variants + exact exceptions (NoSuchAlgorithmException /
 *          NullPointerException / IllegalArgumentException / NoSuchProviderException),
 *          sign/verify round-trip, tamper detection, initSign(PrivateKey) and
 *          initSign(PrivateKey, SecureRandom), update(byte)/update(byte[],off,len)/
 *          update(ByteBuffer) incl. null and bounds checks, state-machine violations
 *          (sign/update/verify before init -> SignatureException), invalid-key
 *          initSign/initVerify -> InvalidKeyException, sign(buf,off,len) short-buffer
 *          -> SignatureException, getAlgorithm/getProvider/getParameters, and
 *          clone() -> CloneNotSupportedException.
 * @run main SignatureTest
 */

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;

public class SignatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static class FakePrivateKey implements PrivateKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return null; }
        public byte[] getEncoded() { return null; }
    }

    static class FakePublicKey implements PublicKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return null; }
        public byte[] getEncoded() { return null; }
    }

    static final byte[] DATA = "the quick brown fox jumps over the lazy dog".getBytes();

    public static void main(String[] args) throws Exception {
        // ---- factory + exception matrix ----
        checkThrows(NoSuchAlgorithmException.class,
                () -> Signature.getInstance("NoSuchSignature_XYZ"), "getInstance(bogus)");
        checkThrows(NullPointerException.class,
                () -> Signature.getInstance(null), "getInstance(null)");
        checkThrows(NullPointerException.class,
                () -> Signature.getInstance(null, Security.getProvider("SUN")),
                "getInstance(null, Provider)");
        checkThrows(IllegalArgumentException.class,
                () -> Signature.getInstance("SHA256withRSA", (Provider) null),
                "getInstance(alg, null Provider)");
        checkThrows(NoSuchProviderException.class,
                () -> Signature.getInstance("SHA256withRSA", "NoSuchProvider_XYZ"),
                "getInstance(alg, bogus provider)");

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        // ---- metadata ----
        Signature sig = Signature.getInstance("SHA256withRSA");
        check("SHA256withRSA".equals(sig.getAlgorithm()), "getAlgorithm");
        check(sig.getProvider() != null, "getProvider non-null");
        check(sig.getParameters() == null, "getParameters null for SHA256withRSA");
        check(sig.toString().startsWith("Signature"), "toString prefix");

        // ---- round-trip sign/verify ----
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(kp.getPrivate());
        signer.update(DATA);
        byte[] signature = signer.sign();
        check(signature != null && signature.length > 0, "sign() non-empty");

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(kp.getPublic());
        verifier.update(DATA);
        check(verifier.verify(signature), "verify round-trip true");

        // ---- tamper detection ----
        Signature verifier2 = Signature.getInstance("SHA256withRSA");
        verifier2.initVerify(kp.getPublic());
        byte[] badData = DATA.clone();
        badData[0] ^= 0x01;
        verifier2.update(badData);
        check(!verifier2.verify(signature), "verify tampered data false");

        // ---- verify with wrong signature bytes ----
        Signature verifier3 = Signature.getInstance("SHA256withRSA");
        verifier3.initVerify(kp.getPublic());
        verifier3.update(DATA);
        byte[] badSig = signature.clone();
        badSig[badSig.length - 1] ^= 0x01;
        check(!verifier3.verify(badSig), "verify tampered signature false");

        // ---- initSign(PrivateKey, SecureRandom) ----
        Signature signer2 = Signature.getInstance("SHA256withRSA");
        signer2.initSign(kp.getPrivate(), new SecureRandom());
        signer2.update(DATA);
        Signature verifier4 = Signature.getInstance("SHA256withRSA");
        verifier4.initVerify(kp.getPublic());
        verifier4.update(DATA);
        check(verifier4.verify(signer2.sign()), "initSign with SecureRandom round-trip");

        // ---- update(byte) ----
        Signature signer3 = Signature.getInstance("SHA256withRSA");
        signer3.initSign(kp.getPrivate());
        for (byte b : DATA) signer3.update(b);
        Signature verifier5 = Signature.getInstance("SHA256withRSA");
        verifier5.initVerify(kp.getPublic());
        verifier5.update(DATA);
        check(verifier5.verify(signer3.sign()), "update(byte) round-trip");

        // ---- update(ByteBuffer) ----
        Signature signer4 = Signature.getInstance("SHA256withRSA");
        signer4.initSign(kp.getPrivate());
        signer4.update(ByteBuffer.wrap(DATA));
        Signature verifier6 = Signature.getInstance("SHA256withRSA");
        verifier6.initVerify(kp.getPublic());
        verifier6.update(ByteBuffer.wrap(DATA));
        check(verifier6.verify(signer4.sign()), "update(ByteBuffer) round-trip");

        // ---- update boundaries ----
        final Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(kp.getPrivate());
        checkThrows(NullPointerException.class, () -> s.update((byte[]) null), "update((byte[])null)");
        checkThrows(IllegalArgumentException.class, () -> s.update((byte[]) null, 0, 1), "update(null,off,len)");
        checkThrows(IllegalArgumentException.class, () -> s.update(new byte[2], 1, 2), "update(off+len > len)");
        checkThrows(IllegalArgumentException.class, () -> s.update(new byte[2], -1, 1), "update(negative off)");
        checkThrows(IllegalArgumentException.class, () -> s.update(new byte[2], 0, -1), "update(negative len)");
        s.update(new byte[4], 0, 0); // legal zero-length update
        check(true, "update(0,0) legal");

        // ---- state-machine violations ----
        final Signature uninit = Signature.getInstance("SHA256withRSA");
        checkThrows(SignatureException.class, () -> uninit.sign(), "sign before initSign");
        checkThrows(SignatureException.class, () -> uninit.update((byte) 1), "update before init");
        checkThrows(SignatureException.class, () -> uninit.verify(new byte[16]), "verify before initVerify");

        // ---- invalid key ----
        final Signature badSigner = Signature.getInstance("SHA256withRSA");
        checkThrows(InvalidKeyException.class, () -> badSigner.initSign(new FakePrivateKey()),
                "initSign non-RSA private key");
        final Signature badVerifier = Signature.getInstance("SHA256withRSA");
        checkThrows(InvalidKeyException.class, () -> badVerifier.initVerify(new FakePublicKey()),
                "initVerify non-RSA public key");

        // ---- sign(buf,off,len) short buffer -> SignatureException ----
        final Signature sb = Signature.getInstance("SHA256withRSA");
        sb.initSign(kp.getPrivate());
        sb.update(DATA);
        checkThrows(SignatureException.class, () -> sb.sign(new byte[8], 0, 8), "sign short buffer");

        // ---- sign((byte[])null, off, len) -> IllegalArgumentException ----
        final Signature nb = Signature.getInstance("SHA256withRSA");
        nb.initSign(kp.getPrivate());
        checkThrows(IllegalArgumentException.class, () -> nb.sign((byte[]) null, 0, 16),
                "sign(null buffer)");

        // ---- sign(buf,off,len) into a large buffer ----
        Signature signer5 = Signature.getInstance("SHA256withRSA");
        signer5.initSign(kp.getPrivate());
        signer5.update(DATA);
        byte[] bigBuf = new byte[1024];
        int wrote = signer5.sign(bigBuf, 64, 256);
        check(wrote > 0, "sign(buf,off,len) returns count");
        check(Arrays.equals(Arrays.copyOfRange(bigBuf, 64, 64 + wrote), signature), "sign(buf,off,len) == sign()");

        // ---- verify(null) -> NPE ----
        final Signature vn = Signature.getInstance("SHA256withRSA");
        vn.initVerify(kp.getPublic());
        checkThrows(NullPointerException.class, () -> vn.verify((byte[]) null), "verify((byte[])null)");

        // ---- clone -> CloneNotSupportedException ----
        final Signature cl = Signature.getInstance("SHA256withRSA");
        checkThrows(CloneNotSupportedException.class, () -> cl.clone(), "clone throws CloneNotSupportedException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
