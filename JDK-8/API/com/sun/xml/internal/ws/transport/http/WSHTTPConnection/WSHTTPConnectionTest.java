/*
 * @test
 * @summary com.sun.xml.internal.ws.transport.http.WSHTTPConnection conformance: reflection (kind, superclass, interfaces, methods).
 * @run main WSHTTPConnectionTest
 */

import java.lang.reflect.Modifier;


public class WSHTTPConnectionTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean has(String[] a, String s) { for (String x : a) if (s.equals(x)) return true; return false; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.xml.internal.ws.transport.http.WSHTTPConnection", false, WSHTTPConnectionTest.class.getClassLoader());
        check(c.getName().equals("com.sun.xml.internal.ws.transport.http.WSHTTPConnection"), "name");
        check(!c.isInterface(), "not interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass().getName().equals("com.oracle.webservices.internal.api.message.BasePropertySet"), "superclass com.oracle.webservices.internal.api.message.BasePropertySet");
        check(c.getInterfaces().length == 0, "no interfaces");
        java.util.Set<String> mn = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mn.add(m.getName());
        check(mn.contains("close"), "method close");
        check(mn.contains("getBaseAddress"), "method getBaseAddress");
        check(mn.contains("getContext"), "method getContext");
        check(mn.contains("getContextPath"), "method getContextPath");
        check(mn.contains("getCookie"), "method getCookie");
        check(mn.contains("getInput"), "method getInput");
        check(mn.contains("getOutput"), "method getOutput");
        check(mn.contains("getPathInfo"), "method getPathInfo");
        check(mn.contains("getProtocol"), "method getProtocol");
        check(mn.contains("getQueryString"), "method getQueryString");
        check(mn.contains("getRequestAttribute"), "method getRequestAttribute");
        check(mn.contains("getRequestHeader"), "method getRequestHeader");
        check(mn.contains("getRequestHeaderNames"), "method getRequestHeaderNames");
        check(mn.contains("getRequestHeaderValues"), "method getRequestHeaderValues");
        check(mn.contains("getRequestHeaders"), "method getRequestHeaders");
        check(mn.contains("getRequestMethod"), "method getRequestMethod");
        check(mn.contains("getRequestScheme"), "method getRequestScheme");
        check(mn.contains("getRequestURI"), "method getRequestURI");
        check(mn.contains("getResponseHeaders"), "method getResponseHeaders");
        check(mn.contains("getServerName"), "method getServerName");
        check(mn.contains("getServerPort"), "method getServerPort");
        check(mn.contains("getStatus"), "method getStatus");
        check(mn.contains("getUserPrincipal"), "method getUserPrincipal");
        check(mn.contains("getWebServiceContextDelegate"), "method getWebServiceContextDelegate");
        check(mn.contains("isClosed"), "method isClosed");
        check(mn.contains("isSecure"), "method isSecure");
        check(mn.contains("isUserInRole"), "method isUserInRole");
        check(mn.contains("setContentLengthResponseHeader"), "method setContentLengthResponseHeader");
        check(mn.contains("setContentTypeResponseHeader"), "method setContentTypeResponseHeader");
        check(mn.contains("setCookie"), "method setCookie");
        check(mn.contains("setResponseHeader"), "method setResponseHeader");
        check(mn.contains("setResponseHeaders"), "method setResponseHeaders");
        check(mn.contains("setStatus"), "method setStatus");
        java.util.Set<String> fn = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) fn.add(f.getName());
        check(fn.contains("INTERNAL_ERR"), "field INTERNAL_ERR");
        check(fn.contains("MALFORMED_XML"), "field MALFORMED_XML");
        check(fn.contains("OK"), "field OK");
        check(fn.contains("ONEWAY"), "field ONEWAY");
        check(fn.contains("UNSUPPORTED_MEDIA"), "field UNSUPPORTED_MEDIA");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
