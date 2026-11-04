/*
 * @test
 * @summary java.util.IdentityHashMap 一致性：白盒（反射结构）+ 黑盒（引用相等语义）。
 * @run main IdentityHashMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractMap、实现 Map/Cloneable/Serializable、
 *   put/get/remove/containsKey/视图成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：IdentityHashMap 用引用相等 (==) 比较 key 与 value（get/containsKey/containsValue/
 *    remove/equals 全用 ==，而非 equals/hashCode）；允许 null key；不保证迭代顺序。
 * 2. 场景：new IdentityHashMap() -> 放入两个内容相等但引用不同的 String 作为两个 key ->
 *    get/containsKey/remove 按引用命中 -> containsValue 按引用命中 -> 视图联动 -> equals。
 * 3. 组合链路：放入 k1/k2（内容同为 "x"）-> 验证两条独立映射 -> 按引用 remove k1 -> 视图迭代。
 * 4. 不变量：size 区分引用不同但 equals 相等的 key；value 比较也用 ==。
 * 5. 边界：null key 允许；equals 的 key 但不同引用视为不同条目。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.IdentityHashMap;
import java.util.Map;

public class IdentityHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <K, V> IdentityHashMap<K, V> roundTrip(IdentityHashMap<K, V> m) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (IdentityHashMap<K, V>) ois.readObject();
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.IdentityHashMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "containsKey", Object.class), "containsKey");
        check(hasMethod(c, "keySet") && hasMethod(c, "entrySet") && hasMethod(c, "values"), "views");
    }

    static void testKeyIdentity() {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k1 = new String("x");
        String k2 = new String("x");
        m.put(k1, "v1");
        m.put(k2, "v2");
        check(m.size() == 2, "two equal-but-distinct keys are two mappings");
        check("v1".equals(m.get(k1)) && "v2".equals(m.get(k2)), "get by identity");
        check(m.get(new String("x")) == null, "get by equal-but-not-identical returns null");
        check(m.containsKey(k1) && !m.containsKey(new String("x")), "containsKey by identity");
        check(m.remove(new String("x")) == null, "remove by equal-but-not-identical no-op");
        check(m.size() == 2, "size unchanged after non-identical remove");
        m.remove(k1);
        check(m.size() == 1 && !m.containsKey(k1) && m.containsKey(k2), "remove by identity");
    }

    static void testValueIdentity() {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String v1 = new String("val");
        String v2 = new String("val");
        m.put("a", v1);
        check(m.containsValue(v1), "containsValue by identity (same ref)");
        check(!m.containsValue(v2), "containsValue: equal-but-not-identical value not found");
        check(!m.remove("a", v2), "remove(K,V): value not identical -> false");
        check(m.remove("a", v1), "remove(K,V): identical value -> true");
        check(m.isEmpty(), "entry removed");
    }

    static void testNullKeyAndViews() {
        IdentityHashMap<String, Integer> m = new IdentityHashMap<>();
        m.put(null, 1);
        m.put("k", 2);
        check(m.get(null) == 1, "null key allowed and retrievable");
        check(m.containsKey(null), "containsKey null");

        check(m.keySet().size() == 2, "keySet size");
        check(m.values().size() == 2, "values size");
        check(m.entrySet().size() == 2, "entrySet size");
        check(m.keySet().remove(null) && !m.containsKey(null), "keySet.remove reflects");
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            check(m.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        m.clear();
        check(m.isEmpty(), "clear");
    }

    static void testEqualsUsesIdentity() {
        String shared = new String("k");
        IdentityHashMap<String, String> a = new IdentityHashMap<>();
        a.put(shared, "v");
        IdentityHashMap<String, String> b = new IdentityHashMap<>();
        b.put(shared, "v");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode by identity");

        IdentityHashMap<String, String> c = new IdentityHashMap<>();
        c.put(new String("k"), new String("v"));
        check(!a.equals(c), "!equals when key/value differ by reference");
    }

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new IdentityHashMap<String, String>(-1),
                    "IdentityHashMap(-1) IAE");
        checkThrows(NullPointerException.class, () -> new IdentityHashMap<String, String>((Map<String, String>) null),
                    "IdentityHashMap(null Map) NPE");
    }

    static void testSerialization() throws Exception {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k = new String("k");
        m.put(k, "v");
        IdentityHashMap<String, String> r = roundTrip(m);
        check(r != m, "round-trip distinct object");
        check(r.size() == 1, "round-trip preserves size");
        check(!r.equals(m), "identity equals broken after round-trip");
        check(r.get(k) == null, "original key reference no longer retrievable");
        Map.Entry<String, String> e = r.entrySet().iterator().next();
        check(e.getKey() != k, "deserialized key is a distinct reference");
        check(e.getValue().equals("v"), "deserialized value present");
        r.put(new String("k2"), "v2");
        check(r.size() == 2 && m.size() == 1, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(IdentityHashMap.class);
        check(osc.getSerialVersionUID() == 8188218128353913216L, "IdentityHashMap serialVersionUID stable");
    }

    static void testBlackBox() {
        IdentityHashMap<String, String> sized = new IdentityHashMap<>(16);
        sized.put("k", "v");
        check(sized.get("k").equals("v"), "ctor (int) works");

        IdentityHashMap<String, String> src = new IdentityHashMap<>();
        String key = new String("x");
        src.put(key, "val");
        IdentityHashMap<String, String> copy = new IdentityHashMap<>(src);
        check(copy.size() == 1 && copy.get(key).equals("val"), "ctor (Map) copies by identity");
        check(copy.get(new String("x")) == null, "copy still uses identity for get");

        IdentityHashMap<String, String> dest = new IdentityHashMap<>();
        dest.putAll(src);
        check(dest.size() == 1 && dest.get(key).equals("val"), "putAll copies");

        // clone independent + equal
        IdentityHashMap<String, String> orig = new IdentityHashMap<>();
        String k = new String("k");
        orig.put(k, "v");
        @SuppressWarnings("unchecked")
        IdentityHashMap<String, String> cloned = (IdentityHashMap<String, String>) orig.clone();
        check(cloned != orig && cloned.equals(orig), "clone independent and equal");
        cloned.put(new String("k2"), "v2");
        check(orig.size() == 1, "clone structural independence");

        // forEach visits every mapping
        final int[] count = {0};
        orig.forEach((kk, vv) -> { count[0]++; });
        check(count[0] == 1, "forEach visits all mappings");

        // replaceAll transforms values in place
        IdentityHashMap<String, Integer> nums = new IdentityHashMap<>();
        String nk = new String("n");
        nums.put(nk, 1);
        nums.replaceAll((kk, vv) -> vv * 10);
        check(nums.get(nk) == 10, "replaceAll transforms values");

        // replace(K, oldValue, newValue) uses identity (==) comparison for the old value
        IdentityHashMap<String, String> r = new IdentityHashMap<>();
        String rk = new String("rk");
        String v1 = new String("val");
        String v2 = new String("val");
        r.put(rk, v1);
        check(!r.replace(rk, v2, "replaced"), "replace(K,old,new) equal-but-not-identical old returns false");
        check(r.replace(rk, v1, "replaced"), "replace(K,old,new) identical old returns true");
        check(r.get(rk).equals("replaced"), "replace(K,old,new) updated value");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testKeyIdentity();
        testValueIdentity();
        testNullKeyAndViews();
        testEqualsUsesIdentity();
        testBlackBox();
        testConstructorMatrix();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
