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
import java.util.ConcurrentModificationException;
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

    // ---- compute/merge full branch matrix ----------------------------------

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
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);

        // putIfAbsent: present keeps and returns existing; absent inserts
        check(m.putIfAbsent("a", 9) == 1 && m.get("a") == 1, "putIfAbsent present keeps");
        check(m.putIfAbsent("b", 2) == null && m.get("b") == 2, "putIfAbsent absent inserts");

        // remove(k, v): only removes when currently mapped to v
        check(!m.remove("a", 99), "remove(k,v) mismatched value false");
        check(m.get("a") == 1, "remove(k,v) mismatch leaves entry");
        check(m.remove("a", 1) && !m.containsKey("a"), "remove(k,v) matched removes");

        // replace(K,V): returns old value; absent key returns null and inserts nothing
        check(m.replace("b", 20) == 2 && m.get("b") == 20, "replace(K,V) returns old");
        check(m.replace("z", 5) == null && !m.containsKey("z"), "replace(K,V) absent no insert");

        // replace(K,old,new): conditional on exact old value
        check(!m.replace("b", 2, 21) && m.get("b") == 20, "replace(K,old,new) mismatch no-op");
        check(m.replace("b", 20, 21) && m.get("b") == 21, "replace(K,old,new) match replaces");
        check(!m.replace("z", 1, 2) && !m.containsKey("z"), "replace(K,old,new) absent false");

        // getOrDefault both branches
        check(m.getOrDefault("b", -1) == 21 && m.getOrDefault("z", -1) == -1, "getOrDefault");

        // computeIfAbsent: absent -> insert; present -> fn NOT invoked; null result -> no entry
        m.clear();
        m.put("a", 1);
        check(m.computeIfAbsent("b", k -> 2) == 2 && m.get("b") == 2, "computeIfAbsent absent inserts");
        final int[] called = {0};
        check(m.computeIfAbsent("a", k -> { called[0]++; return 5; }) == 1,
              "computeIfAbsent present returns existing");
        check(called[0] == 0, "computeIfAbsent present does not invoke fn");
        check(m.computeIfAbsent("c", k -> null) == null, "computeIfAbsent null result null");
        check(!m.containsKey("c"), "computeIfAbsent null result leaves no entry");
        RuntimeException boom = new RuntimeException("boom");
        checkPropagatesSame(boom, () -> m.computeIfAbsent("d", k -> { throw boom; }),
                "computeIfAbsent propagates fn exception");
        check(!m.containsKey("d") && m.size() == 2, "state intact after computeIfAbsent exception");

        // computeIfPresent: present computes; absent -> fn NOT invoked; null removes
        check(m.computeIfPresent("a", (k, v) -> v + 10) == 11 && m.get("a") == 11,
              "computeIfPresent present computes");
        called[0] = 0;
        check(m.computeIfPresent("zz", (k, v) -> { called[0]++; return 1; }) == null,
              "computeIfPresent absent returns null");
        check(called[0] == 0, "computeIfPresent absent does not invoke fn");
        check(m.computeIfPresent("a", (k, v) -> null) == null && !m.containsKey("a"),
              "computeIfPresent null result removes");
        checkPropagatesSame(boom, () -> m.computeIfPresent("b", (k, v) -> { throw boom; }),
                "computeIfPresent propagates fn exception");
        check(m.get("b") == 2, "entry unchanged after computeIfPresent exception");

        // compute: absent inserts (v==null); present recomputes; null removes
        check(m.compute("a", (k, v) -> v == null ? 100 : v + 1) == 100 && m.get("a") == 100,
              "compute absent inserts");
        check(m.compute("a", (k, v) -> v + 1) == 101, "compute present recomputes");
        check(m.compute("a", (k, v) -> null) == null && !m.containsKey("a"), "compute null removes");
        checkPropagatesSame(boom, () -> m.compute("b", (k, v) -> { throw boom; }),
                "compute propagates fn exception");
        check(m.get("b") == 2, "entry unchanged after compute exception");

        // merge: absent puts value (fn not invoked); present remaps; null removes
        m.clear();
        called[0] = 0;
        check(m.merge("a", 1, (x, y) -> { called[0]++; return 5; }) == 1 && called[0] == 0,
              "merge absent puts value without fn");
        check(m.merge("a", 2, Integer::sum) == 3, "merge present remaps");
        check(m.merge("a", 4, (x, y) -> null) == null && !m.containsKey("a"),
              "merge null result removes");
        m.put("b", 1);
        checkPropagatesSame(boom, () -> m.merge("b", 1, (x, y) -> { throw boom; }),
                "merge propagates fn exception");
        check(m.get("b") == 1, "entry unchanged after merge exception");

        // null remapping function / null merge value rejected up front
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent("k", null),
                    "computeIfAbsent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent("k", null),
                    "computeIfPresent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.compute("k", null), "compute null fn NPE");
        checkThrows(NullPointerException.class, () -> m.merge("k", 1, null), "merge null fn NPE");
        checkThrows(NullPointerException.class, () -> m.merge("k", null, Integer::sum),
                    "merge null value NPE");
    }

    // ---- view write channels + fail-fast -----------------------------------

    static void testViewWriteChannels() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);

        java.util.Iterator<String> ki = m.keySet().iterator();
        ki.next();
        ki.remove();
        check(m.size() == 2 && !m.containsKey("a"), "keySet iterator.remove reflects");

        java.util.Iterator<Integer> vi = m.values().iterator();
        vi.next();
        vi.remove();
        check(m.size() == 1, "values iterator.remove reflects");

        java.util.Iterator<Map.Entry<String, Integer>> ei = m.entrySet().iterator();
        Map.Entry<String, Integer> e = ei.next();
        check(e.setValue(30) == 3, "entry.setValue returns old");
        check(m.get("c") == 30, "entry.setValue writes back");
        ei.remove();
        check(m.isEmpty(), "entrySet iterator.remove reflects");

        // views reject add
        checkThrows(UnsupportedOperationException.class, () -> m.keySet().add("x"),
                    "keySet.add UOE");
        checkThrows(UnsupportedOperationException.class, () -> m.values().add(1),
                    "values.add UOE");
        checkThrows(UnsupportedOperationException.class,
                    () -> m.entrySet().add(new java.util.AbstractMap.SimpleEntry<>("x", 1)),
                    "entrySet.add UOE");

        // structural modification during iteration -> CME
        LinkedHashMap<String, Integer> ff = new LinkedHashMap<>();
        ff.put("a", 1);
        ff.put("b", 2);
        java.util.Iterator<String> it = ff.keySet().iterator();
        it.next();
        ff.put("c", 3);
        checkThrows(ConcurrentModificationException.class, it::next, "iterator CME after put");

        // iterator.remove() is the one permitted structural change mid-iteration
        LinkedHashMap<String, Integer> legal = new LinkedHashMap<>();
        legal.put("a", 1);
        legal.put("b", 2);
        legal.put("c", 3);
        java.util.Iterator<String> lit = legal.keySet().iterator();
        lit.next();
        lit.remove();
        check("b".equals(lit.next()) && "c".equals(lit.next()),
              "iteration continues after iterator.remove");
        check(legal.size() == 2, "size after iterator.remove");

        // entry.setValue is non-structural: no CME and iteration continues
        LinkedHashMap<String, Integer> mut = new LinkedHashMap<>();
        mut.put("a", 1);
        mut.put("b", 2);
        java.util.Iterator<Map.Entry<String, Integer>> mit = mut.entrySet().iterator();
        mit.next().setValue(10);
        check("b".equals(mit.next().getKey()), "entry.setValue mid-iteration no CME");
        check(mut.get("a") == 10, "mid-iteration setValue visible");
    }

    // ---- access-order fine-grained read-path semantics ----------------------

    static void testAccessOrderFineGrained() {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true);
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.putIfAbsent("a", 9); // value-reading path records access
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("b", "c", "a")),
              "putIfAbsent(present) records access");
        m.getOrDefault("b", 0);
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("c", "a", "b")),
              "getOrDefault records access");
        m.merge("c", 1, Integer::sum);
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("a", "b", "c")),
              "merge(present) records access");
        m.compute("c", (k, v) -> v + 1);
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("a", "b", "c")),
              "compute(present) records access");
        m.containsKey("a");
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("a", "b", "c")),
              "containsKey does NOT record access");
        m.containsValue(1);
        check(new ArrayList<>(m.keySet()).equals(Arrays.asList("a", "b", "c")),
              "containsValue does NOT record access");
    }

    // ---- Map.hashCode formula lock ------------------------------------------

    static void testHashCodeFormula() {
        // Map.hashCode = sum over entries of (key.hashCode() ^ value.hashCode())
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        int expected = ("a".hashCode() ^ Integer.hashCode(1)) + ("b".hashCode() ^ Integer.hashCode(2));
        check(expected == 192, "hand-computed formula constant");
        check(m.hashCode() == 192, "LinkedHashMap{a=1,b=2}.hashCode == 192");
        m.put("c", null);
        int withNull = 192 + ("c".hashCode() ^ 0);
        check(m.hashCode() == withNull, "null value contributes key.hashCode() ^ 0");
    }

    // ---- accessOrder flag survives serialization ----------------------------

    static void testSerializationAccessOrder() throws Exception {
        LinkedHashMap<String, Integer> m = new LinkedHashMap<>(16, 0.75f, true);
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        LinkedHashMap<String, Integer> r = roundTrip(m);
        check(r.equals(m), "access-order map round-trip equal");
        r.get("b"); // only reorders if accessOrder survived serialization
        check(new ArrayList<>(r.keySet()).equals(Arrays.asList("a", "c", "b")),
              "accessOrder=true preserved through serialization");
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
        testComputeMergeMatrix();
        testViewWriteChannels();
        testAccessOrderFineGrained();
        testHashCodeFormula();
        testSerializationAccessOrder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
