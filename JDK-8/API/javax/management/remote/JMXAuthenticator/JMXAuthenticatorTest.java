/*
 * @test
 * @summary javax.management.remote.JMXAuthenticator conformance: interface reflection.
 * @run main JMXAuthenticatorTest
 */

public class JMXAuthenticatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXAuthenticator");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "authenticate"), "declares authenticate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
