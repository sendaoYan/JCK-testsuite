/*
 * @test
 * @summary java.security.AlgorithmConstraints conformance: white-box structure + black-box behavior.
 * @run main AlgorithmConstraintsTest
 */

import java.lang.reflect.*;
import java.security.*;
import java.util.*;

public class AlgorithmConstraintsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.AlgorithmConstraints");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkMethods(c, new String[]{"permits"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // JDK-27 interface: three abstract permits(...) overloads
        AlgorithmConstraints denyAll = new AlgorithmConstraints() {
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, AlgorithmParameters specs) { return false; }
            public boolean permits(Set<CryptoPrimitive> usage, Key key) { return false; }
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, Key key, AlgorithmParameters specs) { return false; }
        };
        Set<CryptoPrimitive> sig = EnumSet.of(CryptoPrimitive.SIGNATURE);
        check(!denyAll.permits(sig, "SHA256withRSA", (AlgorithmParameters) null), "deny string");
        check(!denyAll.permits(sig, "RSA", (Key) null, null), "deny key+alg");
        KeyPair kpAny = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        check(!denyAll.permits(sig, kpAny.getPublic()), "deny key-only");
        AlgorithmConstraints allowAll = new AlgorithmConstraints() {
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, AlgorithmParameters specs) { return true; }
            public boolean permits(Set<CryptoPrimitive> usage, Key key) { return true; }
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, Key key, AlgorithmParameters specs) { return true; }
        };
        check(allowAll.permits(sig, "anything", (AlgorithmParameters) null), "allow");
        check(allowAll.permits(EnumSet.of(CryptoPrimitive.PUBLIC_KEY_ENCRYPTION), "AES", (Key) null, null), "allow key+alg");
        check(allowAll.permits(sig, kpAny.getPublic()), "allow key-only");
        // selective constraint: SHA-family signatures, RSA keys only
        AlgorithmConstraints selective = new AlgorithmConstraints() {
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, AlgorithmParameters specs) {
                return algorithm != null && algorithm.startsWith("SHA");
            }
            public boolean permits(Set<CryptoPrimitive> usage, Key key) { return "RSA".equals(key.getAlgorithm()); }
            public boolean permits(Set<CryptoPrimitive> usage, String algorithm, Key key, AlgorithmParameters specs) {
                return "RSA".equals(algorithm);
            }
        };
        check(selective.permits(sig, "SHA256withRSA", (AlgorithmParameters) null), "sha ok");
        check(!selective.permits(sig, "MD5withRSA", (AlgorithmParameters) null), "md5 denied");
        KeyPair kp = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        check(selective.permits(sig, kp.getPublic()), "rsa key-only");
        check(selective.permits(sig, "RSA", kp.getPublic(), null), "rsa key+alg");
        try {
            KeyPair dsaKp = KeyPairGenerator.getInstance("DSA").generateKeyPair();
            check(!selective.permits(sig, dsaKp.getPublic()), "dsa key denied");
        } catch (Exception e) {
            check(true, "DSA unavailable; tolerated");
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
