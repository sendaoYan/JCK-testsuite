/*
 * @test
 * @summary sun.security.tools.keytool.Resources_fr conformance: type shape via reflection (headless-safe).
 * @run main Resources_frTest
 */

import java.lang.reflect.Modifier;

public class Resources_frTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.tools.keytool.Resources_fr", false, Resources_frTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.ListResourceBundle"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
