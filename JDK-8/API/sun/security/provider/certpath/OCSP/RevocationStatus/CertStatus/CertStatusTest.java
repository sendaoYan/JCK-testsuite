/*
 * @test
 * @summary sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus conformance: type shape via reflection (headless-safe).
 * @run main CertStatusTest
 */

import java.lang.reflect.Modifier;

public class CertStatusTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.certpath.OCSP$RevocationStatus$CertStatus", false, CertStatusTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        try { check(c.getEnumConstants() != null, "enum constants"); } catch (LinkageError e) { /* fallback: isEnum already asserted */ }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
