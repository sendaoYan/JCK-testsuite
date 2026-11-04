/*
 * @test
 * @summary sun.security.rsa.RSAPublicKeyImpl conformance: type shape via reflection (headless-safe).
 * @run main RSAPublicKeyImplTest
 */

import java.lang.reflect.Modifier;

public class RSAPublicKeyImplTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.rsa.RSAPublicKeyImpl", false, RSAPublicKeyImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.x509.X509Key"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
