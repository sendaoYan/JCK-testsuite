/*
 * @test
 * @summary sun.security.jgss.krb5.Krb5MechFactory conformance: type shape via reflection (headless-safe).
 * @run main Krb5MechFactoryTest
 */

import java.lang.reflect.Modifier;

public class Krb5MechFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.krb5.Krb5MechFactory", false, Krb5MechFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
