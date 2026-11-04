/*
 * @test
 * @summary jdk.security.jarsigner.JarSigner conformance: white-box structure + black-box behavior.
 * @run main JarSignerTest
 */

import jdk.security.jarsigner.*;
import java.security.*;
import java.lang.reflect.*;
import java.util.*;
public class JarSignerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.security.jarsigner.JarSigner");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructors().length == 0, "no public constructor");
        check(hasMethod(c, "sign"), "sign");
        check(hasMethod(c, "getDigestAlgorithm"), "getDigestAlgorithm");
        check(hasMethod(c, "getSignatureAlgorithm"), "getSignatureAlgorithm");
        check(hasMethod(c, "getTsa"), "getTsa");
        check(hasMethod(c, "getSignerName"), "getSignerName");
        check(hasMethod(c, "getProperty"), "getProperty");

    }

    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        PrivateKey pk = kpg.generateKeyPair().getPrivate();
        check(JarSigner.Builder.getDefaultDigestAlgorithm() != null, "default digest algorithm");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(pk) != null, "default signature algorithm");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
