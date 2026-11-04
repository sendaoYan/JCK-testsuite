/*
 * @test
 * @summary java.util.concurrent.ConcurrentHashMap.KeySetView conformance: key-set view.
 * @run main KeySetViewTest
 */

import java.util.concurrent.ConcurrentHashMap;

public class KeySetViewTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Runnable r, Class<? extends Throwable> type, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                                         + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    // ---- appended: mapped-value default semantics ----
    static void testMappedValueSemantics() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        ConcurrentHashMap.KeySetView<String, Integer> view = m.keySet(42);
        check(view.getMappedValue() == 42, "keySet(v).getMappedValue() == v");
        check(view.getMap() == m, "getMap() identity of backing map");
        check(view.add("k"), "keySet(42).add returns true");
        check(m.get("k") == 42, "keySet(42).add behaves as put(k, default)");
        m.put("e", 7);
        check(!view.add("e"), "keySet(42).add on existing key returns false");
        check(m.get("e") == 7, "keySet(42).add existing key does not overwrite");
        check(view.contains("k") && view.contains("e") && !view.contains("zz"), "contains delegates to map");
        m.put("z", 1);
        check(view.size() == m.size(), "view size tracks backing map");
        check(view.remove("z") && !m.containsKey("z"), "view remove writes through to map");
        check(ConcurrentHashMap.newKeySet().getMappedValue() == Boolean.TRUE,
              "newKeySet() maps keys to Boolean.TRUE");
        ConcurrentHashMap.KeySetView<String, Boolean> ks = ConcurrentHashMap.newKeySet(16);
        check(ks.isEmpty() && ks.getMappedValue() == Boolean.TRUE, "newKeySet(int) empty with TRUE default");
        expectThrows(() -> m.keySet().add("nope"), UnsupportedOperationException.class,
                     "keySet() view (null default) add throws UOE");
    }

    static void testViewIterationAndBulk() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        ConcurrentHashMap.KeySetView<String, Integer> view = m.keySet();
        java.util.ArrayList<String> seen = new java.util.ArrayList<>();
        for (String k : view) seen.add(k);
        check(seen.size() == 2, "view iteration visits every key");
        check(view.containsAll(java.util.Arrays.asList("a", "b")), "containsAll");
        check(!view.containsAll(java.util.Arrays.asList("a", "missing")), "containsAll with missing false");
        check(view.removeAll(java.util.Arrays.asList("a")) && m.size() == 1 && !m.containsKey("a"),
              "removeAll writes through to map");
        m.put("s1", 1);
        m.put("s2", 2);
        m.put("s3", 3);
        check(view.retainAll(java.util.Arrays.asList("s1", "s3")) && m.size() == 2 && !m.containsKey("s2"),
              "retainAll writes through to map");
        view.clear();
        check(m.isEmpty(), "view clear empties backing map");
        // iterator obtained before a structural put keeps traversing without CME
        m.put("i1", 1);
        m.put("i2", 2);
        java.util.Iterator<String> it = view.iterator();
        m.put("i3", 3);
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n >= 2, "view iterator weakly consistent (no CME)");
        expectThrows(() -> view.contains(null), NullPointerException.class, "view.contains(null)");
        expectThrows(() -> view.remove(null), NullPointerException.class, "view.remove(null)");
    }

    static void testSerialization() throws Exception {
        ConcurrentHashMap.KeySetView<String, Boolean> ks = ConcurrentHashMap.newKeySet();
        ks.add("x");
        ks.add("y");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(ks);
        }
        @SuppressWarnings("unchecked")
        ConcurrentHashMap.KeySetView<String, Boolean> copy = (ConcurrentHashMap.KeySetView<String, Boolean>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.size() == 2 && copy.contains("x") && copy.contains("y"), "KeySetView round-trip");
        check(Boolean.TRUE.equals(copy.getMappedValue()), "mapped default survives round-trip (equals, not identity)");
        check(copy.add("z") && copy.size() == 3, "resurrected view accepts adds");
    }

    static void testKeySetView() {
        ConcurrentHashMap.KeySetView<String, Boolean> ks = ConcurrentHashMap.newKeySet();
        check(ks.isEmpty(), "newKeySet starts empty");
        check(ks.add("a"), "add returns true");
        check(!ks.add("a"), "add duplicate returns false");
        check(ks.contains("a"), "contains");
        check(ks.size() == 1, "size");
        check(ks.remove("a"), "remove returns true");
        check(ks.isEmpty(), "removed");

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("x", 1);
        ConcurrentHashMap.KeySetView<String, Integer> view = map.keySet();
        check(view.contains("x"), "keySet view contains");
        check(!view.contains("y"), "keySet view does not contain missing");
        check(view.size() == 1, "keySet view size");
    }

    public static void main(String[] args) throws Exception {
        testKeySetView();
        testMappedValueSemantics();
        testViewIterationAndBulk();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
