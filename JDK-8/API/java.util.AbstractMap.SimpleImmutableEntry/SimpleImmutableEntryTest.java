/*
 * @test
 * @summary java.util.AbstractMap.SimpleImmutableEntry conformance: immutable entry.
 * @run main SimpleImmutableEntryTest
 */

import java.util.AbstractMap;
import java.util.Map;

public class SimpleImmutableEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleImmutableEntry() {
        AbstractMap.SimpleImmutableEntry<String, Integer> e = new AbstractMap.SimpleImmutableEntry<>("k", 1);
        check("k".equals(e.getKey()), "getKey");
        check(e.getValue() == 1, "getValue");
        check("k=1".equals(e.toString()), "toString");
        boolean threw = false;
        try {
            e.setValue(2);
        } catch (UnsupportedOperationException expected) {
            threw = true;
        }
        check(threw, "setValue throws UnsupportedOperationException");
        Map.Entry<String, Integer> src = new AbstractMap.SimpleEntry<>("x", 9);
        AbstractMap.SimpleImmutableEntry<String, Integer> copy = new AbstractMap.SimpleImmutableEntry<>(src);
        check("x".equals(copy.getKey()) && copy.getValue() == 9, "copy constructor");
    }

    public static void main(String[] args) {
        testSimpleImmutableEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
