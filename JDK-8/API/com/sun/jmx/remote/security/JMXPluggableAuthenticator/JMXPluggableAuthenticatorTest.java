/*
 * @test
 * @summary com.sun.jmx.remote.security.JMXPluggableAuthenticator conformance: hierarchy and declared members via reflection.
 * @run main JMXPluggableAuthenticatorTest
 */

public class JMXPluggableAuthenticatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.security.JMXPluggableAuthenticator", false, null);
        check(c.getName().equals("com.sun.jmx.remote.security.JMXPluggableAuthenticator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.remote.JMXAuthenticator"), "implements javax.management.remote.JMXAuthenticator");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("authenticate"), "declares authenticate");
        check(ms.contains("authenticationFailure"), "declares authenticationFailure");
        check(ms.contains("logException"), "declares logException");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
