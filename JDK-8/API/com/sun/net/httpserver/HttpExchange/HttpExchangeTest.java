/*
 * @test
 * @summary com.sun.net.httpserver.HttpExchange conformance: hierarchy and declared members via reflection.
 * @run main HttpExchangeTest
 */

public class HttpExchangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpExchange", false, null);
        check(c.getName().equals("com.sun.net.httpserver.HttpExchange"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("close"), "declares close");
        check(ms.contains("getAttribute"), "declares getAttribute");
        check(ms.contains("getHttpContext"), "declares getHttpContext");
        check(ms.contains("getLocalAddress"), "declares getLocalAddress");
        check(ms.contains("getPrincipal"), "declares getPrincipal");
        check(ms.contains("getProtocol"), "declares getProtocol");
        check(ms.contains("getRemoteAddress"), "declares getRemoteAddress");
        check(ms.contains("getRequestBody"), "declares getRequestBody");
        check(ms.contains("getRequestHeaders"), "declares getRequestHeaders");
        check(ms.contains("getRequestMethod"), "declares getRequestMethod");
        check(ms.contains("getRequestURI"), "declares getRequestURI");
        check(ms.contains("getResponseBody"), "declares getResponseBody");
        check(ms.contains("getResponseCode"), "declares getResponseCode");
        check(ms.contains("getResponseHeaders"), "declares getResponseHeaders");
        check(ms.contains("sendResponseHeaders"), "declares sendResponseHeaders");
        check(ms.contains("setAttribute"), "declares setAttribute");
        check(ms.contains("setStreams"), "declares setStreams");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
