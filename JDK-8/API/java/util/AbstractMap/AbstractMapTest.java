/*
 * @test
 * @summary java.util.AbstractMap API conformance: SimpleEntry/SimpleImmutableEntry
 *          (getKey/getValue/setValue returns old/setValue UOE for immutable/equals/hashCode/
 *          toString), AbstractMap equals/hashCode/toString, default put UOE, entrySet/keySet/
 *          values.
 * @run main AbstractMapTest
 */

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class AbstractMapTest {
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

    static void testSimpleEntry() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check("k".equals(e.getKey()) && e.getValue() == 1, "SimpleEntry getKey/getValue");
        check(e.setValue(10) == 1, "setValue returns old value");
        check(e.getValue() == 10, "setValue updates value");

        AbstractMap.SimpleEntry<String, Integer> same = new AbstractMap.SimpleEntry<>("k", 10);
        check(e.equals(same) && e.hashCode() == same.hashCode(), "SimpleEntry equals/hashCode");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k", 11)), "!equals different value");
        check(!e.equals(new AbstractMap.SimpleEntry<>("k2", 10)), "!equals different key");
        check(e.toString().equals("k=10"), "SimpleEntry toString key=value");

        AbstractMap.SimpleEntry<String, Integer> fromMap =
                new AbstractMap.SimpleEntry<>(new AbstractMap.SimpleEntry<>("m", 5));
        check("m".equals(fromMap.getKey()) && fromMap.getValue() == 5, "SimpleEntry(Map.Entry) copy ctor");

        // SimpleEntry permits null key and value.
        AbstractMap.SimpleEntry<String, Integer> nulls = new AbstractMap.SimpleEntry<>(null, null);
        check(nulls.getKey() == null && nulls.getValue() == null, "SimpleEntry(null,null) allowed");
        check(nulls.equals(new AbstractMap.SimpleEntry<String, Integer>(null, null)),
              "SimpleEntry equals with null key/value");
    }

    static void testSimpleImmutableEntry() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check("k".equals(e.getKey()) && e.getValue() == 1, "SimpleImmutableEntry getKey/getValue");
        checkThrows(UnsupportedOperationException.class, () -> e.setValue(10),
                    "SimpleImmutableEntry.setValue UOE");

        AbstractMap.SimpleImmutableEntry<String, Integer> same = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check(e.equals(same) && e.hashCode() == same.hashCode(), "SimpleImmutableEntry equals/hashCode");
        check(e.toString().equals("k=1"), "SimpleImmutableEntry toString");

        AbstractMap.SimpleImmutableEntry<String, Integer> fromMap =
                new AbstractMap.SimpleImmutableEntry<>(new AbstractMap.SimpleEntry<>("m", 5));
        check("m".equals(fromMap.getKey()) && fromMap.getValue() == 5, "SimpleImmutableEntry(Map.Entry) copy");
    }

    static void testDefaultPutUOE() {
        AbstractMap<String, Integer> m = new AbstractMap<String, Integer>() {
            public java.util.Set<java.util.Map.Entry<String, Integer>> entrySet() {
                return new java.util.HashSet<java.util.Map.Entry<String, Integer>>();
            }
        };
        checkThrows(UnsupportedOperationException.class, () -> m.put("k", 1), "default put UOE");
    }

    static void testAbstractMapBehavior() {
        AbstractMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        check(m.size() == 2, "size");
        check(m.get("a") == 1, "get");
        check(m.containsKey("a") && m.containsValue(2), "containsKey/containsValue");
        check(m.keySet().contains("a"), "keySet");
        check(m.values().contains(2), "values");
        check(m.entrySet().size() == 2, "entrySet");

        AbstractMap<String, Integer> same = new HashMap<>();
        same.put("b", 2);
        same.put("a", 1);
        check(m.equals(same), "AbstractMap.equals order-independent");
        check(m.hashCode() == same.hashCode(), "AbstractMap.hashCode order-independent");

        m.remove("a");
        check(!m.containsKey("a") && m.size() == 1, "remove");

        // toString is {k=v, ...} (order may vary); just check it contains both entries
        String s = m.toString();
        check(s.startsWith("{") && s.endsWith("}") && s.contains("b=2"), "AbstractMap.toString");
    }

    // ---- minimal-subclass inherited-operation contract -----------------------

    /** Minimal AbstractMap: only entrySet() implemented, backed by a HashMap. */
    static class MinimalMap extends AbstractMap<String, Integer> {
        final HashMap<String, Integer> backing = new HashMap<>();
        @Override
        public java.util.Set<java.util.Map.Entry<String, Integer>> entrySet() {
            return backing.entrySet();
        }
    }

    static void testMinimalSubclassContract() {
        MinimalMap m = new MinimalMap();
        m.backing.put("a", 1);
        m.backing.put("b", 2);

        // read paths inherited from AbstractMap route through entrySet()
        check(m.size() == 2 && !m.isEmpty(), "inherited size/isEmpty via entrySet");
        check(m.get("a") == 1 && m.get("z") == null, "inherited get scans entrySet");
        check(m.containsKey("a") && !m.containsKey("z"), "inherited containsKey");
        check(m.containsValue(2) && !m.containsValue(9), "inherited containsValue");

        // inherited remove works through the entrySet iterator
        check(m.remove("a") == 1 && !m.containsKey("a"), "inherited remove");
        check(m.remove("a") == null, "inherited remove of absent key null");

        // toString {k=v, ...}
        check(m.toString().equals("{b=2}"), "inherited toString");

        // equals/hashCode per Map contract, across unrelated implementations
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("b", 2);
        check(m.equals(hm) && hm.equals(m), "AbstractMap equals HashMap same content");
        check(m.hashCode() == hm.hashCode(), "AbstractMap hashCode == backing HashMap's");
        check(!m.equals(null), "equals(null) false");
        check(!m.equals("not-a-map"), "equals(non-Map) false");

        // default put (hence putAll) unsupported
        checkThrows(UnsupportedOperationException.class, () -> m.put("c", 3), "default put UOE");
        Map<String, Integer> src = new HashMap<>();
        src.put("x", 1);
        checkThrows(UnsupportedOperationException.class, () -> m.putAll(src), "default putAll UOE");

        // default clear delegates to entrySet().clear()
        m.clear();
        check(m.isEmpty() && m.backing.isEmpty(), "default clear via entrySet");
    }

    /** Read-only entrySet: entrySet().clear() also unsupported. */
    static class ReadOnlyMap extends AbstractMap<String, Integer> {
        final java.util.Set<java.util.Map.Entry<String, Integer>> entries =
                new java.util.HashSet<>();
        ReadOnlyMap() {
            entries.add(new java.util.AbstractMap.SimpleEntry<>("k", 1));
        }
        @Override
        public java.util.Set<java.util.Map.Entry<String, Integer>> entrySet() {
            return java.util.Collections.unmodifiableSet(entries);
        }
    }

    static void testReadOnlyEntrySetClearUoe() {
        ReadOnlyMap m = new ReadOnlyMap();
        check(m.size() == 1, "read-only map readable");
        checkThrows(UnsupportedOperationException.class, m::clear, "clear UOE via unmodifiable entrySet");
        check(m.size() == 1, "map intact after failed clear");
    }

    // ---- SimpleEntry/SimpleImmutableEntry cross-type + serialization ----------

    static void testSimpleEntryCrossTypeAndSerialization() throws Exception {
        java.util.AbstractMap.SimpleEntry<String, Integer> se =
                new java.util.AbstractMap.SimpleEntry<>("k", 1);
        java.util.AbstractMap.SimpleImmutableEntry<String, Integer> sie =
                new java.util.AbstractMap.SimpleImmutableEntry<>("k", 1);

        // Map.Entry.equals is type-agnostic: mutable vs immutable entry with the
        // same key/value are equal, in both directions
        check(se.equals(sie) && sie.equals(se), "SimpleEntry equals SimpleImmutableEntry");

        // equals a live HashMap entry view too
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("k", 1);
        Map.Entry<String, Integer> view = hm.entrySet().iterator().next();
        check(se.equals(view) && view.equals(se), "SimpleEntry equals HashMap entry view");
        check(!se.equals(null) && !se.equals("k=1"), "entry equals(null)/equals(non-entry)");

        // hashCode formula: key.hashCode() ^ value.hashCode() == 106 for ("k", 1)
        check(se.hashCode() == ("k".hashCode() ^ Integer.hashCode(1)), "SimpleEntry hashCode formula");
        check(se.hashCode() == 106 && sie.hashCode() == 106, "hashCode literal 106");

        // serialization round-trip
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(se);
            oos.writeObject(sie);
        }
        try (java.io.ObjectInputStream ois =
                     new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Object seBack = ois.readObject();
            Object sieBack = ois.readObject();
            check(seBack.getClass() == java.util.AbstractMap.SimpleEntry.class,
                  "SimpleEntry round-trip class");
            check(seBack.equals(se), "SimpleEntry round-trip equal");
            check(sieBack.getClass() == java.util.AbstractMap.SimpleImmutableEntry.class,
                  "SimpleImmutableEntry round-trip class");
            check(sieBack.equals(sie), "SimpleImmutableEntry round-trip equal");
        }
    }

    public static void main(String[] args) throws Exception {
        testSimpleEntry();
        testSimpleImmutableEntry();
        testDefaultPutUOE();
        testAbstractMapBehavior();
        testMinimalSubclassContract();
        testReadOnlyEntrySetClearUoe();
        testSimpleEntryCrossTypeAndSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
