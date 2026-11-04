/*
 * @test
 * @summary sun.security.krb5.internal.crypto.NullEType conformance: type shape via reflection (headless-safe).
 * @run main NullETypeTest
 */

import java.lang.reflect.Modifier;

public class NullETypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.NullEType", false, NullETypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.internal.crypto.EType"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
