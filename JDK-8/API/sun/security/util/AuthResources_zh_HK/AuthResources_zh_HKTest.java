/*
 * @test
 * @summary sun.security.util.AuthResources_zh_HK conformance: type shape via reflection (headless-safe).
 * @run main AuthResources_zh_HKTest
 */

import java.lang.reflect.Modifier;

public class AuthResources_zh_HKTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.AuthResources_zh_HK", false, AuthResources_zh_HKTest.class.getClassLoader());
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
