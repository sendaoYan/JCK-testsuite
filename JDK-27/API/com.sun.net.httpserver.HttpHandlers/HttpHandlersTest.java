/*
 * @test
 * @summary com.sun.net.httpserver.HttpHandlers conformance: static factories.
 * @run main HttpHandlersTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HttpHandlersTest {
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
        Class<?> c = load("com.sun.net.httpserver.HttpHandlers");
        check(Modifier.isFinal(c.getModifiers()), "final");
        com.sun.net.httpserver.Headers h = new com.sun.net.httpserver.Headers();
        com.sun.net.httpserver.HttpHandler hd = com.sun.net.httpserver.HttpHandlers.of(200, h, "body");
        check(hd != null, "of non-null");
        com.sun.net.httpserver.HttpHandler h2 = com.sun.net.httpserver.HttpHandlers.handleOrElse(req -> true, hd, hd);
        check(h2 != null, "handleOrElse non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
