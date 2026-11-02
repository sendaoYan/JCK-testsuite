/*
 * @test
 * @summary sun.security.util.math.intpoly.P384OrderField conformance: type shape via reflection (headless-safe).
 * @run main P384OrderFieldTest
 */

import java.lang.reflect.Modifier;

public class P384OrderFieldTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.util.math.intpoly.P384OrderField", false, P384OrderFieldTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.util.math.intpoly.IntegerPolynomial"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
