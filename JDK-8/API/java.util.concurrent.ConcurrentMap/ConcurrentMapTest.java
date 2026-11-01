/*
 * @test
 * @summary java.util.concurrent.ConcurrentMap API conformance: default methods via ConcurrentHashMap.
 * @run main ConcurrentMapTest
 */

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ConcurrentMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testConcurrentMap() {
        ConcurrentMap<String, Integer> m = new ConcurrentHashMap<String, Integer>();
        check(m.putIfAbsent("k", 1) == null, "putIfAbsent inserts when absent");
        check(m.putIfAbsent("k", 2) == 1, "putIfAbsent returns existing");
        check(m.get("k") == 1, "value unchanged");
        check(m.replace("k", 1, 3), "replace(k,old,new)");
        check(m.remove("k", 3), "remove(k,value)");
        check(m.isEmpty(), "empty after remove");
    }

    public static void main(String[] args) {
        testConcurrentMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
