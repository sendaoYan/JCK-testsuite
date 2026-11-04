/*
 * @test
 * @summary javax.script.Bindings conformance: white-box structure + black-box behavior.
 * @run main BindingsTest
 */

import java.lang.reflect.*;
import java.util.*;

public class BindingsTest {
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

    static class MinBindings implements javax.script.Bindings {
        final Map<String, Object> map = new HashMap<>();
        public Object put(String name, Object value) { return map.put(name, value); }
        public void putAll(Map<? extends String, ? extends Object> m) { map.putAll(m); }
        public boolean containsKey(Object key) { return map.containsKey(key); }
        public Object get(Object key) { return map.get(key); }
        public Object remove(Object key) { return map.remove(key); }
        public int size() { return map.size(); }
        public boolean isEmpty() { return map.isEmpty(); }
        public boolean containsValue(Object value) { return map.containsValue(value); }
        public void clear() { map.clear(); }
        public Set<String> keySet() { return map.keySet(); }
        public Collection<Object> values() { return map.values(); }
        public Set<Map.Entry<String, Object>> entrySet() { return map.entrySet(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.script.Bindings");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "java.util.Map"), "extends java.util.Map");
        check(hasMethod(c, "put"), "declares put");
        check(hasMethod(c, "putAll"), "declares putAll");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "remove"), "declares remove");
    }

    static void testBlackBox() throws Exception {
        MinBindings b = new MinBindings();
        check(b.isEmpty(), "empty");
        check(b.put("k", "v") == null, "put returns previous null");
        check(b.size() == 1, "size 1");
        check("v".equals(b.get("k")), "get");
        check(b.containsKey("k"), "containsKey");
        check(b.containsValue("v"), "containsValue");

        // Map.put(K,V) resolves to put(String,Object) through the Bindings type.
        check(b.put("a", Integer.valueOf(1)) == null, "put returns previous null for new key");
        check(Integer.valueOf(1).equals(b.get("a")), "put stores value");

        Map<String, Object> m = new HashMap<>();
        m.put("x", "y");
        b.putAll(m);
        check(b.size() == 3, "putAll size");
        check("y".equals(b.get("x")), "putAll value");

        check(Integer.valueOf(1).equals(b.remove("a")), "remove returns value");
        check(!b.containsKey("a"), "removed absent");
        check(b.size() == 2, "size after remove");

        check(b.keySet().contains("k") && b.keySet().contains("x"), "keySet");
        check(b.values().size() == 2, "values size");
        check(b.entrySet().size() == 2, "entrySet size");

        b.clear();
        check(b.isEmpty(), "clear empties");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
