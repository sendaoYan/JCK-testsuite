/*
 * @test
 * @summary java.util.Map.Entry conformance: getKey/getValue/setValue (returns old), equals/
 *          hashCode contract, and default comparingByKey/comparingByValue (natural + custom
 *          comparator + null-argument NPE).
 * @run main EntryTest
 */

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class EntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected " + type.getSimpleName());
        } catch (Throwable t) {
            if (!type.isInstance(t))
                throw new AssertionError(msg + ": expected " + type.getSimpleName()
                        + " but got " + t.getClass().getSimpleName(), t);
            passed++;
        }
    }

    static void testAccessors() {
        Map.Entry<String, Integer> e = new AbstractMap.SimpleEntry<>("a", 2);
        check("a".equals(e.getKey()), "getKey");
        check(e.getValue() == 2, "getValue");
        check(e.setValue(3) == 2, "setValue returns old");
        check(e.getValue() == 3, "setValue updated value");
        Map.Entry<String, Integer> nulls = new AbstractMap.SimpleEntry<>(null, null);
        check(nulls.getKey() == null && nulls.getValue() == null, "SimpleEntry allows null key/value");
    }

    static void testEqualsHashCode() {
        Map.Entry<String, Integer> a = new AbstractMap.SimpleEntry<>("k", 1);
        Map.Entry<String, Integer> b = new AbstractMap.SimpleEntry<>("k", 1);
        Map.Entry<String, Integer> c = new AbstractMap.SimpleEntry<>("k", 2);
        check(a.equals(b) && b.equals(a), "entries with equal key/value are equal");
        check(a.hashCode() == b.hashCode(), "equal entries have equal hashCode");
        check(!a.equals(c), "entries with different value are not equal");
        check(!a.equals(null), "entry not equal to null");
    }

    static void testComparingByKey() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("b", 1));
        entries.add(new AbstractMap.SimpleEntry<>("a", 2));
        entries.sort(Map.Entry.comparingByKey());
        check("a".equals(entries.get(0).getKey()), "comparingByKey natural");

        entries.sort(Map.Entry.comparingByKey(Comparator.<String>reverseOrder()));
        check("b".equals(entries.get(0).getKey()), "comparingByKey custom comparator");

        expectThrows(NullPointerException.class,
                () -> Map.Entry.<String, Integer>comparingByKey(null), "comparingByKey(null) NPE");
        Comparator<Map.Entry<String, Integer>> byKey = Map.Entry.comparingByKey();
        expectThrows(NullPointerException.class,
                () -> byKey.compare(new AbstractMap.SimpleEntry<>(null, 1),
                        new AbstractMap.SimpleEntry<>("a", 1)),
                "comparingByKey natural null key NPE");
    }

    static void testComparingByValue() {
        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("b", 2));
        entries.add(new AbstractMap.SimpleEntry<>("a", 1));
        entries.sort(Map.Entry.comparingByValue());
        check(entries.get(0).getValue() == 1, "comparingByValue natural");

        entries.sort(Map.Entry.comparingByValue(Comparator.<Integer>reverseOrder()));
        check(entries.get(0).getValue() == 2, "comparingByValue custom comparator");

        expectThrows(NullPointerException.class,
                () -> Map.Entry.<String, Integer>comparingByValue(null), "comparingByValue(null) NPE");
    }

    public static void main(String[] args) {
        testAccessors();
        testEqualsHashCode();
        testComparingByKey();
        testComparingByValue();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
