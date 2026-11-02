/*
 * @test
 * @summary javax.xml.ws.spi.http.HttpExchange conformance: abstract class hierarchy and methods.
 * @run main HttpExchangeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class HttpExchangeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.ws.spi.http.HttpExchange");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getRequestHeaders"), "has method getRequestHeaders");
        check(names.contains("getRequestHeader"), "has method getRequestHeader");
        check(names.contains("getResponseHeaders"), "has method getResponseHeaders");
        check(names.contains("addResponseHeader"), "has method addResponseHeader");
        check(names.contains("getRequestURI"), "has method getRequestURI");
        check(names.contains("getContextPath"), "has method getContextPath");
        check(names.contains("getRequestMethod"), "has method getRequestMethod");
        check(names.contains("getHttpContext"), "has method getHttpContext");
        check(names.contains("close"), "has method close");
        check(names.contains("getRequestBody"), "has method getRequestBody");
        check(names.contains("getResponseBody"), "has method getResponseBody");
        check(names.contains("setStatus"), "has method setStatus");
        check(names.contains("getRemoteAddress"), "has method getRemoteAddress");
        check(names.contains("getLocalAddress"), "has method getLocalAddress");
        check(names.contains("getProtocol"), "has method getProtocol");
        check(names.contains("getScheme"), "has method getScheme");
        check(names.contains("getPathInfo"), "has method getPathInfo");
        check(names.contains("getQueryString"), "has method getQueryString");
        check(names.contains("getAttribute"), "has method getAttribute");
        check(names.contains("getAttributeNames"), "has method getAttributeNames");
        check(names.contains("getUserPrincipal"), "has method getUserPrincipal");
        check(names.contains("isUserInRole"), "has method isUserInRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
