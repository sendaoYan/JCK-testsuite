/*
 * @test
 * @summary sun.security.ssl.TrustManagerFactoryImpl.SimpleFactory conformance: type shape via reflection (headless-safe).
 * @run main SimpleFactoryTest
 */

import java.lang.reflect.Modifier;

public class SimpleFactoryTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.ssl.TrustManagerFactoryImpl$SimpleFactory", false, SimpleFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.security.ssl.TrustManagerFactoryImpl"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
