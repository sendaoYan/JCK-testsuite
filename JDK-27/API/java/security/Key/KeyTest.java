/*
 * @test
 * @summary java.security.Key conformance: white-box structure + black-box behavior.
 * @run main KeyTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Arrays;

public class KeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.Key");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"getAlgorithm", "getEncoded", "getFormat"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        Key key = kp.getPublic();

        check(key instanceof Key, "public key instanceof Key");
        check("RSA".equals(key.getAlgorithm()), "getAlgorithm RSA");
        check("X.509".equals(key.getFormat()), "getFormat X.509");
        byte[] enc = key.getEncoded();
        check(enc != null && enc.length > 0, "getEncoded non-empty");

        // getEncoded returns a defensive copy
        byte[] copy = key.getEncoded();
        copy[0] = 0;
        check(key.getEncoded()[0] != 0, "getEncoded defensive copy");

        // a private key is also a Key with PKCS#8 format
        Key priv = kp.getPrivate();
        check(priv instanceof Key, "private key instanceof Key");
        check("PKCS#8".equals(priv.getFormat()), "private format PKCS#8");
        check(!Arrays.equals(key.getEncoded(), priv.getEncoded()), "public/private encoded differ");
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
