/*
 * @test
 * @summary java.util.jar.Attributes conformance: white-box structure + black-box behavior.
 * @run main AttributesTest
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

public class AttributesTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable action) {
        try {
            action.run();
            check(false, "expected " + type.getSimpleName());
        } catch (Throwable t) {
            check(type.isInstance(t),
                  "expected " + type.getSimpleName() + ", got " + t.getClass().getSimpleName());
        }
    }

    static boolean hasConstructor(Class<?> c, Class<?>... params) {
        try { c.getConstructor(params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.jar.Attributes");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()),
              "Attributes is concrete class");
        check(Map.class.isAssignableFrom(c), "implements java.util.Map");
        check(Cloneable.class.isAssignableFrom(c), "implements java.lang.Cloneable");

        check(hasConstructor(c), "Attributes()");
        check(hasConstructor(c, int.class), "Attributes(int)");
        check(hasConstructor(c, Attributes.class), "Attributes(Attributes)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "getValue", String.class), "getValue(String)");
        check(hasMethod(c, "getValue", Name.class), "getValue(Name)");
        check(hasMethod(c, "put", Object.class, Object.class), "put(Object,Object)");
        check(hasMethod(c, "putValue", String.class, String.class), "putValue(String,String)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "putAll", Map.class), "putAll(Map)");
        check(hasMethod(c, "clear"), "clear()");
        check(hasMethod(c, "size"), "size()");
        check(hasMethod(c, "isEmpty"), "isEmpty()");
        check(hasMethod(c, "keySet"), "keySet()");
        check(hasMethod(c, "values"), "values()");
        check(hasMethod(c, "entrySet"), "entrySet()");
        check(hasMethod(c, "equals", Object.class), "equals(Object)");
        check(hasMethod(c, "hashCode"), "hashCode()");
        check(hasMethod(c, "clone"), "clone()");
    }

    // ---- Part 2: black-box (behavioral) ----

    static void testBasics() {
        Attributes a = new Attributes();
        check(a.isEmpty() && a.size() == 0, "empty initially");
        a.put(Name.MANIFEST_VERSION, "1.0");
        check("1.0".equals(a.getValue(Name.MANIFEST_VERSION)), "put/getValue(Name)");
        check("1.0".equals(a.getValue("Manifest-Version")), "getValue(String)");
        check("1.0".equals(a.getValue("manifest-version")), "getValue(String) case-insensitive");
        check("1.0".equals(a.get(Name.MANIFEST_VERSION)), "get(Name) returns value");
        check(a.size() == 1, "size 1");
        check(a.containsKey(Name.MANIFEST_VERSION), "containsKey(Name)");
        check(a.containsValue("1.0"), "containsValue");
        check(a.keySet().contains(Name.MANIFEST_VERSION), "keySet contains");
        check(a.values().contains("1.0"), "values contains");
        check("1.0".equals(a.remove(Name.MANIFEST_VERSION)), "remove(Name) returns value");
        check(a.isEmpty(), "empty after remove");
    }

    static void testCaseInsensitive() {
        Attributes a = new Attributes();
        check(a.putValue("Foo", "bar") == null, "putValue returns null for new key");
        check("bar".equals(a.getValue("foo")), "getValue lowercase");
        check("bar".equals(a.getValue("FOO")), "getValue uppercase");
        check("bar".equals(a.getValue("Foo")), "getValue exact");
        check("bar".equals(a.putValue("Foo", "baz")), "putValue returns old value on replace");
        check("baz".equals(a.getValue("foo")) && a.size() == 1, "replace keeps single mapping");

        // duplicate-name case-insensitive behavior
        Attributes b = new Attributes();
        b.putValue("X-Header", "a");
        b.putValue("x-header", "b");
        check(b.size() == 1, "case-insensitive duplicate collapses to one mapping");
        check("b".equals(b.getValue("X-HEADER")), "latest duplicate value wins");
    }

    static void testMapViews() {
        Attributes a = new Attributes();
        a.putValue("Alpha", "1");
        a.putValue("Beta", "2");
        a.putValue("Gamma", "3");
        check(a.size() == 3, "size 3");
        check(a.containsKey(new Name("alpha")), "containsKey case-insensitive Name");
        check(a.containsValue("2"), "containsValue");
        check(a.get("missing") == null, "get unknown returns null");

        Set<Map.Entry<Object, Object>> es = a.entrySet();
        check(es.size() == 3, "entrySet size");
        List<String> names = new ArrayList<String>();
        for (Map.Entry<Object, Object> e : es) names.add(e.getKey().toString());
        java.util.Collections.sort(names);
        check(names.equals(java.util.Arrays.asList("Alpha", "Beta", "Gamma")),
              "entrySet keys sorted");

        Collection<Object> vs = a.values();
        check(vs.size() == 3 && vs.contains("1") && vs.contains("2") && vs.contains("3"),
              "values collection contents");
        check(a.keySet().size() == 3, "keySet size");
    }

    static void testCopyCloneEquality() {
        Attributes a = new Attributes();
        a.putValue("A", "1");
        a.putValue("B", "2");

        Attributes copy = new Attributes(a);
        check(copy.size() == 2 && "1".equals(copy.getValue("A")), "copy constructor content");
        copy.putValue("C", "3");
        check(a.size() == 2, "copy constructor is independent");

        Attributes clone = (Attributes) a.clone();
        check(clone != a && clone.equals(a) && clone.hashCode() == a.hashCode(),
              "clone equals/hashCode and distinct object");
        clone.clear();
        check(a.size() == 2 && clone.isEmpty(), "clone modification independent");

        Attributes same = new Attributes();
        same.putValue("A", "1");
        same.putValue("B", "2");
        check(a.equals(same) && a.hashCode() == same.hashCode(), "equals/hashCode for equal maps");
        same.putValue("D", "4");
        check(!a.equals(same), "equals for different maps");
    }

    static void testPutAllAndTypeChecks() {
        Attributes a = new Attributes();
        a.putValue("A", "1");
        Attributes b = new Attributes();
        b.putValue("B", "2");
        a.putAll(b);
        check(a.size() == 2 && "2".equals(a.getValue("B")), "putAll(Attributes) merges");

        final Attributes c = new Attributes();
        final Map<String, String> plain = new HashMap<String, String>();
        plain.put("X", "y");
        expectThrows(ClassCastException.class, new Runnable() {
            public void run() { c.putAll(plain); }
        });
        expectThrows(ClassCastException.class, new Runnable() {
            public void run() { c.put("key", "value"); }
        });
        expectThrows(ClassCastException.class, new Runnable() {
            public void run() { c.put(Name.MAIN_CLASS, Integer.valueOf(1)); }
        });
        check(c.get("string-key") == null, "get with non-Name key returns null");
    }

    static void testInvalidNames() {
        final Attributes a = new Attributes();
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { a.getValue(""); }
        });
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { a.getValue("bad name"); }
        });
        expectThrows(NullPointerException.class, new Runnable() {
            public void run() { a.getValue((String) null); }
        });
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 71; i++) sb.append('a');
        final String tooLong = sb.toString();
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { a.getValue(tooLong); }
        });
        expectThrows(IllegalArgumentException.class, new Runnable() {
            public void run() { a.putValue("bad name", "x"); }
        });
        check(a.isEmpty(), "no side effects from invalid names");
        check(a.toString() != null, "toString non-null");
    }

    static void testBlackBox() {
        testBasics();
        testCaseInsensitive();
        testMapViews();
        testCopyCloneEquality();
        testPutAllAndTypeChecks();
        testInvalidNames();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
