/*
 * @test
 * @summary java.util.SortedMap API conformance: firstKey/lastKey, comparator null for natural
 *          ordering, and headMap/tailMap/subMap range views.
 * @run main SortedMapTest
 */

import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class SortedMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBasics() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(3, "c");
        m.put(1, "a");
        m.put(2, "b");
        check(m.firstKey() == 1 && m.lastKey() == 3, "firstKey/lastKey");
        check(m.comparator() == null, "natural ordering comparator is null");
        check("b".equals(m.get(2)), "get by key");
    }

    static void testRangeViews() {
        SortedMap<Integer, String> m = new TreeMap<>();
        m.put(1, "a");
        m.put(2, "b");
        m.put(3, "c");
        check(m.headMap(2).keySet().equals(new TreeSet<>(Arrays.asList(1))), "headMap (exclusive)");
        check(m.tailMap(2).keySet().equals(new TreeSet<>(Arrays.asList(2, 3))), "tailMap (inclusive)");
        check(m.subMap(1, 3).keySet().equals(new TreeSet<>(Arrays.asList(1, 2))), "subMap [from, to)");
    }

    public static void main(String[] args) {
        testBasics();
        testRangeViews();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
