/*
 * @test
 * @summary java.util.HashMap API conformance: full contract coverage.
 *          constructors + exception paths, put/get/remove incl. null-key/null-value
 *          semantics, views as write channels (remove/removeAll/retainAll/clear/
 *          iterator.remove/add->UOE), fail-fast iterators (CME/ISE/NSEE), Entry.setValue,
 *          Java 8 default methods incl. deep null branches and function side-effect
 *          contracts, clone/toString/equals cross-implementation, toArray contract,
 *          resize with null key, treeified collision bins, subclass overrides, resize
 *          threshold crossings, serialization round-trip + serialVersionUID, view
 *          spliterator characteristics, view streams, removeIf/forEach, and a bounded
 *          reference-release check after clear/clone.
 * @run main HashMapTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class HashMapTest {
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

    static void checkCME(Iterator<?> it, String msg) {
        try {
            it.next();
        } catch (java.util.ConcurrentModificationException expected) {
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected ConcurrentModificationException");
    }

    static HashMap<String, Integer> filled() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        return m;
    }

    @SuppressWarnings("unchecked")
    static <K, V> HashMap<K, V> roundTrip(HashMap<K, V> map) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(map);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (HashMap<K, V>) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testPutGetRemove() {
        HashMap<String, Integer> map = new HashMap<>();
        check(map.size() == 0 && map.isEmpty(), "empty");
        check(map.put("a", 1) == null, "put new returns null");
        check(map.put("a", 2) == 1, "put existing returns old value");
        check(map.put(null, 10) == null, "put null key");
        check(map.put("n", null) == null, "put null value");
        check(map.get("a") == 2, "get");
        check(map.get("missing") == null, "get missing == null");
        check(map.get(null) == 10, "get null key");
        check(map.get("n") == null, "get null value (present but null)");
        check(map.containsKey(null) && map.containsKey("n"), "containsKey");
        check(map.containsValue(10) && map.containsValue(null), "containsValue");
        check(map.remove("a") == 2, "remove returns value");
        check(map.remove("missing") == null, "remove missing == null");
        check(!map.containsKey("a"), "removed key absent");
        check(map.size() == 2, "size after remove");
    }

    static void testViews() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        Set<String> keys = map.keySet();
        check(keys.size() == 3 && keys.containsAll(Arrays.asList("a", "b", "c")), "keySet view");
        check(map.values().size() == 3, "values view");
        check(map.entrySet().size() == 3, "entrySet view");
        check(map.keySet().remove("a") && !map.containsKey("a"), "keySet.remove reflects in map");
        check(map.size() == 2, "map size after keySet.remove");
        map.clear();
        check(map.isEmpty(), "clear");
    }

    static void testEqualsHashCode() {
        HashMap<String, Integer> a = new HashMap<>();
        a.put("x", 1);
        a.put("y", 2);
        HashMap<String, Integer> b = new HashMap<>();
        b.put("y", 2);
        b.put("x", 1);
        check(a.equals(b) && b.equals(a), "equals symmetric regardless of order");
        check(a.hashCode() == b.hashCode(), "equal maps equal hashCode");
        b.put("z", 3);
        check(!a.equals(b), "!equals different content");
    }

    static void testRehash() {
        HashMap<Integer, Integer> map = new HashMap<>(2);
        for (int i = 0; i < 1000; i++) {
            map.put(i, i * 2);
        }
        check(map.size() == 1000, "size after 1000 puts (rehash)");
        for (int i = 0; i < 1000; i++) {
            check(map.get(i) == i * 2, "get after rehash");
        }
    }

    static void testJava8Defaults() {
        HashMap<String, Integer> map = new HashMap<>();
        check(map.getOrDefault("k", 5) == 5, "getOrDefault missing");
        map.put("k", 7);
        check(map.getOrDefault("k", 5) == 7, "getOrDefault present");

        check(map.putIfAbsent("k", 99) == 7, "putIfAbsent present returns existing");
        check(map.putIfAbsent("new", 1) == null, "putIfAbsent absent returns null");
        check(map.get("new") == 1, "putIfAbsent inserted");

        check(map.replace("k", 8) == 7, "replace returns old value");
        check(map.get("k") == 8, "replace updates");
        check(map.replace("absent", 1) == null, "replace absent returns null");
        check(map.replace("k", 8, 9), "replace(K,old,new) matching returns true");
        check(!map.replace("k", 999, 10), "replace(K,old,new) non-matching returns false");
        check(map.remove("k", 9), "remove(K,V) matching");
        check(!map.remove("new", 999), "remove(K,V) non-matching");

        map.clear();
        check(map.computeIfAbsent("k", k -> 3) == 3, "computeIfAbsent computes");
        check(map.computeIfAbsent("k", k -> 99) == 3, "computeIfAbsent present no-op");
        check(map.computeIfPresent("k", (k, v) -> v * 10) == 30, "computeIfPresent computes");
        check(map.computeIfPresent("absent", (k, v) -> 1) == null, "computeIfPresent absent no-op");
        check(map.compute("k", (k, v) -> v + 1) == 31, "compute");
        check(map.merge("k", 100, Integer::sum) == 131, "merge combines");
        check(map.merge("m", 5, Integer::sum) == 5, "merge absent inserts");

        final int[] sum = {0};
        map.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 131 + 5, "forEach sums values");
    }

    // ---- expanded coverage (constructors / putAll / clone / views / entry / CME / resize / treeify / spliterator / serialization) ----

    static void testConstructors() {
        checkThrows(IllegalArgumentException.class, () -> new HashMap<Object, Object>(-1), "HashMap(-1)");
        checkThrows(IllegalArgumentException.class, () -> new HashMap<Object, Object>(-10), "HashMap(-10)");
        checkThrows(IllegalArgumentException.class, () -> new HashMap<Object, Object>(Integer.MIN_VALUE), "HashMap(MIN_VALUE)");
        check(new HashMap<Object, Object>(0).isEmpty(), "HashMap(0) empty");
        check(new HashMap<Object, Object>(16).isEmpty(), "HashMap(16) empty");
        HashMap<String, Integer> m = new HashMap<>(8);
        m.put("a", 1);
        check(m.get("a") == 1, "HashMap(int) usable");

        checkThrows(IllegalArgumentException.class, () -> new HashMap<Object, Object>(-1, 0.75f), "HashMap(-1,0.75f)");
        float[] badLoads = { 0f, +0.0f, -0.1f, -1f, Float.NaN, Float.NEGATIVE_INFINITY };
        for (float lf : badLoads) {
            checkThrows(IllegalArgumentException.class, () -> new HashMap<Object, Object>(16, lf), "HashMap(16," + lf + ")");
        }
        float[] goodLoads = { Float.MIN_VALUE, 0.1f, 0.75f, 1f, 100f, Float.MAX_VALUE };
        for (float lf : goodLoads) {
            check(new HashMap<Object, Object>(1, lf).isEmpty(), "HashMap(1," + lf + ") empty");
        }

        checkThrows(NullPointerException.class, () -> new HashMap<Object, Object>(null), "HashMap(null)");
        HashMap<String, Integer> src = new HashMap<>();
        src.put("a", 1);
        src.put("b", 2);
        src.put(null, 3);
        src.put("n", null);
        HashMap<String, Integer> copy = new HashMap<>(src);
        check(copy.size() == src.size(), "HashMap(Map) copies size");
        check(copy.get("a") == 1 && copy.get("b") == 2 && copy.get(null) == 3, "HashMap(Map) copies entries");
        check(copy.containsKey("n") && copy.get("n") == null, "HashMap(Map) copies null value");
        copy.put("c", 4);
        check(!src.containsKey("c"), "HashMap(Map) is independent of source");
    }

    static void testPutAllReplaceAll() {
        checkThrows(NullPointerException.class, () -> new HashMap<Object, Object>().putAll(null), "putAll(null)");

        HashMap<String, Integer> a = new HashMap<>();
        a.put("x", 1);
        a.put("y", 2);
        HashMap<String, Integer> b = new HashMap<>();
        b.put("y", 20);
        b.put("z", 3);
        b.putAll(a);
        check(b.get("x") == 1, "putAll adds new entries");
        check(b.get("y") == 2, "putAll overwrites existing values");
        check(b.get("z") == 3, "putAll keeps non-overlapping entries");
        check(b.size() == 3, "putAll size");

        HashMap<String, Integer> n = new HashMap<>();
        n.put(null, 5);
        n.put("n", null);
        b.putAll(n);
        check(b.get(null) == 5 && b.containsKey("n") && b.get("n") == null, "putAll null key/value");

        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        m.replaceAll((k, v) -> v * 10);
        check(m.get("a") == 10 && m.get("b") == 20 && m.get("c") == 30, "replaceAll transforms values");
        check(m.size() == 3, "replaceAll keeps size");
        m.replaceAll((k, v) -> null);
        check(m.get("a") == null && m.containsKey("a"), "replaceAll can produce null values");
    }

    @SuppressWarnings("unchecked")
    static void testCloneToString() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put(null, 3);
        m.put("n", null);
        HashMap<String, Integer> c = (HashMap<String, Integer>) m.clone();
        check(c != m, "clone returns a distinct object");
        check(c.equals(m) && c.size() == m.size(), "clone equals original");
        check(c.get("a") == 1 && c.get(null) == 3 && c.containsKey("n") && c.get("n") == null, "clone copies contents");
        c.put("z", 99);
        check(!m.containsKey("z") && m.size() == 4, "clone is structurally independent");
        c.put("a", 100);
        check(m.get("a") == 1, "clone value changes do not affect original");

        HashMap<String, StringBuilder> ms = new HashMap<>();
        StringBuilder sb = new StringBuilder("v");
        ms.put("k", sb);
        HashMap<String, StringBuilder> cs = (HashMap<String, StringBuilder>) ms.clone();
        check(cs.get("k") == sb, "clone is shallow (shares value references)");

        HashMap<String, Integer> t = new HashMap<>();
        t.put(null, null);
        check(t.toString().equals("{null=null}"), "toString with null key/value");
        Map.Entry<String, Integer> e = t.entrySet().iterator().next();
        check(e.toString().equals("null=null"), "entry.toString with null key/value");
        t.put("a", 1);
        check(t.toString().contains("null=null") && t.toString().contains("a=1"), "toString lists all entries");
    }

    static void testViewOperations() {
        HashMap<String, Integer> m = filled();
        check(m.keySet().remove("a") && !m.containsKey("a") && m.size() == 2, "keySet.remove");
        check(m.values().remove(2) && !m.containsValue(2) && m.size() == 1, "values.remove");
        Map.Entry<String, Integer> e = m.entrySet().iterator().next();
        check(m.entrySet().remove(e) && !m.containsKey(e.getKey()), "entrySet.remove");
        check(m.isEmpty(), "all entries removed via views");

        m = filled();
        Iterator<String> ki = m.keySet().iterator();
        ki.next();
        ki.remove();
        check(m.size() == 2, "keySet iterator.remove");

        m = filled();
        Iterator<Integer> vi = m.values().iterator();
        vi.next();
        vi.remove();
        check(m.size() == 2, "values iterator.remove");

        m = filled();
        Iterator<Map.Entry<String, Integer>> ei = m.entrySet().iterator();
        ei.next();
        ei.remove();
        check(m.size() == 2, "entrySet iterator.remove");

        m = filled();
        m.values().clear();
        check(m.isEmpty(), "values().clear()");
        m = filled();
        m.keySet().clear();
        check(m.isEmpty(), "keySet().clear()");

        m = filled();
        m.keySet().removeAll(Arrays.asList("a", "b"));
        check(m.size() == 1 && m.containsKey("c"), "keySet.removeAll");

        m = filled();
        m.values().retainAll(Arrays.asList(2, 3));
        check(m.size() == 2 && !m.containsKey("a"), "values.retainAll");

        final HashMap<String, Integer> uoe = filled();
        checkThrows(UnsupportedOperationException.class, () -> uoe.keySet().add("z"), "keySet.add");
        checkThrows(UnsupportedOperationException.class, () -> uoe.keySet().addAll(Arrays.asList("z")), "keySet.addAll");
        checkThrows(UnsupportedOperationException.class, () -> uoe.values().add(9), "values.add");
        checkThrows(UnsupportedOperationException.class, () -> uoe.values().addAll(Arrays.asList(9)), "values.addAll");
        checkThrows(UnsupportedOperationException.class, () -> uoe.entrySet().add(null), "entrySet.add");
        checkThrows(UnsupportedOperationException.class,
                () -> uoe.entrySet().addAll(Arrays.asList(new AbstractMap.SimpleEntry<String, Integer>("z", 26))),
                "entrySet.addAll");

        m = filled();
        check(m.keySet().contains("a") && !m.keySet().contains("zzz"), "keySet.contains");
        check(m.keySet().containsAll(Arrays.asList("a", "b")), "keySet.containsAll");
        check(m.values().contains(1) && m.values().containsAll(Arrays.asList(1, 2)), "values.contains/containsAll");

        m = filled();
        Object[] arr = m.keySet().toArray();
        check(arr.length == 3, "keySet.toArray()");
        String[] arr2 = m.keySet().toArray(new String[0]);
        check(arr2.length == 3 && new HashSet<>(Arrays.asList(arr2)).containsAll(m.keySet()), "keySet.toArray(T[])");
        String[] big = m.keySet().toArray(new String[10]);
        check(big[3] == null, "toArray(T[]) pads trailing null");
    }

    static void testEntrySetValue() {
        HashMap<String, Integer> m = filled();
        Map.Entry<String, Integer> e = null;
        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            if (entry.getKey().equals("b")) e = entry;
        }
        check(e != null, "found entry b");
        check(e.getKey().equals("b") && e.getValue() == 2, "entry getKey/getValue");
        Integer old = e.setValue(20);
        check(old == 2, "setValue returns the old value");
        check(m.get("b") == 20, "setValue updates the map");

        for (Map.Entry<String, Integer> entry : m.entrySet()) {
            if (entry.getKey().equals("a")) {
                check(entry.setValue(null) == 1, "setValue to null returns old value");
            }
        }
        check(m.get("a") == null && m.containsKey("a"), "setValue(null) keeps the mapping with null value");
    }

    static void testConcurrentModification() {
        HashMap<String, Integer> m = filled();
        Iterator<String> it = m.keySet().iterator();
        m.put("z", 26); // structural
        checkCME(it, "keySet CME after put");

        m = filled();
        it = m.keySet().iterator();
        m.clear();
        checkCME(it, "keySet CME after clear");

        m = filled();
        Iterator<Integer> vit = m.values().iterator();
        m.remove("a");
        checkCME(vit, "values CME after remove");

        m = filled();
        Iterator<Map.Entry<String, Integer>> eit = m.entrySet().iterator();
        HashMap<String, Integer> other = new HashMap<>();
        other.put("w", 1);
        m.putAll(other);
        checkCME(eit, "entrySet CME after putAll");

        m = filled();
        it = m.keySet().iterator();
        while (it.hasNext()) {
            String k = it.next();
            m.put(k, m.get(k) * 10);
        }
        check(m.get("a") == 10 && m.get("b") == 20 && m.get("c") == 30, "value replacement is not structural (no CME)");
    }

    static void testResizeNullKey() {
        HashMap<String, Integer> m = new HashMap<>(2);
        m.put(null, 0);
        check(m.get(null) == 0, "null key before resize");
        for (int i = 0; i < 1000; i++) {
            m.put("k" + i, i);
        }
        check(m.size() == 1001, "size after many resizes");
        check(m.containsKey(null) && m.get(null) == 0, "null key survives resize");
        check(m.remove(null) == 0, "remove null key after resize");
        check(!m.containsKey(null), "null key removed");
    }

    static final class CollidingKey implements Comparable<CollidingKey> {
        final int value;
        CollidingKey(int value) { this.value = value; }
        @Override public int hashCode() { return 0; } // force all keys into one bin
        @Override public boolean equals(Object o) {
            return o instanceof CollidingKey && ((CollidingKey) o).value == value;
        }
        @Override public int compareTo(CollidingKey o) { return Integer.compare(value, o.value); }
    }

    static void testTreeifyNullKey() {
        HashMap<CollidingKey, CollidingKey> m = new HashMap<>(64, 1.0f);
        for (int i = 0; i < 256; i++) {
            CollidingKey k = new CollidingKey(i);
            m.put(k, k);
        }
        m.put(null, null);
        check(m.size() == 257, "size after treeify + null key");
        check(m.containsKey(null), "null key present after treeify");
        for (int i = 0; i < 256; i++) {
            CollidingKey k = new CollidingKey(i);
            check(m.get(k) != null && m.get(k).equals(k), "colliding key retrievable after treeify");
        }
    }

    static void testSpliteratorAndStreams() {
        HashMap<String, Integer> m = filled();

        Spliterator<String> ks = m.keySet().spliterator();
        check(ks.hasCharacteristics(Spliterator.SIZED) && ks.hasCharacteristics(Spliterator.DISTINCT), "keySet spliterator SIZED+DISTINCT");
        check(ks.estimateSize() == m.size(), "keySet spliterator estimateSize");

        Spliterator<Map.Entry<String, Integer>> es = m.entrySet().spliterator();
        check(es.hasCharacteristics(Spliterator.SIZED) && es.hasCharacteristics(Spliterator.DISTINCT), "entrySet spliterator SIZED+DISTINCT");

        Spliterator<Integer> vs = m.values().spliterator();
        check(vs.hasCharacteristics(Spliterator.SIZED) && !vs.hasCharacteristics(Spliterator.DISTINCT), "values spliterator SIZED (not DISTINCT)");

        int[] n = {0};
        m.keySet().spliterator().forEachRemaining(k -> n[0]++);
        check(n[0] == m.size(), "keySet spliterator forEachRemaining");

        check(m.keySet().stream().count() == m.size(), "keySet stream count");
        check(m.values().stream().count() == m.size(), "values stream count");
        check(m.entrySet().stream().count() == m.size(), "entrySet stream count");
        check(m.keySet().parallelStream().count() == m.size(), "keySet parallelStream count");

        int[] n2 = {0};
        m.values().forEach(v -> n2[0]++);
        check(n2[0] == m.size(), "values forEach");

        m = filled();
        m.values().removeIf(v -> v % 2 == 1);
        check(m.size() == 1 && m.containsKey("b") && m.get("b") == 2, "values.removeIf");

        m = filled();
        m.keySet().removeIf(k -> k.equals("a"));
        check(m.size() == 2 && !m.containsKey("a"), "keySet.removeIf");

        m = filled();
        m.entrySet().removeIf(e -> e.getKey().equals("b"));
        check(m.size() == 2 && !m.containsKey("b"), "entrySet.removeIf");
    }

    static void testSerialization() throws Exception {
        check(ObjectStreamClass.lookup(HashMap.class).getSerialVersionUID() == 362498820763181265L, "serialVersionUID");

        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put(null, 3);
        m.put("n", null);
        HashMap<String, Integer> r = roundTrip(m);
        check(r != m && r.size() == m.size(), "round-trip size");
        check(r.equals(m), "round-trip equals");
        check(r.get("a") == 1 && r.get("b") == 2 && r.get(null) == 3, "round-trip contents");
        check(r.containsKey("n") && r.get("n") == null, "round-trip preserves null value");
    }

    // ---- deep contract coverage (validation / copy / clone / replaceAll / view write-channels / iterators / default-method null semantics / invariants / cross-impl equals / toArray / resize / treeify / subclass / thresholds / spliterators / streams / removeIf / GC) ----

    static void testConstructorValidation() {
        int[] badCaps = {-1, -7, -1000, Integer.MIN_VALUE + 1, Integer.MIN_VALUE};
        for (int cap : badCaps) {
            final int c = cap;
            checkThrows(IllegalArgumentException.class, () -> new HashMap<String, Integer>(c),
                    "HashMap(" + cap + ") should throw IAE");
        }
        for (int cap : new int[]{0, 1, 5, 64, 1 << 18}) {
            HashMap<String, Integer> m = new HashMap<>(cap);
            check(m.isEmpty() && m.size() == 0, "HashMap(" + cap + ") is legal and empty");
        }
        float[] badLf = {0f, -0.0f, -0.75f, -3f, -Float.MIN_VALUE,
                Float.NEGATIVE_INFINITY, Float.NaN};
        for (float lf : badLf) {
            final float f = lf;
            checkThrows(IllegalArgumentException.class, () -> new HashMap<String, Integer>(16, f),
                    "HashMap(16, " + lf + ") should throw IAE");
        }
        for (float lf : new float[]{Float.MIN_VALUE, 0.5f, 0.75f, 8f, 256f}) {
            HashMap<String, Integer> m = new HashMap<>(16, lf);
            check(m.isEmpty(), "HashMap(16, " + lf + ") is legal and empty");
        }
        HashMap<String, Integer> edge = new HashMap<>(0, Float.MIN_VALUE);
        edge.put("a", 1);
        edge.put("b", 2);
        check(edge.get("a") == 1 && edge.get("b") == 2, "HashMap(0, MIN_VALUE) still works");
        Map<String, Integer> nullMap = null;
        checkThrows(NullPointerException.class, () -> new HashMap<String, Integer>(nullMap),
                "HashMap((Map)null) should throw NPE");
    }

    static void testCopyConstructorAndPutAll() {
        HashMap<String, StringBuilder> src = new HashMap<>();
        src.put("a", new StringBuilder("va"));
        src.put("b", new StringBuilder("vb"));
        src.put(null, null);
        HashMap<String, StringBuilder> copy = new HashMap<>(src);
        check(copy.size() == 3 && copy.equals(src) && src.equals(copy),
                "copy ctor content equal");
        check(copy.get("a") == src.get("a") && copy.get("b") == src.get("b"),
                "copy ctor keeps value references (shallow)");
        check(copy.containsKey(null) && copy.get(null) == null,
                "copy ctor keeps null key and null value");
        copy.put("onlyInCopy", new StringBuilder("x"));
        check(!src.containsKey("onlyInCopy"), "copy is independent of source");

        checkThrows(NullPointerException.class, () -> src.putAll(null),
                "putAll(null) should throw NPE");

        HashMap<String, Integer> target = new HashMap<>();
        target.putAll(new HashMap<String, Integer>());
        check(target.isEmpty(), "putAll(empty) onto empty map");
        target.put("t1", 1);
        target.putAll(new HashMap<String, Integer>());
        check(target.size() == 1 && target.get("t1") == 1, "putAll(empty) keeps content");

        HashMap<String, Integer> fromEmpty = new HashMap<>();
        HashMap<String, Integer> donor = new HashMap<>();
        donor.put("d1", 10);
        donor.put("d2", 20);
        fromEmpty.putAll(donor);
        check(fromEmpty.size() == 2 && fromEmpty.get("d1") == 10 && fromEmpty.get("d2") == 20,
                "putAll copies all mappings into empty map");

        HashMap<String, Integer> t2 = new HashMap<>();
        t2.put("common", 1);
        t2.put("onlyT2", 2);
        HashMap<String, Integer> d2 = new HashMap<>();
        d2.put("common", 100);
        d2.put("onlyD2", 200);
        t2.putAll(d2);
        check(t2.size() == 3, "putAll union size");
        check(t2.get("common") == 100, "putAll overwrites intersecting key");
        check(t2.get("onlyT2") == 2 && t2.get("onlyD2") == 200,
                "putAll keeps non-intersecting keys");
    }

    static void testClone() {
        HashMap<String, int[]> m = new HashMap<>();
        int[] shared = {1};
        m.put("a", shared);
        m.put("b", new int[]{2});
        @SuppressWarnings("unchecked")
        HashMap<String, int[]> c = (HashMap<String, int[]>) m.clone();
        check(c != m && c.getClass() == HashMap.class, "clone returns a distinct HashMap");
        check(c.size() == 2 && c.equals(m), "clone content equal");
        check(c.get("a") == shared, "clone is shallow: value references shared");
        c.put("c", new int[]{3});
        m.remove("b");
        check(m.size() == 1 && !m.containsKey("c"), "original unaffected by clone mutation");
        check(c.size() == 3 && c.containsKey("b"), "clone unaffected by original mutation");
        @SuppressWarnings("unchecked")
        HashMap<String, int[]> ec = (HashMap<String, int[]>) new HashMap<String, int[]>().clone();
        check(ec.isEmpty(), "clone of empty map is empty");
    }

    static void testReplaceAll() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("n", null);
        m.replaceAll((k, v) -> v == null ? 0 : v * 10);
        check(m.get("a") == 10 && m.get("b") == 20 && m.get("n") == 0,
                "replaceAll transforms all values");
        check(m.size() == 3, "replaceAll keeps size");
        m.replaceAll((k, v) -> null);
        check(m.size() == 3 && m.containsKey("a") && !m.containsValue(10),
                "replaceAll to null is allowed and keeps mappings");
        checkThrows(NullPointerException.class, () -> m.replaceAll(null),
                "replaceAll(null) should throw NPE");
    }

    static void testViewWriteChannels() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        check(m.keySet().removeAll(Arrays.asList("a", "c")), "keySet.removeAll changed");
        check(m.size() == 1 && m.containsKey("b"), "keySet.removeAll reflects in map");
        check(!m.keySet().removeAll(Arrays.asList("zz")), "keySet.removeAll no-op returns false");
        m.put("d", 4);
        check(m.keySet().retainAll(Arrays.asList("b")), "keySet.retainAll changed");
        check(m.size() == 1 && m.containsKey("b") && !m.containsKey("d"),
                "keySet.retainAll reflects in map");
        m.keySet().clear();
        check(m.isEmpty(), "keySet.clear empties map");

        HashMap<String, Integer> nv = new HashMap<>();
        nv.put("k", null);
        nv.put("other", 5);
        check(nv.keySet().remove("k"), "keySet.remove on null-valued key must return true");
        check(!nv.containsKey("k") && nv.size() == 1, "null-valued mapping removed via keySet");
        nv.put("v", null);
        check(nv.values().remove(null), "values.remove(null) must remove the mapping");
        check(!nv.containsKey("v") && !nv.containsValue(null), "null value removed via values view");

        HashMap<String, Integer> vm = new HashMap<>();
        vm.put("a", 1);
        vm.put("b", 2);
        vm.put("c", 3);
        check(vm.values().remove(Integer.valueOf(2)), "values.remove by value");
        check(vm.size() == 2 && !vm.containsValue(2), "values.remove reflects in map");
        check(vm.values().removeAll(Arrays.asList(1, 3)), "values.removeAll");
        check(vm.isEmpty(), "values.removeAll empties map");
        vm.put("x", 1);
        vm.put("y", 2);
        check(vm.values().retainAll(Arrays.asList(1)), "values.retainAll");
        check(vm.size() == 1 && vm.containsKey("x"), "values.retainAll keeps matching mapping");
        vm.values().clear();
        check(vm.isEmpty(), "values.clear empties map");

        HashMap<String, Integer> em = new HashMap<>();
        em.put("a", 1);
        em.put("b", 2);
        em.put("n", null);
        check(em.entrySet().contains(new AbstractMap.SimpleEntry<String, Integer>("a", 1)),
                "entrySet.contains with equal SimpleEntry");
        check(em.entrySet().remove(new AbstractMap.SimpleEntry<String, Integer>("a", 1)),
                "entrySet.remove with equal SimpleEntry");
        check(!em.containsKey("a") && em.size() == 2, "SimpleEntry removal deletes the mapping");
        check(em.entrySet().remove(new AbstractMap.SimpleEntry<String, Integer>("n", null)),
                "entrySet.remove of null-valued entry");
        check(!em.containsKey("n"), "null-valued entry removed via entrySet");
        em.put("c", 3);
        check(em.entrySet().removeAll(Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("b", 2),
                new AbstractMap.SimpleEntry<String, Integer>("c", 3))),
                "entrySet.removeAll");
        check(em.isEmpty(), "entrySet.removeAll empties map");
        em.put("r1", 1);
        em.put("r2", 2);
        check(em.entrySet().retainAll(Arrays.asList(
                new AbstractMap.SimpleEntry<String, Integer>("r1", 1))),
                "entrySet.retainAll");
        check(em.size() == 1 && em.containsKey("r1"), "entrySet.retainAll keeps one");
        em.entrySet().clear();
        check(em.isEmpty(), "entrySet.clear empties map");

        HashMap<String, Integer> am = new HashMap<>();
        am.put("a", 1);
        checkThrows(UnsupportedOperationException.class, () -> am.keySet().add("x"),
                "keySet.add should throw UOE");
        checkThrows(UnsupportedOperationException.class, () -> am.keySet().addAll(Arrays.asList("x")),
                "keySet.addAll should throw UOE");
        checkThrows(UnsupportedOperationException.class, () -> am.values().add(9),
                "values.add should throw UOE");
        checkThrows(UnsupportedOperationException.class, () -> am.values().addAll(Arrays.asList(9)),
                "values.addAll should throw UOE");
        checkThrows(UnsupportedOperationException.class,
                () -> am.entrySet().add(new AbstractMap.SimpleEntry<String, Integer>("x", 9)),
                "entrySet.add should throw UOE");
        checkThrows(UnsupportedOperationException.class,
                () -> am.entrySet().addAll(Arrays.asList(
                        new AbstractMap.SimpleEntry<String, Integer>("x", 9))),
                "entrySet.addAll should throw UOE");
        check(am.size() == 1, "map unchanged after rejected adds");

        HashMap<String, Integer> im = new HashMap<>();
        im.put("a", 1);
        im.put("b", 2);
        im.put("c", 3);
        for (Iterator<String> it = im.keySet().iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
        check(im.isEmpty(), "keySet iterator.remove empties map");
        im.put("a", 1);
        im.put("b", 2);
        for (Iterator<Integer> it = im.values().iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
        check(im.isEmpty(), "values iterator.remove empties map");
        im.put("a", 1);
        im.put("b", 2);
        for (Iterator<Map.Entry<String, Integer>> it = im.entrySet().iterator(); it.hasNext(); ) {
            it.next();
            it.remove();
        }
        check(im.isEmpty(), "entrySet iterator.remove empties map");
    }

    static void testIteratorsAndCME() {
        HashMap<String, Integer> m = filled();
        Iterator<String> it = m.keySet().iterator();
        it.next();
        m.put("newKey", 9);
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                "CME after put of a new key");

        m = filled();
        it = m.keySet().iterator();
        it.next();
        m.remove("a");
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                "CME after remove");

        m = filled();
        it = m.keySet().iterator();
        it.next();
        m.clear();
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                "CME after clear");

        m = filled();
        it = m.keySet().iterator();
        it.next();
        Map<String, Integer> donor = new HashMap<>();
        donor.put("d", 4);
        m.putAll(donor);
        checkThrows(java.util.ConcurrentModificationException.class, it::next,
                "CME after putAll");

        m = filled();
        Iterator<Map.Entry<String, Integer>> eit = m.entrySet().iterator();
        eit.next();
        m.values().remove(Integer.valueOf(1));
        checkThrows(java.util.ConcurrentModificationException.class, eit::next,
                "CME after removal through another view");

        m = filled();
        it = m.keySet().iterator();
        m.put("a", 111);
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n == 3, "no CME on value overwrite, iteration complete");

        m = filled();
        it = m.keySet().iterator();
        checkThrows(IllegalStateException.class, it::remove,
                "remove() before next() should throw ISE");
        it.next();
        it.remove();
        check(m.size() == 2, "iterator.remove removes one mapping");
        int rest = 0;
        while (it.hasNext()) { it.next(); rest++; }
        check(rest == 2, "iteration continues after iterator.remove");

        Iterator<String> empty = new HashMap<String, Integer>().keySet().iterator();
        check(!empty.hasNext(), "empty iterator has no next");
        checkThrows(java.util.NoSuchElementException.class, empty::next,
                "empty iterator next() should throw NSEE");
    }

    static void testJava8DefaultsDeepNullSemantics() {
        HashMap<String, Integer> m = new HashMap<>();

        m.put("nl", null);
        check(m.getOrDefault("nl", 7) == null, "getOrDefault returns mapped null, not default");

        check(m.putIfAbsent("nl", 8) == null, "putIfAbsent on null-valued key returns null");
        check(m.get("nl") == 8, "putIfAbsent overwrote the null value");

        m.put("nl2", null);
        final boolean[] called = {false};
        check(m.computeIfAbsent("nl2", k -> { called[0] = true; return 9; }) == 9,
                "computeIfAbsent on null-valued key computes");
        check(called[0], "computeIfAbsent invoked for null-valued mapping");
        final boolean[] called2 = {false};
        check(m.computeIfAbsent("nl2", k -> { called2[0] = true; return 99; }) == 9,
                "computeIfAbsent present returns current value");
        check(!called2[0], "computeIfAbsent must NOT invoke function when value present");

        m.put("nl3", null);
        final boolean[] called3 = {false};
        check(m.merge("nl3", 10, (a, b) -> { called3[0] = true; return a + b; }) == 10,
                "merge on null-valued key inserts the value");
        check(!called3[0], "merge must NOT invoke function when old value is null");

        m.put("del1", 1);
        check(m.computeIfPresent("del1", (k, v) -> null) == null, "computeIfPresent null return");
        check(!m.containsKey("del1"), "computeIfPresent null return removes mapping");
        m.put("del2", 2);
        check(m.compute("del2", (k, v) -> null) == null, "compute null return");
        check(!m.containsKey("del2"), "compute null return removes mapping");
        check(m.compute("absent1", (k, v) -> null) == null, "compute absent null return");
        check(!m.containsKey("absent1"), "compute absent null return records nothing");
        check(m.computeIfAbsent("absent2", k -> null) == null, "computeIfAbsent null return");
        check(!m.containsKey("absent2"), "computeIfAbsent null return records nothing");
        m.put("del3", 3);
        check(m.merge("del3", 1, (a, b) -> null) == null, "merge null return");
        check(!m.containsKey("del3"), "merge null return removes mapping");

        m.put("keep", 5);
        final RuntimeException boom = new RuntimeException("boom");
        try {
            m.compute("keep", (k, v) -> { throw boom; });
            check(false, "compute with throwing function must propagate");
        } catch (RuntimeException e) {
            check(e == boom, "compute relays the original exception");
        }
        check(m.get("keep") == 5, "map unchanged after throwing compute");
        try {
            m.computeIfAbsent("never", k -> { throw boom; });
            check(false, "computeIfAbsent with throwing function must propagate");
        } catch (RuntimeException e) {
            check(e == boom, "computeIfAbsent relays exception");
        }
        check(!m.containsKey("never"), "no mapping recorded when function throws");
        try {
            m.merge("keep", 1, (a, b) -> { throw boom; });
            check(false, "merge with throwing function must propagate");
        } catch (RuntimeException e) {
            check(e == boom, "merge relays exception");
        }
        check(m.get("keep") == 5, "value intact after throwing merge");
        check(m.merge("never2", 1, (a, b) -> { throw boom; }) == 1,
                "merge with absent key inserts without invoking the function");
        check(m.get("never2") == 1, "absent-key merge recorded the value");
        try {
            m.computeIfPresent("keep", (k, v) -> { throw boom; });
            check(false, "computeIfPresent with throwing function must propagate");
        } catch (RuntimeException e) {
            check(e == boom, "computeIfPresent relays exception");
        }
        check(m.get("keep") == 5, "existing value intact after throwing computeIfPresent");

        m.put("pv", 1);
        m.compute("pv", (k, v) -> { check(v != null, "compute passes non-null old value"); return v; });
        m.compute("nokey", (k, v) -> { check(v == null, "compute passes null for absent key"); return null; });

        checkThrows(NullPointerException.class, () -> m.merge("absent3", null, (a, b) -> 1),
                "merge(k, null, f) with absent key should throw NPE");
        checkThrows(NullPointerException.class, () -> m.merge("pv", null, (a, b) -> 1),
                "merge(k, null, f) with present key should throw NPE");
        checkThrows(NullPointerException.class, () -> m.forEach(null), "forEach(null) NPE");
        checkThrows(NullPointerException.class, () -> m.replaceAll(null), "replaceAll(null) NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent("x", null),
                "computeIfAbsent(k, null) NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent("x", null),
                "computeIfPresent(k, null) NPE");
        checkThrows(NullPointerException.class, () -> m.compute("x", null), "compute(k, null) NPE");
        checkThrows(NullPointerException.class, () -> m.merge("x", 1, null), "merge(k, v, null) NPE");

        check(m.putIfAbsent(null, 1) == null, "putIfAbsent(null, v) allowed");
        check(m.getOrDefault(null, 99) == 1, "getOrDefault(null, d) works");
        check(m.merge(null, 1, (a, b) -> a + b) == 2, "merge(null, v, f) allowed");
        check(m.compute(null, (k, v) -> v + 1) == 3, "compute(null, f) allowed");
        check(m.remove(null, 3), "remove(null, v) works");
        check(m.replace(null, 4) == null, "replace(null, v) on absent null key");
        check(m.putIfAbsent(null, 5) == null, "putIfAbsent(null,5) after removal");
        check(m.replace(null, 5, 6) && m.get(null) == 6, "replace(null, old, new) matches null key");
        m.remove(null);

        m.put("z", null);
        check(m.remove("z", null), "remove(k, null) removes null-valued mapping");
        m.put("z", null);
        check(!m.remove("z", 99) && m.containsKey("z"),
                "remove(k, nonNull) does not match null value");
        check(m.replace("z", null, 7), "replace(k, null, v) matches null old value");
        check(m.get("z") == 7, "replace(k, null, v) writes the new value");
        check(!m.replace("z", 999, 8) && m.get("z") == 7, "replace(k, wrongOld, v) is a no-op");
        check(m.replace("z", 7, null), "replace(k, v, null) allowed");
        check(m.get("z") == null && m.containsKey("z"), "mapping kept after replace to null");
    }

    static void testStateInvariants() {
        HashMap<String, Integer> m = new HashMap<>();
        for (int i = 0; i < 8; i++) m.put("e" + i, i * 2);
        m.put(null, 100);

        check(m.remove("e3") == 6, "remove returns old value");
        check(m.size() == 8 && !m.containsKey("e3"), "size after remove");
        boolean siblings = true;
        for (int i = 0; i < 8; i++) {
            if (i != 3 && (m.get("e" + i) == null || m.get("e" + i) != i * 2)) siblings = false;
        }
        check(siblings, "all sibling mappings survive the remove");
        check(m.get(null) == 100, "null key mapping survives");

        check(!m.remove("e1", 999), "remove(K,V) value mismatch returns false");
        check(m.get("e1") == 2 && m.size() == 8, "failed remove(K,V) leaves map untouched");
        check(!m.replace("e2", 999, 888), "replace(K,V,V) old mismatch returns false");
        check(m.get("e2") == 4, "failed replace leaves value untouched");
        check(m.remove("noSuchKey") == null, "remove absent returns null");
        check(m.size() == 8, "size unchanged after failed operations");

        m.clear();
        check(m.size() == 0 && m.isEmpty(), "clear empties map");
        boolean allGone = true;
        for (int i = 0; i < 8; i++) {
            if (m.get("e" + i) != null || m.containsKey("e" + i)) allGone = false;
        }
        check(allGone && m.get(null) == null && !m.containsKey(null),
                "every key gone after clear, verified per key");
    }

    static void testEqualsCrossImplAndToString() {
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("a", 1);
        hm.put("b", 2);
        Map<String, Integer> tm = new TreeMap<>();
        tm.put("b", 2);
        tm.put("a", 1);
        Map<String, Integer> lm = new LinkedHashMap<>();
        lm.put("a", 1);
        lm.put("b", 2);
        check(hm.equals(tm) && tm.equals(hm), "HashMap equals TreeMap, symmetric");
        check(hm.equals(lm) && lm.equals(hm), "HashMap equals LinkedHashMap");
        check(hm.hashCode() == tm.hashCode() && hm.hashCode() == lm.hashCode(),
                "equal maps have equal hashCode across implementations");
        tm.put("c", 3);
        check(!hm.equals(tm) && !tm.equals(hm), "different content not equal");
        check(!hm.equals(null), "map never equals null");
        check(!hm.equals("a string"), "map never equals a non-Map object");

        HashMap<String, Integer> nm = new HashMap<>();
        nm.put(null, null);
        nm.put("a", 1);
        String s = nm.toString();
        check(s != null && s.contains("null=null"), "map toString handles null key/value");
        check(new HashMap<String, Integer>().toString().equals("{}"), "empty map toString is {}");
        boolean entryOk = false;
        for (Map.Entry<String, Integer> e : nm.entrySet()) {
            if (e.getKey() == null) { check(e.toString() != null, "entry toString is null-safe"); entryOk = true; }
        }
        check(entryOk, "null-key entry present");
    }

    static void testToArrayContract() {
        HashMap<String, Integer> m = filled();
        Set<String> ks = m.keySet();
        Object[] objs = ks.toArray();
        check(objs.length == 3 && ks.containsAll(Arrays.asList(objs)), "keySet.toArray() content");
        String[] exact = ks.toArray(new String[0]);
        check(exact.length == 3 && new HashSet<>(Arrays.asList(exact)).containsAll(ks),
                "keySet.toArray(empty array) has exact size");
        String[] buf = new String[3];
        check(ks.toArray(buf) == buf, "toArray(fit-size array) reuses the array");
        check(new HashSet<>(Arrays.asList(buf)).containsAll(ks), "reused array holds all keys");
        String[] big = new String[5];
        check(ks.toArray(big) == big, "toArray(bigger array) reuses the array");
        check(big[3] == null, "toArray(bigger array) null-terminates at size");

        List<Integer> vs = Arrays.asList(m.values().toArray(new Integer[0]));
        check(vs.size() == 3 && vs.containsAll(Arrays.asList(1, 2, 3)),
                "values.toArray(T[]) content");
        Integer[] vbuf = new Integer[3];
        check(m.values().toArray(vbuf) == vbuf, "values.toArray(fit) reuses the array");
        Integer[] vbig = new Integer[4];
        check(m.values().toArray(vbig) == vbig && vbig[3] == null,
                "values.toArray(big) reuses and null-terminates");

        HashMap<String, Integer> empty = new HashMap<>();
        check(empty.keySet().toArray().length == 0, "empty keySet toArray length 0");
        check(empty.values().toArray(new Integer[0]).length == 0, "empty values toArray length 0");
    }

    static void testNullKeyAtResize() {
        HashMap<Integer, Integer> m = new HashMap<>(2);
        for (int i = 0; i < 300; i++) {
            m.put(null, null);
            m.put(i, i * 3);
            m.remove(null);
            if (m.get(i) != i * 3) check(false, "entry lost during resize with null key, i=" + i);
        }
        check(m.size() == 300, "size after growth with null-key churn");
        check(m.get(299) == 299 * 3, "spot check after growth");
        m.put(null, -1);
        check(m.get(null) == -1 && m.size() == 301, "null key works after all resizes");
    }

    static void testPutOverwriteDuringIteration() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i < 13; i++) keys.add("k" + i);
        for (int cut = 0; cut < keys.size(); cut++) {
            HashMap<String, Integer> m = new HashMap<>(16);
            for (int i = 0; i < keys.size(); i++) m.put(keys.get(i), i);
            Set<String> seen = new HashSet<>();
            int consumed = 0;
            for (Iterator<String> it = m.keySet().iterator(); it.hasNext(); ) {
                String k = it.next();
                seen.add(k);
                if (consumed++ == cut) m.put("k0", 999);
            }
            check(seen.size() == keys.size() && seen.containsAll(keys),
                    "no lost/duplicated keys when overwriting during iteration, cut=" + cut);
            check(m.get("k0") == 999, "overwrite visible after iteration, cut=" + cut);
        }
    }

    static final class CollidingKey42 implements Comparable<CollidingKey42> {
        final int id;
        CollidingKey42(int id) { this.id = id; }
        @Override public int hashCode() { return 42; }
        @Override public boolean equals(Object o) {
            return (o instanceof CollidingKey42) && ((CollidingKey42) o).id == id;
        }
        @Override public int compareTo(CollidingKey42 o) { return Integer.compare(id, o.id); }
    }

    static void testTreeifiedBins() {
        HashMap<CollidingKey42, String> m = new HashMap<>(64);
        for (int i = 0; i < 10; i++) m.put(new CollidingKey42(i), "v" + i);
        m.put(null, "nv");
        check(m.size() == 11, "size after treeified inserts");
        boolean all = true;
        for (int i = 0; i < 10; i++) {
            if (!("v" + i).equals(m.get(new CollidingKey42(i)))) all = false;
        }
        check(all, "get on treeified bin");
        check("nv".equals(m.get(null)), "get null key alongside treeified bin");
        int n = 0;
        for (Map.Entry<CollidingKey42, String> e : m.entrySet()) n++;
        check(n == 11, "iterate map containing treeified bin");
        boolean removed = true;
        for (int i = 0; i < 10; i++) {
            if (!("v" + i).equals(m.remove(new CollidingKey42(i)))) removed = false;
        }
        check(removed, "remove from treeified bin returns old values");
        check("nv".equals(m.remove(null)) && m.isEmpty(), "map empty after tree drain");

        HashMap<CollidingKey42, String> m2 = new HashMap<>(64);
        for (int i = 0; i < 11; i++) m2.put(new CollidingKey42(i), "w" + i);
        for (Iterator<Map.Entry<CollidingKey42, String>> it = m2.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<CollidingKey42, String> e = it.next();
            if (e.getKey().id == 5) e.setValue("NEW");
            else it.remove();
        }
        check(m2.size() == 1, "only the setValue entry remains");
        check("NEW".equals(m2.get(new CollidingKey42(5))), "setValue survived sibling iterator.remove");
        check(!m2.containsValue("w5"), "old value replaced");
    }

    static final class LyingHashMap extends HashMap<String, Integer> {
        @Override public int size() { return super.size() + 1; }
        @Override public boolean isEmpty() { return false; }
    }

    static void testSubclassOverrides() {
        LyingHashMap m = new LyingHashMap();
        check(m.get("absent") == null, "get on overridden subclass");
        check(!m.containsKey("absent"), "containsKey on overridden subclass");
        check(m.remove("absent") == null, "remove on overridden subclass");
        check(m.replace("absent", 1) == null, "replace(k,v) on overridden subclass");
        check(!m.replace("absent", 1, 2), "replace(k,v,v) on overridden subclass");
        check(m.computeIfPresent("absent", (k, v) -> 1) == null,
                "computeIfPresent on overridden subclass");
        check(m.getOrDefault("absent", 9) == 9, "getOrDefault on overridden subclass");
        m.put("a", 1);
        check(m.remove("a") == 1, "real data ops still work on subclass");
    }

    static void testResizeThresholds() {
        int[] caps = {8, 16, 32, 64};
        for (int cap : caps) {
            int threshold = (int) (cap * 0.75f);
            HashMap<Integer, Integer> m = new HashMap<>(cap);
            for (int i = 0; i <= threshold + 1; i++) {
                m.put(i, i + 1);
                if (m.size() != i + 1) check(false, "size drift at i=" + i + ", cap=" + cap);
            }
            boolean ok = true;
            for (int i = 0; i <= threshold + 1; i++) {
                if (m.get(i) != i + 1) ok = false;
            }
            check(ok, "all entries intact across resize boundary, cap=" + cap);
            int n = 0;
            for (Integer k : m.keySet()) n++;
            check(n == threshold + 2, "iteration count across resize boundary, cap=" + cap);
        }
    }

    static void testSerializationRoundTrip() throws Exception {
        HashMap<String, Integer> m = filled();
        m.put(null, null);
        HashMap<String, Integer> back = roundTrip(m);
        check(back.getClass() == HashMap.class, "round-trip keeps HashMap type");
        check(back.size() == 4 && back.equals(m) && m.equals(back), "round-trip content equal");
        check(back.get(null) == null && back.get("a") == 1, "round-trip null key/value");
        back.put("added", 9);
        back.remove("b");
        check(back.size() == 4 && back.get("added") == 9 && !back.containsKey("b"),
                "deserialized map is fully functional");
        HashMap<String, Integer> emptyBack = roundTrip(new HashMap<String, Integer>());
        check(emptyBack.isEmpty(), "empty map round-trip");
    }

    static void testViewSpliterators() {
        HashMap<String, Integer> m = filled();
        Spliterator<String> ks = m.keySet().spliterator();
        check(ks.hasCharacteristics(Spliterator.SIZED), "keySet spliterator SIZED");
        check(ks.hasCharacteristics(Spliterator.DISTINCT), "keySet spliterator DISTINCT");
        check(ks.estimateSize() == 3 && ks.getExactSizeIfKnown() == 3,
                "keySet spliterator exact size");
        checkThrows(IllegalStateException.class, ks::getComparator,
                "unsorted spliterator getComparator should throw ISE");

        Spliterator<Integer> vs = m.values().spliterator();
        check(vs.hasCharacteristics(Spliterator.SIZED), "values spliterator SIZED");
        check(!vs.hasCharacteristics(Spliterator.DISTINCT),
                "values spliterator must not claim DISTINCT");
        check(vs.getExactSizeIfKnown() == 3, "values spliterator exact size");

        Spliterator<Map.Entry<String, Integer>> es = m.entrySet().spliterator();
        check(es.hasCharacteristics(Spliterator.SIZED), "entrySet spliterator SIZED");

        final int[] count = {0};
        Spliterator<String> adv = m.keySet().spliterator();
        while (adv.tryAdvance(k -> count[0]++)) { /* walk all */ }
        check(count[0] == 3, "tryAdvance visits exactly size elements");
    }

    static void testViewStreams() {
        HashMap<String, Integer> m = filled();
        Set<String> expectKeys = new HashSet<>(Arrays.asList("a", "b", "c"));

        check(m.keySet().stream().count() == 3, "keySet stream count");
        Set<String> noA = m.keySet().stream().filter(k -> !"a".equals(k)).collect(Collectors.toSet());
        check(noA.equals(new HashSet<>(Arrays.asList("b", "c"))), "keySet stream filter+collect");
        Set<String> noAp = m.keySet().parallelStream().filter(k -> !"a".equals(k))
                .collect(Collectors.toSet());
        check(noAp.equals(noA), "parallel stream matches sequential");
        check(m.keySet().stream().anyMatch("a"::equals), "anyMatch");
        check(m.keySet().stream().noneMatch("zz"::equals), "noneMatch");
        check(m.keySet().stream().allMatch(Objects::nonNull), "allMatch");
        check(m.keySet().stream().findAny().isPresent(), "findAny present");
        check(m.keySet().stream().mapToInt(String::length).sum() == 3, "mapToInt sum");
        check(m.keySet().stream().toArray().length == 3, "stream toArray length");

        check(m.values().stream().mapToInt(Integer::intValue).sum() == 6, "values stream sum");
        check(m.values().stream().distinct().count() == 3, "values distinct count");
        check(m.values().parallelStream().mapToInt(Integer::intValue).sum() == 6,
                "values parallel sum");

        check(m.entrySet().stream().count() == 3, "entrySet stream count");
        Set<String> keysFromEntries = m.entrySet().stream()
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        check(keysFromEntries.equals(expectKeys), "entrySet stream map to keys");
        check(m.entrySet().stream().filter(e -> e.getValue() >= 2).count() == 2,
                "entrySet stream filter");
        Set<String> parKeys = m.entrySet().parallelStream()
                .map(Map.Entry::getKey).collect(Collectors.toSet());
        check(parKeys.equals(expectKeys), "entrySet parallel stream matches sequential");
    }

    static void testViewRemoveIfForEach() {
        HashMap<String, Integer> m = new HashMap<>();
        m.put("xa", 1);
        m.put("xb", 2);
        m.put("good", 3);
        check(m.keySet().removeIf(k -> k.startsWith("x")), "keySet.removeIf removed some");
        check(m.size() == 1 && m.containsKey("good"), "keySet.removeIf reflects in map");

        HashMap<String, Integer> nv = new HashMap<>();
        nv.put("a", null);
        nv.put("b", 1);
        check(nv.values().removeIf(Objects::isNull),
                "values.removeIf removes the null-valued mapping");
        check(nv.size() == 1 && nv.containsKey("b"), "values.removeIf keeps others");

        HashMap<String, Integer> em = filled();
        check(em.entrySet().removeIf(e -> e.getValue() > 2), "entrySet.removeIf removed some");
        check(em.size() == 2 && !em.containsValue(3), "entrySet.removeIf reflects in map");
        check(!em.entrySet().removeIf(e -> false), "removeIf no-op returns false");

        HashMap<String, Integer> t = filled();
        checkThrows(UnsupportedOperationException.class,
                () -> t.keySet().removeIf(k -> { throw new UnsupportedOperationException("boom"); }),
                "removeIf relays RuntimeException");
        checkThrows(Error.class,
                () -> t.keySet().removeIf(k -> { throw new Error("fatal"); }),
                "removeIf relays Error");

        HashMap<String, Integer> f = filled();
        Set<String> visited = new HashSet<>();
        f.keySet().forEach(visited::add);
        check(visited.equals(new HashSet<>(Arrays.asList("a", "b", "c"))),
                "keySet forEach covers all");
        checkThrows(NullPointerException.class, () -> f.keySet().forEach(null),
                "keySet forEach(null) NPE");
        checkThrows(IllegalStateException.class,
                () -> f.values().forEach(v -> { if (v == 1) throw new IllegalStateException("stop"); }),
                "forEach relays action exception");
        Set<String> ev = new HashSet<>();
        f.entrySet().forEach(e -> ev.add(e.getKey()));
        check(ev.size() == 3, "entrySet forEach covers all");
    }

    static void testClearReleasesReferences() {
        List<WeakReference<byte[]>> refs = new ArrayList<>();
        HashMap<Integer, byte[]> m = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            byte[] payload = new byte[1024];
            refs.add(new WeakReference<>(payload));
            m.put(i, payload);
        }
        @SuppressWarnings("unchecked")
        HashMap<Integer, byte[]> c = (HashMap<Integer, byte[]>) m.clone();
        check(c.size() == 100, "clone before leak check");
        m.clear();
        c.clear();
        check(m.isEmpty() && c.isEmpty(), "both maps cleared");
        boolean cleared = false;
        for (int attempt = 0; attempt < 10 && !cleared; attempt++) {
            System.gc();
            cleared = true;
            for (WeakReference<byte[]> r : refs) {
                if (r.get() != null) { cleared = false; break; }
            }
        }
        check(cleared, "values should be collectable after clear (possible reference leak)");
    }

    public static void main(String[] args) throws Exception {
        testPutGetRemove();
        testViews();
        testEqualsHashCode();
        testRehash();
        testJava8Defaults();
        testConstructors();
        testConstructorValidation();
        testPutAllReplaceAll();
        testCopyConstructorAndPutAll();
        testCloneToString();
        testClone();
        testReplaceAll();
        testViewOperations();
        testViewWriteChannels();
        testEntrySetValue();
        testConcurrentModification();
        testIteratorsAndCME();
        testResizeNullKey();
        testNullKeyAtResize();
        testPutOverwriteDuringIteration();
        testTreeifyNullKey();
        testTreeifiedBins();
        testSpliteratorAndStreams();
        testViewSpliterators();
        testViewStreams();
        testViewRemoveIfForEach();
        testSerialization();
        testSerializationRoundTrip();
        testJava8DefaultsDeepNullSemantics();
        testStateInvariants();
        testEqualsCrossImplAndToString();
        testToArrayContract();
        testSubclassOverrides();
        testResizeThresholds();
        testClearReleasesReferences();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
