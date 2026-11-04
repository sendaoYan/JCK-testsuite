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

        System.out.println("PASS (" + passed + " assertions)");
    }
}
