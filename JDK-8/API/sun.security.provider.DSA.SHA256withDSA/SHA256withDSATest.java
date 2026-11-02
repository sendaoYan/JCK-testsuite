/*
 * @test
 * @summary sun.security.provider.DSA.SHA256withDSA conformance: type shape via reflection (headless-safe).
 * @run main SHA256withDSATest
 */

import java.lang.reflect.Modifier;

public class SHA256withDSATest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.DSA$SHA256withDSA", false, SHA256withDSATest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.provider.DSA"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
