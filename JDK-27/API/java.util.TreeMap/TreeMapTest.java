/*
 * @test
 * @summary java.util.TreeMap API conformance: natural ordering and comparator
 *          construction, put/get/remove, first/last (key and entry), NavigableMap
 *          ops (lowerKey/floorKey/ceilingKey/higherKey, pollFirst/pollLast), and the
 *          subMap/headMap/tailMap range views.
 * @run main TreeMapTest
 */

import java.util.TreeMap;
import java.util.Comparator;
import java.util.Map;

public class TreeMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testNaturalOrder() {
        TreeMap<String, Integer> map = new TreeMap<>();
        check(map.comparator() == null, "natural ordering comparator null");
        map.put("c", 3);
        map.put("a", 1);
        map.put("b", 2);
        check(map.firstKey().equals("a") && map.lastKey().equals("c"), "firstKey/lastKey");
        check(map.firstEntry().getValue() == 1 && map.lastEntry().getValue() == 3,
              "firstEntry/lastEntry");
        check(map.size() == 3 && map.get("b") == 2, "size/get");
        check(map.put("a", 10) == 1 && map.get("a") == 10, "put existing returns old");
        check(map.remove("b") == 2 && !map.containsKey("b"), "remove");
    }

    static void testComparator() {
        TreeMap<Integer, String> map = new TreeMap<>(Comparator.reverseOrder());
        map.put(1, "one");
        map.put(3, "three");
        map.put(2, "two");
        check(map.firstKey() == 3 && map.lastKey() == 1, "reverse comparator order");
    }

    static void testNavigable() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i += 2) {
            map.put(i, "v" + i); // 1,3,5,7,9
        }
        check(map.lowerKey(5) == 3, "lowerKey");
        check(map.floorKey(5) == 5 && map.floorKey(4) == 3, "floorKey");
        check(map.ceilingKey(5) == 5 && map.ceilingKey(4) == 5, "ceilingKey");
        check(map.higherKey(5) == 7, "higherKey");

        check(map.pollFirstEntry().getKey() == 1, "pollFirstEntry");
        check(map.pollLastEntry().getKey() == 9, "pollLastEntry");
        check(map.size() == 3, "size after polls");
    }

    static void testRangeViews() {
        TreeMap<Integer, String> map = new TreeMap<>();
        for (int i = 1; i <= 10; i++) {
            map.put(i, "v" + i);
        }
        check(map.subMap(3, 7).size() == 4 && map.subMap(3, 7).firstKey() == 3
              && map.subMap(3, 7).lastKey() == 6, "subMap fromKey inclusive, toKey exclusive");
        check(map.headMap(4).size() == 3, "headMap");
        check(map.tailMap(8).size() == 3, "tailMap");
        check(map.subMap(3, true, 7, true).size() == 5, "subMap inclusive bounds");
    }

    public static void main(String[] args) {
        testNaturalOrder();
        testComparator();
        testNavigable();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
