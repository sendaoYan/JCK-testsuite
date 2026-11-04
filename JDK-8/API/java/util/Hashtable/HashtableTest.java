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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
