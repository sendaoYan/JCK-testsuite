/*
 * @test
 * @summary java.util.concurrent.ConcurrentMap conformance: white-box structure + black-box behavior.
 * @run main ConcurrentMapTest
 */

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

public class ConcurrentMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static Class<?> cls(String n) {
        try { return Class.forName(n); } catch (ClassNotFoundException e) { throw new AssertionError(e); }
    }

    static void hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); } catch (NoSuchMethodException e) { throw new AssertionError("missing " + name + " on " + c.getName(), e); }
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = cls("java.util.concurrent.ConcurrentMap");
        check(c.isInterface(), "ConcurrentMap is an interface");
        check(Map.class.isAssignableFrom(c), "extends Map");
        hasMethod(c, "putIfAbsent", Object.class, Object.class);
        hasMethod(c, "remove", Object.class, Object.class);
        hasMethod(c, "replace", Object.class, Object.class, Object.class);
        hasMethod(c, "replace", Object.class, Object.class);
        hasMethod(c, "getOrDefault", Object.class, Object.class);
        hasMethod(c, "computeIfAbsent", Object.class, Function.class);
        hasMethod(c, "computeIfPresent", Object.class, BiFunction.class);
        hasMethod(c, "compute", Object.class, BiFunction.class);
        hasMethod(c, "merge", Object.class, Object.class, BiFunction.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ConcurrentMap<String, Integer> m = new ConcurrentHashMap<>();
        check(m.putIfAbsent("k", 1) == null, "putIfAbsent inserts when absent");
        check(m.putIfAbsent("k", 2) == 1, "putIfAbsent returns existing");
        check(m.get("k") == 1, "value unchanged");
        check(m.replace("k", 1, 3), "replace(k, old, new) matching");
        check(!m.replace("k", 9, 9), "replace(k, old, new) non-matching false");
        check(m.replace("k", 4) == 3, "replace(k, v) returns old");
        check(m.remove("k", 4), "remove(k, v) matching true");
        check(m.isEmpty(), "empty after remove");
        check(m.getOrDefault("z", 0) == 0, "getOrDefault missing");
        check(m.computeIfAbsent("a", String::length) == 1, "computeIfAbsent computes");
        check(m.computeIfPresent("a", (k, v) -> v + 10) == 11, "computeIfPresent");
        check(m.compute("b", (k, v) -> (v == null ? 0 : v) + 5) == 5, "compute");
        check(m.merge("a", 100, Integer::sum) == 111, "merge combines");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
