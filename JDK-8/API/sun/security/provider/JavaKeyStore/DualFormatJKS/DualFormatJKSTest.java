/*
 * @test
 * @summary sun.security.provider.JavaKeyStore.DualFormatJKS conformance: type shape via reflection (headless-safe).
 * @run main DualFormatJKSTest
 */

import java.lang.reflect.Modifier;

public class DualFormatJKSTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.JavaKeyStore$DualFormatJKS", false, DualFormatJKSTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.provider.KeyStoreDelegator"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
