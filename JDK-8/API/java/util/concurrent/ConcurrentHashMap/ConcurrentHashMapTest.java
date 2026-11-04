/*
 * @test
 * @summary java.util.concurrent.ConcurrentHashMap API conformance: put/get/remove,
 *          putIfAbsent/computeIfAbsent, containsKey/containsValue, and null rejection.
 * @run main ConcurrentHashMapTest
 */

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testMap() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        m.put("a", 1);
        check(m.get("a") == 1, "put/get");
        check(m.putIfAbsent("a", 2) == 1, "putIfAbsent existing");
        check(m.putIfAbsent("b", 3) == null && m.get("b") == 3, "putIfAbsent new");
        check(m.computeIfAbsent("c", String::length) == 1, "computeIfAbsent");
        check(m.remove("a") == 1 && m.size() == 2, "remove");
        check(m.containsKey("b") && m.containsValue(3), "containsKey/containsValue");
    }

    static void testNullRejection() {
        ConcurrentHashMap<String, Integer> m = new ConcurrentHashMap<>();
        try {
            m.put(null, 1);
            throw new AssertionError("put(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            m.put("k", null);
            throw new AssertionError("put null value: expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    public static void main(String[] args) {
        testMap();
        testNullRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
