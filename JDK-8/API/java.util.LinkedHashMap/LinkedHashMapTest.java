/*
 * @test
 * @summary java.util.LinkedHashMap API conformance: insertion-order iteration by
 *          default, access-order iteration (constructor accessOrder=true, get moves an
 *          entry to the end), basic put/get/remove, and removeEldestEntry (LRU eviction).
 * @run main LinkedHashMapTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class LinkedHashMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInsertionOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("a", 10); // update, position unchanged
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "insertion order preserved");
        check(map.get("b") == 2, "get");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("a", "b", "c")),
              "get does not reorder in insertion mode");
        check(map.remove("b") == 2 && map.size() == 2, "remove");
    }

    static void testAccessOrder() {
        LinkedHashMap<String, Integer> map = new LinkedHashMap<>(16, 0.75f, true);
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.get("a"); // a is now most recently accessed
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("b", "c", "a")),
              "access order moves accessed entry to end");
        map.put("b", 20); // update also counts as access
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList("c", "a", "b")),
              "put update moves entry to end");
    }

    static void testRemoveEldestEntry() {
        class LRU extends LinkedHashMap<Integer, String> {
            LRU() { super(16, 0.75f, true); }
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 2;
            }
        }
        LRU lru = new LRU();
        lru.put(1, "one");
        lru.put(2, "two");
        lru.put(3, "three"); // evicts eldest (key 1)
        check(lru.size() == 2 && !lru.containsKey(1), "removeEldestEntry evicts eldest");
        check(lru.containsKey(2) && lru.containsKey(3), "remaining entries present");
    }

    public static void main(String[] args) {
        testInsertionOrder();
        testAccessOrder();
        testRemoveEldestEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
