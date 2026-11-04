/*
 * @test
 * @summary java.util.Hashtable API conformance: full contract coverage.
 *          put/get/remove + null key/value rejection (NullPointerException) on every
 *          entry point incl. overridden default methods, contains/containsKey/containsValue
 *          + legacy contains() alias, key/value/entry views (live + fail-fast), keys()/
 *          elements() Enumeration (order-free + fail-fast), constructor matrix (negative/
 *          zero capacity, loadFactor <=0/NaN IAE, null Map/entry NPE, copy), equals/
 *          hashCode order-independence + cross-implementation equality, clone shallow,
 *          serialization round-trip + serialVersionUID, and state invariants.
 * @run main HashtableTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ConcurrentModificationException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class HashtableTest {
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

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    @SuppressWarnings("unchecked")
    static <K, V> Hashtable<K, V> roundTrip(Hashtable<K, V> t) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(t); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (Hashtable<K, V>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testPutGetRemove() {
        Hashtable<String, Integer> t = new Hashtable<>();
        check(t.isEmpty() && t.size() == 0, "empty");
        check(t.put("a", 1) == null, "put new returns null");
        check(t.put("a", 2) == 1, "put existing returns old");
        check(t.get("a") == 2, "get");
        check(t.get("missing") == null, "get missing");
        check(t.remove("a") == 2 && !t.containsKey("a"), "remove");
        check(t.put("b", 3) == null && t.size() == 1, "size after put");
    }

    static void testNullRejection() {
        Hashtable<String, Integer> t = new Hashtable<>();
        expectNPE(() -> t.put(null, 1), "put null key");
        expectNPE(() -> t.put("k", null), "put null value");
        expectNPE(() -> t.get(null), "get null key");
        expectNPE(() -> t.containsKey(null), "containsKey null");
        expectNPE(() -> t.contains(null), "contains null");
        expectNPE(() -> t.remove(null), "remove null key");
    }

    static void testContains() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.containsKey("a") && t.containsKey("b"), "containsKey");
        check(!t.containsKey("z"), "!containsKey");
        check(t.containsValue(1) && t.containsValue(2), "containsValue");
        // Legacy Hashtable.contains(Object) is an alias for containsValue, not containsKey.
        check(t.contains(1) && t.contains(2), "contains (value)");
        check(!t.contains(999), "!contains");
    }

    static void testViews() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        t.put("c", 3);
        check(t.keySet().size() == 3 && t.keySet().contains("a"), "keySet");
        check(t.values().size() == 3, "values");
        check(t.entrySet().size() == 3, "entrySet");
        t.clear();
        check(t.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        Hashtable<String, Integer> a = new Hashtable<>();
        a.put("x", 1);
        Hashtable<String, Integer> b = new Hashtable<>();
        b.put("x", 1);
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equals/hashCode");
    }

    // ---- constructor matrix -------------------------------------------------

    static void testConstructorMatrix() {
        // Hashtable() default
        Hashtable<String, Integer> d = new Hashtable<>();
        d.put("k", 1);
        check(d.get("k") == 1, "default ctor usable");

        // Hashtable(int): negative IAE, zero usable
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(-1),
                    "Hashtable(-1) IAE");
        Hashtable<String, Integer> zero = new Hashtable<>(0);
        zero.put("k", 1);
        check(zero.get("k") == 1, "Hashtable(0) usable");

        // Hashtable(int, float): loadFactor <= 0 or NaN IAE
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, 0.0f),
                    "Hashtable(1, 0.0f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, -0.5f),
                    "Hashtable(1, -0.5f) IAE");
        checkThrows(IllegalArgumentException.class, () -> new Hashtable<String, Integer>(1, Float.NaN),
                    "Hashtable(1, NaN) IAE");
        Hashtable<String, Integer> lf = new Hashtable<>(1, 0.5f);
        lf.put("k", 1);
        check(lf.get("k") == 1, "Hashtable(int, float) usable");

        // Hashtable(Map): null map NPE, null key/value NPE, copy
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

    // ---- extended null rejection (overridden default methods) --------------

    static void testNullRejectionExtended() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("k", 1);
        expectNPE(() -> t.containsValue(null), "containsValue null");
        expectNPE(() -> t.remove("k", null), "remove(K,V) null value");
        expectNPE(() -> t.replace(null, 1), "replace null key");
        expectNPE(() -> t.replace("k", null), "replace(K,V) null value");
        expectNPE(() -> t.replace(null, 1, 2), "replace(K,old,new) null key");
        expectNPE(() -> t.replace("k", 1, null), "replace(K,old,new) null new value");
        expectNPE(() -> t.putIfAbsent(null, 1), "putIfAbsent null key");
        expectNPE(() -> t.putIfAbsent("k", null), "putIfAbsent null value");
        expectNPE(() -> t.getOrDefault(null, 1), "getOrDefault null key");
        expectNPE(() -> t.computeIfAbsent(null, k -> 1), "computeIfAbsent null key");
        expectNPE(() -> t.computeIfPresent(null, (k, v) -> 1), "computeIfPresent null key");
        expectNPE(() -> t.compute(null, (k, v) -> 1), "compute null key");
        expectNPE(() -> t.merge(null, 1, (a, b) -> a), "merge null key");
        expectNPE(() -> t.merge("k", 1, null), "merge null remappingFunction");
    }

    // ---- Enumeration + fail-fast -------------------------------------------

    static void testEnumerationAndFailFast() {
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);

        int keys = 0, vals = 0;
        for (Enumeration<String> e = t.keys(); e.hasMoreElements(); ) {
            check(t.containsKey(e.nextElement()), "keys enumeration matches");
            keys++;
        }
        for (Enumeration<Integer> e = t.elements(); e.hasMoreElements(); ) {
            check(t.containsValue(e.nextElement()), "elements enumeration matches");
            vals++;
        }
        check(keys == 2 && vals == 2, "enumeration counts");

        // Enumeration (keys()/elements()) is NOT fail-fast (nextElement() does not
        // check modCount); only the view Iterators are fail-fast. Lock that in.
        Hashtable<String, Integer> m = new Hashtable<>();
        m.put("a", 1);
        m.put("b", 2);
        Enumeration<String> e = m.keys();
        m.put("c", 3);
        int seen = 0;
        while (e.hasMoreElements()) { check(m.containsKey(e.nextElement()), "enumeration after modify"); seen++; }
        check(seen >= 1, "enumeration drains without CME after modification");
    }

    // ---- live views --------------------------------------------------------

    static void testViewLive() {
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
        check(t.isEmpty(), "map empty after view clear");

        // view iterators are fail-fast
        Hashtable<String, Integer> ff = new Hashtable<>();
        ff.put("a", 1);
        ff.put("b", 2);
        java.util.Iterator<String> it = ff.keySet().iterator();
        it.next();
        ff.put("c", 3);
        checkThrows(ConcurrentModificationException.class, it::next, "keySet iterator CME");
    }

    // ---- clone + serialization + cross-impl equals + invariants -------------

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
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);

        // putIfAbsent: present keeps and returns existing; absent inserts
        check(t.putIfAbsent("a", 9) == 1 && t.get("a") == 1, "putIfAbsent present keeps");
        check(t.putIfAbsent("b", 2) == null && t.get("b") == 2, "putIfAbsent absent inserts");

        // remove(k, v): only removes when currently mapped to v
        check(!t.remove("a", 99), "remove(k,v) mismatched value false");
        check(t.get("a") == 1, "remove(k,v) mismatch leaves entry");
        check(t.remove("a", 1) && !t.containsKey("a"), "remove(k,v) matched removes");

        // replace(K,V): returns old; absent returns null without inserting
        check(t.replace("b", 20) == 2 && t.get("b") == 20, "replace(K,V) returns old");
        check(t.replace("z", 5) == null && !t.containsKey("z"), "replace(K,V) absent no insert");

        // replace(K,old,new): conditional on exact old value
        check(!t.replace("b", 2, 21) && t.get("b") == 20, "replace(K,old,new) mismatch no-op");
        check(t.replace("b", 20, 21) && t.get("b") == 21, "replace(K,old,new) match replaces");
        check(!t.replace("z", 1, 2) && !t.containsKey("z"), "replace(K,old,new) absent false");

        // getOrDefault both branches
        check(t.getOrDefault("b", -1) == 21 && t.getOrDefault("z", -1) == -1, "getOrDefault");

        // computeIfAbsent: absent inserts; present does NOT invoke fn; null result leaves no entry
        t.clear();
        t.put("a", 1);
        check(t.computeIfAbsent("b", k -> 2) == 2 && t.get("b") == 2, "computeIfAbsent absent inserts");
        final int[] called = {0};
        check(t.computeIfAbsent("a", k -> { called[0]++; return 5; }) == 1,
              "computeIfAbsent present returns existing");
        check(called[0] == 0, "computeIfAbsent present does not invoke fn");
        check(t.computeIfAbsent("c", k -> null) == null, "computeIfAbsent null result null");
        check(!t.containsKey("c"), "computeIfAbsent null result leaves no entry");
        RuntimeException boom = new RuntimeException("boom");
        checkPropagatesSame(boom, () -> t.computeIfAbsent("d", k -> { throw boom; }),
                "computeIfAbsent propagates fn exception");
        check(!t.containsKey("d") && t.size() == 2, "state intact after computeIfAbsent exception");

        // null key rejected before the mapping function runs
        called[0] = 0;
        checkThrows(NullPointerException.class, () -> t.computeIfAbsent(null, k -> { called[0]++; return 1; }),
                    "computeIfAbsent(null key) NPE");
        check(called[0] == 0, "computeIfAbsent null key does not invoke fn");

        // computeIfPresent: present computes; absent does NOT invoke fn; null result removes
        check(t.computeIfPresent("a", (k, v) -> v + 10) == 11 && t.get("a") == 11,
              "computeIfPresent present computes");
        called[0] = 0;
        check(t.computeIfPresent("zz", (k, v) -> { called[0]++; return 1; }) == null,
              "computeIfPresent absent returns null");
        check(called[0] == 0, "computeIfPresent absent does not invoke fn");
        check(t.computeIfPresent("a", (k, v) -> null) == null && !t.containsKey("a"),
              "computeIfPresent null result removes");
        checkPropagatesSame(boom, () -> t.computeIfPresent("b", (k, v) -> { throw boom; }),
                "computeIfPresent propagates fn exception");
        check(t.get("b") == 2, "entry unchanged after computeIfPresent exception");

        // compute: absent inserts (v==null); present recomputes; null result removes
        check(t.compute("a", (k, v) -> v == null ? 100 : v + 1) == 100 && t.get("a") == 100,
              "compute absent inserts");
        check(t.compute("a", (k, v) -> v + 1) == 101, "compute present recomputes");
        check(t.compute("a", (k, v) -> null) == null && !t.containsKey("a"), "compute null removes");
        checkPropagatesSame(boom, () -> t.compute("b", (k, v) -> { throw boom; }),
                "compute propagates fn exception");
        check(t.get("b") == 2, "entry unchanged after compute exception");

        // merge: absent puts value without invoking fn; present remaps; null result removes
        t.clear();
        called[0] = 0;
        check(t.merge("a", 1, (x, y) -> { called[0]++; return 5; }) == 1 && called[0] == 0,
              "merge absent puts value without fn");
        check(t.merge("a", 2, Integer::sum) == 3, "merge present remaps");
        check(t.merge("a", 4, (x, y) -> null) == null && !t.containsKey("a"),
              "merge null result removes");
        t.put("b", 1);
        checkPropagatesSame(boom, () -> t.merge("b", 1, (x, y) -> { throw boom; }),
                "merge propagates fn exception");
        check(t.get("b") == 1, "entry unchanged after merge exception");
        checkThrows(NullPointerException.class, () -> t.merge("b", 1, null), "merge null fn NPE");
        checkThrows(NullPointerException.class, () -> t.merge(null, 1, (x, y) -> x),
                    "merge null key NPE");

        // Hashtable-specific: containsValue(null) rejected, unlike HashMap
        expectNPE(() -> t.containsValue(null), "containsValue(null) NPE");
    }

    // ---- Dictionary inheritance dispatch ------------------------------------

    static void testDictionaryDispatch() {
        Hashtable<String, Integer> ht = new Hashtable<>();
        Dictionary<String, Integer> d = ht;
        check(d.isEmpty() && d.size() == 0, "Dictionary isEmpty/size on empty");
        check(d.put("k", 1) == null, "Dictionary.put");
        check(d.get("k") == 1, "Dictionary.get");
        check(d.size() == 1 && !d.isEmpty(), "Dictionary size reflects put");
        int keys = 0;
        for (Enumeration<String> e = d.keys(); e.hasMoreElements(); ) {
            check("k".equals(e.nextElement()), "Dictionary.keys element");
            keys++;
        }
        int vals = 0;
        for (Enumeration<Integer> e = d.elements(); e.hasMoreElements(); ) {
            check(e.nextElement() == 1, "Dictionary.elements element");
            vals++;
        }
        check(keys == 1 && vals == 1, "Dictionary enumeration counts");
        check(d.remove("k") == 1 && d.size() == 0, "Dictionary.remove reflects");

        Hashtable<String, Integer> empty = new Hashtable<>();
        check(!empty.keys().hasMoreElements() && !empty.elements().hasMoreElements(),
              "empty keys()/elements() hasMoreElements false");
    }

    // ---- view iterator write channels + fail-fast ---------------------------

    static void testViewIteratorsExtended() {
        // values() iterator.remove deletes the underlying mapping
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        t.put("c", 3);
        java.util.Iterator<Integer> vi = t.values().iterator();
        vi.next();
        vi.remove();
        check(t.size() == 2, "values iterator.remove reflects");

        // entrySet() iterator.remove
        java.util.Iterator<Map.Entry<String, Integer>> ei = t.entrySet().iterator();
        ei.next();
        ei.remove();
        check(t.size() == 1, "entrySet iterator.remove reflects");

        // keySet() iterator.remove then continue without CME
        java.util.Iterator<String> ki = t.keySet().iterator();
        String first = ki.next();
        ki.remove();
        check(t.size() == 0, "keySet iterator.remove drains");

        // values() iterator CME after structural modification
        Hashtable<String, Integer> fv = new Hashtable<>();
        fv.put("a", 1);
        fv.put("b", 2);
        java.util.Iterator<Integer> vit = fv.values().iterator();
        vit.next();
        fv.put("c", 3);
        checkThrows(ConcurrentModificationException.class, vit::next, "values iterator CME");

        // entrySet() iterator CME after structural modification
        Hashtable<String, Integer> fe = new Hashtable<>();
        fe.put("a", 1);
        fe.put("b", 2);
        java.util.Iterator<Map.Entry<String, Integer>> eit = fe.entrySet().iterator();
        eit.next();
        fe.remove("b");
        checkThrows(ConcurrentModificationException.class, eit::next, "entrySet iterator CME");

        // entry.setValue during iteration is non-structural: no CME
        Hashtable<String, Integer> sv = new Hashtable<>();
        sv.put("a", 1);
        sv.put("b", 2);
        int bumped = 0;
        for (Map.Entry<String, Integer> e : sv.entrySet()) {
            e.setValue(e.getValue() + 10);
            bumped++;
        }
        check(bumped == 2 && sv.get("a") == 11 && sv.get("b") == 12,
              "entry.setValue mid-iteration no CME and writes back");
    }

    // ---- Map.hashCode formula lock -------------------------------------------

    static void testHashCodeFormula() {
        // Map.hashCode = sum over entries of (key.hashCode() ^ value.hashCode())
        Hashtable<String, Integer> t = new Hashtable<>();
        t.put("a", 1);
        t.put("b", 2);
        check(t.hashCode() == 192, "Hashtable{a=1,b=2}.hashCode == 192");
    }

    public static void main(String[] args) throws Exception {
        testPutGetRemove();
        testNullRejection();
        testContains();
        testViews();
        testEqualsHashCode();
        testConstructorMatrix();
        testNullRejectionExtended();
        testEnumerationAndFailFast();
        testViewLive();
        testClone();
        testSerialization();
        testCrossImplEquals();
        testStateInvariants();
        testComputeMergeMatrix();
        testDictionaryDispatch();
        testViewIteratorsExtended();
        testHashCodeFormula();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
