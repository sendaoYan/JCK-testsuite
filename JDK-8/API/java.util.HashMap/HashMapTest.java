/*
 * @test
 * @summary java.util.HashMap API conformance: put/get/remove (incl. null keys/values
 *          and return values), size/isEmpty/clear, containsKey/containsValue, keySet/
 *          values/entrySet views, equals/hashCode, rehash under load, and the Java 8
 *          default methods (getOrDefault/putIfAbsent/replace/remove(K,V)/computeIfAbsent/
 *          computeIfPresent/compute/merge/forEach).
 * @run main HashMapTest
 */

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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
        check(keys.size() == 3 && keys.containsAll(java.util.Arrays.asList("a", "b", "c")),
              "keySet view");
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

    public static void main(String[] args) {
        testPutGetRemove();
        testViews();
        testEqualsHashCode();
        testRehash();
        testJava8Defaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
