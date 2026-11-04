/*
 * @test
 * @summary java.util.AbstractMap API conformance: inherited map operations via a HashMap.
 * @run main AbstractMapTest
 */

import java.util.AbstractMap;
import java.util.HashMap;

public class AbstractMapTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractMap() {
        AbstractMap<String, Integer> m = new HashMap<>();
        m.put("x", 1);
        check(m.size() == 1, "size");
        check(m.get("x") == 1, "get");
        check(m.containsKey("x"), "containsKey");
        check(m.containsValue(1), "containsValue");
        check(m.keySet().contains("x"), "keySet");
        check(m.entrySet().size() == 1, "entrySet");
    }

    public static void main(String[] args) {
        testAbstractMap();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
