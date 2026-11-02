/*
 * @test
 * @summary com.sun.jndi.toolkit.url.Uri conformance: hierarchy and declared members via reflection.
 * @run main UriTest
 */

public class UriTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.toolkit.url.Uri", false, null);
        check(c.getName().equals("com.sun.jndi.toolkit.url.Uri"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("acceptsFragment"), "declares acceptsFragment");
        check(ms.contains("checkSchemeOnly"), "declares checkSchemeOnly");
        check(ms.contains("getHost"), "declares getHost");
        check(ms.contains("getPath"), "declares getPath");
        check(ms.contains("getPort"), "declares getPort");
        check(ms.contains("getQuery"), "declares getQuery");
        check(ms.contains("getScheme"), "declares getScheme");
        check(ms.contains("init"), "declares init");
        check(ms.contains("isSchemeOnly"), "declares isSchemeOnly");
        check(ms.contains("newInvalidURISchemeException"), "declares newInvalidURISchemeException");
        check(ms.contains("parse"), "declares parse");
        check(ms.contains("parseCompat"), "declares parseCompat");
        check(ms.contains("parseLegacy"), "declares parseLegacy");
        check(ms.contains("parseMode"), "declares parseMode");
        check(ms.contains("parseStrict"), "declares parseStrict");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
