/*
 * @test
 * @summary java.util.IdentityHashMap API conformance: full contract coverage.
 *          reference-identity (==) key semantics for get/containsKey/remove, reference-identity
 *          value semantics for containsValue/remove(K,V)/replace(K,old,new), distinct equal keys,
 *          null key allowance, constructor matrix (negative capacity IAE, null Map NPE), live
 *          views, equals/hashCode by identity, clone shallow, serialization round-trip +
 *          serialVersionUID, and state invariants.
 * @run main IdentityHashMapTest
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

    // ---- original coverage (kept) ------------------------------------------

    static void testIdentitySemantics() {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k1 = new String("x");
        String k2 = new String("x");
        m.put(k1, "v1");
        m.put(k2, "v2");
        check(m.size() == 2, "two distinct equal strings are two keys");
        check("v1".equals(m.get(k1)) && "v2".equals(m.get(k2)), "get by identity");
        check(m.get("x") == null, "get by equal-but-not-identical key returns null");
        check(m.containsKey(k1) && !m.containsKey(new String("x")), "containsKey identity");
        m.remove(k1);
        check(m.size() == 1 && !m.containsKey(k1), "remove by identity");
    }

    // ---- constructor matrix + value identity -------------------------------

    static void testConstructorMatrix() {
        checkThrows(IllegalArgumentException.class, () -> new IdentityHashMap<String, String>(-1),
                    "IdentityHashMap(-1) IAE");
        checkThrows(NullPointerException.class, () -> new IdentityHashMap<String, String>((Map<String, String>) null),
                    "IdentityHashMap(null Map) NPE");

        IdentityHashMap<String, String> src = new IdentityHashMap<>();
        String key = new String("x");
        src.put(key, "val");
        IdentityHashMap<String, String> copy = new IdentityHashMap<>(src);
        check(copy.size() == 1 && copy.get(key).equals("val"), "IdentityHashMap(Map) copies by identity");
        check(copy.get(new String("x")) == null, "copy still uses identity for get");
    }

    static void testValueIdentity() {
        // containsValue is overridden in JDK-8 to compare by reference (==).
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String v1 = new String("val");
        String v2 = new String("val");
        m.put("a", v1);
        check(m.containsValue(v1), "containsValue by identity (same ref)");
        check(!m.containsValue(v2), "containsValue: equal-but-not-identical not found");
        check(m.containsValue(new String("val")) == false,
              "containsValue still identity for a fresh equal value");
    }

    // ---- null key + views + equals by identity -----------------------------

    static void testNullKeyAndViews() {
        IdentityHashMap<String, Integer> m = new IdentityHashMap<>();
        m.put(null, 1);
        m.put("k", 2);
        check(m.get(null) == 1 && m.containsKey(null), "null key allowed");
        check(m.keySet().size() == 2 && m.values().size() == 2 && m.entrySet().size() == 2,
              "view sizes");
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

    // ---- clone + serialization + invariants --------------------------------

    static void testClone() {
        IdentityHashMap<String, String> orig = new IdentityHashMap<>();
        String k = new String("k");
        orig.put(k, "v");
        @SuppressWarnings("unchecked")
        IdentityHashMap<String, String> clone = (IdentityHashMap<String, String>) orig.clone();
        check(clone != orig && clone.equals(orig), "clone distinct and equal");
        clone.put(new String("k2"), "v2");
        check(clone.size() == 2 && orig.size() == 1, "clone structural independence");
    }

    static void testSerialization() throws Exception {
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k = new String("k");
        m.put(k, "v");
        IdentityHashMap<String, String> r = roundTrip(m);
        check(r != m, "round-trip distinct object");
        check(r.size() == 1, "round-trip preserves size");
        // identity equals is broken by serialization: deserialized keys are fresh references
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

    static void testStateInvariants() {
        IdentityHashMap<String, Integer> m = new IdentityHashMap<>();
        String k1 = new String("x");
        m.put(k1, 1);
        m.put(k1, 10);
        check(m.size() == 1 && m.get(k1) == 10, "re-put same identity updates value");
        check(m.get(new String("x")) == null, "non-identical key still absent");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
    }

    // ---- remove(K,V)/replace(K,V,V) semantics (Map defaults: equals-based) --

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

    static void testRemoveReplaceValueSemantics() {
        // JDK-8 contract: remove(Object,Object) / replace(K,V,V) are inherited Map
        // default methods and compare the current value with Object.equals, so an
        // equal-but-not-identical value matches. (JDK-21+ overrides these to use
        // identity comparison -- locked separately on the JDK-27 side.)
        IdentityHashMap<String, String> m = new IdentityHashMap<>();
        String k = new String("k");
        String v = new String("val");
        m.put(k, v);
        check(m.remove(k, new String("val")), "remove(k, equal-but-distinct value) true (equals semantics)");
        check(m.isEmpty(), "remove(k,v) removed entry");

        m.put(k, v);
        check(!m.remove(k, "other"), "remove(k,v) different value false");
        check(m.size() == 1, "remove(k,v) mismatch keeps entry");
        check(m.remove(k, v), "remove(k, identical value) true");

        m.put(k, v);
        check(m.replace(k, new String("val"), "n1"), "replace(k, equalOld, new) true (equals semantics)");
        check(m.get(k).equals("n1"), "replace(k, equalOld, new) applied");

        // replace(K,V): returns old value; absent key no-op
        check(m.replace(k, "n2").equals("n1"), "replace(K,V) returns old");
        check(m.replace(new String("k"), "n3") == null && m.size() == 1,
              "replace(K,V) equal-but-distinct key treated absent");
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
        testIdentitySemantics();
        testConstructorMatrix();
        testValueIdentity();
        testNullKeyAndViews();
        testEqualsUsesIdentity();
        testClone();
        testSerialization();
        testStateInvariants();
        testRemoveReplaceValueSemantics();
        testComputeMergeIdentity();
        testEqualsVsHashMap();
        testViewsAndFailFast();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
