/*
 * @test
 * @summary com.sun.net.httpserver.HttpsServer conformance: hierarchy and declared members via reflection.
 * @run main HttpsServerTest
 */

public class HttpsServerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpsServer", false, null);
        check(c.getName().equals("com.sun.net.httpserver.HttpsServer"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.net.httpserver.HttpServer"), "superclass com.sun.net.httpserver.HttpServer");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("create"), "declares create");
        check(ms.contains("getHttpsConfigurator"), "declares getHttpsConfigurator");
        check(ms.contains("setHttpsConfigurator"), "declares setHttpsConfigurator");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
