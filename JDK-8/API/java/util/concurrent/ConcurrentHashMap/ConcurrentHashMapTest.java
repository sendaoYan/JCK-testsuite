/*
 * @test
 * @summary java.util.concurrent.ConcurrentHashMap conformance: white-box (reflection
 *          structure) + black-box (put/get/remove, putIfAbsent/compute/computeIfAbsent/
 *          merge atomic semantics, null rejection, weakly-consistent iterators, bulk
 *          forEach/reduce/search, keySet view, mappingCount).
 * @run main ConcurrentHashMapTest
 */

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConcurrentHashMapTest {
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

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; }
        catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentHashMap");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == AbstractMap.class, "extends AbstractMap");
        check(ConcurrentMap.class.isAssignableFrom(c), "implements ConcurrentMap");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null && c.getConstructor(int.class) != null
              && c.getConstructor(Map.class) != null, "constructors ()/(int)/(Map)");
        check(hasMethod(c, "put", Object.class, Object.class), "put");
        check(hasMethod(c, "get", Object.class), "get");
        check(hasMethod(c, "remove", Object.class), "remove(Object)");
        check(hasMethod(c, "remove", Object.class, Object.class), "remove(Object,Object)");
        check(hasMethod(c, "putIfAbsent", Object.class, Object.class), "putIfAbsent");
        check(hasMethod(c, "computeIfAbsent", Object.class, Function.class), "computeIfAbsent");
        check(hasMethod(c, "compute", Object.class, BiFunction.class), "compute");
        check(hasMethod(c, "merge", Object.class, Object.class, BiFunction.class), "merge");
        check(hasMethod(c, "containsKey", Object.class) && hasMethod(c, "containsValue", Object.class),
              "containsKey/containsValue");
        check(hasMethod(c, "size") && hasMethod(c, "mappingCount"), "size/mappingCount");
        check(hasMethod(c, "keySet") && hasMethod(c, "values") && hasMethod(c, "entrySet"),
              "keySet/values/entrySet");
        check(hasMethod(c, "forEach", java.util.function.BiConsumer.class), "forEach");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testPutGetRemove() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        check(m.isEmpty() && m.size() == 0, "empty initially");
        check(m.put("a", 1) == null, "put new returns null");
        check(m.put("a", 2) == 1, "put existing returns old");
        check(m.put("b", 3) == null && m.put("c", 4) == null, "put more");
        check(m.get("a") == 2 && m.get("missing") == null, "get");
        check(m.containsKey("a") && !m.containsKey("z"), "containsKey");
        check(m.containsValue(3) && !m.containsValue(99), "containsValue");
        check(m.size() == 3, "size");
        check(m.mappingCount() == 3, "mappingCount equals size");
        check(m.remove("a") == 2, "remove returns value");
        check(!m.containsKey("a") && m.size() == 2, "removed");
        check(m.remove("b", 999) == false, "remove(K,V) non-matching false");
        check(m.remove("b", 3), "remove(K,V) matching true");
        check(!m.containsKey("b"), "b removed");
        m.clear();
        check(m.isEmpty() && m.size() == 0, "clear empties");
    }

    static void testNullRejection() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        expectThrows(() -> m.put(null, 1), NullPointerException.class, "put(null key)");
        expectThrows(() -> m.put("k", null), NullPointerException.class, "put(null value)");
        expectThrows(() -> m.get(null), NullPointerException.class, "get(null)");
        expectThrows(() -> m.containsKey(null), NullPointerException.class, "containsKey(null)");
        expectThrows(() -> m.remove(null), NullPointerException.class, "remove(null)");
        expectThrows(() -> m.putIfAbsent(null, 1), NullPointerException.class, "putIfAbsent(null)");
        expectThrows(() -> m.getOrDefault(null, 0), NullPointerException.class, "getOrDefault(null)");
        expectThrows(() -> m.computeIfAbsent(null, k -> 1), NullPointerException.class, "computeIfAbsent(null)");
        expectThrows(() -> m.compute(null, (k, v) -> 1), NullPointerException.class, "compute(null)");
        expectThrows(() -> m.merge(null, 1, Integer::sum), NullPointerException.class, "merge(null)");
        expectThrows(() -> m.replace(null, 1), NullPointerException.class, "replace(null)");
    }

    static void testAtomicOperations() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        check(m.putIfAbsent("a", 2) == 1, "putIfAbsent present returns existing");
        check(m.putIfAbsent("b", 3) == null && m.get("b") == 3, "putIfAbsent absent inserts");

        check(m.computeIfAbsent("c", String::length) == 1, "computeIfAbsent computes");
        check(m.computeIfAbsent("c", k -> 99) == 1, "computeIfAbsent present no-op");
        check(m.computeIfAbsent("d", k -> null) == null && !m.containsKey("d"),
              "computeIfAbsent null result does not insert");

        check(m.compute("a", (k, v) -> v + 10) == 11, "compute computes");
        m.put("gone", 1);
        check(m.compute("gone", (k, v) -> null) == null && !m.containsKey("gone"),
              "compute null result removes");

        check(m.merge("a", 100, Integer::sum) == 111, "merge combines");
        check(m.merge("e", 5, Integer::sum) == 5, "merge absent inserts");
        m.put("rm", 1);
        check(m.merge("rm", 2, (a, b) -> null) == null && !m.containsKey("rm"),
              "merge null result removes");

        check(m.replace("a", 111) == 111, "replace(K,V) returns old");
        check(m.replace("a", 111, 222), "replace(K,old,new) matching");
        check(!m.replace("a", 999, 1), "replace(K,old,new) non-matching false");
        check(m.get("a") == 222, "replace updated");
        check(m.getOrDefault("a", 0) == 222, "getOrDefault present");
        check(m.getOrDefault("z", 0) == 0, "getOrDefault missing");
    }

    static void testViewsAndNewKeySet() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        check(m.keySet().size() == 3 && m.keySet().contains("a"), "keySet");
        check(m.values().size() == 3 && m.values().contains(2), "values");
        check(m.entrySet().size() == 3, "entrySet");

        Set<String> ks = ConcurrentHashMap.<String>newKeySet();
        check(ks.add("x") && ks.add("y") && !ks.add("x"), "newKeySet set semantics");
        check(ks.contains("x") && ks.size() == 2, "newKeySet size/contains");
        ks.remove("x");
        check(!ks.contains("x"), "newKeySet remove");
    }

    static void testWeaklyConsistentIterator() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        int seen = 0;
        for (Map.Entry<String, Integer> e : m.entrySet()) {
            seen++;
            m.put("d", 4); // concurrent modification during iteration does not throw
            m.remove("a");
        }
        check(seen >= 1, "iterator does not throw ConcurrentModificationException");
        check(m.containsKey("d") && !m.containsKey("a"), "modifications applied");
    }

    static void testBulkOperations() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        final int[] sum = {0};
        m.forEach((k, v) -> sum[0] += v);
        check(sum[0] == 6, "forEach sums values");

        int reduce = m.reduceValues(1, Integer::sum);
        check(reduce == 6, "reduceValues sums");
        int search = m.searchValues(1, v -> v == 2 ? v : null);
        check(search == 2, "searchValues finds 2");
        int reduceKeys = m.reduceKeysToInt(1, String::length, 0, Integer::sum);
        check(reduceKeys == 3, "reduceKeysToInt sums key lengths");
    }

    static void testBlackBox() {
        // Constructors (int), (Map), (int,float), (int,float,int)
        ConcurrentHashMap<String, Integer> c1 = new ConcurrentHashMap<>(16);
        check(c1.isEmpty(), "constructor(int) empty");
        java.util.Map<String, Integer> seed = new java.util.HashMap<>();
        seed.put("a", 1);
        seed.put("b", 2);
        ConcurrentHashMap<String, Integer> c2 = new ConcurrentHashMap<>(seed);
        check(c2.size() == 2 && c2.get("a") == 1, "constructor(Map) copies");
        check(new ConcurrentHashMap<String, Integer>(32, 0.75f).isEmpty(), "constructor(int,float) empty");
        check(new ConcurrentHashMap<String, Integer>(32, 0.75f, 16).isEmpty(), "constructor(int,float,int) empty");

        // putAll
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.putAll(seed);
        check(m.size() == 2 && m.get("b") == 2, "putAll");

        // computeIfPresent
        m.computeIfPresent("a", (k, v) -> v + 10);
        check(m.get("a") == 11, "computeIfPresent present computes");
        check(m.computeIfPresent("zz", (k, v) -> 99) == null && !m.containsKey("zz"),
              "computeIfPresent absent no-op");

        // replaceAll
        m.replaceAll((k, v) -> v * 2);
        check(m.get("a") == 22 && m.get("b") == 4, "replaceAll maps values");

        // elements()/keys() enumerations
        java.util.Enumeration<Integer> en = m.elements();
        int sum = 0;
        while (en.hasMoreElements()) sum += en.nextElement();
        check(sum == 26, "elements() enumerates values");
        java.util.Enumeration<String> keys = m.keys();
        int keyCount = 0;
        while (keys.hasMoreElements()) { keys.nextElement(); keyCount++; }
        check(keyCount == 2, "keys() enumerates keys");

        // equals/hashCode (Map contract vs a plain HashMap)
        java.util.HashMap<String, Integer> hm = new java.util.HashMap<>(m);
        check(m.equals(hm) && hm.equals(m), "equals plain map");
        check(m.hashCode() == hm.hashCode(), "hashCode matches Map contract");

        // newKeySet(int)
        java.util.Set<String> ks = ConcurrentHashMap.<String>newKeySet(8);
        check(ks.add("x") && ks.add("y") && ks.size() == 2, "newKeySet(int)");

        // search / searchKeys / searchEntries
        ConcurrentHashMap<String, Integer> s = new ConcurrentHashMap<>();
        s.put("aa", 1);
        s.put("bbb", 2);
        s.put("c", 3);
        check(s.search(1, (k, v) -> v == 2 ? v : null) == 2, "search finds value 2");
        check("bbb".equals(s.searchKeys(1, k -> k.length() == 3 ? k : null)),
              "searchKeys finds key of length 3");
        java.util.Map.Entry<String, Integer> ef = s.searchEntries(1, e -> e.getValue() == 3 ? e : null);
        check(ef != null && "c".equals(ef.getKey()), "searchEntries finds entry");

        // reduce (full), reduceKeys, reduceEntries
        check(s.reduce(1, (k, v) -> k.length(), Integer::sum) == 6, "reduce sums key lengths");
        check(s.reduceKeys(1, (k1, k2) -> k1 + k2).length() == 6, "reduceKeys concatenates keys");
        check(s.reduceKeys(1, String::length, Integer::sum) == 6, "reduceKeys(transformer,reducer)");
        check(s.reduceEntries(1, e -> e.getValue(), Integer::sum) == 6, "reduceEntries(transformer,reducer)");
        java.util.Map.Entry<String, Integer> eSum = s.reduceEntries(1,
                (e1, e2) -> new java.util.AbstractMap.SimpleImmutableEntry<>("", e1.getValue() + e2.getValue()));
        check(eSum.getValue() == 6, "reduceEntries(reducer) sums values");

        // reduceToInt / reduceToLong / reduceToDouble
        check(s.reduceToInt(1, (k, v) -> v, 0, Integer::sum) == 6, "reduceToInt sums values");
        check(s.reduceToLong(1, (k, v) -> (long) v, 0L, Long::sum) == 6L, "reduceToLong sums values");
        check(s.reduceToDouble(1, (k, v) -> (double) v, 0.0, Double::sum) == 6.0, "reduceToDouble sums values");

        // reduceKeysTo* / reduceValuesTo* / reduceEntriesTo*
        check(s.reduceKeysToLong(1, k -> (long) k.length(), 0L, Long::sum) == 6L, "reduceKeysToLong");
        check(s.reduceKeysToDouble(1, k -> (double) k.length(), 0.0, Double::sum) == 6.0, "reduceKeysToDouble");
        check(s.reduceValuesToInt(1, v -> v, 0, Integer::sum) == 6, "reduceValuesToInt");
        check(s.reduceValuesToLong(1, v -> (long) v, 0L, Long::sum) == 6L, "reduceValuesToLong");
        check(s.reduceValuesToDouble(1, v -> (double) v, 0.0, Double::sum) == 6.0, "reduceValuesToDouble");
        check(s.reduceEntriesToInt(1, e -> e.getValue(), 0, Integer::sum) == 6, "reduceEntriesToInt");
        check(s.reduceEntriesToLong(1, e -> (long) e.getValue(), 0L, Long::sum) == 6L, "reduceEntriesToLong");
        check(s.reduceEntriesToDouble(1, e -> (double) e.getValue(), 0.0, Double::sum) == 6.0, "reduceEntriesToDouble");

        // forEachKey/Value/Entry (long, Consumer) and (long, Function, Consumer)
        final java.util.ArrayList<String> seenKeys = new java.util.ArrayList<>();
        s.forEachKey(1, seenKeys::add);
        seenKeys.sort(String::compareTo);
        check(seenKeys.toString().equals("[aa, bbb, c]"), "forEachKey(long,Consumer)");
        final int[] keyLenSum = {0};
        s.forEachKey(1, String::length, len -> keyLenSum[0] += len);
        check(keyLenSum[0] == 6, "forEachKey(long,Function,Consumer)");
        final int[] valSum = {0};
        s.forEachValue(1, v -> valSum[0] += v);
        check(valSum[0] == 6, "forEachValue(long,Consumer)");
        final int[] valDoubled = {0};
        s.forEachValue(1, v -> v * 2, d -> valDoubled[0] += d);
        check(valDoubled[0] == 12, "forEachValue(long,Function,Consumer)");
        final java.util.ArrayList<String> seenEntries = new java.util.ArrayList<>();
        s.forEachEntry(1, e -> seenEntries.add(e.getKey() + "=" + e.getValue()));
        check(seenEntries.size() == 3, "forEachEntry(long,Consumer)");
        final int[] entryValSum = {0};
        s.forEachEntry(1, e -> e.getValue(), v -> entryValSum[0] += v);
        check(entryValSum[0] == 6, "forEachEntry(long,Function,Consumer)");

        // toString
        check(s.toString() != null && s.toString().contains("aa"), "toString non-null with content");
    }

    // ---- Part 3: contract deepening (appended) ----
    static void testConstructorMatrix() {
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(-1),
                     IllegalArgumentException.class, "ctor(-1)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(-1, 0.75f),
                     IllegalArgumentException.class, "ctor(-1,0.75f)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(16, 0f),
                     IllegalArgumentException.class, "ctor(16,0f)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(16, -1f),
                     IllegalArgumentException.class, "ctor(16,-1f)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(16, Float.NaN),
                     IllegalArgumentException.class, "ctor(16,NaN)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(16, 0.75f, 0),
                     IllegalArgumentException.class, "ctor(16,0.75f,0)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(16, 0.75f, -3),
                     IllegalArgumentException.class, "ctor(16,0.75f,-3)");
        expectThrows(() -> new ConcurrentHashMap<String, Integer>(null),
                     NullPointerException.class, "ctor(null Map)");
        check(new ConcurrentHashMap<String, Integer>(0).isEmpty(), "ctor(0) legal");
        check(new ConcurrentHashMap<String, Integer>(Integer.MAX_VALUE).isEmpty(),
              "ctor(MAX_VALUE) capped legal");
    }

    static void testNullRejectionFull() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        expectThrows(() -> m.containsValue(null), NullPointerException.class, "containsValue(null)");
        expectThrows(() -> m.contains(null), NullPointerException.class, "contains(null) legacy == containsValue");
        expectThrows(() -> m.remove(null, 1), NullPointerException.class, "remove(null,v)");
        expectThrows(() -> m.replace("a", null), NullPointerException.class, "replace(k,null)");
        expectThrows(() -> m.replace("a", null, 2), NullPointerException.class, "replace(k,null,v)");
        expectThrows(() -> m.replace(null, 1, 2), NullPointerException.class, "replace(null,v1,v2)");
        expectThrows(() -> m.getOrDefault(null, 0), NullPointerException.class, "getOrDefault(null,d)");
        expectThrows(() -> m.computeIfPresent(null, (k, v) -> 1), NullPointerException.class, "computeIfPresent(null)");
        expectThrows(() -> m.computeIfAbsent("k", null), NullPointerException.class, "computeIfAbsent(k,null fn)");
        expectThrows(() -> m.compute("k", null), NullPointerException.class, "compute(k,null fn)");
        expectThrows(() -> m.merge("k", null, Integer::sum), NullPointerException.class, "merge(k,null v)");
        expectThrows(() -> m.merge("k", 1, null), NullPointerException.class, "merge(k,v,null fn)");
        expectThrows(() -> m.putAll(null), NullPointerException.class, "putAll(null)");
        java.util.HashMap<String, Integer> nullKey = new java.util.HashMap<>();
        nullKey.put(null, 1);
        expectThrows(() -> m.putAll(nullKey), NullPointerException.class, "putAll(map with null key)");
        java.util.HashMap<String, Integer> nullVal = new java.util.HashMap<>();
        nullVal.put("zz", null);
        expectThrows(() -> m.putAll(nullVal), NullPointerException.class, "putAll(map with null value)");
        expectThrows(() -> m.keySet().contains(null), NullPointerException.class, "keySet().contains(null)");
        expectThrows(() -> m.values().contains(null), NullPointerException.class, "values().contains(null)");
        // remove(k, null) does NOT throw: null can never be a stored value, so no match
        check(!m.remove("a", null), "remove(k,null) returns false without throwing");
        check(m.containsKey("a") && m.get("a") == 1, "remove(k,null) leaves mapping intact");
    }

    static void testComputeMergeBranchMatrix() {
        // computeIfAbsent: absent -> function invoked exactly once, result stored
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        final int[] calls = {0};
        check(m.computeIfAbsent("a", k -> { calls[0]++; return 7; }) == 7, "cIA absent computes");
        check(calls[0] == 1, "cIA fn called exactly once on absent");
        check(m.get("a") == 7 && m.containsKey("a"), "cIA absent stores mapping");
        check(m.computeIfAbsent("a", k -> { calls[0]++; return 99; }) == 7, "cIA present returns current value");
        check(calls[0] == 1, "cIA fn NOT invoked when present (counter proof)");
        check(m.computeIfAbsent("b", k -> { calls[0]++; return null; }) == null, "cIA null result returns null");
        check(!m.containsKey("b"), "cIA null result leaves no entry");
        check(calls[0] == 2, "cIA null-result fn still invoked once");
        RuntimeException boom = new RuntimeException("boom-cIA");
        try {
            m.computeIfAbsent("boom", k -> { throw boom; }); // absent key: fn invoked
            throw new AssertionError("computeIfAbsent exception must propagate");
        } catch (RuntimeException t) {
            check(t == boom, "cIA propagates the original exception object");
        }
        check(m.get("a") == 7, "cIA exception leaves mapping unchanged");

        // computeIfPresent: absent -> fn not invoked, returns null, no entry
        calls[0] = 0;
        check(m.computeIfPresent("zz", (k, v) -> { calls[0]++; return 1; }) == null, "cIP absent returns null");
        check(calls[0] == 0, "cIP fn NOT invoked when absent (counter proof)");
        check(!m.containsKey("zz"), "cIP absent creates nothing");
        check(m.computeIfPresent("a", (k, v) -> { calls[0]++; return v == 7 ? v + 1 : -1; }) == 8,
              "cIP present recomputes with old value");
        check(calls[0] == 1 && m.get("a") == 8, "cIP result stored");
        check(m.computeIfPresent("a", (k, v) -> null) == null, "cIP null result removes mapping");
        check(!m.containsKey("a") && m.size() == 0, "cIP null removed the entry");

        // compute: absent branch sees null old value
        check(m.compute("n", (k, v) -> v == null ? 5 : -1) == 5, "compute absent inserts");
        check(m.get("n") == 5, "compute absent stored");
        check(m.compute("n", (k, v) -> v * 2) == 10, "compute present updates");
        check(m.compute("n", (k, v) -> null) == null, "compute null removes");
        check(!m.containsKey("n"), "compute null removed entry");
        try {
            m.compute("x", (k, v) -> { throw boom; });
            throw new AssertionError("compute exception must propagate");
        } catch (RuntimeException t) {
            check(t == boom, "compute propagates the original exception object");
        }
        check(!m.containsKey("x"), "compute exception leaves no entry");

        // merge: absent -> fn not invoked, value used directly
        calls[0] = 0;
        check(m.merge("m1", 3, (x, y) -> { calls[0]++; return x + y; }) == 3, "merge absent uses value directly");
        check(calls[0] == 0, "merge absent does NOT invoke fn (counter proof)");
        check(m.merge("m1", 4, (x, y) -> { calls[0]++; return x * y; }) == 12, "merge present applies fn(old,new)");
        check(calls[0] == 1 && m.get("m1") == 12, "merge result stored");
        check(m.merge("m1", 5, (x, y) -> null) == null, "merge null result removes");
        check(!m.containsKey("m1"), "merge null removed entry");
        m.merge("m2", 1, Integer::sum);
        try {
            m.merge("m2", 1, (x, y) -> { throw boom; });
            throw new AssertionError("merge exception must propagate");
        } catch (RuntimeException t) {
            check(t == boom, "merge propagates the original exception object");
        }
        check(m.get("m2") == 1, "merge exception leaves mapping unchanged");
    }

    static void testViewsWriteThrough() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        check(m.keySet().remove("a") && !m.containsKey("a") && m.size() == 2, "keySet remove writes through");
        check(m.values().remove(2) && !m.containsKey("b") && m.size() == 1, "values remove writes through");
        Map.Entry<String, Integer> e = m.entrySet().iterator().next();
        check(m.entrySet().remove(e) && m.isEmpty(), "entrySet remove(entry) writes through");
        expectThrows(() -> m.keySet().add("z"), UnsupportedOperationException.class, "keySet().add");
        expectThrows(() -> m.values().add(9), UnsupportedOperationException.class, "values().add");
        // CHM extension: entrySet().add(e) has put() semantics (returns true only for a new key)
        m.put("a", 1);
        check(m.entrySet().add(new java.util.AbstractMap.SimpleEntry<>("z", 9)) && m.get("z") == 9,
              "entrySet().add inserts a new mapping");
        check(!m.entrySet().add(new java.util.AbstractMap.SimpleEntry<>("a", 100)) && m.get("a") == 100,
              "entrySet().add on existing key returns false but overwrites");
        // entries handed out by the entrySet iterator support setValue and write through
        m.put("w", 1);
        for (Map.Entry<String, Integer> we : m.entrySet()) {
            if (we.getKey().equals("w")) we.setValue(42);
        }
        check(m.get("w") == 42, "entrySet iterator entry setValue writes through");
    }

    static void testKeySetViewMapped() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        ConcurrentHashMap.KeySetView<String, Integer> view = m.keySet(42);
        check(view.getMappedValue() == 42, "keySet(v).getMappedValue() == v");
        check(view.add("k"), "keySet(42).add returns true");
        check(m.get("k") == 42, "keySet(42).add == put(k, default)");
        m.put("e", 7);
        check(!view.add("e"), "keySet(42).add existing key returns false");
        check(m.get("e") == 7, "keySet(42).add existing does not overwrite");
        check(view.contains("k") && view.contains("e") && !view.contains("zz"), "view contains delegates");
        m.put("z", 1);
        check(view.size() == m.size(), "view size tracks backing map");
        check(view.remove("z") && !m.containsKey("z"), "view remove writes through");
        check(ConcurrentHashMap.newKeySet().getMappedValue() == Boolean.TRUE,
              "newKeySet() maps keys to Boolean.TRUE");
        java.util.Iterator<String> it = view.iterator();
        m.put("extra", 9); // structural change after iterator obtained
        int n = 0;
        while (it.hasNext()) { it.next(); n++; }
        check(n >= 2, "view iterator traverses without CME after structural put");
    }

    static void testIteratorWeakConsistencyContract() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        m.put("c", 3);
        java.util.Iterator<String> it = m.keySet().iterator();
        m.put("d", 4); // structural insert after iterator obtained
        m.remove("a"); // structural remove after iterator obtained
        int count = 0;
        while (it.hasNext()) { check(it.next() != null, "iterated key non-null"); count++; }
        check(count >= 1 && count <= 4, "weakly consistent iterator completes without CME");
        check(m.containsKey("d") && !m.containsKey("a"), "post-modification state consistent");
        check(m.size() == 3, "final size exact");
        // Contrast (in-test): java.util.ArrayList for the same scenario IS fail-fast
        java.util.ArrayList<String> al = new java.util.ArrayList<>(java.util.Arrays.asList("a", "b", "c"));
        java.util.Iterator<String> alIt = al.iterator();
        al.add("d");
        boolean cme = false;
        try { alIt.next(); } catch (java.util.ConcurrentModificationException expected) { cme = true; }
        check(cme, "contrast: ArrayList iterator throws CME after structural add");
    }

    static void testSerializationRoundTrip() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ConcurrentHashMap.class).getSerialVersionUID()
              == 7249069246763182397L, "serialVersionUID locked");
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        m.put("b", 2);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) {
            oos.writeObject(m);
        }
        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, Integer> copy = (ConcurrentHashMap<String, Integer>)
            new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy.equals(m) && copy.size() == 2, "serialization round-trip preserves content");
        check(copy.get("a") == 1 && copy.containsKey("b"), "deserialized values readable");
        check(copy.put("c", 3) == null && copy.size() == 3, "resurrected map accepts puts");
    }

    static void testSizeMappingCountResize() {
        ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(4); // tiny table forces resize
        boolean allPutsNull = true;
        for (int i = 0; i < 100; i++) allPutsNull &= (m.put(i, i * 2) == null);
        check(allPutsNull, "100 puts across resize all returned null");
        check(m.size() == 100, "size after 100 puts");
        check(m.mappingCount() == 100, "mappingCount == size");
        check(!m.isEmpty(), "not empty");
        boolean allGetsOk = true;
        for (int i = 0; i < 100; i++) allGetsOk &= (m.get(i) == i * 2);
        check(allGetsOk, "all values readable after resize");
        boolean allRemovesOk = true;
        for (int i = 0; i < 50; i++) allRemovesOk &= (m.remove(i) == i * 2);
        check(allRemovesOk && m.size() == 50, "remove half, size tracks");
        check(m.mappingCount() == 50, "mappingCount after removals");
        // cross dimension: compute branches after resize
        check(m.computeIfAbsent(500, k -> 1) == 1, "computeIfAbsent after resize");
        check(m.compute(500, (k, v) -> v + 1) == 2, "compute after resize");
        check(m.merge(500, 10, Integer::sum) == 12, "merge after resize");
        check(m.size() == 51, "size after cross ops");
    }

    static void testEqualsToStringContract() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        java.util.HashMap<String, Integer> hm = new java.util.HashMap<>();
        String[] keys = {"x", "y", "z"};
        for (String k : keys) { m.put(k, k.length()); hm.put(k, k.length()); }
        check(m.equals(hm) && hm.equals(m), "equals symmetric vs HashMap");
        check(m.hashCode() == hm.hashCode(), "hashCode equal vs HashMap");
        check(m.entrySet().equals(hm.entrySet()), "entrySet content equal");
        String ts = m.toString();
        check(ts.startsWith("{") && ts.endsWith("}"), "toString uses Map {..} format");
        check(ts.contains("x=1") && ts.contains("y=1") && ts.contains("z=1"), "toString content complete");
        hm.put("extra", 9);
        check(!m.equals(hm) && !hm.equals(m), "different size not equal");
        check(!m.equals(null), "equals(null) false");
    }

    static void testConcurrentPutConsistency() throws Exception {
        // bounded, daemon-threaded proof that concurrent puts stay consistent
        final ConcurrentHashMap<Integer, Integer> m = new ConcurrentHashMap<>(16);
        final int perThread = 500;
        final java.util.concurrent.CountDownLatch done = new java.util.concurrent.CountDownLatch(2);
        Runnable r = () -> {
            int base = Integer.parseInt(Thread.currentThread().getName());
            for (int i = 0; i < perThread; i++) m.put(base * 10000 + i, i);
            done.countDown();
        };
        Thread t0 = new Thread(r, "0");
        Thread t1 = new Thread(r, "1");
        t0.setDaemon(true);
        t1.setDaemon(true);
        t0.start();
        t1.start();
        check(done.await(5, java.util.concurrent.TimeUnit.SECONDS), "both workers finished within 5s");
        check(m.size() == 2 * perThread, "all concurrent puts visible");
        boolean all = true;
        for (int i = 0; i < perThread; i++)
            all &= (m.get(i) == i && m.get(10000 + i) == i);
        check(all, "every concurrent mapping readable");
        check(m.mappingCount() == 2 * perThread, "mappingCount matches after concurrent fill");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPutGetRemove();
        testNullRejection();
        testAtomicOperations();
        testViewsAndNewKeySet();
        testWeaklyConsistentIterator();
        testBulkOperations();
        testBlackBox();
        testConstructorMatrix();
        testNullRejectionFull();
        testComputeMergeBranchMatrix();
        testViewsWriteThrough();
        testKeySetViewMapped();
        testIteratorWeakConsistencyContract();
        testSerializationRoundTrip();
        testSizeMappingCountResize();
        testEqualsToStringContract();
        testConcurrentPutConsistency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
