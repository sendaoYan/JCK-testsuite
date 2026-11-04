/*
 * @test
 * @summary java.util.Map API conformance: put/get/remove return semantics, containsKey/
 *          containsValue, keySet/values/entrySet, and the Java 8 default methods
 *          getOrDefault/putIfAbsent/computeIfAbsent/replaceAll/forEach.
 * @run main MapTest
 */

import java.util.HashMap;
import java.util.Map;

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

    public static void main(String[] args) {
        testBasics();
        testViews();
        testDefaults();
        testDefaultMethodContract();
        testDefaultReplaceAllForEach();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
