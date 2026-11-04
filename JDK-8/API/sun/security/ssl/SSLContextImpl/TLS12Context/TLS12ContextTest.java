/*
 * @test
 * @summary sun.security.ssl.SSLContextImpl.TLS12Context conformance: type shape via reflection (headless-safe).
 * @run main TLS12ContextTest
 */

import java.lang.reflect.Modifier;

public class TLS12ContextTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.ssl.SSLContextImpl$TLS12Context", false, TLS12ContextTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.ssl.SSLContextImpl$AbstractTLSContext"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
