/*
 * @test
 * @summary java.util.LinkedHashMap API conformance: full contract coverage.
 *          insertion-order iteration by default (re-put keeps position, get does not reorder),
 *          access-order iteration (get/put-update move entry to end), removeEldestEntry LRU
 *          eviction, constructor matrix (negative capacity IAE, loadFactor NaN/<=0 IAE, null
 *          Map NPE, Map copy preserves source encounter order), null key/value allowance, clone
 *          shallow, serialization round-trip + serialVersionUID, equals/hashCode order-
 *          independence + cross-implementation (HashMap), and state invariants.
 * @run main LinkedHashMapTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapTest {
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
    static <K, V> LinkedHashMap<K, V> roundTrip(LinkedHashMap<K, V> m) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(m); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (LinkedHashMap<K, V>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testInsertionOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("a", 10); // update, position unchanged
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "insertion order preserved");
        check(map.get("b") == 2, "get");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "get does not reorder in insertion mode");
        check(map.remove("b") == 2 && map.size() == 2, "remove");
    }

    static void testAccessOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.get("a"); // a is now most recently accessed
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("b", "c", "a")),
              "access order moves accessed entry to end");
        map.put("b", 20); // update also counts as access
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("c", "a", "b")),
              "put update moves entry to end");
    }

    static void testRemoveEldestEntry() {
        class LRU extends LinkedHashMap<Integer, String> {
            LRU() { super(16, 0.75f, true); }
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 2;
            }
        }
        LRU lru = new LRU();
        lru.put(1, "one");
        lru.put(2, "two");
        lru.put(3, "three"); // evicts eldest (key 1)
        check(lru.size() == 2 && !lru.containsKey(1), "removeEldestEntry evicts eldest");
        check(lru.containsKey(2) && lru.containsKey(3), "remaining entries present");
    }

    // ---- constructor matrix + null allowance -------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashMap<String, Integer>(-1),
                    "LinkedHashMap(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashMap<String, Integer>(1, 0.0f),
                    "LinkedHashMap(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new LinkedHashMap<String, Integer>(1, Float.NaN),
                    "LinkedHashMap(1, NaN) IAE");
        checkThrows(NullPointerException.class, () -> new LinkedHashMap<String, Integer>((Map<String, Integer>) null),
                    "LinkedHashMap(null Map) NPE");

        LinkedHashMap<String, Integer> src = new LinkedHashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        src.put("c", 3);
        LinkedHashMap<String, Integer> copy = new LinkedHashMap<>(src);
        check(new ArrayList<>(copy.keySet()).equals(Arrays.asList("a", "b", "c")),
              "LinkedHashMap(Map) preserves source order");
        check(copy.equals(src), "LinkedHashMap(Map) copies content");
    }

    static void testNullAllowance() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put(null, 100);
        map.put("n", null);
        check(map.get(null) == 100 && map.get("n") == null, "null key/value allowed");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(null, "n")),
              "null key inserted in position");
        check(map.containsKey(null) && map.containsValue(null), "null query");
    }

    // ---- clone + serialization + cross-impl equals + invariants ------------

    static void testClone() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Integer> clone = (LinkedHashMap<String, Integer>) m.clone();
        check(clone != m && clone.equals(m), "clone distinct and equal");
        check(new ArrayList<>(clone.keySet()).equals(Arrays.asList("a", "b")), "clone preserves order");
        clone.put("c", 3);
        check(clone.size() == 3 && m.size() == 2, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        LinkedHashMap<String, Integer> r = roundTrip(m);
        check(r != m && r.equals(m), "round-trip distinct and equal");
        check(new ArrayList<>(r.keySet()).equals(Arrays.asList("a", "b")), "round-trip preserves order");
        r.put("c", 3);
        check(r.size() == 3 && m.size() == 2, "round-trip result independent");
        java.io.ObjectStreamClass osc = java.io.ObjectStreamClass.lookup(LinkedHashMap.class);
        check(osc.getSerialVersionUID() == 3801124242820219131L, "LinkedHashMap serialVersionUID stable");
    }

    static void testCrossImplEquals() {
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>();
        lhm.put("a", 1);
        lhm.put("b", 2);
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("b", 2);
        hm.put("a", 1);
        check(lhm.equals(hm) && hm.equals(lhm), "LinkedHashMap/HashMap equals order-independent");
        check(lhm.hashCode() == hm.hashCode(), "hashCode equal across implementations");
        hm.put("c", 3);
        check(!lhm.equals(hm), "!equals different content");
    }

    static void testStateInvariants() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("a", 10);
        check(m.size() == 2 && m.get("a") == 10, "re-put updates value without size change");
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("a", "b")),
              "re-put keeps original position");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
        check(!m.containsKey("a") && !m.containsKey("b"), "clear removed keys");
    }

    public static void main(String[] args) throws Exception {
        testInsertionOrder();
        testAccessOrder();
        testRemoveEldestEntry();
        testConstructorMatrix();
        testNullAllowance();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
