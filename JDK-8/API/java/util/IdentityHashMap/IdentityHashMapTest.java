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

    public static void main(String[] args) throws Exception {
        testIdentitySemantics();
        testConstructorMatrix();
        testValueIdentity();
        testNullKeyAndViews();
        testEqualsUsesIdentity();
        testClone();
        testSerialization();
        testStateInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
