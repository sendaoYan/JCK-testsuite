/*
 * @test
 * @summary com.sun.net.httpserver.Filter.Chain conformance: invokes handler.
 * @run main ChainTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ChainTest {
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
        final int[] count = {0};
        java.util.List<com.sun.net.httpserver.Filter> filters = new java.util.ArrayList<>();
        com.sun.net.httpserver.Filter.Chain chain = new com.sun.net.httpserver.Filter.Chain(filters, ex -> { count[0]++; });
        chain.doFilter(null);
        check(count[0] == 1, "handler invoked once");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
