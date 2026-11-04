/*
 * @test
 * @summary java.util.Map.Entry conformance: key/value/setValue/comparingBy.
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

    static void testEntry() {
        Map.Entry<String, Integer> e = new AbstractMap.SimpleEntry<>("a", 2);
        check("a".equals(e.getKey()), "getKey");
        check(e.getValue() == 2, "getValue");
        check(e.setValue(3) == 2, "setValue returns old");

        List<Map.Entry<String, Integer>> entries = new ArrayList<>();
        entries.add(new AbstractMap.SimpleEntry<>("b", 1));
        entries.add(new AbstractMap.SimpleEntry<>("a", 2));
        Comparator<Map.Entry<String, Integer>> byKey = Map.Entry.comparingByKey();
        entries.sort(byKey);
        check("a".equals(entries.get(0).getKey()), "comparingByKey");
        Comparator<Map.Entry<String, Integer>> byValue = Map.Entry.comparingByValue();
        entries.sort(byValue);
        check(entries.get(0).getValue() == 1, "comparingByValue");
    }

    public static void main(String[] args) {
        testEntry();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
