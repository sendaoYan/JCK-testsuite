/*
 * @test
 * @summary com.sun.net.httpserver.HttpServer conformance: hierarchy and declared members via reflection.
 * @run main HttpServerTest
 */

public class HttpServerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpServer", false, null);
        check(c.getName().equals("com.sun.net.httpserver.HttpServer"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("bind"), "declares bind");
        check(ms.contains("create"), "declares create");
        check(ms.contains("createContext"), "declares createContext");
        check(ms.contains("getAddress"), "declares getAddress");
        check(ms.contains("getExecutor"), "declares getExecutor");
        check(ms.contains("removeContext"), "declares removeContext");
        check(ms.contains("setExecutor"), "declares setExecutor");
        check(ms.contains("start"), "declares start");
        check(ms.contains("stop"), "declares stop");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
