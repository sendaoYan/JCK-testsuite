/*
 * @test
 * @summary java.util.concurrent.ConcurrentMap API conformance: white-box structure
 *          (interface, default-method faces) + black-box behavior. Contract level:
 *          atomic putIfAbsent/remove(k,v)/replace branch matrix (absent / present /
 *          stale old-value, equals-based matching) driven via ConcurrentHashMap;
 *          inherited ConcurrentMap default methods (getOrDefault/forEach/replaceAll/
 *          computeIfAbsent/computeIfPresent/compute/merge) observed through a minimal
 *          delegating implementation that overrides nothing; function-invocation
 *          counters; exception relay; interface hierarchy and generic bounds.
 * @run main ConcurrentMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ConcurrentMapTest {
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

    // ---- original smoke test (kept) ----
    static void testConcurrentMap() {
        ConcurrentMap<String, Integer> m = new ConcurrentHashMap<String, Integer>();
        check(m.putIfAbsent("k", 1) == null, "putIfAbsent inserts when absent");
        check(m.putIfAbsent("k", 2) == 1, "putIfAbsent returns existing");
        check(m.get("k") == 1, "value unchanged");
        check(m.replace("k", 1, 3), "replace(k,old,new)");
        check(m.remove("k", 3), "remove(k,value)");
        check(m.isEmpty(), "empty after remove");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.concurrent.ConcurrentMap");
        check(c.isInterface(), "ConcurrentMap is an interface");
        check(!c.isAnnotation() && !c.isEnum(), "plain interface, not annotation/enum");
        check(Modifier.isInterface(c.getModifiers()), "modifiers carry interface bit");
        check(Map.class.isAssignableFrom(c), "ConcurrentMap is assignable to java.util.Map");
        Class<?>[] supers = c.getInterfaces();
        check(supers.length == 1 && supers[0] == Map.class, "direct superinterface is exactly Map");

        // the four atomic operations are (re)declared abstract on ConcurrentMap
        String[][] abs = {
            {"putIfAbsent", "java.lang.Object", "java.lang.Object"},
            {"remove", "java.lang.Object", "java.lang.Object"},
            {"replace", "java.lang.Object", "java.lang.Object", "java.lang.Object"},
            {"replace", "java.lang.Object", "java.lang.Object"},
        };
        for (String[] m : abs) {
            Class<?>[] ps = new Class<?>[m.length - 1];
            for (int i = 1; i < m.length; i++) ps[i - 1] = Class.forName(m[i]);
            Method mm = c.getMethod(m[0], ps);
            check(Modifier.isAbstract(mm.getModifiers()),
                  m[0] + " is abstract on ConcurrentMap");
            check(mm.getDeclaringClass() == c, m[0] + " declared by ConcurrentMap itself");
        }

        // default methods re-declared by ConcurrentMap (over Map's own defaults)
        Method getOrDefault = c.getMethod("getOrDefault", Object.class, Object.class);
        check(getOrDefault.isDefault(), "getOrDefault is a default method");
        check(getOrDefault.getDeclaringClass() == c, "getOrDefault declared by ConcurrentMap");
        Method forEach = c.getMethod("forEach", java.util.function.BiConsumer.class);
        check(forEach.isDefault() && forEach.getDeclaringClass() == c, "forEach default on ConcurrentMap");
        Method replaceAll = c.getMethod("replaceAll", BiFunction.class);
        check(replaceAll.isDefault() && replaceAll.getDeclaringClass() == c, "replaceAll default on ConcurrentMap");
        check(c.getMethod("computeIfAbsent", Object.class, Function.class).isDefault(),
              "computeIfAbsent default on ConcurrentMap");
        check(c.getMethod("computeIfPresent", Object.class, BiFunction.class).isDefault(),
              "computeIfPresent default on ConcurrentMap");
        check(c.getMethod("compute", Object.class, BiFunction.class).isDefault(),
              "compute default on ConcurrentMap");
        check(c.getMethod("merge", Object.class, Object.class, BiFunction.class).isDefault(),
              "merge default on ConcurrentMap");

        // generics: K and V
        check(c.getTypeParameters().length == 2, "two type parameters K,V");
        check("K".equals(c.getTypeParameters()[0].getName())
              && "V".equals(c.getTypeParameters()[1].getName()), "type parameter names K,V");
    }

    // ---- Part 2: atomic operation branch matrix via ConcurrentHashMap ----
    static void testAtomicBranchMatrix() {
        ConcurrentMap<String, Integer> m = new ConcurrentHashMap<String, Integer>();
        // putIfAbsent: absent -> null return + mapping inserted
        check(m.putIfAbsent("a", 1) == null, "putIfAbsent absent returns null");
        check(m.containsKey("a") && m.get("a") == 1 && m.size() == 1, "putIfAbsent absent inserted mapping");
        // putIfAbsent: present -> returns existing value, no overwrite
        check(m.putIfAbsent("a", 2) == 1, "putIfAbsent present returns existing value");
        check(m.get("a") == 1 && m.size() == 1, "putIfAbsent present leaves value and size unchanged");
        // putIfAbsent after explicit removal re-inserts
        m.remove("a");
        check(m.isEmpty(), "map emptied for round two");
        check(m.putIfAbsent("a", 10) == null && m.get("a") == 10, "putIfAbsent re-inserts after removal");

        // remove(k,v): absent key -> false, map untouched
        check(!m.remove("zz", 1), "remove(k,v) absent key false");
        // remove(k,v): present key, mismatched value -> false, mapping kept
        check(!m.remove("a", 99), "remove(k,v) value mismatch false");
        check(m.get("a") == 10, "remove(k,v) mismatch keeps mapping");
        // remove(k,v): match -> true, mapping removed
        check(m.remove("a", 10), "remove(k,v) match true");
        check(!m.containsKey("a"), "remove(k,v) match removes mapping");
        // remove(k,v) uses equals, not identity (equal but distinct value object)
        ConcurrentMap<String, String> sm = new ConcurrentHashMap<String, String>();
        sm.put("s", new String("v1"));
        check(sm.remove("s", new String("v1")), "remove(k,v) matches by equals not identity");
        check(sm.isEmpty(), "equals-matched remove cleared mapping");

        // replace(k,v): absent key -> null and NO insertion
        check(m.replace("zz", 5) == null, "replace(k,v) absent returns null");
        check(!m.containsKey("zz"), "replace(k,v) absent does not insert");
        // replace(k,v): present -> returns old value, updates
        m.put("b", 2);
        check(m.replace("b", 20) == 2, "replace(k,v) present returns old value");
        check(m.get("b") == 20, "replace(k,v) present updates value");
        // replace(k,v) matches by equals for the return value
        sm.put("s", new String("v1"));
        check("v1".equals(sm.replace("s", "v2")), "replace(k,v) returns prior value by equals");
        sm.remove("s");

        // replace(k,old,new): absent key -> false
        check(!m.replace("zz", 1, 2), "replace(k,o,n) absent key false");
        check(!m.containsKey("zz"), "replace(k,o,n) absent does not insert");
        // replace(k,old,new): stale old value -> false, value kept
        check(!m.replace("b", 2, 21), "replace(k,o,n) stale old false");
        check(m.get("b") == 20, "replace(k,o,n) stale keeps current value");
        // replace(k,old,new): match -> true, updates
        check(m.replace("b", 20, 22), "replace(k,o,n) match true");
        check(m.get("b") == 22, "replace(k,o,n) match updates value");
        // stale-value race shape: old value that was just overwritten must fail
        check(!m.replace("b", 20, 23), "replace(k,o,n) old no longer current fails");
        check(m.get("b") == 22, "value survives failed conditional replace");
        // equals-based old-value matching (equal but distinct object)
        sm.put("s", new String("v1"));
        check(sm.replace("s", new String("v1"), "v3"), "replace(k,o,n) matches old by equals");
        check("v3".equals(sm.get("s")), "equals-matched conditional replace updated");
    }

    // ---- Part 3: minimal implementation inheriting ConcurrentMap default methods ----
    // Delegates everything to ConcurrentHashMap and overrides NO ConcurrentMap method,
    // so computeIfAbsent/computeIfPresent/compute/merge/forEach/replaceAll/getOrDefault
    // are exactly the default implementations inherited from ConcurrentMap.
    static class MinimalConcurrentMap<K, V> implements ConcurrentMap<K, V> {
        final ConcurrentMap<K, V> back = new ConcurrentHashMap<K, V>();
        public int size() { return back.size(); }
        public boolean isEmpty() { return back.isEmpty(); }
        public boolean containsKey(Object key) { return back.containsKey(key); }
        public boolean containsValue(Object value) { return back.containsValue(value); }
        public V get(Object key) { return back.get(key); }
        public V put(K key, V value) { return back.put(key, value); }
        public V remove(Object key) { return back.remove(key); }
        public void putAll(Map<? extends K, ? extends V> m) { back.putAll(m); }
        public void clear() { back.clear(); }
        public Set<K> keySet() { return back.keySet(); }
        public Collection<V> values() { return back.values(); }
        public Set<Map.Entry<K, V>> entrySet() { return back.entrySet(); }
        public V putIfAbsent(K key, V value) { return back.putIfAbsent(key, value); }
        public boolean remove(Object key, Object value) { return back.remove(key, value); }
        public boolean replace(K key, V oldValue, V newValue) { return back.replace(key, oldValue, newValue); }
        public V replace(K key, V value) { return back.replace(key, value); }
    }

    static final class Boom extends RuntimeException { }

    static void testMinimalDefaults() {
        MinimalConcurrentMap<String, Integer> m = new MinimalConcurrentMap<String, Integer>();

        // getOrDefault: absent -> default returned, no insertion
        check(m.getOrDefault("x", 42) == 42, "default getOrDefault absent returns default");
        check(!m.containsKey("x"), "default getOrDefault absent does not insert");
        m.put("x", 1);
        check(m.getOrDefault("x", 42) == 1, "default getOrDefault present returns mapped value");

        // forEach: visits every live entry
        m.put("y", 2);
        final int[] sum = {0};
        final int[] visits = {0};
        m.forEach(new java.util.function.BiConsumer<String, Integer>() {
            public void accept(String k, Integer v) { sum[0] += v; visits[0]++; }
        });
        check(sum[0] == 3, "default forEach summed all values");
        check(visits[0] == 2, "default forEach visited every entry");
        // forEach on empty map simply does not run
        new MinimalConcurrentMap<String, Integer>().forEach((k, v) -> { throw new Boom(); });
        check(true, "default forEach on empty map invokes nothing");

        // computeIfAbsent: absent -> function applied once, mapping stored
        final int[] calls = {0};
        Integer r = m.computeIfAbsent("len", k -> { calls[0]++; return k.length(); });
        check(r == 3 && calls[0] == 1, "default computeIfAbsent absent applies function once");
        check(m.get("len") == 3, "default computeIfAbsent stores result");
        // computeIfAbsent: present -> function NOT invoked, existing value returned
        r = m.computeIfAbsent("len", k -> { calls[0]++; return 99; });
        check(r == 3, "default computeIfAbsent present returns existing value");
        check(calls[0] == 1, "default computeIfAbsent present does not invoke function");
        // computeIfAbsent: function returns null -> no mapping, returns null
        r = m.computeIfAbsent("nul", k -> null);
        check(r == null, "default computeIfAbsent null result returns null");
        check(!m.containsKey("nul"), "default computeIfAbsent null result inserts nothing");

        // computeIfPresent: absent -> function NOT invoked, returns null
        final int[] pCalls = {0};
        r = m.computeIfPresent("absent", (k, v) -> { pCalls[0]++; return 7; });
        check(r == null && pCalls[0] == 0, "default computeIfPresent absent skips function");
        // computeIfPresent: present -> applied with old value, result stored
        r = m.computeIfPresent("x", (k, v) -> { pCalls[0]++; return v + 10; });
        check(r == 11 && pCalls[0] == 1, "default computeIfPresent present applies function");
        check(m.get("x") == 11, "default computeIfPresent stores new value");
        // computeIfPresent: null result -> mapping removed
        r = m.computeIfPresent("x", (k, v) -> null);
        check(r == null, "default computeIfPresent null result returns null");
        check(!m.containsKey("x"), "default computeIfPresent null result removes mapping");

        // compute: absent key (null old value) -> applied, result stored
        r = m.compute("c", (k, v) -> (v == null ? 100 : v + 1));
        check(r == 100, "default compute absent sees null old value");
        check(m.get("c") == 100, "default compute absent stores result");
        // compute: present -> applied with old value
        r = m.compute("c", (k, v) -> v + 1);
        check(r == 101 && m.get("c") == 101, "default compute present updates");
        // compute: null result on present mapping -> removed
        r = m.compute("c", (k, v) -> null);
        check(r == null && !m.containsKey("c"), "default compute null result removes mapping");
        // compute: null result on absent key -> nothing inserted
        r = m.compute("gone", (k, v) -> null);
        check(r == null && !m.containsKey("gone"), "default compute null on absent inserts nothing");

        // merge: absent key -> function NOT invoked, provided value stored directly
        final int[] mCalls = {0};
        r = m.merge("mrg", 5, (a, b) -> { mCalls[0]++; return a + b; });
        check(r == 5 && mCalls[0] == 0, "default merge absent stores value without calling function");
        check(m.get("mrg") == 5, "default merge absent mapping present");
        // merge: present -> remapping(old, provided)
        r = m.merge("mrg", 3, (a, b) -> { mCalls[0]++; return a * b; });
        check(r == 15 && mCalls[0] == 1, "default merge present calls function with (old, value)");
        // merge: null result -> mapping removed
        r = m.merge("mrg", 1, (a, b) -> null);
        check(r == null && !m.containsKey("mrg"), "default merge null result removes mapping");

        // replaceAll: rewrites every value through the function
        m.clear();
        m.put("a", 1);
        m.put("b", 2);
        m.replaceAll((k, v) -> v * 10);
        check(m.get("a") == 10 && m.get("b") == 20, "default replaceAll rewrites all values");
        check(m.size() == 2, "default replaceAll keeps key set");

        // exception relay: the exact thrown instance propagates, mapping unchanged
        m.clear();
        final Boom boom = new Boom();
        try {
            m.computeIfAbsent("k", k -> { throw boom; });
            check(false, "default computeIfAbsent must relay function exception");
        } catch (Throwable t) {
            check(t == boom, "default computeIfAbsent relays the original exception instance");
        }
        check(!m.containsKey("k"), "failed computeIfAbsent left no mapping");
        m.put("e", 1);
        try {
            m.computeIfPresent("e", (k, v) -> { throw boom; });
            check(false, "default computeIfPresent must relay function exception");
        } catch (Throwable t) {
            check(t == boom, "default computeIfPresent relays the original exception instance");
        }
        check(m.get("e") == 1, "failed computeIfPresent left mapping unchanged");
        try {
            m.merge("e", 2, (a, b) -> { throw boom; });
            check(false, "default merge must relay function exception");
        } catch (Throwable t) {
            check(t == boom, "default merge relays the original exception instance");
        }
        check(m.get("e") == 1, "failed merge left mapping unchanged");

        // null function argument -> NPE (default implementations dereference it)
        expectThrows(new Runnable() { public void run() { m.computeIfAbsent("n", (Function<String, Integer>) null); } },
                     NullPointerException.class, "computeIfAbsent(null function)");
        expectThrows(new Runnable() { public void run() { m.merge("e", 1, null); } },
                     NullPointerException.class, "merge(null function)");
    }

    // ---- Part 4: hierarchy and generic bounds ----
    static void testHierarchyAndGenerics() {
        ConcurrentHashMap<String, Integer> chm = new ConcurrentHashMap<String, Integer>();
        check(chm instanceof ConcurrentMap, "ConcurrentHashMap implements ConcurrentMap");
        check(chm instanceof Map, "ConcurrentHashMap is a java.util.Map");
        ConcurrentSkipListMap<String, Integer> cslm = new ConcurrentSkipListMap<String, Integer>();
        check(cslm instanceof ConcurrentMap, "ConcurrentSkipListMap implements ConcurrentMap");
        check(ConcurrentMap.class.isAssignableFrom(ConcurrentHashMap.class),
              "ConcurrentMap.isAssignableFrom(ConcurrentHashMap)");
        check(Map.class.isAssignableFrom(ConcurrentMap.class), "Map.isAssignableFrom(ConcurrentMap)");

        // a ConcurrentMap reference is usable through every supertype
        ConcurrentMap<String, Integer> cm = chm;
        cm.put("k", 1);
        Map<String, Integer> asMap = cm;
        check(asMap.get("k") == 1 && asMap.size() == 1, "operations through Map view");
        Map<?, ?> wildcard = cm;
        check(Integer.valueOf(1).equals(wildcard.get("k")), "get through wildcard-typed Map reference");
        check(wildcard.containsKey("k"), "containsKey through wildcard-typed reference");

        // equals/hashCode follow the Map contract across implementations
        Map<String, Integer> plain = new HashMap<String, Integer>();
        plain.put("k", 1);
        check(cm.equals(plain) && plain.equals(cm), "ConcurrentMap equals plain Map with same mappings");
        check(cm.hashCode() == plain.hashCode(), "hashCode follows Map entry-sum contract");
        plain.put("k", 2);
        check(!cm.equals(plain), "different mappings are not equal");

        // entrySet view is live on the interface reference
        Set<Map.Entry<String, Integer>> es = cm.entrySet();
        check(es.size() == 1, "entrySet size via interface reference");
        check(cm.entrySet().iterator().hasNext(), "entrySet iterable via interface reference");
        cm.put("j", 5);
        check(es.size() == 2, "entrySet reflects later inserts");
        cm.clear();
        check(es.isEmpty(), "entrySet reflects clear");
    }

    public static void main(String[] args) throws Exception {
        testConcurrentMap();
        testWhiteBox();
        testAtomicBranchMatrix();
        testMinimalDefaults();
        testHierarchyAndGenerics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
