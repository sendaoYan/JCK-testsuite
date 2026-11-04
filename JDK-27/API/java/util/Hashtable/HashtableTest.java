/*
 * @test
 * @summary java.util.Hashtable 一致性：白盒（反射结构）+ 黑盒（null 拒绝 + Enumeration）。
 * @run main HashtableTest
 *
 * 白盒（结构）：Class.forName 加载、继承 Dictionary、实现 Map/Cloneable/Serializable、
 *   put/get/remove/contains/containsKey/containsValue/keys/elements/默认方法成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：Hashtable 是线程安全哈希表（方法 synchronized），不允许 null key 也不允许
 *    null value（put/get/containsKey/contains/remove 以及被重写的默认方法均抛 NPE）。
 * 2. 场景：new Hashtable() -> put/get/remove -> contains/containsValue -> Enumeration keys/
 *    elements -> putIfAbsent/getOrDefault/replace/compute/merge -> 视图 -> equals/hashCode。
 * 3. 组合链路：先正常插入验证契约，再逐一触发 null 参数异常，最后验证 Enumeration 与视图。
 * 4. 不变量：contains(Object) 是 containsValue 的别名；equals/hashCode 与顺序无关。
 * 5. 边界/异常：接受 key/value 的方法对 null 抛 NPE；compute/merge 返回 null 时删除/不插入条目。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

public class HashtableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
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
    static <K, V> Hashtable<K, V> roundTrip(Hashtable<K, V> t) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(t); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Hashtable<K, V>) ois.readObject();
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Hashtable");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.Dictionary"), "extends Dictionary");
        check(Map.class.isAssignableFrom(c), "implements Map");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "contains", Object.class), "contains(Object)");
        check(hasMethod(c, "containsKey", Object.class), "containsKey(Object)");
        check(hasMethod(c, "containsValue", Object.class), "containsValue(Object)");
        check(hasMethod(c, "keys") && hasMethod(c, "elements"), "keys/elements (Enumeration)");
        check(hasMethod(c, "putIfAbsent", Object.class, Object.class), "putIfAbsent");
        check(hasMethod(c, "compute", Object.class, BiFunction.class), "compute");
        check(hasMethod(c, "merge", Object.class, Object.class, BiFunction.class), "merge");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testBasicContract() {
        Hashtable<String, Integer> t = new Hashtable<>();
        check(t.isEmpty() && t.size() == 0, "empty");
        check(t.put("a", 1) == null, "put new returns null");
        check(t.put("a", 2) == 1, "put existing returns old");
        check(t.put("b", 3) == null, "put another");
        check(t.get("a") == 2 && t.get("missing") == null, "get");
        check(t.remove("a") == 2 && !t.containsKey("a"), "remove");
        check(t.remove("a") == null, "remove absent returns null");
        check(t.size() == 1, "size");
    }

    static void testNullRejection() {
        Hashtable<String, Integer> t = new Hashtable<>();
        expectNPE(() -> t.put(null, 1), "put null key");
        expectNPE(() -> t.put("k", null), "put null value");
        expectNPE(() -> t.get(null), "get null");
        expectNPE(() -> t.containsKey(null), "containsKey null");
        expectNPE(() -> t.contains(null), "contains null");
        expectNPE(() -> t.containsValue(null), "containsValue null");
        expectNPE(() -> t.remove(null), "remove null");
        expectNPE(() -> t.remove("k", null), "remove(K,V) null value");
        expectNPE(() -> t.replace(null, 1), "replace null key");
        expectNPE(() -> t.replace("k", null), "replace(K,V) null value");
        expectNPE(() -> t.putIfAbsent(null, 1), "putIfAbsent null key");
        expectNPE(() -> t.putIfAbsent("k", null), "putIfAbsent null value");
        expectNPE(() -> t.getOrDefault(null, 1), "getOrDefault null key");
        expectNPE(() -> t.computeIfAbsent(null, k -> 1), "computeIfAbsent null key");
        expectNPE(() -> t.computeIfPresent(null, (k, v) -> 1), "computeIfPresent null key");
        expectNPE(() -> t.compute(null, (k, v) -> 1), "compute null key");
        expectNPE(() -> t.merge(null, 1, (a, b) -> a), "merge null key");
    }

    static void testContainsAndEnumeration() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.containsKey("a") && !t.containsKey("z"), "containsKey");
        check(t.containsValue(1) && !t.containsValue(99), "containsValue");
        check(t.contains(1) && t.contains(2), "contains aliases containsValue");
        check(!t.contains("a"), "contains('a') false because 'a' is key not value");

        int keyCount = 0, valCount = 0;
        for (Enumeration<String> e = t.keys(); e.hasMoreElements(); ) {
            String k = e.nextElement();
            check(t.containsKey(k), "enumeration key present");
            keyCount++;
        }
        for (Enumeration<Integer> e = t.elements(); e.hasMoreElements(); ) {
            check(t.containsValue(e.nextElement()), "enumeration value present");
            valCount++;
        }
        check(keyCount == 2 && valCount == 2, "enumeration counts");
    }

    static void testDefaultMethodsAndViews() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        check(t.getOrDefault("a", 0) == 1, "getOrDefault present");
        check(t.getOrDefault("z", 9) == 9, "getOrDefault missing");
        check(t.putIfAbsent("a", 5) == 1, "putIfAbsent present returns existing");
        check(t.putIfAbsent("n", 6) == null && t.get("n") == 6, "putIfAbsent absent inserts");
        check(t.replace("a", 10) == 1 && t.get("a") == 10, "replace(K,V)");
        check(t.replace("a", 10, 11) && t.get("a") == 11, "replace(K,old,new)");
        t.replaceAll((k, v) -> v * 2);
        check(t.get("a") == 22, "replaceAll");
        check(t.compute("n", (k, v) -> v + 1) == 13, "compute");
        check(t.merge("n", 3, Integer::sum) == 16, "merge");
        // 返回 null 的 compute/merge 语义：删除条目或保持不存在（不抛异常）
        t.put("tmp", 1);
        check(t.compute("tmp", (k, v) -> null) == null && !t.containsKey("tmp"),
              "compute null result removes");
        check(t.computeIfAbsent("fresh", k -> null) == null && !t.containsKey("fresh"),
              "computeIfAbsent null result does not insert");
        t.put("mrg", 1);
        check(t.merge("mrg", 2, (a, b) -> null) == null && !t.containsKey("mrg"),
              "merge null result removes");

        final int[] sum = {0};
        t.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 22 + 16, "forEach");

        check(t.keySet().size() == 2, "keySet");
        check(t.values().size() == 2, "values");
        check(t.entrySet().size() == 2, "entrySet");
        check(t.keySet().remove("n") && !t.containsKey("n"), "keySet.remove reflects");
        for (Map.Entry<String, Integer> e : t.entrySet()) {
            check(t.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        t.clear();
        check(t.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        Hashtable<String, Integer> a = new Hashtable<>();
        a.put("x", 1);
        a.put("y", 2);
        Hashtable<String, Integer> b = new Hashtable<>();
        b.put("y", 2);
        b.put("x", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode order-independent");
        b.put("z", 3);
        check(!a.equals(b), "!equals different content");
    }

    static void testBlackBox() {
        Hashtable<String, Integer> a = new Hashtable<>(16, 0.75f);
        a.put("x", 1);
        check(a.get("x") == 1, "ctor (int,float) works");
        Hashtable<String, Integer> b = new Hashtable<>(8);
        b.put("y", 2);
        check(b.get("y") == 2, "ctor (int) works");

        Hashtable<String, Integer> src = new Hashtable<>();
        src.put("a", 1);
        src.put("b", 2);
        Hashtable<String, Integer> dest = new Hashtable<>();
        dest.putAll(src);
        check(dest.equals(src) && dest.size() == 2, "putAll copies");

        // toString renders "{key=value, ...}" containing each mapping
        String s = src.toString();
        check(s.startsWith("{") && s.endsWith("}"), "toString wrapped in braces");
        check(s.contains("a=1") && s.contains("b=2"), "toString contains mappings");

        // rehash is protected; expose via a minimal subclass to assert it is callable and
        // preserves all mappings.
        class ExposedHashtable extends Hashtable<String, Integer> {
            void rehashExposed() { rehash(); }
        }
        ExposedHashtable ht = new ExposedHashtable();
        for (int i = 0; i < 100; i++) ht.put("k" + i, i);
        ht.rehashExposed();
        check(ht.size() == 100, "rehash preserves size");
        boolean allPresent = true;
        for (int i = 0; i < 100; i++) allPresent &= ht.get("k" + i) == i;
        check(allPresent, "rehash preserves all mappings");
    }

    // ---- contract deep-dive (constructor matrix / live views + fail-fast /
    //      clone / serialization / cross-impl equals / state invariants) ------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(-1),
                    "Hashtable(-1) IAE");
        Hashtable<String, Integer> zero = new Hashtable<>(0);
        zero.put("k", 1);
        check(zero.get("k") == 1, "Hashtable(0) usable");
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, 0.0f),
                    "Hashtable(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, -0.5f),
                    "Hashtable(1, -0.5f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, Float.NaN),
                    "Hashtable(1, NaN) IAE");
        checkThrows(NullPointerException.class, () -> new Hashtable<String, Integer>((Map<String, Integer>) null),
                    "Hashtable(null Map) NPE");
        Map<String, Integer> nullKey = new HashMap<>();
        nullKey.put(null, 1);
        checkThrows(NullPointerException.class, () -> new Hashtable<String, Integer>(nullKey),
                    "Hashtable(Map with null key) NPE");
        Map<String, Integer> nullValue = new HashMap<>();
        nullValue.put("k", null);
        checkThrows(NullPointerException.class, () -> new Hashtable<String, Integer>(nullValue),
                    "Hashtable(Map with null value) NPE");
        Map<String, Integer> src = new HashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        Hashtable<String, Integer> copy = new Hashtable<>(src);
        check(copy.size() == 2 && copy.get("a") == 1 && copy.get("b") == 2, "Hashtable(Map) copies");
    }

    static void testViewLiveAndFailFast() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.keySet().remove("a") && !t.containsKey("a"), "keySet.remove reflects");
        check(t.values().remove(2) && !t.containsValue(2), "values.remove reflects");
        t.put("c", 3);
        for (Map.Entry<String, Integer> e : t.entrySet()) {
            if (e.getKey().equals("c")) {
                check(e.setValue(30) == 3, "entry.setValue returns old");
                check(t.get("c") == 30, "entry.setValue reflects");
            }
        }
        t.keySet().clear();
        check(t.isEmpty(), "keySet.clear empties map");

        // view iterators are fail-fast
        Hashtable<String, Integer> ff = new Hashtable<>();
        ff.put("a", 1);
        ff.put("b", 2);
        Iterator<String> it = ff.keySet().iterator();
        it.next();
        ff.put("c", 3);
        checkThrows(ConcurrentModificationException.class, it::next, "keySet iterator CME");

        // Enumeration (keys()/elements()) is NOT fail-fast: nextElement() does not
        // check modCount; it drains without CME even after modification.
        Hashtable<String, Integer> m = new Hashtable<>();
        m.put("a", 1);
        m.put("b", 2);
        Enumeration<String> e = m.keys();
        m.put("c", 3);
        int drained = 0;
        while (e.hasMoreElements()) { check(m.containsKey(e.nextElement()), "enumeration after modify"); drained++; }
        check(drained >= 1, "enumeration drains without CME after modification");
    }

    static void testClone() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        @SuppressWarnings("unchecked")
        Hashtable<String, Integer> clone = (Hashtable<String, Integer>) t.clone();
        check(clone != t, "clone distinct object");
        check(clone.equals(t), "clone content equal");
        clone.put("b", 2);
        check(clone.size() == 2 && t.size() == 1, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        Hashtable<String, Integer> r = roundTrip(t);
        check(r != t, "round-trip distinct object");
        check(r.equals(t) && r.size() == 2, "round-trip content equal");
        r.put("c", 3);
        check(r.size() == 3 && t.size() == 2, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(Hashtable.class);
        check(osc.getSerialVersionUID() == 1421746759512286392L, "Hashtable serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        Hashtable<String, Integer> ht = new Hashtable<>();
        ht.put("a", 1);
        ht.put("b", 2);
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("b", 2);
        hm.put("a", 1);
        check(ht.equals(hm), "Hashtable.equals(HashMap) order-independent");
        check(hm.equals(ht), "HashMap.equals(Hashtable) order-independent");
        check(ht.hashCode() == hm.hashCode(), "hashCode equal across implementations");
        hm.put("c", 3);
        check(!ht.equals(hm), "!equals different content");
    }

    static void testStateInvariants() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        expectNPE(() -> t.put("c", null), "failed put leaves state");
        check(t.size() == 2 && t.containsKey("a") && t.containsKey("b") && !t.containsKey("c"),
              "state intact after failed put");
        t.clear();
        check(t.isEmpty() && t.size() == 0, "clear empties");
        check(!t.containsKey("a") && !t.containsKey("b"), "clear removed keys");

        // Hashtable.merge allows a null value argument (unlike put/putIfAbsent/replace):
        // a present key keeps/returns the remapping result, an absent key inserts nothing.
        Hashtable<String, Integer> mt = new Hashtable<>();
        mt.put("k", 5);
        check(mt.merge("k", null, (a, b) -> a) == 5 && mt.get("k") == 5,
              "merge null value on present key keeps old");
        check(mt.merge("absent", null, (a, b) -> a) == null && !mt.containsKey("absent"),
              "merge null value on absent key inserts nothing");
        expectNPE(() -> mt.merge("k", 1, null), "merge null remappingFunction");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBasicContract();
        testNullRejection();
        testContainsAndEnumeration();
        testDefaultMethodsAndViews();
        testEqualsHashCode();
        testBlackBox();
        testConstructorMatrix();
        testViewLiveAndFailFast();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
