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

    public static void main(String[] args) {
        testBasics();
        testViews();
        testDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
