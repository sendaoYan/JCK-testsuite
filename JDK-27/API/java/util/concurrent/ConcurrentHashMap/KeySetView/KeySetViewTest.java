/*
 * @test
 * @summary java.util.concurrent.ConcurrentHashMap$KeySetView conformance: white-box structure + black-box behavior.
 * @run main KeySetViewTest
 */

import java.util.*;
import java.util.concurrent.*;

public class KeySetViewTest {
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
        Class<?> c = cls("java.util.concurrent.ConcurrentHashMap$KeySetView");
        check(!c.isInterface() && java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(Set.class.isAssignableFrom(c), "implements Set");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        hasMethod(c, "add", Object.class);
        hasMethod(c, "remove", Object.class);
        hasMethod(c, "contains", Object.class);
        hasMethod(c, "getMappedValue");
        hasMethod(c, "getMap");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ConcurrentHashMap.KeySetView<String, Boolean> ks = ConcurrentHashMap.newKeySet();
        check(ks.isEmpty(), "newKeySet starts empty");
        check(ks.add("a"), "add returns true");
        check(!ks.add("a"), "add duplicate returns false");
        check(ks.contains("a") && ks.size() == 1, "contains/size");
        check(ks.remove("a") && ks.isEmpty(), "remove");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("x", 1);
        ConcurrentHashMap.KeySetView<String, Integer> view = map.keySet();
        check(view.contains("x") && !view.contains("y"), "keySet view contains");
        check(view.size() == 1, "keySet view size");
        check(view.getMap() == map, "getMap returns backing map");
        check(view.getMappedValue() == null, "keySet() view has null mapped value");
        boolean threw = false;
        try { view.add("y"); } catch (UnsupportedOperationException expected) { threw = true; }
        check(threw, "keySet() view add throws UnsupportedOperationException (null mapped value)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
