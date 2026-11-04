/*
 * @test
 * @summary java.util.EnumMap 一致性：白盒（反射结构）+ 黑盒（枚举声明序 + 构造器 + null 语义）。
 * @run main EnumMapTest
 *
 * 白盒（结构）：Class.forName 加载、继承 AbstractMap、实现 Cloneable/Serializable、
 *   put/get/remove/containsKey/containsValue/视图/clone 成员形态。
 * 黑盒（行为）设计案：
 * 1. 契约：EnumMap 以单一枚举类型为 key 的有序 Map，键序固定为枚举声明序（ordinal）；
 *    key 不能为 null（put 抛 NPE），value 允许 null；底层用数组实现。
 * 2. 场景：new EnumMap(Color.class) -> put 乱序 -> keySet 按 ordinal 迭代 -> get/remove ->
 *    putIfAbsent/compute/merge -> EnumMap(EnumMap) 拷贝 -> EnumMap(Map) 拷贝 -> clone。
 * 3. 组合链路：乱序插入 3 个枚举键 -> 校验声明序迭代 -> 默认方法更新 -> 构造器拷贝/clone。
 * 4. 不变量：迭代顺序恒等于枚举声明序，与插入顺序无关；equals 与顺序无关。
 * 5. 边界/异常：put(null,...) 抛 NPE；get/remove/containsKey(null) 优雅返回 null/false；
 *    EnumMap(空 Map) 抛 IAE；null value 允许。
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EnumMapTest {
    enum Color { RED, GREEN, BLUE }

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.EnumMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass().getName().equals("java.util.AbstractMap"), "extends AbstractMap");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(hasMethod(c, "put", Object.class, Object.class), "put(K,V)");
        check(hasMethod(c, "get", Object.class), "get(Object)");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "containsKey", Object.class), "containsKey");
        check(hasMethod(c, "containsValue", Object.class), "containsValue");
        check(hasMethod(c, "keySet") && hasMethod(c, "values") && hasMethod(c, "entrySet"), "views");
        check(hasMethod(c, "clone"), "clone");
    }

    static void testOrderAndOps() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        check(map.isEmpty() && map.size() == 0, "empty");
        check(map.put(Color.BLUE, "blue") == null, "put new returns null");
        check(map.put(Color.RED, "red") == null, "put new returns null");
        map.put(Color.GREEN, "green");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "keySet iterated in declaration order");
        check(new ArrayList<>(map.values()).equals(Arrays.asList("red", "green", "blue")),
              "values follow key order");
        check(map.get(Color.RED).equals("red") && map.get(Color.BLUE).equals("blue"), "get");
        check(map.put(Color.RED, "RED").equals("red"), "put existing returns old");
        check(map.get(Color.RED).equals("RED"), "put updates");
        check(map.size() == 3, "size");
        check(map.containsKey(Color.GREEN) && !map.containsKey("GREEN"), "containsKey (absent non-enum)");
        check(map.containsValue("green") && !map.containsValue("nope"), "containsValue");
        check(map.remove(Color.GREEN).equals("green") && !map.containsKey(Color.GREEN), "remove");
        check(map.remove(Color.GREEN) == null, "remove absent returns null");
    }

    static void testNullHandling() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        expectThrows(() -> map.put(null, "x"), NullPointerException.class, "put null key");
        check(map.get(null) == null, "get null key returns null");
        check(map.remove(null) == null, "remove null key returns null");
        check(!map.containsKey(null), "containsKey null returns false");
        map.put(Color.RED, null);
        check(map.containsKey(Color.RED) && map.get(Color.RED) == null, "null value allowed");
    }

    static void testDefaultMethods() {
        EnumMap<Color, Integer> map = new EnumMap<>(Color.class);
        map.put(Color.RED, 1);
        check(map.getOrDefault(Color.RED, 0) == 1, "getOrDefault present");
        check(map.getOrDefault(Color.GREEN, 7) == 7, "getOrDefault missing");
        check(map.putIfAbsent(Color.RED, 99) == 1, "putIfAbsent present returns existing");
        check(map.putIfAbsent(Color.BLUE, 3) == null && map.get(Color.BLUE) == 3, "putIfAbsent inserts");
        check(map.replace(Color.RED, 10) == 1 && map.get(Color.RED) == 10, "replace");
        check(map.compute(Color.GREEN, (k, v) -> v == null ? 2 : v + 1) == 2, "compute absent");
        check(map.merge(Color.GREEN, 3, Integer::sum) == 5, "merge");
        map.replaceAll((k, v) -> v * 10);
        check(map.get(Color.RED) == 100 && map.get(Color.BLUE) == 30, "replaceAll");
        final int[] sum = {0};
        map.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 100 + 50 + 30, "forEach sums values");
    }

    static void testConstructorsAndClone() {
        EnumMap<Color, String> src = new EnumMap<>(Color.class);
        src.put(Color.RED, "red");
        src.put(Color.BLUE, "blue");

        EnumMap<Color, String> copy = new EnumMap<>(src);
        check(copy.equals(src) && copy != src, "EnumMap(EnumMap) copies");
        check(new ArrayList<>(copy.keySet()).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "copy preserves declaration order");

        Map<Color, String> plain = new java.util.HashMap<>();
        plain.put(Color.BLUE, "blue");
        plain.put(Color.RED, "red");
        EnumMap<Color, String> fromMap = new EnumMap<>(plain);
        check(fromMap.equals(src), "EnumMap(Map) copies content");
        check(new ArrayList<>(fromMap.keySet()).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "fromMap iteration in declaration order");

        expectThrows(() -> new EnumMap<Color, String>(new java.util.HashMap<>()),
                     IllegalArgumentException.class, "EnumMap(empty Map) throws IAE");

        EnumMap<Color, String> cloned = copy.clone();
        check(cloned.equals(copy) && cloned != copy, "clone deep copies");
        cloned.put(Color.GREEN, "green");
        check(!copy.containsKey(Color.GREEN), "clone independent of original");
    }

    @SuppressWarnings("unchecked")
    static <K extends Enum<K>, V> EnumMap<K, V> roundTrip(EnumMap<K, V> m) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (EnumMap<K, V>) ois.readObject();
        }
    }

    // ---- contract deep-dive (constructor matrix / null-key rejection /
    //      serialization / cross-impl equals) -------------------------------

    static void testConstructorMatrix() {
        expectThrows(() -> new EnumMap<Color, String>((Class<Color>) null), NullPointerException.class,
                     "EnumMap(null Class) NPE");
        expectThrows(() -> new EnumMap<Color, String>(new HashMap<Color, String>()),
                     IllegalArgumentException.class, "EnumMap(empty Map) IAE");
        Map<Color, String> nullKey = new HashMap<>();
        nullKey.put(null, "x");
        expectThrows(() -> new EnumMap<Color, String>(nullKey), NullPointerException.class,
                     "EnumMap(Map with null key) NPE");
    }

    static void testNullKeyRejectionExtended() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        expectThrows(() -> map.putIfAbsent(null, "x"), NullPointerException.class, "putIfAbsent(null) NPE");
        expectThrows(() -> map.computeIfAbsent(null, k -> "x"), NullPointerException.class, "computeIfAbsent(null) NPE");
        expectThrows(() -> map.compute(null, (k, v) -> "x"), NullPointerException.class, "compute(null) NPE");
        expectThrows(() -> map.merge(null, "x", (a, b) -> a), NullPointerException.class, "merge(null) NPE");
        check(!map.containsValue(null), "containsValue(null) returns false");
        check(map.getOrDefault(null, "D").equals("D"), "getOrDefault(null) returns default");
    }

    static void testSerialization() throws Exception {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "red");
        m.put(Color.GREEN, "green");
        EnumMap<Color, String> r = roundTrip(m);
        check(r != m && r.equals(m), "round-trip distinct and equal");
        r.put(Color.BLUE, "blue");
        check(r.size() == 3 && m.size() == 2, "round-trip result independent");
        check(new ArrayList<>(r.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "round-trip preserves declaration order");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(EnumMap.class);
        check(osc.getSerialVersionUID() == 458661240069192865L, "EnumMap serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        EnumMap<Color, String> em = new EnumMap<>(Color.class);
        em.put(Color.RED, "red");
        em.put(Color.BLUE, "blue");
        HashMap<Color, String> hm = new HashMap<>();
        hm.put(Color.BLUE, "blue");
        hm.put(Color.RED, "red");
        check(em.equals(hm) && hm.equals(em), "EnumMap/HashMap equals order-independent");
        check(em.hashCode() == hm.hashCode(), "hashCode equal across implementations");
        hm.put(Color.GREEN, "green");
        check(!em.equals(hm), "!equals different content");
    }

    static void testViews() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        map.put(Color.RED, "red");
        map.put(Color.GREEN, "green");
        map.put(Color.BLUE, "blue");
        check(map.keySet().size() == 3 && map.entrySet().size() == 3 && map.values().size() == 3,
              "view sizes");
        check(map.keySet().remove(Color.GREEN) && !map.containsKey(Color.GREEN), "keySet.remove reflects");
        for (Map.Entry<Color, String> e : map.entrySet()) {
            check(map.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }
        map.clear();
        check(map.isEmpty(), "clear");
    }

    static void testBlackBox() {
        // putAll copies all mappings from any Map with matching key type
        EnumMap<Color, String> target = new EnumMap<>(Color.class);
        target.put(Color.RED, "old");
        Map<Color, String> src = new java.util.HashMap<>();
        src.put(Color.GREEN, "green");
        src.put(Color.BLUE, "blue");
        src.put(Color.RED, "red");
        target.putAll(src);
        check(target.size() == 3, "putAll copies all mappings");
        check(target.get(Color.RED).equals("red") && target.get(Color.GREEN).equals("green")
              && target.get(Color.BLUE).equals("blue"), "putAll overwrites and inserts");
        check(new ArrayList<>(target.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "putAll keeps declaration order");

        // hashCode follows the Map contract: equal maps have equal hashCode
        EnumMap<Color, String> a = new EnumMap<>(Color.class);
        a.put(Color.RED, "red");
        a.put(Color.GREEN, "green");
        a.put(Color.BLUE, "blue");
        EnumMap<Color, String> b = new EnumMap<>(Color.class);
        b.put(Color.BLUE, "blue");
        b.put(Color.GREEN, "green");
        b.put(Color.RED, "red");
        check(a.equals(b), "equal EnumMaps are equal");
        check(a.hashCode() == b.hashCode(), "equal EnumMaps have equal hashCode");
        b.put(Color.BLUE, "changed");
        check(!a.equals(b), "different content not equal");
    }

    // ---- compute/merge full branch matrix -----------------------------------

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

    static void testComputeMergeMatrix() {
        EnumMap<Color, Integer> m = new EnumMap<>(Color.class);
        m.put(Color.RED, 1);

        // putIfAbsent / conditional remove / replace both forms / getOrDefault
        check(m.putIfAbsent(Color.RED, 9) == 1 && m.get(Color.RED) == 1, "putIfAbsent present keeps");
        check(m.putIfAbsent(Color.GREEN, 2) == null && m.get(Color.GREEN) == 2,
              "putIfAbsent absent inserts");
        check(!m.remove(Color.RED, 99), "remove(k,v) mismatched value false");
        check(m.remove(Color.RED, 1) && !m.containsKey(Color.RED), "remove(k,v) matched removes");
        check(m.replace(Color.GREEN, 20) == 2 && m.get(Color.GREEN) == 20, "replace(K,V) returns old");
        check(m.replace(Color.BLUE, 5) == null && !m.containsKey(Color.BLUE),
              "replace(K,V) absent no insert");
        check(!m.replace(Color.GREEN, 2, 21) && m.get(Color.GREEN) == 20,
              "replace(K,old,new) mismatch no-op");
        check(m.replace(Color.GREEN, 20, 21) && m.get(Color.GREEN) == 21,
              "replace(K,old,new) match replaces");
        check(m.getOrDefault(Color.GREEN, -1) == 21 && m.getOrDefault(Color.BLUE, -1) == -1,
              "getOrDefault");

        // computeIfAbsent
        m.clear();
        m.put(Color.RED, 1);
        check(m.computeIfAbsent(Color.GREEN, k -> 2) == 2 && m.get(Color.GREEN) == 2,
              "computeIfAbsent absent inserts");
        final int[] called = {0};
        check(m.computeIfAbsent(Color.RED, k -> { called[0]++; return 5; }) == 1,
              "computeIfAbsent present returns existing");
        check(called[0] == 0, "computeIfAbsent present does not invoke fn");
        check(m.computeIfAbsent(Color.BLUE, k -> null) == null, "computeIfAbsent null result null");
        check(!m.containsKey(Color.BLUE), "computeIfAbsent null result leaves no entry");
        RuntimeException boom = new RuntimeException("boom");
        checkPropagatesSame(boom, () -> m.computeIfAbsent(Color.BLUE, k -> { throw boom; }),
                "computeIfAbsent propagates fn exception");
        check(!m.containsKey(Color.BLUE) && m.size() == 2, "state intact after exception");

        // computeIfPresent
        check(m.computeIfPresent(Color.RED, (k, v) -> v + 10) == 11 && m.get(Color.RED) == 11,
              "computeIfPresent present computes");
        called[0] = 0;
        check(m.computeIfPresent(Color.BLUE, (k, v) -> { called[0]++; return 1; }) == null,
              "computeIfPresent absent returns null");
        check(called[0] == 0, "computeIfPresent absent does not invoke fn");
        check(m.computeIfPresent(Color.RED, (k, v) -> null) == null && !m.containsKey(Color.RED),
              "computeIfPresent null result removes");
        checkPropagatesSame(boom, () -> m.computeIfPresent(Color.GREEN, (k, v) -> { throw boom; }),
                "computeIfPresent propagates fn exception");
        check(m.get(Color.GREEN) == 2, "entry unchanged after computeIfPresent exception");

        // compute
        check(m.compute(Color.RED, (k, v) -> v == null ? 100 : v + 1) == 100,
              "compute absent inserts");
        check(m.compute(Color.RED, (k, v) -> v + 1) == 101, "compute present recomputes");
        check(m.compute(Color.RED, (k, v) -> null) == null && !m.containsKey(Color.RED),
              "compute null removes");
        checkPropagatesSame(boom, () -> m.compute(Color.GREEN, (k, v) -> { throw boom; }),
                "compute propagates fn exception");
        check(m.get(Color.GREEN) == 2, "entry unchanged after compute exception");

        // merge
        m.clear();
        called[0] = 0;
        check(m.merge(Color.RED, 1, (x, y) -> { called[0]++; return 5; }) == 1 && called[0] == 0,
              "merge absent puts value without fn");
        check(m.merge(Color.RED, 2, Integer::sum) == 3, "merge present remaps");
        check(m.merge(Color.RED, 4, (x, y) -> null) == null && !m.containsKey(Color.RED),
              "merge null result removes");
        m.put(Color.GREEN, 1);
        checkPropagatesSame(boom, () -> m.merge(Color.GREEN, 1, (x, y) -> { throw boom; }),
                "merge propagates fn exception");
        check(m.get(Color.GREEN) == 1, "entry unchanged after merge exception");
        checkThrows(NullPointerException.class, () -> m.merge(Color.BLUE, null, Integer::sum),
                    "merge null value NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent(Color.BLUE, null),
                    "computeIfAbsent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent(Color.BLUE, null),
                    "computeIfPresent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.compute(Color.BLUE, null),
                    "compute null fn NPE");
        checkThrows(NullPointerException.class, () -> m.merge(Color.BLUE, 1, null),
                    "merge null fn NPE");
    }

    // ---- branches specific to keys mapped to null ----------------------------

    static void testMappedToNullBranches() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, null);

        // get()==null but containsKey: computeIfAbsent still applies the function
        check(m.computeIfAbsent(Color.RED, k -> "x").equals("x"), "computeIfAbsent mapped-to-null calls fn");
        check(m.get(Color.RED).equals("x"), "mapped-to-null replaced by fn result");

        // remove(k, null) removes when the current value is null
        m.put(Color.GREEN, null);
        check(m.remove(Color.GREEN, null) && !m.containsKey(Color.GREEN),
              "remove(k, null) removes null-valued entry");
        check(m.containsValue(null) == false, "no null value remains");

        // replace(k, null, v) succeeds when mapped to null
        m.put(Color.BLUE, null);
        check(m.replace(Color.BLUE, null, "b"), "replace(k, null, v) on null value");
        check(m.get(Color.BLUE).equals("b"), "replaced from null");

        // merge treats a null-valued entry as absent: value inserted, fn not invoked
        m.put(Color.RED, null);
        final int[] called = {0};
        check("5".equals(m.merge(Color.RED, "5", (x, y) -> { called[0]++; return "fn"; })),
              "merge on mapped-to-null puts value");
        check(called[0] == 0, "merge on mapped-to-null does not invoke fn");
        check(m.get(Color.RED).equals("5"), "merge value stored");

        // non-matching old value leaves entry untouched
        check(!m.replace(Color.RED, "zz", "y") && m.get(Color.RED).equals("5"),
              "replace mismatch on mapped value no-op");
        // null-value queries
        check(m.containsValue(null) == false, "containsValue(null) false without null values");
        m.put(Color.GREEN, null);
        check(m.containsValue(null), "containsValue(null) true with null value");
    }

    // ---- wrong key type ------------------------------------------------------

    static void testWrongKeyType() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "r");
        @SuppressWarnings("rawtypes")
        Map raw = m;
        checkThrows(ClassCastException.class, () -> raw.put(new Object(), "x"),
                    "put(wrong key type) CCE");
        check(m.size() == 1, "failed put leaves state");
        check(raw.get(new Object()) == null, "get(wrong key type) null");
        check(!raw.containsKey(new Object()), "containsKey(wrong key type) false");
    }

    // ---- iterators are index-based (NOT fail-fast) ---------------------------

    static void testIteratorsNotFailFast() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "r");
        m.put(Color.GREEN, "g");
        java.util.Iterator<Color> it = m.keySet().iterator();
        check(Color.RED == it.next(), "first key");
        m.put(Color.BLUE, "b"); // higher-ordinal insertion mid-iteration
        check(Color.GREEN == it.next(), "second key after structural add");
        check(Color.BLUE == it.next(), "later-ordinal add is seen by in-flight iterator");
        check(!it.hasNext(), "iterator exhausted");
    }

    // ---- bulk operations on views --------------------------------------------

    static void testBulkViewOps() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "r");
        m.put(Color.GREEN, "g");
        m.put(Color.BLUE, "b");
        check(m.keySet().containsAll(Arrays.asList(Color.RED, Color.BLUE)), "keySet containsAll");
        check(m.keySet().retainAll(Arrays.asList(Color.RED, Color.GREEN)),
              "keySet retainAll modified");
        check(m.size() == 2 && !m.containsKey(Color.BLUE), "retainAll reflected");
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN)),
              "declaration order kept after retainAll");

        HashMap<Color, String> src = new HashMap<>();
        src.put(Color.BLUE, "bb");
        m.putAll(src);
        check(m.size() == 3 && m.get(Color.BLUE).equals("bb"), "putAll from HashMap");

        m.keySet().removeAll(Arrays.asList(Color.RED));
        check(m.size() == 2 && !m.containsKey(Color.RED), "keySet removeAll reflected");
    }

    // ---- empty map serialization + order after round-trip ---------------------

    static void testEmptyAndRoundTripOrder() throws Exception {
        EnumMap<Color, String> e = new EnumMap<>(Color.class);
        EnumMap<Color, String> back = roundTrip(e);
        check(back.isEmpty() && back.equals(e), "empty EnumMap round-trip");
        back.put(Color.GREEN, "g");
        check(back.size() == 1, "deserialized empty map usable");

        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.BLUE, "blue");
        m.put(Color.RED, "red");
        EnumMap<Color, String> r = roundTrip(m);
        check(new ArrayList<>(r.keySet()).equals(Arrays.asList(Color.RED, Color.BLUE)),
              "round-trip iterates in ordinal order regardless of insertion order");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOrderAndOps();
        testNullHandling();
        testDefaultMethods();
        testConstructorsAndClone();
        testViews();
        testBlackBox();
        testConstructorMatrix();
        testNullKeyRejectionExtended();
        testSerialization();
        testCrossImplEquals();
        testComputeMergeMatrix();
        testMappedToNullBranches();
        testWrongKeyType();
        testIteratorsNotFailFast();
        testBulkViewOps();
        testEmptyAndRoundTripOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
