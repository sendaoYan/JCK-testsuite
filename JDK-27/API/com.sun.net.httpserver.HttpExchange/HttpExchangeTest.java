/*
 * @test
 * @summary com.sun.net.httpserver.HttpExchange conformance: abstract exchange.
 * @run main HttpExchangeTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpExchangeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.net.httpserver.HttpExchange");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(interfaces(c).contains("com.sun.net.httpserver.Request"), "implements Request");
        Set<String> m = methodNames(c);
        String[] names = {"getRequestHeaders","getResponseHeaders","getRequestURI","getRequestMethod","getHttpContext","close","getRequestBody","getResponseBody","sendResponseHeaders","getRemoteAddress","getLocalAddress","getProtocol","getPrincipal"};
        for (String n : names) check(m.contains(n), n);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
