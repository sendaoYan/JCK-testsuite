/*
 * @test
 * @summary com.sun.net.httpserver.Headers conformance: add/get/first/set.
 * @run main HeadersTest
 */

import java.lang.reflect.*;
import java.util.*;

public class HeadersTest {
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
        com.sun.net.httpserver.Headers h = new com.sun.net.httpserver.Headers();
        check(h.isEmpty(), "init empty");
        h.add("X-Test", "a");
        h.add("X-Test", "b");
        check(h.get("X-Test").size() == 2, "two values");
        check(h.getFirst("X-Test").equals("a"), "getFirst");
        h.set("X-Single", "v");
        check(h.getFirst("X-Single").equals("v"), "set");
        check(h.containsKey("X-Test"), "containsKey");
        check(h.size() == 2, "size 2");
        h.remove("X-Single");
        check(!h.containsKey("X-Single"), "removed");
        com.sun.net.httpserver.Headers h2 = com.sun.net.httpserver.Headers.of("K", "v1");
        check(h2.getFirst("K").equals("v1"), "of");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
