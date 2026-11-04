/*
 * @test
 * @summary java.security.Signer conformance: white-box structure + black-box behavior.
 * @run main SignerTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class SignerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Signer");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.Identity"), "superclass");
        checkMethods(c, new String[]{"getPrivateKey", "setKeyPair", "toString"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Signer is an abstract subclass of Identity carrying a private key
        Signer s = new Signer("jck-signer") {};
        check(s.getName().equals("jck-signer"), "name");
        check(s.getPrivateKey() == null, "null key");
        check(s instanceof Identity && s instanceof Principal, "hierarchy");
        // setKeyPair installs both halves
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        s.setKeyPair(kp);
        check(s.getPrivateKey() == kp.getPrivate(), "private key");
        check(s.getPublicKey().equals(kp.getPublic()), "public key");
        // resetting with the same pair succeeds; JDK-27 replaces silently even with a different pair
        s.setKeyPair(new KeyPair(kp.getPublic(), kp.getPrivate()));
        check(s.getPrivateKey() != null, "same pair ok");
        KeyPair other = kpg.generateKeyPair();
        s.setKeyPair(other);
        check(s.getPrivateKey() == other.getPrivate() && s.getPublicKey().equals(other.getPublic()), "different pair replaces");
        // toString mentions the signer identity
        check(s.toString().contains("jck-signer"), "toString");
        // null key pair rejected
        try {
            s.setKeyPair(null);
            check(false, "null pair");
        } catch (NullPointerException | InvalidParameterException e) {
            check(true, "null pair rejected");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
