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


    // ===================== 契约深化：类型检查/Name 合法性矩阵/clone 深拷贝/视图写通道 =====================
    static void testContracts() throws Exception {
        Attributes a = new Attributes();
        expectThrows(ClassCastException.class, () -> a.put("not-a-name", "v"));
        expectThrows(ClassCastException.class, () -> a.put(new Name("Ok-Name"), Integer.valueOf(42)));
        expectThrows(NullPointerException.class, () -> a.getValue((String) null));
        check(a.getValue((Name) null) == null, "getValue(null Name) returns null");
        expectThrows(IllegalArgumentException.class, () -> new Attributes(-1));
        new Attributes(0);   // 容量 0 合法（按默认处理）
        check(true, "Attributes(0) legal");
        // Name 合法性矩阵（实测：字母数字/_/-合法；空格/:/{/*/./@ 非法；长度 <=70）
        String[] valid = {"a", "ab", "Manifest-Version", "x", "digit123", "under_score", "hyphen-name",
                          "70charsxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"};
        for (String s : valid) {
            Name n = new Name(s);
            check(n.toString().equals(s), "Name valid: " + s);
        }
        String[] invalid = {"has space", "colon:", "brace{}", "dot.name", "star*", "at@"};
        for (String s : invalid) {
            expectThrows(IllegalArgumentException.class, () -> new Name(s));
        }
        expectThrows(NullPointerException.class, () -> new Name(null));
        // Name equals/hashCode 大小写不敏感
        Name n1 = new Name("AbC-d"), n2 = new Name("aBC-D");
        check(n1.equals(n2) && n1.hashCode() == n2.hashCode(), "Name case-insensitive equals/hashCode");
        check(!n1.equals(new Name("aBC-e")), "Name not equal different suffix");
        check(!n1.equals("AbC-d"), "Name not equal to String");
        // 常量 toString 精确
        check("Manifest-Version".equals(Name.MANIFEST_VERSION.toString()), "MANIFEST_VERSION text");
        check("Main-Class".equals(Name.MAIN_CLASS.toString()), "MAIN_CLASS text");
        check("Sealed".equals(Name.SEALED.toString()), "SEALED text");
        check("Content-Type".equals(Name.CONTENT_TYPE.toString()), "CONTENT_TYPE text");
        // clone 深拷贝
        Attributes orig = new Attributes();
        orig.putValue("Alpha", "1");
        Attributes copy = (Attributes) orig.clone();
        check(copy.getValue("Alpha").equals("1"), "clone copies mappings");
        orig.putValue("Beta", "2");
        check(copy.getValue("Beta") == null && copy.size() == 1, "clone is shallow-copied map, independent");
        // entrySet 迭代器 remove 反映回 map
        Attributes it = new Attributes();
        it.putValue("K1", "v1");
        it.putValue("K2", "v2");
        java.util.Iterator<Map.Entry<Object, Object>> i = it.entrySet().iterator();
        i.next();
        i.remove();
        check(it.size() == 1, "entrySet iterator.remove writes through");
        // putAll 另一 Attributes（大小写折叠）
        Attributes src2 = new Attributes();
        src2.putValue("Common", "x");
        Attributes dst = new Attributes();
        dst.putValue("COMMON", "y");
        dst.putAll(src2);
        check(dst.size() == 1 && "x".equals(dst.getValue("common")), "putAll case-collapses to source value");
        // Map 通用 containsKey 对 String 键（非 Name）不命中
        Attributes cks = new Attributes();
        cks.putValue("Foo", "bar");
        check(!cks.containsKey("Foo"), "containsKey(String) does not match");
        check(cks.containsKey(new Name("FOO")), "containsKey(Name) case-insensitive");
        check(cks.remove("Foo") == null, "remove(String) returns null without CCE (no match)");
    }

    public static void main(String[] args) throws Exception {
        testContracts();
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
