/*
 * @test
 * @summary com.sun.net.httpserver.Filter conformance: static factories.
 * @run main FilterTest
 */

import java.lang.reflect.*;
import java.util.*;

public class FilterTest {
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
        Class<?> c = load("com.sun.net.httpserver.Filter");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        check(m.contains("doFilter"), "doFilter");
        check(m.contains("description"), "description");
        check(m.contains("beforeHandler"), "beforeHandler");
        check(m.contains("afterHandler"), "afterHandler");
        check(m.contains("adaptRequest"), "adaptRequest");
        com.sun.net.httpserver.Filter f = com.sun.net.httpserver.Filter.beforeHandler("desc", ex -> {});
        check(f.description().equals("desc"), "beforeHandler description");
        com.sun.net.httpserver.Filter aft = com.sun.net.httpserver.Filter.afterHandler("after", ex -> {});
        check(aft.description().equals("after"), "afterHandler description");
        com.sun.net.httpserver.Filter r = com.sun.net.httpserver.Filter.adaptRequest("adapt", req -> req);
        check(r.description().equals("adapt"), "adaptRequest description");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
