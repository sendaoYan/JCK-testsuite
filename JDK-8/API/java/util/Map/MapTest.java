/*
 * @test
 * @summary java.util.Map API conformance: put/get/remove return semantics, containsKey/
 *          containsValue, keySet/values/entrySet, and the Java 8 default methods
 *          getOrDefault/putIfAbsent/computeIfAbsent/replaceAll/forEach.
 * @run main MapTest
 */

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class MapTest {
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

    static void testBasics() {
        Map<String, Integer> m = new HashMap<>();
        check(m.put("a", 1) == null, "put new returns null");
        check(m.put("a", 2) == 1, "put existing returns old value");
        check(m.get("a") == 2 && m.get("z") == null, "get");
        check(m.size() == 1, "size");
        check(m.containsKey("a") && !m.containsKey("z"), "containsKey");
        check(m.containsValue(2) && !m.containsValue(1), "containsValue");
        check(m.remove("a") == 2 && m.isEmpty(), "remove returns value, map empty");
    }

    static void testViews() {
        Map<String, Integer> m = new HashMap<>();
        m.put("x", 10);
        m.put("y", 20);
        check(m.keySet().size() == 2 && m.keySet().contains("x"), "keySet");
        check(m.values().contains(20), "values");
        check(m.entrySet().size() == 2, "entrySet");
    }

    static void testDefaults() {
        Map<String, Integer> m = new HashMap<>();
        m.put("x", 10);
        check(m.getOrDefault("x", -1) == 10 && m.getOrDefault("z", -1) == -1, "getOrDefault");
        check(m.putIfAbsent("x", 99) == 10, "putIfAbsent existing");
        check(m.putIfAbsent("z", 5) == null && m.get("z") == 5, "putIfAbsent new");
        check(m.computeIfAbsent("q", String::length) == 1 && m.get("q") == 1, "computeIfAbsent");
        m.replaceAll((k, v) -> v + 1);
        check(m.get("x") == 11, "replaceAll");
        final int[] count = {0};
        m.forEach((k, v) -> count[0]++);
        check(count[0] == m.size(), "forEach visits every entry");
    }

    static void testDefaultMethodContract() {
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);

        // remove(key, value) only when currently mapped to value
        check(m.remove("a", 2) == false, "remove(k,v) wrong value false");
        check(m.remove("a", 1) == true && !m.containsKey("a"), "remove(k,v) matching true");

        m.put("a", 1);
        check(m.replace("a", 10) == 1, "replace(k,v) returns old");
        check(m.get("a") == 10, "replace(k,v) updates");
        check(m.replace("z", 10) == null, "replace(k,v) absent returns null");

        check(m.replace("a", 10, 20) == true, "replace(k,old,new) matching true");
        check(m.replace("a", 99, 20) == false, "replace(k,old,new) wrong old false");
        check(m.get("a") == 20, "replace(k,old,new) updated");

        // computeIfPresent: present -> compute; absent -> no-op
        check(m.computeIfPresent("a", (k, v) -> v + 1) == 21, "computeIfPresent present");
        check(m.computeIfPresent("z", (k, v) -> v + 1) == null, "computeIfPresent absent no-op");
        check(!m.containsKey("z"), "computeIfPresent absent no mapping");

        // computeIfPresent: null result removes
        check(m.computeIfPresent("a", (k, v) -> null) == null, "computeIfPresent null result");
        check(!m.containsKey("a"), "computeIfPresent null result removes");

        // compute: always computes; null result removes
        m.put("a", 5);
        check(m.compute("a", (k, v) -> v * 2) == 10, "compute applies");
        check(m.compute("b", (k, v) -> 7) == 7, "compute absent inserts");
        check(m.get("b") == 7, "compute absent inserted");
        check(m.compute("b", (k, v) -> null) == null, "compute null result");
        check(!m.containsKey("b"), "compute null result removes");

        // merge: absent -> puts value; present -> remap; null result removes
        m.clear();
        m.put("a", 2);
        check(m.merge("a", 3, Integer::sum) == 5, "merge present sum");
        check(m.merge("b", 10, Integer::sum) == 10, "merge absent puts value");
        check(m.get("b") == 10, "merge absent value stored");
        check(m.merge("b", 1, (old, inc) -> null) == null, "merge null result");
        check(!m.containsKey("b"), "merge null result removes");

        // null remappingFunction / value -> NPE
        checkThrows(NullPointerException.class, () -> m.merge("a", null, Integer::sum),
                    "merge null value NPE");
        checkThrows(NullPointerException.class, () -> m.merge("a", 1, null), "merge null fn NPE");
        checkThrows(NullPointerException.class, () -> m.compute("a", null), "compute null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfAbsent("a", null),
                    "computeIfAbsent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.computeIfPresent("a", null),
                    "computeIfPresent null fn NPE");
        checkThrows(NullPointerException.class, () -> m.replaceAll(null), "replaceAll null fn NPE");
    }

    static void testDefaultReplaceAllForEach() {
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.replaceAll((k, v) -> v * 10);
        check(m.get("a") == 10 && m.get("b") == 20, "replaceAll transforms all values");

        final int[] sum = {0};
        m.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 30, "forEach visits all entries");
    }

    // ---- equals/hashCode formula lock ---------------------------------------

    static void testEqualsHashCodeFormula() {
        // hashCode = sum over entries of (key.hashCode() ^ value.hashCode());
        // "a".hashCode()==97, "b".hashCode()==98, Integer(1).hashCode()==1, Integer(2).hashCode()==2.
        // 97^1==96, 98^2==96 -> {"a"=1,"b"=2} hashes to 192 on every Map implementation.
        Map<String, Integer> hm = new HashMap<>();
        hm.put("a", 1);
        hm.put("b", 2);
        check(hm.hashCode() == 192, "HashMap hashCode formula == 192, got " + hm.hashCode());
        Map<String, Integer> tm = new TreeMap<>();
        tm.put("b", 2);
        tm.put("a", 1);
        check(tm.hashCode() == 192, "TreeMap hashCode formula == 192");
        Map<String, Integer> lhm = new LinkedHashMap<>();
        lhm.put("b", 2);
        lhm.put("a", 1);
        check(lhm.hashCode() == 192, "LinkedHashMap hashCode formula == 192");
        check(hm.equals(tm) && tm.equals(hm), "TreeMap equals HashMap symmetric");
        check(hm.equals(lhm) && lhm.equals(hm), "LinkedHashMap equals HashMap symmetric");
        check(hm.hashCode() == tm.hashCode() && tm.hashCode() == lhm.hashCode(),
                "equal maps equal hashCode across implementations");

        // null key hashes 0, null value hashes 0.
        Map<String, Integer> nv = new HashMap<>();
        nv.put("a", null); // 97 ^ 0
        check(nv.hashCode() == 97, "null value entry hash == key hash (97)");
        Map<String, Integer> nk = new HashMap<>();
        nk.put(null, 1);  // 0 ^ 1
        check(nk.hashCode() == 1, "null key entry hash == value hash (1)");
        check(nk.containsKey(null) && nk.get(null) == 1, "null key get/containsKey");

        // equals is entrySet-based: entrySet equality across implementations.
        check(hm.entrySet().equals(tm.entrySet()) && tm.entrySet().equals(lhm.entrySet()),
                "entrySet equality across implementations");
        Map<String, Integer> diff = new HashMap<>(hm);
        diff.put("b", 3);
        check(!hm.equals(diff) && !diff.equals(hm), "different value not equal");
        diff.put("b", 2);
        diff.put("c", 3);
        check(!hm.equals(diff), "different size not equal");
        check(hm.equals(hm), "equals reflexive");
        check(!hm.equals(null), "equals(null) false");
        check(!hm.equals(Arrays.asList("a", "b")), "equals non-Map false");
        check(new HashMap<String, Integer>().equals(new TreeMap<String, Integer>()),
                "empty maps equal across implementations");
    }

    // ---- Map.Entry contract --------------------------------------------------

    static void testEntryContract() {
        Map<String, Integer> m = new HashMap<>();
        m.put("k", 1);
        Map.Entry<String, Integer> live = m.entrySet().iterator().next();
        check("k".equals(live.getKey()), "live entry getKey");
        check(live.setValue(2) == 1, "live entry setValue returns old");
        check(m.get("k") == 2, "live entry setValue writes through");
        check(live.getValue() == 2, "entry getValue after setValue");

        // equals/hashCode formula: key.hashCode() ^ value.hashCode(); "k".hashCode()==107, 107^2==105.
        check(live.hashCode() == 105, "entry hashCode formula == 105, got " + live.hashCode());
        Map.Entry<String, Integer> indep = new AbstractMap.SimpleEntry<>("k", 2);
        check(live.equals(indep) && indep.equals(live), "live entry equals independent SimpleEntry");
        check(live.hashCode() == indep.hashCode(), "equal entries equal hashCode");
        check(!live.equals(new AbstractMap.SimpleEntry<>("k", 3)), "different value entries unequal");
        check(!live.equals(new AbstractMap.SimpleEntry<>("j", 2)), "different key entries unequal");
        check(!live.equals(null), "entry equals(null) false");
        check(!live.equals("k=2"), "entry equals(non-entry) false");
        check(live.toString().contains("k=2"), "entry toString contains key=value");

        // SimpleImmutableEntry rejects mutation.
        Map.Entry<String, Integer> imm = new AbstractMap.SimpleImmutableEntry<>("k", 2);
        checkThrows(UnsupportedOperationException.class, () -> imm.setValue(9),
                "SimpleImmutableEntry setValue UOE");
        check(imm.equals(live) && imm.hashCode() == 105, "SimpleImmutableEntry equals/hashCode same formula");
    }

    // ---- default method implementation paths via minimal Map probe ----------

    static class ProbeMap implements Map<String, Integer> {
        final HashMap<String, Integer> backend = new HashMap<>();
        int puts, removes, getCalls, containsKeyCalls, entrySetCalls, entrySetIterations;

        public int size() { return backend.size(); }
        public boolean isEmpty() { return backend.isEmpty(); }
        public boolean containsKey(Object k) { containsKeyCalls++; return backend.containsKey(k); }
        public boolean containsValue(Object v) { return backend.containsValue(v); }
        public Integer get(Object k) { getCalls++; return backend.get(k); }
        public Integer put(String k, Integer v) { puts++; return backend.put(k, v); }
        public Integer remove(Object k) { removes++; return backend.remove(k); }
        public void putAll(Map<? extends String, ? extends Integer> m) { backend.putAll(m); }
        public void clear() { backend.clear(); }
        public Set<String> keySet() { return backend.keySet(); }
        public Collection<Integer> values() { return backend.values(); }
        public Set<Map.Entry<String, Integer>> entrySet() {
            entrySetCalls++;
            return new HashSet<>(backend.entrySet());
        }
    }

    static void testDefaultMethodImplementationPaths() {
        // ProbeMap implements Map directly, so all default methods run the
        // interface's own implementations (no superclass overrides).
        ProbeMap p = new ProbeMap();
        p.put("x", 1);
        int putsBefore = p.puts;
        check(p.putIfAbsent("x", 99) == 1 && p.get("x") == 1, "default putIfAbsent present keeps value");
        check(p.puts == putsBefore, "default putIfAbsent present does not call put");
        check(p.putIfAbsent("y", 2) == null && p.get("y") == 2, "default putIfAbsent absent inserts");
        check(p.puts == putsBefore + 1, "default putIfAbsent absent calls put exactly once");

        // getOrDefault: present returns mapped value; absent consults map then default.
        p.getCalls = 0;
        check(p.getOrDefault("x", -1) == 1, "default getOrDefault present");
        check(p.getCalls >= 1, "default getOrDefault consults get");
        p.getCalls = 0;
        p.containsKeyCalls = 0;
        check(p.getOrDefault("zz", -1) == -1, "default getOrDefault absent returns default");
        check(p.getCalls + p.containsKeyCalls >= 1, "default getOrDefault absent consults the map");
        p.put("n", null);
        check(p.getOrDefault("n", -1) == null, "default getOrDefault present-null returns null not default");

        // computeIfAbsent: function skipped when present (even when value is null).
        final int[] calls = {0};
        check(p.computeIfAbsent("x", k -> { calls[0]++; return 7; }) == 1, "default computeIfAbsent present");
        check(calls[0] == 0, "default computeIfAbsent present skips function");
        check(p.computeIfAbsent("n", k -> { calls[0]++; return 8; }) == 8,
                "default computeIfAbsent present-null recomputes");
        check(calls[0] == 1, "default computeIfAbsent null value treated as absent");
        check(p.computeIfAbsent("new", k -> null) == null && !p.containsKey("new"),
                "default computeIfAbsent null result no entry");

        // computeIfPresent: absent (or null value) skips function entirely.
        calls[0] = 0;
        p.put("nullv", null);
        int puts2 = p.puts, rem2 = p.removes;
        check(p.computeIfPresent("absent", (k, v) -> { calls[0]++; return 1; }) == null,
                "default computeIfPresent absent returns null");
        check(p.computeIfPresent("nullv", (k, v) -> { calls[0]++; return 1; }) == null,
                "default computeIfPresent null-value returns null");
        check(calls[0] == 0, "default computeIfPresent skips function when absent/null");
        check(p.puts == puts2 && p.removes == rem2, "default computeIfPresent absent mutates nothing");

        // merge: absent (or null value) takes value directly, function not invoked.
        calls[0] = 0;
        check(p.merge("m1", 5, (o, v) -> { calls[0]++; return o + v; }) == 5, "default merge absent uses value");
        check(calls[0] == 0, "default merge absent skips function");
        check(p.merge("m1", 5, Integer::sum) == 10, "default merge present combines");
        check(p.merge("m2", 1, (o, v) -> null) == 1, "default merge null-value uses value");
        checkThrows(NullPointerException.class, () -> p.merge("m1", null, Integer::sum),
                "default merge null value NPE");
        checkThrows(NullPointerException.class, () -> p.merge("m1", 1, null),
                "default merge null fn NPE");

        // remove(k,v) consults get/remove; present-null removes via default impl.
        ProbeMap q = new ProbeMap();
        q.put("k", null);
        check(q.remove("k", null), "default remove(k,null) removes present-null mapping");
        check(!q.containsKey("k") && q.removes == 1, "default remove(k,v) delegates to remove(key)");

        // replaceAll/forEach iterate entrySet.
        ProbeMap r = new ProbeMap();
        r.put("a", 1);
        r.put("b", 2);
        r.replaceAll((k, v) -> v * 10);
        check(r.get("a") == 10 && r.get("b") == 20, "default replaceAll transforms all");
        check(r.entrySetCalls >= 1, "default replaceAll iterates entrySet");
        final int[] seen = {0};
        r.forEach((k, v) -> seen[0]++);
        check(seen[0] == 2, "default forEach visits every entry");
        checkThrows(NullPointerException.class, () -> r.forEach(null), "default forEach(null) NPE");
    }

    // ---- present-null-value branch matrix (HashMap carrier) ------------------

    static void testPresentNullValueBranches() {
        Map<String, Integer> m = new HashMap<>();
        m.put("k", null);

        // putIfAbsent overwrites a null present value and returns null (the old value).
        check(m.putIfAbsent("k", 1) == null, "putIfAbsent present-null returns null");
        check(m.get("k") == 1, "putIfAbsent present-null overwrote");

        // getOrDefault on present-null returns null, not the default.
        m.put("k", null);
        check(m.getOrDefault("k", -1) == null, "getOrDefault present-null returns null");

        // computeIfAbsent sees null value as absent and recomputes.
        final int[] calls = {0};
        check(m.computeIfAbsent("k", k -> { calls[0]++; return 2; }) == 2, "computeIfAbsent present-null recomputes");
        check(calls[0] == 1 && m.get("k") == 2, "computeIfAbsent present-null stores result");

        // computeIfPresent ignores a null-valued entry (treated as absent).
        calls[0] = 0;
        m.put("k", null);
        check(m.computeIfPresent("k", (k, v) -> { calls[0]++; return 3; }) == null,
                "computeIfPresent present-null no-op");
        check(calls[0] == 0 && m.containsKey("k"), "computeIfPresent present-null leaves entry");

        // compute sees oldValue==null; null result removes the entry entirely.
        check(m.compute("k", (k, v) -> v == null ? 4 : 9) == 4, "compute present-null sees null v");
        check(m.compute("k", (k, v) -> null) == null && !m.containsKey("k"),
                "compute present-null null-result removes entry");
        m.putIfAbsent("k", null); // restore null value
        check(m.compute("k", (k, v) -> null) == null && !m.containsKey("k"),
                "compute null-result removes present-null entry");

        // merge treats null value as absent: takes value, function skipped.
        calls[0] = 0;
        m.put("k", null);
        check(m.merge("k", 7, (o, x) -> { calls[0]++; return o + x; }) == 7, "merge present-null uses value");
        check(calls[0] == 0, "merge present-null skips function");

        // remove(k, null) removes a present-null mapping.
        m.put("k", null);
        check(m.remove("k", null), "remove(k,null) removes present-null");
        check(!m.containsKey("k"), "entry gone after remove(k,null)");
        check(!m.remove("zz", null), "remove(k,null) absent returns false");

        // replace(k, null, v) succeeds because old value null == null.
        m.put("k", null);
        check(m.replace("k", null, 5), "replace(k,null,v) matches null old");
        check(m.get("k") == 5, "replace(k,null,v) updated");
        // replace(k,v) returns previous null.
        m.put("k", null);
        check(m.replace("k", 6) == null && m.get("k") == 6, "replace(k,v) present-null returns null old");

        // exception passthrough leaves the mapping untouched (non-null current value).
        m.put("e", 1);
        final RuntimeException boom = new RuntimeException("boom");
        checkThrows(RuntimeException.class, () -> m.compute("e", (k, v) -> { throw boom; }),
                "compute exception propagates");
        check(m.get("e") == 1, "compute exception leaves entry intact");
        checkThrows(RuntimeException.class, () -> m.merge("e", 1, (o, x) -> { throw boom; }),
                "merge exception propagates");
        check(m.get("e") == 1, "merge exception leaves entry intact");
        // exception identity is preserved (same throwable object relayed).
        try {
            m.computeIfAbsent("x9", k -> { throw boom; });
            check(false, "computeIfAbsent exception relays");
        } catch (RuntimeException rel) {
            check(rel == boom, "computeIfAbsent relays the same throwable object");
        }
    }

    // ---- view semantics -------------------------------------------------------

    static void testViewSemantics() {
        Map<String, Integer> m = new HashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        Set<String> keys = m.keySet();
        Collection<Integer> vals = m.values();
        Set<Map.Entry<String, Integer>> entries = m.entrySet();
        check(keys.size() == 2 && vals.size() == 2 && entries.size() == 2, "view sizes track map");
        m.put("c", 3);
        check(keys.size() == 3 && vals.size() == 3 && entries.size() == 3, "views grow with map");
        check(keys.contains("c") && vals.contains(3), "views see new entry");
        keys.remove("a");
        check(!m.containsKey("a") && m.size() == 2, "keySet.remove removes entry");
        vals.remove(2);
        check(!m.containsKey("b") && m.size() == 1, "values.remove removes entry");
        entries.clear();
        check(m.isEmpty(), "entrySet.clear empties map");
        checkThrows(UnsupportedOperationException.class, () -> keys.add("z"), "keySet.add UOE");
        checkThrows(UnsupportedOperationException.class, () -> vals.add(9), "values.add UOE");
        checkThrows(UnsupportedOperationException.class, () -> entries.add(new AbstractMap.SimpleEntry<>("z", 9)),
                "entrySet.add UOE");

        // view equals: keySet is a Set, entrySet equality is entry-by-entry.
        Map<String, Integer> a = new HashMap<>();
        a.put("a", 1);
        a.put("b", 2);
        Map<String, Integer> b = new TreeMap<>();
        b.put("b", 2);
        b.put("a", 1);
        check(a.keySet().equals(b.keySet()), "keySet equals across implementations");
        check(a.entrySet().equals(b.entrySet()), "entrySet equals across implementations");
        check(a.values().containsAll(b.values()) && b.values().containsAll(a.values()),
                "values containments equal");
        check(a.toString().startsWith("{") && a.toString().endsWith("}"), "Map toString braces");
    }

    // ---- iteration / state invariants ---------------------------------------

    static void testIterationAndInvariants() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("b", 2);
        m.put("a", 1);
        m.put("c", 3);
        List<String> order = new ArrayList<>();
        for (String k : m.keySet()) order.add(k);
        check(order.equals(Arrays.asList("b", "a", "c")), "LinkedHashMap preserves insertion order");
        check(m.toString().equals("{b=2, a=1, c=3}"), "toString insertion ordered: " + m);

        m.remove("a");
        check(m.size() == 2 && m.containsKey("b") && m.containsKey("c"), "state after remove");
        m.clear();
        check(m.isEmpty() && m.size() == 0 && !m.containsKey("b"), "clear empties");
        check(m.keySet().isEmpty() && m.values().isEmpty() && m.entrySet().isEmpty(), "views empty after clear");
        check(m.get("b") == null, "get on empty null");
        check(m.remove("b") == null, "remove on empty null");
        check(!m.containsValue(1), "containsValue empty false");
    }

    public static void main(String[] args) {
        testBasics();
        testViews();
        testDefaults();
        testDefaultMethodContract();
        testDefaultReplaceAllForEach();
        testEqualsHashCodeFormula();
        testEntryContract();
        testDefaultMethodImplementationPaths();
        testPresentNullValueBranches();
        testViewSemantics();
        testIterationAndInvariants();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
