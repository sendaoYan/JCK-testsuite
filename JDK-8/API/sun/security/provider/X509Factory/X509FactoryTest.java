/*
 * @test
 * @summary sun.security.provider.X509Factory conformance: type shape via reflection (headless-safe).
 * @run main X509FactoryTest
 */

import java.lang.reflect.Modifier;

public class X509FactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.X509Factory", false, X509FactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.cert.CertificateFactorySpi"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
