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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPutGetRemove();
        testNullRejection();
        testAtomicOperations();
        testViewsAndNewKeySet();
        testWeaklyConsistentIterator();
        testBulkOperations();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
