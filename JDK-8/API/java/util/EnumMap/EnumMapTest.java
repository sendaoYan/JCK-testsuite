/*
 * @test
 * @summary java.util.EnumMap API conformance: full contract coverage.
 *          keySet/values/entrySet iterated in enum declaration (ordinal) order regardless of
 *          insertion order, put/get/remove, null key rejection (put/putIfAbsent/computeIfAbsent/
 *          compute/merge NPE) vs graceful null-key queries (get/remove/containsKey/containsValue
 *          null/false), null value acceptance, constructor matrix (null Class NPE, empty Map IAE,
 *          null-key Map NPE, EnumMap/Map copy), default methods (getOrDefault/putIfAbsent/
 *          replace/compute/merge/replaceAll/forEach), live views, equals/hashCode order-
 *          independence + cross-implementation (HashMap), clone deep, serialization round-trip +
 *          serialVersionUID, and state invariants.
 * @run main EnumMapTest
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
    static <K extends Enum<K>, V> EnumMap<K, V> roundTrip(EnumMap<K, V> m) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (EnumMap<K, V>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testOrderAndOps() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        check(map.put(Color.BLUE, "blue") == null, "put new returns null");
        check(map.put(Color.RED, "red") == null, "put new returns null");
        map.put(Color.GREEN, "green");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "keySet iterated in ordinal order");
        check(new ArrayList<>(map.values()).equals(Arrays.asList("red", "green", "blue")),
              "values follow key order");
        check(map.get(Color.RED).equals("red") && map.get(Color.BLUE).equals("blue"), "get");
        check(map.put(Color.RED, "RED").equals("red"), "put existing returns old");
        check(map.get(Color.RED).equals("RED"), "put updates");
        check(map.size() == 3, "size");
        check(map.containsKey(Color.GREEN), "containsKey");
        check(map.containsValue("green"), "containsValue");
        check(!map.containsValue("nope"), "!containsValue");
        check(map.remove(Color.GREEN).equals("green") && !map.containsKey(Color.GREEN), "remove");
    }

    static void testNullHandling() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        checkThrows(NullPointerException.class, () -> map.put(null, "x"), "put(null) NPE");
        map.put(Color.RED, null);
        check(map.containsKey(Color.RED) && map.get(Color.RED) == null, "null value allowed");
    }

    static void testConstructors() {
        EnumMap<Color, String> src = new EnumMap<>(Color.class);
        src.put(Color.RED, "red");
        EnumMap<Color, String> copy = new EnumMap<>(src);
        check(copy.equals(src), "EnumMap(EnumMap) copies");

        Map<Color, String> plain = new HashMap<>();
        plain.put(Color.BLUE, "blue");
        EnumMap<Color, String> fromMap = new EnumMap<>(plain);
        check(fromMap.get(Color.BLUE).equals("blue"), "EnumMap(Map) copies");
    }

    // ---- constructor matrix + null-key rejection (extended) ----------------

    static void testConstructorMatrix() {
        checkThrows(NullPointerException.class, () -> new EnumMap<Color, String>((Class<Color>) null),
                    "EnumMap(null Class) NPE");
        checkThrows(IllegalArgumentException.class, () -> new EnumMap<Color, String>(new HashMap<Color, String>()),
                    "EnumMap(empty Map) IAE");
        Map<Color, String> nullKey = new HashMap<>();
        nullKey.put(null, "x");
        checkThrows(NullPointerException.class, () -> new EnumMap<Color, String>(nullKey),
                    "EnumMap(Map with null key) NPE");

        EnumMap<Color, String> src = new EnumMap<>(Color.class);
        src.put(Color.RED, "red");
        src.put(Color.BLUE, "blue");
        EnumMap<Color, String> copy = new EnumMap<>(src);
        check(copy.equals(src) && copy != src, "EnumMap(EnumMap) copies independently");
    }

    static void testNullKeyRejection() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        checkThrows(NullPointerException.class, () -> map.put(null, "x"), "put(null) NPE");
        checkThrows(NullPointerException.class, () -> map.putIfAbsent(null, "x"), "putIfAbsent(null) NPE");
        checkThrows(NullPointerException.class, () -> map.computeIfAbsent(null, k -> "x"), "computeIfAbsent(null) NPE");
        checkThrows(NullPointerException.class, () -> map.compute(null, (k, v) -> "x"), "compute(null) NPE");
        checkThrows(NullPointerException.class, () -> map.merge(null, "x", (a, b) -> a), "merge(null) NPE");

        // graceful null-key queries (no NPE)
        check(map.get(null) == null, "get(null) returns null");
        check(map.remove(null) == null, "remove(null) returns null");
        check(!map.containsKey(null), "containsKey(null) returns false");
        check(!map.containsValue(null), "containsValue(null) returns false");
        check(map.getOrDefault(null, "D").equals("D"), "getOrDefault(null) returns default");
    }

    // ---- default methods + views -------------------------------------------

    static void testDefaultMethods() {
        EnumMap<Color, Integer> map = new EnumMap<>(Color.class);
        map.put(Color.RED, 1);
        check(map.getOrDefault(Color.RED, 0) == 1, "getOrDefault present");
        check(map.getOrDefault(Color.GREEN, 7) == 7, "getOrDefault missing");
        check(map.putIfAbsent(Color.RED, 99) == 1, "putIfAbsent present returns existing");
        check(map.putIfAbsent(Color.BLUE, 3) == null && map.get(Color.BLUE) == 3, "putIfAbsent inserts");
        check(map.replace(Color.RED, 10) == 1 && map.get(Color.RED) == 10, "replace(K,V)");
        check(map.replace(Color.RED, 10, 11) && map.get(Color.RED) == 11, "replace(K,old,new)");
        check(map.compute(Color.GREEN, (k, v) -> v == null ? 2 : v + 1) == 2, "compute absent");
        check(map.merge(Color.GREEN, 3, Integer::sum) == 5, "merge");
        map.replaceAll((k, v) -> v * 10);
        check(map.get(Color.RED) == 110 && map.get(Color.BLUE) == 30, "replaceAll");
        final int[] sum = {0};
        map.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 110 + 50 + 30, "forEach sums values");
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

    // ---- clone + serialization + cross-impl equals + invariants ------------

    static void testClone() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "red");
        m.put(Color.BLUE, "blue");
        EnumMap<Color, String> clone = m.clone();
        check(clone != m && clone.equals(m), "clone distinct and equal");
        clone.put(Color.GREEN, "green");
        check(clone.size() == 3 && m.size() == 2, "clone structural independence");
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

    static void testStateInvariants() {
        EnumMap<Color, String> m = new EnumMap<>(Color.class);
        m.put(Color.RED, "red");
        m.put(Color.GREEN, "green");
        checkThrows(NullPointerException.class, () -> m.put(null, "x"), "failed put leaves state");
        check(m.size() == 2 && m.containsKey(Color.RED) && m.containsKey(Color.GREEN),
              "state intact after failed put");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
        check(!m.containsKey(Color.RED) && !m.containsKey(Color.GREEN), "clear removed keys");
    }

    public static void main(String[] args) throws Exception {
        testOrderAndOps();
        testNullHandling();
        testConstructors();
        testConstructorMatrix();
        testNullKeyRejection();
        testDefaultMethods();
        testViews();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
