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

        // ==== contract deepening: DSA loop, verify(off,len), PSS, state machine ====

        // ---- DSA-1024 closed loop: sign -> verify true / tampered data false / different message false ----
        KeyPairGenerator dsaKpg = KeyPairGenerator.getInstance("DSA");
        dsaKpg.initialize(1024);
        KeyPair dsaKp = dsaKpg.generateKeyPair();
        Signature dsaSigner = Signature.getInstance("SHA256withDSA");
        dsaSigner.initSign(dsaKp.getPrivate());
        dsaSigner.update(DATA);
        byte[] dsaSig = dsaSigner.sign();
        check(dsaSig != null && dsaSig.length > 0, "DSA sign non-empty");
        Signature dsaVerifier = Signature.getInstance("SHA256withDSA");
        dsaVerifier.initVerify(dsaKp.getPublic());
        dsaVerifier.update(DATA);
        check(dsaVerifier.verify(dsaSig), "DSA verify round-trip true");
        Signature dsaTamper = Signature.getInstance("SHA256withDSA");
        dsaTamper.initVerify(dsaKp.getPublic());
        byte[] flipped = DATA.clone();
        flipped[flipped.length - 1] ^= 0x01;
        dsaTamper.update(flipped);
        check(!dsaTamper.verify(dsaSig), "DSA verify different message false");
        Signature dsaOther = Signature.getInstance("SHA256withDSA");
        dsaOther.initVerify(dsaKpg.generateKeyPair().getPublic());
        dsaOther.update(DATA);
        check(!dsaOther.verify(dsaSig), "DSA verify wrong key false");

        // ---- cross-algorithm key misuse -> InvalidKeyException ----
        Signature rsaSigX = Signature.getInstance("SHA256withRSA");
        checkThrows(InvalidKeyException.class, () -> rsaSigX.initSign(dsaKp.getPrivate()),
                "initSign DSA private on RSA signature");
        Signature rsaSigY = Signature.getInstance("SHA256withRSA");
        checkThrows(InvalidKeyException.class, () -> rsaSigY.initVerify(dsaKp.getPublic()),
                "initVerify DSA public on RSA signature");

        // ---- verify(sig, off, len) offset variant ----
        Signature offVer = Signature.getInstance("SHA256withRSA");
        offVer.initVerify(kp.getPublic());
        offVer.update(DATA);
        byte[] sigOnce = signOnce(kp, DATA);
        byte[] padded = new byte[sigOnce.length + 16];
        System.arraycopy(sigOnce, 0, padded, 8, sigOnce.length);
        check(offVer.verify(padded, 8, sigOnce.length), "verify(off,len) at embedded offset true");
        check(!offVer.verify(padded, 0, sigOnce.length), "verify(off,len) wrong offset false");
        // short len: RSA engine demands exact signature length -> SignatureException
        offVer.update(DATA);
        final Signature offVerS = offVer;
        checkThrows(SignatureException.class, () -> offVerS.verify(padded, 8, sigOnce.length - 1),
                "verify(off,len) short len wrong length");
        final Signature offVerF = offVer;
        checkThrows(IllegalArgumentException.class, () -> offVerF.verify(padded, -1, 4),
                "verify(off,len) negative offset");
        checkThrows(IllegalArgumentException.class, () -> offVerF.verify(padded, 0, padded.length + 1),
                "verify(off,len) len overflow");

        // ---- state machine: verify() resets, re-verify without re-update -> false ----
        Signature reVer = Signature.getInstance("SHA256withRSA");
        reVer.initVerify(kp.getPublic());
        reVer.update(DATA);
        check(reVer.verify(sigOnce), "re-verify first pass true");
        check(!reVer.verify(sigOnce), "verify again without re-update false");
        reVer.update(DATA);
        check(reVer.verify(sigOnce), "verify after re-update true again");

        // ---- sign() after initVerify -> SignatureException (state) ----
        Signature wrongState = Signature.getInstance("SHA256withRSA");
        wrongState.initVerify(kp.getPublic());
        checkThrows(SignatureException.class, () -> wrongState.sign(), "sign after initVerify");
        // fresh (uninitialized) engine: getParameters is null, no state leakage
        Signature fresh = Signature.getInstance("SHA256withRSA");
        check(fresh.getParameters() == null, "getParameters null on fresh SHA256withRSA");
        // re-init is permitted at any time (javadoc: calling initSign again negates
        // an earlier call); the fresh sign/verify sequence after re-init stays consistent
        Signature reInit = Signature.getInstance("SHA256withRSA");
        reInit.initSign(kp.getPrivate());
        reInit.update(DATA);
        reInit.initSign(kp.getPrivate());
        reInit.update(DATA);
        Signature reInitVer = Signature.getInstance("SHA256withRSA");
        reInitVer.initVerify(kp.getPublic());
        reInitVer.update(DATA);
        check(reInitVer.verify(reInit.sign()), "sign/verify consistent after re-initSign");
        // verify(...) followed by another verify of a wrong-length input -> SignatureException
        Signature lenCheck = Signature.getInstance("SHA256withDSA");
        lenCheck.initVerify(dsaKp.getPublic());
        lenCheck.update(DATA);
        checkThrows(SignatureException.class, () -> lenCheck.verify(new byte[3]),
                "verify wrong-length signature");

        // ---- failed sign(buf,off,len) attempts do not corrupt accumulator ----
        Signature robust = Signature.getInstance("SHA256withRSA");
        robust.initSign(kp.getPrivate());
        robust.update(DATA);
        checkThrows(IllegalArgumentException.class, () -> robust.sign(new byte[1024], 500, 700),
                "sign(buf,off,len) overflow IAE");
        checkThrows(IllegalArgumentException.class, () -> robust.sign(new byte[8], -1, 4),
                "sign(buf,off,len) negative offset IAE");
        byte[] robustSig = robust.sign();
        Signature robustVer = Signature.getInstance("SHA256withRSA");
        robustVer.initVerify(kp.getPublic());
        robustVer.update(DATA);
        check(robustVer.verify(robustSig), "state intact after failed sign(buf,off,len)");

        // ---- chunked updates equivalent to one-shot ----
        Signature chunkSigner = Signature.getInstance("SHA256withRSA");
        chunkSigner.initSign(kp.getPrivate());
        chunkSigner.update(DATA, 0, 1);
        chunkSigner.update(DATA, 1, DATA.length - 2);
        chunkSigner.update(DATA, DATA.length - 1, 1);
        byte[] chunkSig = chunkSigner.sign();
        Signature chunkVer = Signature.getInstance("SHA256withRSA");
        chunkVer.initVerify(kp.getPublic());
        chunkVer.update(DATA);
        check(chunkVer.verify(chunkSig), "chunked updates verify against one-shot data");

        // ---- RSASSA-PSS: setParameter spec round-trip (both before and after init) ----
        Signature pssSigner = Signature.getInstance("RSASSA-PSS");
        java.security.spec.PSSParameterSpec pssSpec = new java.security.spec.PSSParameterSpec(
                "SHA-256", "MGF1", java.security.spec.MGF1ParameterSpec.SHA256, 32, 1);
        pssSigner.setParameter(pssSpec);
        pssSigner.initSign(kp.getPrivate());
        pssSigner.update(DATA);
        byte[] pssBytes = pssSigner.sign();
        check(pssBytes.length > 0, "PSS sign non-empty");
        Signature pssVer1 = Signature.getInstance("RSASSA-PSS");
        pssVer1.setParameter(pssSpec);
        pssVer1.initVerify(kp.getPublic());
        pssVer1.update(DATA);
        check(pssVer1.verify(pssBytes), "PSS verify (setParameter before init)");
        Signature pssVer2 = Signature.getInstance("RSASSA-PSS");
        pssVer2.initVerify(kp.getPublic());
        pssVer2.setParameter(pssSpec);
        pssVer2.update(DATA);
        check(pssVer2.verify(pssBytes), "PSS verify (setParameter after init)");
        // getParameters reflects the applied PSS spec
        java.security.AlgorithmParameters pssParams = pssVer2.getParameters();
        check(pssParams != null, "PSS getParameters non-null after setParameter");
        check(pssParams.toString().contains("SHA-256"), "PSS parameters mention SHA-256");
        // invalid parameter specs -> InvalidAlgorithmParameterException
        Signature pssBad = Signature.getInstance("RSASSA-PSS");
        checkThrows(java.security.InvalidAlgorithmParameterException.class,
                () -> pssBad.setParameter((java.security.spec.AlgorithmParameterSpec) null),
                "PSS setParameter(null)");
        // MD2 spec is accepted lazily (validated at init): initSign then fails with ProviderException
        Signature pssMd2 = Signature.getInstance("RSASSA-PSS");
        pssMd2.setParameter(new java.security.spec.PSSParameterSpec(
                "MD2", "MGF1", java.security.spec.MGF1ParameterSpec.SHA1, 20, 1));
        checkThrows(java.security.ProviderException.class, () -> pssMd2.initSign(kp.getPrivate()),
                "PSS MD2 digest unavailable at initSign");
        Signature pssMgf = Signature.getInstance("RSASSA-PSS");
        checkThrows(java.security.InvalidAlgorithmParameterException.class, () -> pssMgf.setParameter(
                        new java.security.spec.PSSParameterSpec("SHA-256", "BOGUS-MGF", java.security.spec.MGF1ParameterSpec.SHA1, 20, 1)),
                "PSS setParameter bogus MGF");

        // ---- deprecated setParameter(String,Object)/getParameter(String) -> InvalidParameterException ----
        Signature dep = Signature.getInstance("SHA256withDSA");
        checkThrows(java.security.InvalidParameterException.class, () -> dep.setParameter("X", "y"),
                "setParameter(String,Object) unsupported");
        check(dep.getParameter("X") == null, "getParameter(String) returns null");

        // ---- getInstance with explicit provider instance ----
        Signature viaProvider = Signature.getInstance("SHA256withRSA", Security.getProvider("SunRsaSign"));
        check("SunRsaSign".equals(viaProvider.getProvider().getName()), "getInstance(alg, Provider) provider");
        Signature viaName = Signature.getInstance("SHA256withRSA", "SunRsaSign");
        check("SunRsaSign".equals(viaName.getProvider().getName()), "getInstance(alg, providerName) provider");
        checkThrows(NullPointerException.class, () -> Signature.getInstance(null, "SunRsaSign"),
                "getInstance(null, providerName)");
        checkThrows(NoSuchAlgorithmException.class, () -> Signature.getInstance("BogusSig_XYZ", "SunRsaSign"),
                "getInstance(bogus, providerName)");
        check(sig.getAlgorithm().equals("SHA256withRSA") && sig.toString().contains("SHA256withRSA"),
                "toString contains algorithm");

        System.out.println("PASS (" + passed + " assertions)");
    }

    static byte[] signOnce(KeyPair kp, byte[] data) throws Exception {
        Signature s = Signature.getInstance("SHA256withRSA");
        s.initSign(kp.getPrivate());
        s.update(data);
        return s.sign();
    }
}
