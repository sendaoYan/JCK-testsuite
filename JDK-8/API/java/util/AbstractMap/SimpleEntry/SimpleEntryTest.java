/*
 * @test
 * @summary java.util.AbstractMap.SimpleEntry conformance: key/value/setValue/equals.
 * @run main SimpleEntryTest
 */

import java.util.AbstractMap;
import java.util.Map;

public class SimpleEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleEntry() {
        AbstractMap.SimpleEntry<String, Integer> e = new AbstractMap.SimpleEntry<>("k", 1);
        check("k".equals(e.getKey()), "getKey");
        check(e.getValue() == 1, "getValue");
        check(e.setValue(2) == 1, "setValue returns old");
        check(e.getValue() == 2, "setValue applied");
        check("k=2".equals(e.toString()), "toString");
        Map.Entry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleEntry<String, Integer> copy = new AbstractMap.SimpleEntry<>(src);
        check("x".equals(copy.getKey()) && copy.getValue() == 9, "copy constructor");
        AbstractMap.SimpleEntry<String, Integer> same = new AbstractMap.SimpleEntry<>("k", 2);
        check(e.equals(same), "equals");
        check(e.hashCode() == same.hashCode(), "hashCode");
    }

    public static void main(String[] args) {
        testSimpleEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
