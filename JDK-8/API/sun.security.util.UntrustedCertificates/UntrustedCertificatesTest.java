/*
 * @test
 * @summary sun.security.util.UntrustedCertificates conformance: type shape via reflection (headless-safe).
 * @run main UntrustedCertificatesTest
 */

import java.lang.reflect.Modifier;

public class UntrustedCertificatesTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.UntrustedCertificates", false, UntrustedCertificatesTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
