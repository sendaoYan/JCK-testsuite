/*
 * @test
 * @summary javax.script.SimpleBindings conformance: white-box structure + black-box behavior.
 * @run main SimpleBindingsTest
 */

import java.lang.reflect.*;
import java.util.*;

public class SimpleBindingsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.SimpleBindings");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(hasInterface(c, "javax.script.Bindings"), "implements Bindings");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "remove"), "declares remove");
    }

    static void testBlackBox() throws Exception {
        javax.script.SimpleBindings b = new javax.script.SimpleBindings();
        check(b.isEmpty(), "empty");
        b.put("k", "v");
        check(!b.isEmpty(), "not empty");
        check(b.size() == 1, "size");
        check("v".equals(b.get("k")), "get");
        check(b.containsKey("k"), "containsKey");
        check(b.containsValue("v"), "containsValue");
        check(b.keySet().contains("k"), "keySet");
        check(b.values().size() == 1, "values size");
        check(b.entrySet().size() == 1, "entrySet size");

        check("v".equals(b.put("k", "v2")), "put returns previous value");
        check("v2".equals(b.get("k")), "put replaces");

        Map<String, Object> m = new HashMap<>();
        m.put("a", Integer.valueOf(1));
        b.putAll(m);
        check(b.size() == 2, "putAll size");
        check(Integer.valueOf(1).equals(b.remove("a")), "remove returns value");
        check(b.size() == 1, "size after remove");

        // Key validation.
        boolean npe = false;
        try { b.put(null, "x"); } catch (NullPointerException e) { npe = true; }
        check(npe, "null key NPE");
        boolean iae = false;
        try { b.put("", "x"); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "empty key IAE");
        boolean cce = false;
        try { b.get(Integer.valueOf(1)); } catch (ClassCastException e) { cce = true; }
        check(cce, "non-String key ClassCastException");
        boolean npeCk = false;
        try { b.containsKey(null); } catch (NullPointerException e) { npeCk = true; }
        check(npeCk, "containsKey null NPE");
        boolean iaeRm = false;
        try { b.remove(""); } catch (IllegalArgumentException e) { iaeRm = true; }
        check(iaeRm, "remove empty key IAE");

        b.clear();
        check(b.isEmpty(), "clear empties");

        javax.script.SimpleBindings b2 = new javax.script.SimpleBindings(new HashMap<>());
        check(b2.isEmpty(), "ctor map empty");
        boolean npeCtor = false;
        try { new javax.script.SimpleBindings(null); } catch (NullPointerException e) { npeCtor = true; }
        check(npeCtor, "ctor null map NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
