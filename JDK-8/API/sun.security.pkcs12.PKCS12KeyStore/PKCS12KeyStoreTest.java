/*
 * @test
 * @summary sun.security.pkcs12.PKCS12KeyStore conformance: type shape via reflection (headless-safe).
 * @run main PKCS12KeyStoreTest
 */

import java.lang.reflect.Modifier;

public class PKCS12KeyStoreTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.pkcs12.PKCS12KeyStore", false, PKCS12KeyStoreTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.KeyStoreSpi"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
