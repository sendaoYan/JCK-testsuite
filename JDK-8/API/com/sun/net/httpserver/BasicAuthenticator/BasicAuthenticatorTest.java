/*
 * @test
 * @summary com.sun.net.httpserver.BasicAuthenticator conformance: hierarchy and declared members via reflection.
 * @run main BasicAuthenticatorTest
 */

public class BasicAuthenticatorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.BasicAuthenticator", false, null);
        check(c.getName().equals("com.sun.net.httpserver.BasicAuthenticator"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.Authenticator"), "superclass com.sun.net.httpserver.Authenticator");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("authenticate"), "declares authenticate");
        check(ms.contains("checkCredentials"), "declares checkCredentials");
        check(ms.contains("getRealm"), "declares getRealm");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
