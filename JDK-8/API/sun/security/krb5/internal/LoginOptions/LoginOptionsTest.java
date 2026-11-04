/*
 * @test
 * @summary sun.security.krb5.internal.LoginOptions conformance: type shape via reflection (headless-safe).
 * @run main LoginOptionsTest
 */

import java.lang.reflect.Modifier;

public class LoginOptionsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.LoginOptions", false, LoginOptionsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.internal.KDCOptions"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
