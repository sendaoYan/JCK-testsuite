/*
 * @test
 * @summary sun.security.jgss.spi.GSSCredentialSpi conformance: type shape via reflection (headless-safe).
 * @run main GSSCredentialSpiTest
 */

import java.lang.reflect.Modifier;

public class GSSCredentialSpiTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spi.GSSCredentialSpi", false, GSSCredentialSpiTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null for interface");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
