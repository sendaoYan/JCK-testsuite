/*
 * @test
 * @summary sun.security.provider.certpath.PKIXTimestampParameters conformance: type shape via reflection (headless-safe).
 * @run main PKIXTimestampParametersTest
 */

import java.lang.reflect.Modifier;

public class PKIXTimestampParametersTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.certpath.PKIXTimestampParameters", false, PKIXTimestampParametersTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.cert.PKIXBuilderParameters"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
