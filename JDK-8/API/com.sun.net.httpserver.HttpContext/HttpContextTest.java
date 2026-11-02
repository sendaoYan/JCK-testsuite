/*
 * @test
 * @summary com.sun.net.httpserver.HttpContext conformance: hierarchy and declared members via reflection.
 * @run main HttpContextTest
 */

public class HttpContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpContext", false, null);
        check(c.getName().equals("com.sun.net.httpserver.HttpContext"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAttributes"), "declares getAttributes");
        check(ms.contains("getAuthenticator"), "declares getAuthenticator");
        check(ms.contains("getFilters"), "declares getFilters");
        check(ms.contains("getHandler"), "declares getHandler");
        check(ms.contains("getPath"), "declares getPath");
        check(ms.contains("getServer"), "declares getServer");
        check(ms.contains("setAuthenticator"), "declares setAuthenticator");
        check(ms.contains("setHandler"), "declares setHandler");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
