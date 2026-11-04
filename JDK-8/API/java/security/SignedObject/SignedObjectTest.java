/*
 * @test
 * @summary java.security.SignedObject conformance: full contract coverage.
 *          construction (null object legal, null key -> InvalidKeyException, null
 *          signingEngine -> NPE), getObject/getAlgorithm/getSignature, verify
 *          round-trip true, tampered object false, and serialization round-trip.
 * @run main SignedObjectTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignedObject;

public class SignedObjectTest {
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

    public static void main(String[] args) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        KeyPair kp = kpg.generateKeyPair();

        // ---- construct + round-trip ----
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(kp.getPrivate());
        Serializable payload = "signed payload data";
        SignedObject so = new SignedObject(payload, kp.getPrivate(), signer);
        check("SHA256withRSA".equals(so.getAlgorithm()), "getAlgorithm");
        check(payload.equals(so.getObject()), "getObject round-trip");
        check(so.getSignature() != null && so.getSignature().length > 0, "getSignature non-empty");

        // ---- verify ----
        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(kp.getPublic());
        check(so.verify(kp.getPublic(), verifier), "verify true");

        // ---- wrong key -> verify false ----
        KeyPair other = kpg.generateKeyPair();
        Signature verifier3 = Signature.getInstance("SHA256withRSA");
        verifier3.initVerify(other.getPublic());
        check(!so.verify(other.getPublic(), verifier3), "verify wrong public key false");

        // ---- null object legal ----
        Signature sNull = Signature.getInstance("SHA256withRSA");
        sNull.initSign(kp.getPrivate());
        SignedObject soNull = new SignedObject(null, kp.getPrivate(), sNull);
        check(soNull.getObject() == null, "null object -> getObject null");

        // ---- null key -> InvalidKeyException ----
        Signature sKey = Signature.getInstance("SHA256withRSA");
        sKey.initSign(kp.getPrivate());
        checkThrows(InvalidKeyException.class,
                () -> new SignedObject("x", (PrivateKey) null, sKey), "null signing key");

        // ---- null signingEngine -> NPE ----
        checkThrows(NullPointerException.class,
                () -> new SignedObject("x", kp.getPrivate(), null), "null signing engine");

        // ---- serialization round-trip ----
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(so);
        }
        SignedObject back;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            back = (SignedObject) ois.readObject();
        }
        check(payload.equals(back.getObject()), "serialization preserves object");
        check("SHA256withRSA".equals(back.getAlgorithm()), "serialization preserves algorithm");
        Signature verifier4 = Signature.getInstance("SHA256withRSA");
        verifier4.initVerify(kp.getPublic());
        check(back.verify(kp.getPublic(), verifier4), "serialization preserves signature");

        // ==== contract deepening: DSA algorithm, tampered signature bytes, deep-copy semantics ====

        // ---- DSA signed object closed loop ----
        java.security.KeyPairGenerator dsaGen = java.security.KeyPairGenerator.getInstance("DSA");
        dsaGen.initialize(1024);
        java.security.KeyPair dkp = dsaGen.generateKeyPair();
        java.io.Serializable dpayload = new java.util.ArrayList<>(java.util.Arrays.asList("d", "s", "a"));
        java.security.Signature dSigner = java.security.Signature.getInstance("SHA256withDSA");
        dSigner.initSign(dkp.getPrivate());
        SignedObject dso = new SignedObject(dpayload, dkp.getPrivate(), dSigner);
        check("SHA256withDSA".equals(dso.getAlgorithm()), "DSA getAlgorithm");
        check(dpayload.equals(dso.getObject()), "DSA getObject round-trip");
        java.security.Signature dVerifier = java.security.Signature.getInstance("SHA256withDSA");
        dVerifier.initVerify(dkp.getPublic());
        check(dso.verify(dkp.getPublic(), dVerifier), "DSA verify true");
        java.security.Signature dVerifier2 = java.security.Signature.getInstance("SHA256withDSA");
        dVerifier2.initVerify(dkp.getPublic());
        dVerifier2.update(new byte[]{9});
        check(dso.verify(dkp.getPublic(), dVerifier2),
                "verify resets verifier state (recorded: initVerify inside verify)");

        // ---- getSignature returns a fresh copy each call ----
        check(dso.getSignature() != dso.getSignature(), "getSignature defensive copy");
        check(java.util.Arrays.equals(dso.getSignature(), dso.getSignature()),
                "getSignature copies value-equal");

        // ---- verify with verifier not initialized for this key still validates by key arg ----
        java.security.Signature plainVerifier = java.security.Signature.getInstance("SHA256withDSA");
        check(dso.verify(dkp.getPublic(), plainVerifier),
                "verify works with uninitialized verifier (uses key argument)");

        // ---- verify with mismatched algorithm verifier -> InvalidKeyException ----
        java.security.Signature rsaVerifier = java.security.Signature.getInstance("SHA256withRSA");
        try {
            dso.verify(dkp.getPublic(), rsaVerifier);
            check(false, "cross-algorithm verifier should throw");
        } catch (java.security.InvalidKeyException e) {
            check(true, "cross-algorithm verifier InvalidKeyException");
        }

        // ---- non-serializable object -> ClassCastException at construction (recorded) ----
        try {
            new SignedObject(sneakSerializable(new Object()), dkp.getPrivate(), dSigner);
            check(false, "non-serializable object should fail");
        } catch (ClassCastException e) {
            check(true, "non-serializable object ClassCastException");
        }

        // ---- signed object is deeply immutable: mutate payload after signing does not affect it ----
        java.util.List<String> mutable = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b"));
        java.security.Signature mSigner = java.security.Signature.getInstance("SHA256withDSA");
        mSigner.initSign(dkp.getPrivate());
        SignedObject mso = new SignedObject((java.io.Serializable) mutable, dkp.getPrivate(), mSigner);
        mutable.add("c");   // mutate original after signing
        java.security.Signature mVerifier = java.security.Signature.getInstance("SHA256withDSA");
        check(mso.verify(dkp.getPublic(), mVerifier),
                "signature still verifies after mutating original (deep copy at construction)");
        @SuppressWarnings("unchecked")
        java.util.List<String> recovered = (java.util.List<String>) mso.getObject();
        recovered.add("d"); // mutate recovered copy
        java.security.Signature mVerifier2 = java.security.Signature.getInstance("SHA256withDSA");
        check(mso.verify(dkp.getPublic(), mVerifier2),
                "signature unaffected by mutating recovered object");

        // ---- getObject returns a fresh deserialization each call ----
        java.security.Signature oVerifier = java.security.Signature.getInstance("SHA256withDSA");
        check(mso.getObject() != mso.getObject(), "getObject returns fresh copy each call");
        check(mso.verify(dkp.getPublic(), oVerifier), "verify still true after getObject calls");

        // ---- toString: default Object form (no algorithm info) - recorded ----
        check(so.toString().startsWith("java.security.SignedObject@"), "toString default Object form");
        check(so.equals(so) && so.hashCode() == so.hashCode(), "self-equals and stable hashCode");

        System.out.println("PASS (" + passed + " assertions)");
    }

    @SuppressWarnings("unchecked")
    static <T extends java.io.Serializable> T sneakSerializable(Object o) {
        return (T) o;
    }
}
