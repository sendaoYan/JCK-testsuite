/*
 * @test
 * @summary sun.security.rsa.RSAKeyPairGenerator.PSS conformance: type shape via reflection (headless-safe).
 * @run main PSSTest
 */

import java.lang.reflect.Modifier;

public class PSSTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.rsa.RSAKeyPairGenerator$PSS", false, PSSTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.rsa.RSAKeyPairGenerator"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
