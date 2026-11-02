/*
 * @test
 * @summary com.sun.net.httpserver.spi.HttpServerProvider conformance: hierarchy and declared members via reflection.
 * @run main HttpServerProviderTest
 */

public class HttpServerProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.spi.HttpServerProvider", false, null);
        check(c.getName().equals("com.sun.net.httpserver.spi.HttpServerProvider"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createHttpServer"), "declares createHttpServer");
        check(ms.contains("createHttpsServer"), "declares createHttpsServer");
        check(ms.contains("loadProviderAsService"), "declares loadProviderAsService");
        check(ms.contains("loadProviderFromProperty"), "declares loadProviderFromProperty");
        check(ms.contains("provider"), "declares provider");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
