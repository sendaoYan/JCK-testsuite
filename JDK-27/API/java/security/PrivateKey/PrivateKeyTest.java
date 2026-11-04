/*
 * @test
 * @summary java.security.PrivateKey conformance: white-box structure + black-box behavior.
 * @run main PrivateKeyTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;

public class PrivateKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PrivateKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        checkInterfaces(c, new String[]{"java.security.AsymmetricKey", "javax.security.auth.Destroyable"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PrivateKey priv = kp.getPrivate();

        check(priv instanceof PrivateKey, "instanceof PrivateKey");
        check(priv instanceof java.security.AsymmetricKey, "instanceof AsymmetricKey");
        check(priv instanceof Key, "instanceof Key");
        check(priv instanceof javax.security.auth.Destroyable, "instanceof Destroyable");
        check("RSA".equals(priv.getAlgorithm()), "getAlgorithm RSA");
        check("PKCS#8".equals(priv.getFormat()), "getFormat PKCS#8");
        check(priv.getEncoded() != null && priv.getEncoded().length > 0, "getEncoded non-empty");
        check(((java.security.AsymmetricKey) priv).getParams() == null, "getParams default null");
    }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
