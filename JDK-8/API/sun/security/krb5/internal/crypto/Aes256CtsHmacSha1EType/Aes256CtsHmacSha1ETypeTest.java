/*
 * @test
 * @summary sun.security.krb5.internal.crypto.Aes256CtsHmacSha1EType conformance: type shape via reflection (headless-safe).
 * @run main Aes256CtsHmacSha1ETypeTest
 */

import java.lang.reflect.Modifier;

public class Aes256CtsHmacSha1ETypeTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.Aes256CtsHmacSha1EType", false, Aes256CtsHmacSha1ETypeTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.krb5.internal.crypto.EType"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
