/*
 * @test
 * @summary sun.security.krb5.internal.util.KrbDataInputStream conformance: type shape via reflection (headless-safe).
 * @run main KrbDataInputStreamTest
 */

import java.lang.reflect.Modifier;

public class KrbDataInputStreamTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.util.KrbDataInputStream", false, KrbDataInputStreamTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.BufferedInputStream"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
