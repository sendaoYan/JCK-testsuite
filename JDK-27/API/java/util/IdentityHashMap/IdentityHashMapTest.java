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

    // ---- JDK-21+ override: remove(K,V) uses identity -------------------------

    static void checkPropagatesSame(RuntimeException marker, ThrowingRunnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": nothing was thrown");
        } catch (Throwable t) {
            if (t == marker) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected same " + marker.getClass().getSimpleName()
                    + " instance but got " + t.getClass().getName(), t);
        }
    }

    static void testRemoveIdentityOverride() {
        // JDK-21+ declares remove(Object,Object) with identity (==) value
        // comparison; JDK-8 inherited the Map default and used equals. Lock the
        // identity behavior and the structural fact of the override itself.
        try {
            IdentityHashMap.class.getDeclaredMethod("remove", Object.class, Object.class);
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("JDK-27 IdentityHashMap must declare remove(Object,Object)");
        }
        try {
            IdentityHashMap.class.getDeclaredMethod("replace", Object.class, Object.class, Object.class);
            passed++;
        } catch (NoSuchMethodException e) {
            throw new AssertionError("JDK-27 IdentityHashMap must declare replace(K,V,V)");
        }

        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k = new String("k");
        String v = new String("val");
        m.put(k, v);
        check(!m.remove(k, new String("val")), "remove(k, equal-but-distinct value) false (identity)");
        check(m.size() == 1, "remove(k,v) identity mismatch keeps entry");
        check(!m.remove(k, "other"), "remove(k,v) different value false");
        check(m.remove(k, v), "remove(k, identical value) true");
        check(m.isEmpty(), "remove(k,v) removed entry");
    }

    // ---- compute/merge matrix under identity key semantics -------------------

    static void testComputeMergeIdentity() {
        IdentityHashMap<String, Integer> m = new IdentityHashMap<>();
        String a = new String("a");
        m.put(a, 1);

        // same-reference key: present branches never invoke the function
        final int[] called = {0};
        check(m.computeIfAbsent(a, k -> { called[0]++; return 5; }) == 1,
              "computeIfAbsent same-ref key returns existing");
        check(called[0] == 0, "computeIfAbsent present does not invoke fn");
        check(m.computeIfAbsent(a, k -> null) == 1, "computeIfAbsent same-ref null-result still existing");

        // equal-but-distinct key: looked up by identity -> treated absent -> inserts a second mapping
        check(m.computeIfAbsent(new String("a"), k -> 42) == 42,
              "computeIfAbsent equal-distinct key inserts");
        check(m.size() == 2 && m.get(a) == 1, "two mappings for equal keys");
        check(m.get(new String("a")) == null, "neither mapping found via equal-distinct get");

        // putIfAbsent same split
        String b = new String("b");
        check(m.putIfAbsent(a, 9) == 1, "putIfAbsent same-ref present returns existing");
        check(m.putIfAbsent(b, 2) == null && m.size() == 3, "putIfAbsent new key inserts");

        // compute / computeIfPresent / merge on the same reference
        check(m.compute(a, (k, v) -> v + 1) == 2, "compute same-ref recomputes");
        check(m.compute(a, (k, v) -> null) == null && !m.containsKey(a), "compute null removes");
        check(m.computeIfPresent(new String("a"), (k, v) -> 99) == null,
              "computeIfPresent equal-distinct key treated absent");
        check(m.size() == 2, "unreachable equal-key mapping still occupies a slot");

        check(m.merge(b, 10, Integer::sum) == 12, "merge same-ref remaps");
        check(m.merge(b, 10, (x, y) -> null) == null && !m.containsKey(b), "merge null removes");
        m.put(b, 3);

        // exceptions propagate as the same instance and leave state intact
        RuntimeException boom = new RuntimeException("boom");
        checkPropagatesSame(boom, () -> m.computeIfAbsent(new String("z"), k -> { throw boom; }),
                "computeIfAbsent propagates fn exception");
        checkPropagatesSame(boom, () -> m.computeIfPresent(b, (k, v) -> { throw boom; }),
                "computeIfPresent propagates fn exception");
        check(m.get(b) == 3, "entry unchanged after computeIfPresent exception");
        checkPropagatesSame(boom, () -> m.compute(new String("y"), (k, v) -> { throw boom; }),
                "compute propagates fn exception");
        check(m.size() == 2, "size intact after exceptions");

        // null keys participate normally in identity semantics
        m.put(null, 7);
        check(m.computeIfAbsent(null, k -> 99) == 7, "computeIfAbsent null key present");
        check(m.compute(null, (k, v) -> v + 1) == 8, "compute null key updates");

        // null remapping functions rejected
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent("k", null),
                    "computeIfAbsent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent("k", null),
                    "computeIfPresent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.compute("k", null), "compute null fn NPE");
        checkThrows(NullPointerException.class, () -> m.merge("k", 1, null), "merge null fn NPE");
    }

    // ---- IdentityHashMap vs HashMap equals contract divergence ---------------

    static void testEqualsVsHashMap() {
        String shared = new String("k");
        IdentityHashMap<String, String> ih = new IdentityHashMap<>();
        ih.put(shared, new String("v"));
        java.util.HashMap<String, String> hm = new java.util.HashMap<>();
        hm.put(shared, new String("v"));
        check(!ih.equals(hm), "IdentityHashMap.equals(HashMap) false (identity-based)");
        // deliberate Map-contract violation (documented): the asymmetry is the
        // observable contract — HashMap looks its own keys up in the other map
        // and finds them because the key reference is shared.
        check(hm.equals(ih), "HashMap.equals(IdentityHashMap) true with shared key refs (asymmetric)");
        java.util.HashMap<String, String> hm2 = new java.util.HashMap<>();
        hm2.put(new String("k"), new String("v"));
        check(!hm2.equals(ih), "HashMap with distinct key refs not equal");

        // two identity maps with the very same key/value references ARE equal
        IdentityHashMap<String, String> a = new IdentityHashMap<>();
        a.put(shared, "v");
        IdentityHashMap<String, String> b = new IdentityHashMap<>();
        b.put(shared, "v");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "same references equal + same hashCode");
        check(a.hashCode() == a.hashCode(), "hashCode stable across calls");
    }

    // ---- views: fail-fast + write channels + toString ------------------------

    static void testViewsAndFailFast() {
        // fail-fast iterator: structural modification -> CME
        IdentityHashMap<String, Integer> m = new IdentityHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        java.util.Iterator<String> it = m.keySet().iterator();
        it.next();
        m.put("c", 3);
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                    "identity map iterator CME after put");

        // keySet iterator.remove reflects into the map
        IdentityHashMap<String, Integer> kr = new IdentityHashMap<>();
        kr.put("a", 1);
        kr.put("b", 2);
        java.util.Iterator<String> ki = kr.keySet().iterator();
        ki.next();
        ki.remove();
        check(kr.size() == 1, "keySet iterator.remove reflects");

        // values iterator.remove reflects
        java.util.Iterator<Integer> vi = kr.values().iterator();
        vi.next();
        vi.remove();
        check(kr.isEmpty(), "values iterator.remove drains");

        // entry.setValue writes back
        IdentityHashMap<String, Integer> sv = new IdentityHashMap<>();
        sv.put("k", 1);
        Map.Entry<String, Integer> e = sv.entrySet().iterator().next();
        check(e.setValue(5) == 1, "entry.setValue returns old");
        check(sv.get("k") == 5, "entry.setValue writes back");

        // getOrDefault uses identity lookup
        IdentityHashMap<String, Integer> gd = new IdentityHashMap<>();
        gd.put("k", 1);
        check(gd.getOrDefault("k", -1) == 1 && gd.getOrDefault(new String("k"), -1) == -1,
              "getOrDefault identity lookup");

        // toString is well-formed for a simple map (single entry: "{k=1}")
        IdentityHashMap<String, Integer> ts = new IdentityHashMap<>();
        ts.put("k", 1);
        check(ts.toString().equals("{k=1}"), "toString {k=1}");

        // views reject add
        checkThrows(UnsupportedOperationException.class, () -> m.keySet().add("x"),
                    "keySet.add UOE");
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
        testRemoveIdentityOverride();
        testComputeMergeIdentity();
        testEqualsVsHashMap();
        testViewsAndFailFast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
