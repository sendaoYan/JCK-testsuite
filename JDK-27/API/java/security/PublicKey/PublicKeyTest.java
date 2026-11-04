/*
 * @test
 * @summary java.security.PublicKey conformance: white-box structure + black-box behavior.
 * @run main PublicKeyTest
 */

import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

public class PublicKeyTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.PublicKey");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        checkInterfaces(c, new String[]{"java.security.AsymmetricKey"});
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        PublicKey pub = kp.getPublic();

        check(pub instanceof PublicKey, "instanceof PublicKey");
        check(pub instanceof java.security.AsymmetricKey, "instanceof AsymmetricKey");
        check(pub instanceof Key, "instanceof Key");
        check("RSA".equals(pub.getAlgorithm()), "getAlgorithm RSA");
        check("X.509".equals(pub.getFormat()), "getFormat X.509");
        check(pub.getEncoded() != null && pub.getEncoded().length > 0, "getEncoded non-empty");

        // AsymmetricKey.getParams() default returns null
        check(((java.security.AsymmetricKey) pub).getParams() == null, "getParams default null");
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
