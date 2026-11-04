/*
 * @test
 * @summary sun.security.jgss.spnego.NegTokenTarg conformance: type shape via reflection (headless-safe).
 * @run main NegTokenTargTest
 */

import java.lang.reflect.Modifier;

public class NegTokenTargTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spnego.NegTokenTarg", false, NegTokenTargTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.jgss.spnego.SpNegoToken"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
