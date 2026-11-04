/*
 * @test
 * @summary java.security.SignedObject conformance: white-box structure + black-box behavior.
 * @run main SignedObjectTest
 */

import java.lang.reflect.Modifier;
import java.io.Serializable;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.SignedObject;
import java.util.Arrays;

public class SignedObjectTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SignedObject");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"getObject", "getSignature", "getAlgorithm", "verify"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        Serializable payload = "the signed payload";
        Signature signer = Signature.getInstance("SHA256withRSA");
        SignedObject so = new SignedObject(payload, kp.getPrivate(), signer);

        check(so.getObject().equals(payload), "getObject returns original payload");
        check(so.getSignature() != null && so.getSignature().length > 0, "getSignature non-empty");
        check("SHA256withRSA".equals(so.getAlgorithm()), "getAlgorithm");

        // getSignature returns a defensive copy
        byte[] sig1 = so.getSignature();
        sig1[0] = 0;
        byte[] sig2 = so.getSignature();
        check(!Arrays.equals(sig1, sig2), "getSignature defensive copy");

        // verify with the correct public key
        Signature verifier = Signature.getInstance("SHA256withRSA");
        check(so.verify(kp.getPublic(), verifier), "verify with correct key");

        // verify with a different key must fail
        KeyPairGenerator kpg2 = KeyPairGenerator.getInstance("RSA");
        kpg2.initialize(2048);
        KeyPair other = kpg2.generateKeyPair();
        Signature verifier2 = Signature.getInstance("SHA256withRSA");
        check(!so.verify(other.getPublic(), verifier2), "verify with wrong key fails");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
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
