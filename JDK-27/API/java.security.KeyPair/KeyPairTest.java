/*
 * @test
 * @summary java.security.KeyPair conformance: white-box structure + black-box behavior.
 * @run main KeyPairTest
 */

import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyPair");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isFinal(c.getModifiers()), "final class");
        checkInterfaces(c, new String[]{"java.io.Serializable", "java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"getPublic", "getPrivate"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair generated = kpg.generateKeyPair();
        PublicKey pub = generated.getPublic();
        PrivateKey priv = generated.getPrivate();
        check(pub != null && priv != null, "generated keys non-null");

        // Explicit construction roundtrips the same keys
        KeyPair kp = new KeyPair(pub, priv);
        check(kp.getPublic() == pub, "getPublic identity");
        check(kp.getPrivate() == priv, "getPrivate identity");
        check("RSA".equals(kp.getPublic().getAlgorithm()), "public algorithm RSA");
        check("RSA".equals(kp.getPrivate().getAlgorithm()), "private algorithm RSA");
        check(kp.getPublic().getEncoded() != null, "public encoded non-null");
        check(kp.getPrivate().getEncoded() != null, "private encoded non-null");
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
