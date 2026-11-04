/*
 * @test
 * @summary java.util.WeakHashMap API conformance: full contract coverage.
 *          basic map operations, constructor matrix (negative capacity IAE, loadFactor NaN/<=0
 *          IAE, null Map NPE), null key/value allowance, live views (keySet/values/entrySet
 *          remove reflects), equals/hashCode order-independence, putAll, NOT Serializable and
 *          NOT Cloneable (structural fact), and state invariants. Weak-key GC timing is
 *          intentionally NOT asserted (nondeterministic).
 * @run main WeakHashMapTest
 */

import java.util.Map;
import java.util.WeakHashMap;

public class WeakHashMapTest {
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

    // ---- original coverage (kept) ------------------------------------------

    static void testWeakHashMap() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        check(m.size() == 2, "size");
        check(m.get("a") == 1, "get");
        check(m.containsKey("b"), "containsKey");
        check(m.remove("a") == 1, "remove");
        check(m.size() == 1, "size after remove");
    }

    // ---- constructor matrix + null semantics -------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new WeakHashMap<String, Integer>(-1),
                    "WeakHashMap(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> new WeakHashMap<String, Integer>(1, 0.0f),
                    "WeakHashMap(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new WeakHashMap<String, Integer>(1, Float.NaN),
                    "WeakHashMap(1, NaN) IAE");
        checkThrows(NullPointerException.class, () -> new WeakHashMap<String, Integer>((Map<String, Integer>) null),
                    "WeakHashMap(null Map) NPE");

        WeakHashMap<String, Integer> src = new WeakHashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        WeakHashMap<String, Integer> copy = new WeakHashMap<>(src);
        check(copy.equals(src) && copy.size() == 2, "WeakHashMap(Map) copies");
    }

    static void testNullSemantics() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put(null, 7);
        m.put("n", null);
        check(m.get(null) == 7 && m.containsKey(null), "null key allowed");
        check(m.get("n") == null && m.containsKey("n"), "null value allowed");
        check(m.containsValue(null), "containsValue(null)");
        m.remove(null);
        check(!m.containsKey(null), "remove null key");
    }

    // ---- views + equals + structural facts + invariants --------------------

    static void testViewsAndEquals() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        check(m.keySet().size() == 3 && m.values().size() == 3 && m.entrySet().size() == 3,
              "view sizes");
        check(m.keySet().remove("a") && !m.containsKey("a"), "keySet.remove reflects");
        check(m.values().remove(2) && !m.containsValue(2), "values.remove reflects");
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            check(m.get(e.getKey()).equals(e.getValue()), "entrySet matches map");
        }

        WeakHashMap<String, Integer> a = new WeakHashMap<>();
        a.put("x", 1);
        a.put("y", 2);
        WeakHashMap<String, Integer> b = new WeakHashMap<>();
        b.put("y", 2);
        b.put("x", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode order-independent");
        b.put("z", 3);
        check(!a.equals(b), "!equals different content");
    }

    static void testStructuralFacts() {
        // WeakHashMap is NOT Serializable and does NOT implement Cloneable.
        check(java.io.ObjectStreamClass.lookup(WeakHashMap.class) == null,
              "WeakHashMap is NOT Serializable");
        check(!java.lang.Cloneable.class.isAssignableFrom(WeakHashMap.class),
              "WeakHashMap does NOT implement Cloneable");
    }

    static void testStateInvariants() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("a", 10);
        check(m.size() == 2 && m.get("a") == 10, "re-put updates value without size change");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
        check(!m.containsKey("a") && !m.containsKey("b"), "clear removed keys");
    }

    // ---- compute/merge full branch matrix -----------------------------------

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
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);

        // putIfAbsent / conditional remove / replace both forms / getOrDefault
        check(m.putIfAbsent("a", 9) == 1 && m.get("a") == 1, "putIfAbsent present keeps");
        check(m.putIfAbsent("b", 2) == null && m.get("b") == 2, "putIfAbsent absent inserts");
        check(!m.remove("a", 99), "remove(k,v) mismatched value false");
        check(m.remove("a", 1) && !m.containsKey("a"), "remove(k,v) matched removes");
        check(m.replace("b", 20) == 2 && m.get("b") == 20, "replace(K,V) returns old");
        check(m.replace("z", 5) == null && !m.containsKey("z"), "replace(K,V) absent no insert");
        check(!m.replace("b", 2, 21) && m.get("b") == 20, "replace(K,old,new) mismatch no-op");
        check(m.replace("b", 20, 21) && m.get("b") == 21, "replace(K,old,new) match replaces");
        check(m.getOrDefault("b", -1) == 21 && m.getOrDefault("z", -1) == -1, "getOrDefault");

        // computeIfAbsent
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

        // computeIfPresent
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

        // compute
        check(m.compute("a", (k, v) -> v == null ? 100 : v + 1) == 100 && m.get("a") == 100,
              "compute absent inserts");
        check(m.compute("a", (k, v) -> v + 1) == 101, "compute present recomputes");
        check(m.compute("a", (k, v) -> null) == null && !m.containsKey("a"), "compute null removes");
        checkPropagatesSame(boom, () -> m.compute("b", (k, v) -> { throw boom; }),
                "compute propagates fn exception");
        check(m.get("b") == 2, "entry unchanged after compute exception");

        // merge
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
        checkThrows(NullPointerException.class, () -> m.merge("b", null, Integer::sum),
                    "merge null value NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent("k", null),
                    "computeIfAbsent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent("k", null),
                    "computeIfPresent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.compute("k", null), "compute null fn NPE");
        checkThrows(NullPointerException.class, () -> m.merge("k", 1, null), "merge null fn NPE");
    }

    // ---- iteration content, iterator write channels, fail-fast ---------------

    static void testIterationAndCme() {
        WeakHashMap<String, Integer> m = new WeakHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (java.util.Iterator<String> it = m.keySet().iterator(); it.hasNext(); )
            seen.add(it.next());
        check(seen.equals(new java.util.HashSet<>(java.util.Arrays.asList("a", "b", "c"))),
              "iteration visits exactly the keys (order-free)");

        // entrySet iterator.remove reflects
        java.util.Iterator<Map.Entry<String, Integer>> ei = m.entrySet().iterator();
        ei.next();
        ei.remove();
        check(m.size() == 2, "entrySet iterator.remove reflects");

        // keySet iterator.remove reflects
        java.util.Iterator<String> ki = m.keySet().iterator();
        ki.next();
        ki.remove();
        check(m.size() == 1, "keySet iterator.remove reflects");

        // values iterator.remove reflects
        java.util.Iterator<Integer> vi = m.values().iterator();
        vi.next();
        vi.remove();
        check(m.isEmpty(), "values iterator.remove drains");

        // fail-fast: structural modification during iteration -> CME
        WeakHashMap<String, Integer> ff = new WeakHashMap<>();
        ff.put("a", 1);
        ff.put("b", 2);
        java.util.Iterator<String> it = ff.keySet().iterator();
        it.next();
        ff.put("c", 3);
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                    "iterator CME after put");

        // iterator.remove() then keep iterating is legal
        WeakHashMap<String, Integer> legal = new WeakHashMap<>();
        legal.put("a", 1);
        legal.put("b", 2);
        legal.put("c", 3);
        java.util.Iterator<String> lit = legal.keySet().iterator();
        lit.next();
        lit.remove();
        check(lit.hasNext(), "iteration continues after iterator.remove");
    }

    // ---- cross-implementation equality (equals-based keys) --------------------

    static void testEqualsHashMap() {
        WeakHashMap<String, Integer> wh = new WeakHashMap<>();
        wh.put("a", 1);
        wh.put("b", 2);
        java.util.HashMap<String, Integer> hm = new java.util.HashMap<>();
        hm.put("b", 2);
        hm.put("a", 1);
        check(wh.equals(hm) && hm.equals(wh), "WeakHashMap/HashMap equals symmetric");
        check(wh.hashCode() == hm.hashCode(), "hashCode equal across implementations");

        // null-key content equality also holds
        WeakHashMap<String, Integer> wn = new WeakHashMap<>();
        wn.put(null, 1);
        wn.put("b", 2);
        java.util.HashMap<String, Integer> hn = new java.util.HashMap<>();
        hn.put(null, 1);
        hn.put("b", 2);
        check(wn.equals(hn) && wn.hashCode() == hn.hashCode(), "null-key content equals HashMap");
    }

    public static void main(String[] args) throws Exception {
        testWeakHashMap();
        testConstructorMatrix();
        testNullSemantics();
        testViewsAndEquals();
        testStructuralFacts();
        testStateInvariants();
        testComputeMergeMatrix();
        testIterationAndCme();
        testEqualsHashMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
