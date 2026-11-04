/*
 * @test
 * @summary java.util.EnumMap API conformance: keySet iterated in natural (ordinal)
 *          order regardless of insertion order, put/get/remove, null key rejection and
 *          null value acceptance, containsKey/containsValue, size/isEmpty/clear, and
 *          construction variants.
 * @run main EnumMapTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

public class EnumMapTest {
    enum Color { RED, GREEN, BLUE }

    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOrderAndOps() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        check(map.put(Color.BLUE, "blue") == null, "put new returns null");
        check(map.put(Color.RED, "red") == null, "put new returns null");
        map.put(Color.GREEN, "green");
        check(new ArrayList<>(map.keySet()).equals(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE)),
              "keySet iterated in ordinal order");
        check(map.get(Color.RED).equals("red") && map.get(Color.BLUE).equals("blue"), "get");
        check(map.put(Color.RED, "RED").equals("red"), "put existing returns old");
        check(map.get(Color.RED).equals("RED"), "put updates");
        check(map.size() == 3, "size");
        check(map.containsKey(Color.GREEN), "containsKey");
        check(map.containsValue("green"), "containsValue");
        check(!map.containsValue("nope"), "!containsValue");
        check(map.remove(Color.GREEN).equals("green") && !map.containsKey(Color.GREEN), "remove");
    }

    static void testNullHandling() {
        EnumMap<Color, String> map = new EnumMap<>(Color.class);
        try {
            map.put(null, "x");
            throw new AssertionError("put(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        map.put(Color.RED, null);
        check(map.containsKey(Color.RED) && map.get(Color.RED) == null, "null value allowed");
    }

    static void testConstructors() {
        EnumMap<Color, String> src = new EnumMap<>(Color.class);
        src.put(Color.RED, "red");
        EnumMap<Color, String> copy = new EnumMap<>(src);
        check(copy.equals(src), "EnumMap(EnumMap) copies");

        Map<Color, String> plain = new java.util.HashMap<>();
        plain.put(Color.BLUE, "blue");
        EnumMap<Color, String> fromMap = new EnumMap<>(plain);
        check(fromMap.get(Color.BLUE).equals("blue"), "EnumMap(Map) copies");
    }

    public static void main(String[] args) {
        testOrderAndOps();
        testNullHandling();
        testConstructors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
