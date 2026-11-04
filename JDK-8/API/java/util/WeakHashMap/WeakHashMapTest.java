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

    public static void main(String[] args) throws Exception {
        testWeakHashMap();
        testConstructorMatrix();
        testNullSemantics();
        testViewsAndEquals();
        testStructuralFacts();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
